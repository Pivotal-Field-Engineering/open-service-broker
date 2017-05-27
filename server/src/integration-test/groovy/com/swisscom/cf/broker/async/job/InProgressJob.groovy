package com.swisscom.cf.broker.async.job

import com.swisscom.cf.broker.model.LastOperation
import com.swisscom.cf.broker.provisioning.async.AsyncOperationResult
import com.swisscom.cf.broker.provisioning.lastoperation.LastOperationJobContext

import java.util.concurrent.atomic.AtomicInteger

public class InProgressJob extends AbstractLastOperationJob {
    public static final AtomicInteger ExecutionCount = new AtomicInteger()

    @Override
    public AsyncOperationResult handleJob(LastOperationJobContext context) {
        ExecutionCount.incrementAndGet()
        return new AsyncOperationResult(status: LastOperation.Status.IN_PROGRESS)
    }
}