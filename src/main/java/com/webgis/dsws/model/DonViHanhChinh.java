package com.webgis.dsws.model;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "don_vi_hanh_chinh")
public class DonViHanhChinh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ten;
    private String tenTiengAnh;
    private String capHanhChinh;
    private String adminLevel;

    @Column(columnDefinition = "geometry")
    private Geometry ranhGioi;

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point centerPoint;

    @ManyToOne
    @JoinColumn(name = "don_vi_cha_id")
    private DonViHanhChinh donViCha;

    @OneToMany(mappedBy = "donViCha")
    private Set<DonViHanhChinh> dsDonViCon;

    @OneToMany(mappedBy = "donViHanhChinh")
    private Set<TrangTrai> trangTrais;
}