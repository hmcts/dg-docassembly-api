package uk.gov.hmcts.reform.dg.docassembly.appinsights;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.telemetry.Duration;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DependencyProfilerAspect {

    @Autowired
    private TelemetryClient telemetryClient;

    @Around("@annotation(DependencyProfiler)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        Object proceed = joinPoint.proceed();

        long executionTime = System.currentTimeMillis() - start;

        telemetryClient.trackDependency("docmosis", "render", new Duration(executionTime), true);

        return proceed;
    }

}
