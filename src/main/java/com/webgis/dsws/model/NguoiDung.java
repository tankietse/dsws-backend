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

    // private String maNguoiDung;
    @NotBlank
    private String tenDangNhap;
    @NotBlank
    private String matKhauHash;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String hoTen;
    @NotBlank
    private String soDienThoai;
    private String chucVu;

    @ManyToOne
    @JoinColumn(name = "don_vi_id")
    private DonVi donVi;

    private LocalDateTime ngayTao;
    private LocalDateTime lanDangNhapCuoi;
    private Boolean trangThaiHoatDong;

    @OneToMany(mappedBy = "nguoiDung")
    private Set<NguoiDungVaiTro> vaiTros;

    @OneToMany(mappedBy = "nguoiTao")
    private Set<CaBenh> caBenhTao;

    @OneToMany(mappedBy = "nguoiDuyet")
    private Set<CaBenh> caBenhDuyet;

    @OneToMany(mappedBy = "nguoiCapNhat")
    private Set<DienBienCaBenh> dienBienCapNhat;

    @OneToMany(mappedBy = "nguoiTao")
    private Set<VungDich> vungDichTao;

    @OneToMany(mappedBy = "nguoiThucHien")
    private Set<VungDichBienPhap> bienPhapThucHien;

    @OneToMany(mappedBy = "nguoiTao")
    private Set<CanhBao> canhBaoTao;
}