package com.webgis.dsws.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Đối tượng chứa phản hồi sau khi đăng nhập thành công.
 */
@Data
@AllArgsConstructor
public class AuthResponse {
    private String jwt_token;
    private String redirectUrl;

    public AuthResponse(String jwt_token) {
        this.jwt_token = jwt_token;
        this.redirectUrl = "/";
    }
}