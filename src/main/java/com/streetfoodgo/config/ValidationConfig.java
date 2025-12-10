<<<<<<< HEAD
package com.streetfoodgo.config;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Validation configuration.
 */
@Configuration
public class ValidationConfig {

    @SuppressWarnings("resource")
    @Bean
    public Validator validator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }
}
=======
package com.streetfoodgo.config;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Validation configuration.
 */
@Configuration
public class ValidationConfig {

    @SuppressWarnings("resource")
    @Bean
    public Validator validator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }
}
>>>>>>> 9b38f066bea97c7a2a625f3a7df998a0e8370cfe
