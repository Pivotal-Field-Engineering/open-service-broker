package com.swisscom.cf.broker.services.mongodb.enterprise.statemachine

import com.swisscom.cf.broker.services.bosh.statemachine.BoshStateMachineContext
import com.swisscom.cf.broker.services.mongodb.enterprise.MongoDbEnterpriseConfig
import com.swisscom.cf.broker.services.mongodb.enterprise.MongoDbEnterpriseFreePortFinder
import com.swisscom.cf.broker.services.mongodb.enterprise.opsmanager.OpsManagerFacade
import groovy.transform.CompileStatic


@CompileStatic
class MongoDbEnterperiseStateMachineContext extends BoshStateMachineContext {
    OpsManagerFacade opsManagerFacade
    MongoDbEnterpriseFreePortFinder mongoDbEnterpriseFreePortFinder
    MongoDbEnterpriseConfig mongoDbEnterpriseConfig
}