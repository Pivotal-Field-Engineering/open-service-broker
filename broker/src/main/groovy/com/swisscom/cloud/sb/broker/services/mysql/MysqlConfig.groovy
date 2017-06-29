package com.swisscom.cloud.sb.broker.services.mysql

import com.swisscom.cloud.sb.broker.services.bosh.BoshBasedServiceConfig
import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@CompileStatic
@Configuration
@ConfigurationProperties(prefix = "com.swisscom.cloud.sb.broker.service.mysql")
class MysqlConfig implements BoshBasedServiceConfig {


    @Override
    public String toString() {
        return "MysqlConfig{}";
    }
}
