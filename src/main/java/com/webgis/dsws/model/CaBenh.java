package com.webgis.dsws.model;

import java.sql.Date;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ca_benh")
public class CaBenh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "trang_trai_id")
    private TrangTrai trangTrai;

    @ManyToOne
    @JoinColumn(name = "benh_id")
    private Benh benh;

    @NotNull
    private Date ngayPhatHien;

    private String moTaBanDau;

    private Integer soCaNhiemBanDau;

    private Integer soCaTuVongBanDau;

    private String nguyenNhanDuDoan;

    @Column(columnDefinition = "geometry")
    private Geometry viTriPhatHien;

    @ManyToOne
    @JoinColumn(name = "nguoi_tao_id")
    private NguoiDung nguoiTao;

    private Date ngayTao;

    @ManyToOne
    @JoinColumn(name = "nguoi_duyet_id")
    private NguoiDung nguoiDuyet;

    private Date ngayDuyet;

    private Boolean daKetThuc;

    @OneToMany(mappedBy = "caBenh")
    private Set<DienBienCaBenh> dienBienCaBenhs;
}
