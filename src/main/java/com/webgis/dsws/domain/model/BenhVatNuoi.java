package com.webgis.dsws.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "benh_vat_nuoi")
public class BenhVatNuoi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Sử dụng FetchType.LAZY để tránh việc load dữ liệu không cần thiết
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_benh", nullable = false)
    private Benh benh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_loai_vat_nuoi", nullable = false)
    private LoaiVatNuoi loaiVatNuoi;

    private Float tiLeLayNhiem;
    private Float tiLeTuVong;
    private String dacDiemRieng;
}