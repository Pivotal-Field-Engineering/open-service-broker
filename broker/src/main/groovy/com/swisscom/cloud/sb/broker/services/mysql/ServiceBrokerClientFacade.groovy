package com.swisscom.cloud.sb.broker.services.mysql

import com.swisscom.cloud.sb.client.ServiceBrokerClient

/**
 *
 */
class ServiceBrokerClientFacade {

    public ServiceBrokerClient createServiceBrokerClient(String broker_url, String broker_auth_password) {
        new ServiceBrokerClient('https://' + broker_url, 'cf', broker_auth_password)
    }
}
