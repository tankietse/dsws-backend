package com.webgis.dsws.domain.model;

import com.webgis.dsws.domain.model.enums.TrangThaiEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "nguoi_dung_trang_trai")
public class NguoiDungTrangTrai {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "nguoi_dung_id")
    private NguoiDung nguoiDung;

    @ManyToOne
    @JoinColumn(name = "trang_trai_id")
    private TrangTrai trangTrai;

    @Enumerated(EnumType.STRING)
    private TrangThaiEnum trangThai;
}