package com.webgis.dsws.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lich_su_cap_nhat")
public class LichSuCapNhat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Bảng cập nhật không được để trống")
    private String bangCapNhat;

    @NotNull(message = "ID bản ghi không được để trống")
    private Long idBanGhi;

    @Column(columnDefinition = "jsonb")
    private String duLieuCu;

    @Column(columnDefinition = "jsonb")
    private String duLieuMoi;

    @ManyToOne
    @JoinColumn(name = "nguoi_dung_id")
    private NguoiDung nguoiDung;

    private LocalDateTime thoiGianCapNhat;

    @NotBlank(message = "Lý do không được để trống")
    private String lyDo;
}

