package com.webgis.dsws.domain.dto;

import com.webgis.dsws.domain.model.DonVi;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Đối tượng chứa thông tin đăng ký người dùng mới.
 */
@Data
public class RegisterRequest {
    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    @Email(message = "Email không hợp lệ")
    private String email;

    private String hoTen;
    private String soDienThoai;
}