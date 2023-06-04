package uk.gov.hmcts.reform.dg.docassembly.appinsights;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DependencyProfiler {

    String name();

    String action();
}

