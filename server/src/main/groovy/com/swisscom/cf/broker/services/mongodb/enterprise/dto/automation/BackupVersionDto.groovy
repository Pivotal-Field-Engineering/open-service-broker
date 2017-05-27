package com.swisscom.cf.broker.services.mongodb.enterprise.dto.automation

import groovy.transform.CompileStatic

@CompileStatic
class BackupVersionDto implements Serializable {
    String hostname
    String logPath
    LogRotateDto logRotate
}