package com.springbootlearning.learningspringboot3;

import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasRole;

import java.util.stream.Stream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ComplexSecurityConfig {

  @Bean
  SecurityFilterChain configureSecurity(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(requests -> requests //
        .requestMatchers("/resources/**", "/about", "/login").permitAll() //
        .requestMatchers(HttpMethod.GET, "/admin/**").hasRole("ADMIN") //
        .requestMatchers("/db/**").access((authentication, object) -> {
      boolean anyMissing = Stream.of("ADMIN", "DBA")//
          .map(role -> hasRole(role).check(authentication, object).isGranted()) //
          .filter(granted -> !granted) //
          .findAny() //
          .orElse(false); //
      return new AuthorizationDecision(!anyMissing);
    }) //
        .anyRequest().denyAll()) //
        .formLogin((login)->login.loginPage("/login" ));
    return http.build();
  }

}
