package com.webgis.dsws.domain.model;

import java.sql.Date;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trang_trai_vat_nuoi")
public class TrangTraiVatNuoi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_trang_trai", nullable = false)
    private TrangTrai trangTrai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_loai_vat_nuoi", nullable = false)
    private LoaiVatNuoi loaiVatNuoi;

    @NotNull(message = "Số lượng không được để trống")
    private Integer soLuong;

    private Date ngayCapNhat;
    // private String nguonGoc;

    @PrePersist
    protected void onCreate() {
        ngayCapNhat = new Date(System.currentTimeMillis());
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = new Date(System.currentTimeMillis());
        // TODO: Thêm logic
    }
}
