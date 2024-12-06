package com.webgis.dsws.controller.api;

import com.webgis.dsws.domain.dto.AuthRequest;
import com.webgis.dsws.domain.dto.AuthResponse;
import com.webgis.dsws.domain.dto.RegisterRequest;
import com.webgis.dsws.domain.model.NguoiDung;
import com.webgis.dsws.domain.model.enums.VaiTroEnum;
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
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import com.webgis.dsws.domain.dto.NguoiDungDTO;
import jakarta.persistence.EntityNotFoundException;

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
        return ResponseEntity.ok(new AuthResponse(jwt_token, "/")); // Set appropriate redirect URL
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

    /**
     * Lấy thông tin profile của user hiện tại
     */
    @GetMapping("/api/v1/auth/profile")
    @PreAuthorize("isAuthenticated()") // Add this annotation
    public ResponseEntity<?> getCurrentUserProfile() {
        try {
            NguoiDung currentUser = nguoiDungService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not authenticated"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", currentUser.getId());
            response.put("hoTen", currentUser.getHoTen());
            response.put("email", currentUser.getEmail());
            response.put("soDienThoai", currentUser.getSoDienThoai());
            response.put("chucVu", currentUser.getChucVu());
            response.put("trangThaiHoatDong", currentUser.getTrangThaiHoatDong());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error fetching user profile: " + e.getMessage()));
        }
    }

    /**
     * Cập nhật thông tin profile
     */
    @PutMapping("/api/v1/auth/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody NguoiDungDTO userDto) {
        try {
            NguoiDung currentUser = nguoiDungService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Không có quyền cập nhật");
            }

            userDto.setId(currentUser.getId()); // Ensure updating current user
            nguoiDungService.update(userDto);
            return ResponseEntity.ok("Cập nhật thông tin thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật thông tin");
        }
    }

    /**
     * Thêm người dùng mới (Admin only)
     */
    @PostMapping("/api/v1/auth/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> addUser(@Valid @RequestBody NguoiDungDTO userDto) {
        try {
            nguoiDungService.save(userDto);
            return ResponseEntity.status(HttpStatus.CREATED).body("Tạo người dùng thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi tạo người dùng");
        }
    }

    /**
     * Cập nhật thông tin người dùng (Admin only)
     */
    @PutMapping("/api/v1/auth/users/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody NguoiDungDTO userDto) {
        try {
            userDto.setId(id);
            nguoiDungService.update(userDto);
            return ResponseEntity.ok("Cập nhật người dùng thành công");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật người dùng");
        }
    }

    @PutMapping("/api/v1/auth/users/{id}/roles")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateUserRoles(@PathVariable Long id, @RequestBody VaiTroEnum role) {
        try {
            nguoiDungService.addRoleToUser(id, role);
            return ResponseEntity.ok("Cập nhật vai trò thành công");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật vai trò: " + e.getMessage());
        }
    }

    @DeleteMapping("/api/v1/auth/users/{id}/roles")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> removeUserRole(@PathVariable Long id, @RequestBody VaiTroEnum role) {
        try {
            nguoiDungService.removeRoleFromUser(id, role);
            return ResponseEntity.ok("Xóa vai trò thành công");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa vai trò: " + e.getMessage());
        }
    }

    /**
     * Xóa người dùng (Admin only)
     */
    @DeleteMapping("/api/v1/auth/users/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            nguoiDungService.deleteById(id);
            return ResponseEntity.ok("Xóa người dùng thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xóa người dùng");
        }
    }

    /**
     * Lấy danh sách người dùng (Admin only)
     */
    @GetMapping("/api/v1/auth/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<NguoiDungDTO> users = nguoiDungService.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy danh sách người dùng");
        }
    }
}