package com.epac.imposition.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@org.springframework.context.annotation.Configuration
@EnableWebMvc
@ComponentScan(basePackages = { "com.epac.imposition.*"})
public class SpringMvcConfig {

}