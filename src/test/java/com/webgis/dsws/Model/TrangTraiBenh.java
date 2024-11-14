package com.webgis.dsws.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trang_trai_benh")
public class TrangTraiBenh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "trang_trai_id")
    private TrangTrai trangTrai;

    @ManyToOne
    @JoinColumn(name = "benh_id")
    private Benh benh;

    private LocalDateTime ngayPhatHien;

    @NotBlank(message = "Mô tả không được để trống")
    private String moTa;

    @Enumerated(EnumType.STRING)
    private TrangThai trangThai;

    @ManyToOne
    @JoinColumn(name = "nguoi_tao_id")
    private NguoiDung nguoiTao;

    @ManyToOne
    @JoinColumn(name = "nguoi_duyet_id")
    private NguoiDung nguoiDuyet;

    private LocalDateTime ngayTao;
    private LocalDateTime ngayDuyet;
}