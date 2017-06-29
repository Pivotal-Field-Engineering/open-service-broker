package com.swisscom.cloud.sb.broker.services.mysql

import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.model.*
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.bosh.AbstractBoshBasedServiceProviderSpec
import com.swisscom.cloud.sb.broker.services.bosh.BoshTemplate
import com.swisscom.cloud.sb.broker.services.bosh.statemachine.BoshDeprovisionState
import com.swisscom.cloud.sb.broker.services.bosh.statemachine.BoshProvisionState
import com.swisscom.cloud.sb.broker.util.ServiceDetailKey
import com.swisscom.cloud.sb.broker.util.ServiceDetailsHelper
import com.swisscom.cloud.sb.client.ServiceBrokerClient
import groovy.json.JsonSlurper
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

import static ServiceDetail.from

class MysqlServiceProviderSpec extends AbstractBoshBasedServiceProviderSpec<MysqlServiceProvider> {
    private String serviceInstanceGuid = 'serviceInstanceGuid'


    def setup(){
        serviceProvider.serviceConfig = new MysqlConfig(retryIntervalInSeconds: 1, maxRetryDurationInMinutes: 1)
    }

    def "template customization generates route from instance id"(){
        //leaving pwd generation to credhub and config expansion in service definition
        given:
        //Not pulling any configuration from persistent service for now
        //def serviceInstance = new ServiceInstance()
        //1 * serviceProvider.provisioningPersistenceService.getServiceInstance(serviceInstanceGuid) >> serviceInstance

        BoshTemplate template = Mock(BoshTemplate)

        //not yet fetching data from the template itself such as instance cout
        // def instanceCount = 3
        //1 * template.instanceCount() >> instanceCount
        def request = new ProvisionRequest(serviceInstanceGuid: serviceInstanceGuid)

        when:
        def details = serviceProvider.customizeBoshTemplate(template,request)

        then: "template gets asked for replacements"
        1 * template.replace(MysqlServiceProvider.EXTERNAL_HOST_KEY,"mysql-on-demand-" + serviceInstanceGuid + ".nd-cfapi.itn.ftgroup")
        1 * template.replace(MysqlServiceProvider.BROKER_PASSWORD_KEY,_ as String)

        and: "returns service details mutations to be persisted"
        ! ServiceDetailsHelper.from(details).getValue(ServiceDetailKey.PASSWORD).isEmpty()

    }

    def "StateMachineContext is created correctly"(){
        given:
        def context = new LastOperationJobContext()
        when:
        def stateMachineContext = serviceProvider.createStateMachineContext(context)
        then:
        stateMachineContext.lastOperationJobContext == context
    }

    def "Provisioning StateMachine is created correctly"(){
        when:
        def result = serviceProvider.createProvisionStateMachine(new LastOperationJobContext())
        then: "it simply performs a 'bosh deploy'"
        result.states.first() == BoshProvisionState.CREATE_DEPLOYMENT
        result.states.last() == BoshProvisionState.CHECK_BOSH_DEPLOYMENT_TASK_STATE
    }

    def "provision state is initialized correctly if context does not contain any state"(){
        given:
        def context = new LastOperationJobContext(lastOperation: new LastOperation())
        when:
        def state = serviceProvider.getProvisionState(context)
        then:
        state == BoshProvisionState.CREATE_DEPLOYMENT
    }

    def "provision state is initialized correctly if context include some previous state"(){
        given:
        def context = new LastOperationJobContext(lastOperation: new LastOperation(internalState: BoshProvisionState.CHECK_BOSH_DEPLOYMENT_TASK_STATE.toString()))
        when:
        def state = serviceProvider.getProvisionState(context)
        then:
        state == BoshProvisionState.CHECK_BOSH_DEPLOYMENT_TASK_STATE
    }

    def "happy path: requestProvision"(){
        given:
        def context = new LastOperationJobContext(lastOperation: new LastOperation(internalState: BoshProvisionState.CHECK_BOSH_DEPLOYMENT_TASK_STATE.toString()))
        when:
        def result=serviceProvider.requestProvision(context)
        then:
        result
    }

    def "deprovision state is initialized correctly if context does not contain any state"(){
        given:
        def context = new LastOperationJobContext(lastOperation: new LastOperation())
        when:
        def state = serviceProvider.getDeprovisionState(context)
        then:
        state == BoshDeprovisionState.DELETE_BOSH_DEPLOYMENT
    }

    def "deprovision state is initialized correctly if context include some previous state"(){
        given:
        def context = new LastOperationJobContext(lastOperation: new LastOperation(internalState: BoshDeprovisionState.CHECK_BOSH_UNDEPLOY_TASK_STATE.toString()))
        when:
        def state = serviceProvider.getDeprovisionState(context)
        then:
        state == BoshDeprovisionState.CHECK_BOSH_UNDEPLOY_TASK_STATE
    }

    def "happy path: requestDeprovision"(){
        given:
        def context = new LastOperationJobContext(lastOperation: new LastOperation(internalState: BoshDeprovisionState.DELETE_BOSH_DEPLOYMENT.toString()))
        boshFacade.deleteBoshDeploymentIfExists(context) >> Optional.of("aBoshTaskId")
        when:
        def result=serviceProvider.requestDeprovision(context)
        then:
        result
    }

    def "Bind functions correctly"() {
        given:
        BindRequest request = new BindRequest(
                service: new CFService(guid: "aServiceGuid"),
                plan: new Plan(guid: "aPlanGuid"),
                app_guid: "anAppGuid",
                serviceInstance: new ServiceInstance(guid: 'aServiceInstantceGuid', details: [from(ServiceDetailKey.PASSWORD, 'broker_password')]))
        serviceProvider.serviceBrokerClientFacade = Mock(ServiceBrokerClientFacade)
        ServiceBrokerClient serviceBrokerClient = Mock(ServiceBrokerClient)
        1 * serviceProvider.serviceBrokerClientFacade.createServiceBrokerClient(*_) >> serviceBrokerClient
        def credentials = new HashMap<String, Object>()
        credentials.put("uri", "mysql://login:pwd@192.168.0.1:3306/db")
        def bindingResponse = CreateServiceInstanceAppBindingResponse.newInstance().withCredentials(credentials)
        def responseEntity = new ResponseEntity<CreateServiceInstanceAppBindingResponse>(bindingResponse, HttpStatus.CREATED)
        1* serviceBrokerClient.createServiceInstanceBinding(_) >> responseEntity

        when:
        def bindResult = serviceProvider.bind(request)
        then: "mysql broker binding is invoked and resulting credentials returned"
        def resultingCredentials = bindResult.credentials.toJson()
        def json = new JsonSlurper().parseText(resultingCredentials)
        json.credentials.uri == 'mysql://login:pwd@192.168.0.1:3306/db'
    }

    def "Unbind functions correctly"() {
        given: "an unbind request is received"
        UnbindRequest request = new UnbindRequest(
                service: new CFService(guid: "aServiceGuid"),
                /** not yet supported in swisscom controller the hints
                plan: new Plan(guid: "aPlanGuid"),
                 */
                serviceInstance: new ServiceInstance(
                        guid: 'aServiceInstanceGuid',
                        plan: new Plan(guid: "aServicePlanGuid"),
                        details: [from(ServiceDetailKey.PASSWORD, 'broker_password')]),
                binding: new ServiceBinding(guid: "aServiceBindingGuid",
                        details: []))
        and:
        serviceProvider.serviceBrokerClientFacade = Mock(ServiceBrokerClientFacade)
        ServiceBrokerClient serviceBrokerClient = Mock(ServiceBrokerClient)
        1 * serviceProvider.serviceBrokerClientFacade.createServiceBrokerClient(*_) >> serviceBrokerClient

        def responseEntity = new ResponseEntity<Void>(HttpStatus.OK)
        1* serviceBrokerClient.deleteServiceInstanceBinding(*_) >> responseEntity


        when:
        serviceProvider.unbind(request)
        then: "it delegates to the mysql_release broker"

    }
}
