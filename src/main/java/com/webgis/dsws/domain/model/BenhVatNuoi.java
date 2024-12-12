package com.webgis.dsws.domain.model;

import jakarta.persistence.*;
import lombok.*;

import com.webgis.dsws.domain.model.ids.BenhVatNuoiId;

@Entity
@Table(name = "benh_vatnuoi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BenhVatNuoi {

    @EmbeddedId
    private BenhVatNuoiId id = new BenhVatNuoiId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("benhId")
    @JoinColumn(name = "benh_id", nullable = false)
    private Benh benh;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("loaiVatNuoiId")
    @JoinColumn(name = "loai_vatnuoi_id", nullable = false)
    private LoaiVatNuoi loaiVatNuoi;

    private float tiLeLayNhiem;
    private float tiLeChet;
    private float tiLeHoiPhuc;
    private String datDiemRieng;
}