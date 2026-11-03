package com.furniture.config;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.furniture.security.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                .authorizeHttpRequests(auth -> auth

                        // PUBLIC APIs
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/api/client-view/**").permitAll()

                        // AUTHENTICATED USERS
                        .requestMatchers("/api/auth/me").authenticated()

                        // EMPLOYEE OWN PROFILE
                        .requestMatchers(HttpMethod.GET, "/api/employees/me")
                        .hasAnyRole("ADMIN", "EMPLOYEE")

                        // ATTENDANCE REQUESTS
                        .requestMatchers(HttpMethod.POST, "/api/attendance-requests")
                        .hasAnyRole("ADMIN", "EMPLOYEE")

                        .requestMatchers(HttpMethod.GET, "/api/attendance-requests/employee/**")
                        .hasAnyRole("ADMIN", "EMPLOYEE")

                        // ATTENDANCE RECORDS
                        .requestMatchers(HttpMethod.GET, "/api/attendances/employee/**")
                        .hasAnyRole("ADMIN", "EMPLOYEE")

                        // TASKS
                        .requestMatchers(HttpMethod.GET, "/api/tasks/my")
                        .hasAnyRole("ADMIN", "EMPLOYEE")

                        .requestMatchers(HttpMethod.PUT, "/api/tasks/*/status")
                        .hasAnyRole("ADMIN", "EMPLOYEE")

                        // PROJECTS
                        .requestMatchers(HttpMethod.GET, "/api/projects/my")
                        .hasAnyRole("ADMIN", "EMPLOYEE")

                        // ALL REMAINING API ENDPOINTS - ADMIN ONLY
                        .requestMatchers("/api/**").hasRole("ADMIN")

                        // OTHER REQUESTS
                        .anyRequest().authenticated()
                )

                .exceptionHandling(exception -> exception

                        .authenticationEntryPoint((request, response, ex) ->
                        writeError(response, 401,
                                "Login required or token invalid"))

                        .accessDeniedHandler((request, response, ex) ->
                        writeError(response, 403,
                                "You do not have permission to do this"))
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

                return http.build();
        }

        private static void writeError(
                HttpServletResponse response,
                int status,
                String message) throws IOException {

                response.setStatus(status);
                response.setContentType("application/json");

                response.getWriter().write(
                "{\"status\":" + status
                + ",\"message\":\"" + message
                + "\",\"timestamp\":\"" + LocalDateTime.now() + "\"}"
                );
        }
}