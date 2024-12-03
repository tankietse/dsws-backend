package com.webgis.dsws.controller.api;

import com.webgis.dsws.domain.dto.AuthRequest;
import com.webgis.dsws.domain.dto.AuthResponse;
import com.webgis.dsws.domain.dto.RegisterRequest;
import com.webgis.dsws.domain.service.NguoiDungService;
import com.webgis.dsws.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller xử lý các yêu cầu xác thực (Authentication) như đăng nhập, đăng
 * ký.
 */
@RestController
public class AuthApi {

    private final AuthenticationManager authenticationManager;
    private final NguoiDungService nguoiDungService;
    private final JwtUtil jwtUtil;

    public AuthApi(AuthenticationManager authenticationManager, NguoiDungService nguoiDungService, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.nguoiDungService = nguoiDungService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Xử lý yêu cầu đăng nhập.
     *
     * @param request Đối tượng chứa thông tin đăng nhập (tên đăng nhập và mật khẩu)
     * @return ResponseEntity chứa token JWT nếu đăng nhập thành công
     */
    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        String jwt_token = jwtUtil.generateToken(authentication.getName());
        return ResponseEntity.ok(new AuthResponse(jwt_token, "/")); // Explicitly set redirect URL
    }

    /**
     * Xử lý yêu cầu đăng ký người dùng mới.
     *
     * @param request Đối tượng chứa thông tin đăng ký (tên đăng nhập, mật khẩu,
     *                email, v.v.)
     * @return ResponseEntity chứa thông báo hoặc thông tin người dùng mới tạo
     */
    @PostMapping("/api/v1/auth/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        // Thực hiện logic đăng ký người dùng
        // Kiểm tra xem tên người dùng đã tồn tại chưa
        if (nguoiDungService.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Tên đăng nhập đã tồn tại");
        }

        // Tạo người dùng mới
        nguoiDungService.registerNewUser(request);
        return ResponseEntity.ok("Đăng ký thành công");
    }

    /**
     * Xác thực token JWT.
     *
     * @param bearerToken Chuỗi token cần xác thực
     * @return ResponseEntity cho biết token hợp lệ hay không
     */
    @GetMapping("/api/v1/auth/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String bearerToken) {
        try {
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                String token = bearerToken.substring(7);
                if (jwtUtil.validateToken(token)) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("valid", true);
                    response.put("username", jwtUtil.getUsernameFromToken(token));
                    // Có thể thêm các thông tin khác như roles, expirationTime, etc.
                    return ResponseEntity.ok(response);
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "error", "Invalid token"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "error", e.getMessage()));
        }
    }
}