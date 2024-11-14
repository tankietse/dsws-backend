package com.webgis.dsws.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trang_trai_vat_nuoi")
public class TrangTraiVatNuoi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "trang_trai_id")
    private TrangTrai trangTrai;

    @ManyToOne
    @JoinColumn(name = "loai_vat_nuoi_id")
    private LoaiVatNuoi loaiVatNuoi;

    @NotNull(message = "Số lượng không được để trống")
    private Integer soLuong;
}

