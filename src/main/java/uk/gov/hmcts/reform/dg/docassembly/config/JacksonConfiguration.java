package uk.gov.hmcts.reform.dg.docassembly.config;

import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@Configuration
public class JacksonConfiguration {

    /*
     * Jackson Afterburner module to speed up serialization/deserialization.
     */
    @Bean
    public BlackbirdModule blackbirdModule() {
        return new BlackbirdModule();
    }

    /*
     * Module for serialization/deserialization of ConstraintViolationProblem.
     */
    @Bean
    ConstraintViolationProblemModule constraintViolationProblemModule() {
        return new ConstraintViolationProblemModule();
    }

}
