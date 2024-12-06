package com.webgis.dsws.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.webgis.dsws.domain.service.NguoiDungService;
import com.webgis.dsws.util.JwtUtil;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * Filter xử lý ủy quyền dựa trên JWT token.
 * Kiểm tra và xác thực token trong mọi request đến API được bảo vệ.
 */
@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    @Value("${jwt.token-expiration}")
    private long tokenExpiration;

    private final JwtUtil jwtUtil;
    private final NguoiDungService userService;

    public JwtAuthorizationFilter(JwtUtil jwtUtil, NguoiDungService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    /**
     * Xử lý việc kiểm tra và xác thực JWT token trong request
     * 
     * @param request  Request HTTP cần kiểm tra
     * @param response Response HTTP
     * @param chain    Chuỗi filter tiếp theo
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Skip authorization for permitted paths
        String path = request.getRequestURI();
        if (shouldNotFilter(request)) {
            chain.doFilter(request, response);
            return;
        }

        log.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());

        try {
            String token = extractToken(request);
            Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

            if (token != null && (existingAuth == null || existingAuth instanceof AnonymousAuthenticationToken)) {
                log.debug("Đã tìm thấy JWT token trong request, xác thực hiện tại là ẩn danh hoặc null");

                // Remove 'Bearer ' prefix if present
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }

                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.getUsernameFromToken(token);
                    log.debug("Token valid for user: {}", username);

                    try {
                        UserDetails userDetails = userService.loadUserByUsername(username);
                        if (userDetails != null && userDetails.isEnabled()) {
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities());
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            log.debug("Successfully authenticated user: {}", username);
                        }
                    } catch (Exception e) {
                        log.error("Error loading user details", e);
                        SecurityContextHolder.clearContext();
                    }
                } else {
                    log.warn("Invalid token received");
                    SecurityContextHolder.clearContext();
                }
            }
        } catch (Exception e) {
            log.error("Error in JWT filter", e);
            SecurityContextHolder.clearContext();
        } finally {
            chain.doFilter(request, response);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/auth/login") ||
                path.startsWith("/auth/register") ||
                path.equals("/api/v1/auth/login") ||
                path.equals("/api/v1/auth/register") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/gif/") ||
                path.startsWith("/img/") ||
                request.getRequestURI().contains("arcgis.com") ||
                isStaticResource(path);
    }

    private boolean isStaticResource(String path) {
        return path.endsWith(".js") ||
                path.endsWith(".css") ||
                path.endsWith(".png") ||
                path.endsWith(".jpg") ||
                path.endsWith(".gif") ||
                path.endsWith(".woff") ||
                path.endsWith(".woff2") ||
                path.endsWith(".ttf") ||
                path.endsWith(".map");
    }

    private String extractToken(HttpServletRequest request) {
        // First check for token in cookies
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("JWT_TOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // Fallback to Authorization header if cookie is not present
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}