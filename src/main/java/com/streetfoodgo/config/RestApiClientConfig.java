<<<<<<< HEAD
package com.streetfoodgo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestApiClientConfig {

    // @future Get me from application properties!
    public static final String BASE_URL = "http://localhost:8081";

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
=======
package com.streetfoodgo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestApiClientConfig {

    // @future Get me from application properties!
    public static final String BASE_URL = "http://localhost:8081";

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
>>>>>>> 9b38f066bea97c7a2a625f3a7df998a0e8370cfe
