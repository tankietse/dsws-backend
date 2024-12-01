package com.webgis.dsws.model.dto;

import com.webgis.dsws.model.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NguoiDungDTO {
    private Long id; // Có thể null khi tạo mới
    @NotBlank
    private String tenDangNhap;
    @NotBlank
    private String matKhauHash; // Dùng cho mật khẩu gốc khi tạo hoặc cập nhật
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String hoTen;
    @NotBlank
    private String soDienThoai;
    private String chucVu;
    private Boolean trangThaiHoatDong;
}

