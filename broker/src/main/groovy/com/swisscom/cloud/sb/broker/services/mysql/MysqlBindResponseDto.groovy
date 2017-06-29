package com.swisscom.cloud.sb.broker.services.mysql

import com.swisscom.cloud.sb.broker.binding.BindResponseDto
import groovy.json.JsonBuilder

class MysqlBindResponseDto implements BindResponseDto {

    Map<String, Object> credentials


    @Override
    String toJson() {
        def jsonBuilder = createBuilder()
        return jsonBuilder.toPrettyString()
    }

    protected JsonBuilder createBuilder() {
        def jsonBuilder = new JsonBuilder()
        jsonBuilder.credentials(credentials)
        return jsonBuilder
    }
}
