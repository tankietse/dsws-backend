package com.webgis.dsws.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "benh_vat_nuoi")
public class BenhVatNuoi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_benh")
    private Benh benh;

    @ManyToOne
    @JoinColumn(name = "id_loai_vat_nuoi")
    private LoaiVatNuoi loaiVatNuoi;

    private Float tiLeLayNhiem;
    private Float tiLeTuVong;
    private String dacDiemRieng;
}