package com.webgis.dsws.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "nguoi_dung")
public class NguoiDung {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String tenDangNhap;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String matKhauHash;

    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Họ tên không được để trống")
    private String hoTen;

    private LocalDateTime ngayTao;
    private boolean trangThaiHoatDong;

    @OneToMany(mappedBy = "nguoiDung")
    private Set<TrangTrai> trangTrais;

    @ManyToMany
    @JoinTable(
            name = "nguoi_dung_vai_tro",
            joinColumns = @JoinColumn(name = "nguoi_dung_id"),
            inverseJoinColumns = @JoinColumn(name = "vai_tro_id")
    )
    private Set<VaiTro> vaiTros;
}