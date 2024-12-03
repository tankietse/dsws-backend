package com.webgis.dsws.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webgis.dsws.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Filter xử lý xác thực người dùng và tạo JWT token.
 * Được kích hoạt khi người dùng đăng nhập vào hệ thống.
 */
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
            UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        setFilterProcessesUrl("/api/v1/auth/login"); // Endpoint for authentication
    }

    public Authentication getAuthentication(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    /**
     * Xử lý yêu cầu xác thực từ người dùng
     * 
     * @param request  Request HTTP chứa thông tin đăng nhập
     * @param response Response HTTP
     * @return Đối tượng Authentication nếu xác thực thành công
     * @throws AuthenticationException nếu xác thực thất bại
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        String username = null;
        String password = null;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> credentials = objectMapper.readValue(request.getInputStream(),
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
            username = credentials.get("username");
            password = credentials.get("password");
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse authentication request body", e);
        }

        if (username == null || password == null) {
            throw new RuntimeException("Username or password not provided");
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,
                password);

        return authenticationManager.authenticate(authenticationToken);
    }

    /**
     * Xử lý sau khi xác thực thành công
     * Tạo JWT token và gửi về client
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain, Authentication authResult) throws IOException {
        String token = jwtUtil.generateToken(authResult.getName());

        // Add Authorization header
        response.addHeader("Authorization", "Bearer " + token);

        // Set cookie
        StringBuilder cookieValue = new StringBuilder();
        cookieValue.append("JWT_TOKEN=").append(token)
                .append("; Max-Age=").append(jwtUtil.getExpirationInSeconds())
                .append("; Path=/")
                .append("; HttpOnly");
        if (request.isSecure()) {
            cookieValue.append("; Secure");
        }
        cookieValue.append("; SameSite=Lax");

        response.addHeader("Set-Cookie", cookieValue.toString());

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", true);
        responseBody.put("redirectUrl", "/");
        responseBody.put("token", token); // Include token in response body

        new ObjectMapper().writeValue(response.getOutputStream(), responseBody);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + failed.getMessage() + "\"}");
    }
}