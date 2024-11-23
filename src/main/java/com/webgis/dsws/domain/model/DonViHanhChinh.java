package com.webgis.dsws.domain.model;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import com.webgis.dsws.domain.model.enums.AdminLevelEnum;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "donViCha", "dsDonViCon" }) // Thêm exclude để tránh lỗi vòng lặp vô hạn
@Entity
@Table(name = "don_vi_hanh_chinh")
public class DonViHanhChinh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // @Column(name = "ma", unique = true)
    // private String code;

    @Column(name = "ten")
    private String ten;

    @Column(name = "ten_tieng_anh")
    private String tenTiengAnh;

    @Column(name = "cap_hanh_chinh")
    private String capHanhChinh;

    // @Column(name = "dien_tich")
    // private Double dienTich;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminLevelEnum adminLevel;

    @ManyToOne(fetch = FetchType.LAZY) // Thêm LAZY loading
    @JoinColumn(name = "id_cha")
    private DonViHanhChinh donViCha;

    @OneToMany(mappedBy = "donViCha", fetch = FetchType.LAZY)
    private List<DonViHanhChinh> dsDonViCon;

    @Column(name = "ranh_gioi", columnDefinition = "geometry")
    private Geometry ranhGioi;

    @Column(name = "diem_trung_tam", columnDefinition = "geometry")
    private Point centerPoint;

}