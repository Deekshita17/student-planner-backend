package com.studentplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @SpringBootApplication: This is the starting point of our Spring Boot application.
 * It combines three annotations:
 * 1. @Configuration: Tags the class as a source of bean definitions for the application context.
 * 2. @EnableAutoConfiguration: Tells Spring Boot to start adding beans based on classpath settings.
 * 3. @ComponentScan: Tells Spring to look for components, configurations, and services in the com.studentplanner package.
 */
@SpringBootApplication
public class StudentPlannerApplication {

    public static void main(String[] args) {
        // Starts the entire Spring Boot application and embedded Tomcat server
        SpringApplication.run(StudentPlannerApplication.class, args);
    }
}
