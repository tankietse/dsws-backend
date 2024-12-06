package com.webgis.dsws.config;

import com.webgis.dsws.security.JwtAuthenticationFilter;
import com.webgis.dsws.security.JwtAuthorizationFilter;
import com.webgis.dsws.domain.service.NguoiDungService;
import com.webgis.dsws.util.JwtUtil;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import java.util.Arrays;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Cấu hình bảo mật cho ứng dụng.
 * Định nghĩa các quy tắc bảo mật, xác thực và phân quyền.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
        private final NguoiDungService userService;
        private final JwtUtil jwtUtil;

        @Bean
        public UserDetailsService userDetailsService() {
                return userService;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
                var auth = new DaoAuthenticationProvider();
                auth.setUserDetailsService(userDetailsService());
                auth.setPasswordEncoder(passwordEncoder());
                return auth;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
                return configuration.getAuthenticationManager();
        }

        /**
         * Cấu hình chuỗi bảo mật
         * 
         * @param http Đối tượng HttpSecurity để cấu hình
         * @return SecurityFilterChain đã được cấu hình
         */
        @Bean
        public SecurityFilterChain securityFilterChain(@NotNull HttpSecurity http,
                        AuthenticationConfiguration authConfig) throws Exception {
                log.info("Configuring security filter chain");

                AuthenticationManager authenticationManager = authenticationManager(authConfig);
                JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager,
                                jwtUtil, userService);
                JwtAuthorizationFilter jwtAuthorizationFilter = new JwtAuthorizationFilter(jwtUtil, userService);

                log.debug("Setting up JWT filters");

                http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/swagger-ui/**",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui.html",
                                                                "/swagger-resources/**",
                                                                "/webjars/**",
                                                                "/actuator/**",
                                                                "/api/v1/status",
                                                                "/api/v1/health",
                                                                "/api/v1/info",
                                                                "/api/v1/auth/**",
                                                                "/images/**",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/gif/**",
                                                                "/fonts/**",
                                                                "/auth/**",
                                                                "/img/**",
                                                                "/favicon.ico",
                                                                "/error")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .exceptionHandling(handling -> handling
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        if (request.getRequestURI().startsWith("/api/")) {
                                                                // Trả lỗi JSON cho API request
                                                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                                                response.setContentType("application/json");
                                                                response.getWriter().write("{\"error\":\""
                                                                                + authException.getMessage() + "\"}");
                                                        } else {
                                                                // Chuyển hướng cho non-API request
                                                                response.sendRedirect("/auth/login");
                                                        }
                                                }))

                                .addFilter(jwtAuthenticationFilter)
                                .addFilterBefore(jwtAuthorizationFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                // Allow cookies in CORS configuration
                http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

                log.info("Security filter chain configured successfully");
                return http.build();
        }

        /**
         * Cấu hình CORS
         * 
         * @return CorsConfigurationSource để xử lý CORS
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                // Specify the actual origin instead of "*"
                configuration.setAllowedOrigins(Arrays.asList("http://localhost:8081")); // Replace with your frontend
                                                                                         // origin
                configuration.setAllowedMethods(Arrays.asList("*"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setExposedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
                configuration.setAllowCredentials(true); // Allow credentials (cookies)
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
