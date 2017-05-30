package com.swisscom.cloud.sb.broker.cfextensions.endpoint

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.util.ServiceDetailType
import com.swisscom.cloud.sb.broker.util.ServiceDetailsHelper
import com.swisscom.cloud.sb.model.endpoint.Endpoint
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

@Component
@CompileStatic
class EndpointLookup {
    public static final Collection<String> DEFAULT_PROTOCOLS = Collections.unmodifiableCollection(["tcp"])

    Collection<Endpoint> findEndpoints(ServiceInstance serviceInstance, EndpointConfig config) {
        def result = new LinkedList<Endpoint>()
        def portsForServiceInstance = ServiceDetailsHelper.from(serviceInstance).findAllWithServiceDetailType(ServiceDetailType.PORT)
        parseProtocols(config).each { String protocol ->
            portsForServiceInstance.each {
                String port ->
                    result.add(new Endpoint(protocol: protocol, ports: port, destination: config.ipRange))
            }
        }

        return result
    }

    private Collection<String> parseProtocols(EndpointConfig config) {
        def result = config.protocols?.split(",")
        if (result == null || result.size() == 0) {
            return DEFAULT_PROTOCOLS
        }
        return Arrays.asList(result)
    }
}
