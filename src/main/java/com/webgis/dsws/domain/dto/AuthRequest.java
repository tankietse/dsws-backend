
package com.webgis.dsws.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * Đối tượng chứa thông tin đăng nhập.
 */
@Data
public class AuthRequest {
    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}