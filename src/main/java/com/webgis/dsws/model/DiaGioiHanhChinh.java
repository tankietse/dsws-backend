package com.webgis.dsws.model;

import jakarta.persistence.*;
import lombok.Data;

import org. locationtech.jts.geom.Point;
import org.hibernate.internal.build.AllowNonPortable;
import org.locationtech.jts.geom.Polygon; // Chọn thư viện JTS (Java Topology Suite) thay vì AWT (Abstract Window Toolkit) do hỗ trợ phức tạp hơn.


@Data
@Entity
@Table(name = "DiaGioiHanhChinh")
@AllowNonPortable
public class DiaGioiHanhChinh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenDiaGioi")
    private String tenDiaGioi;

    @Column(name = "tenDiaGioiEn")
    private String tenDiaGioiEn;

    // Loại địa giới hành chính: quốc gia, tỉnh, huyện, xã
    @Column(name = "loai")
    private String loai;

    @Column(name = "wikidata")
    private String wikidata;

    @Column(name = "wikipedia")
    private String wikipedia;

    @Column(name = "geom")
    private Polygon geom;

    @Column(name = "centerPoint", columnDefinition = "geometry(Point, 4326)")
    private Point centerPoint;

    @ManyToOne
    @JoinColumn(name = "donViCha_id")
    private DiaGioiHanhChinh donViCha;

    // @OneToMany(mappedBy = "donViCon_id")
    // private Set<DiaGioiHanhChinh> donViCon;

    // Phân loại đơn vị hành chính theo Việt Nam
    @Enumerated(EnumType.STRING)
    @Column(name = "phanLoaiDVHC", nullable = false)
    private PhanLoaiDonViHanhChinh phanLoai;

    // Cấp hành chính theo Việt Nam
    @Enumerated(EnumType.STRING)
    @Column(name = "capHanhChinh", nullable = false)
    private CapHanhChinh capHanhChinh;

    // Admin level theo OpenStreetMap
    @Column(name = "adminLevel")
    private Integer adminLevel;

    @PrePersist
    @PreUpdate
    public void setDefaultAdminLevel() {
        if (this.adminLevel == null && this.phanLoai != null) {
            this.adminLevel = this.phanLoai.getDefaultAdminLevel();
        }
    }

    //Từ adminLevel, phân loại đơn vị hành chính và cấp hành chính, xác định phân loại đơn vị hành chính

    public void setPhanLoai() {
        if (this.adminLevel != null) {
            if (this.adminLevel == 4) {
                this.phanLoai = PhanLoaiDonViHanhChinh.TINH;
            } else if (this.adminLevel == 6) {
                this.phanLoai = PhanLoaiDonViHanhChinh.THANH_PHO_THUOC_TINH;
            } else if (this.adminLevel == 8) {
                this.phanLoai = PhanLoaiDonViHanhChinh.HUYEN;
            } else if (this.adminLevel == 10) {
                this.phanLoai = PhanLoaiDonViHanhChinh.XA;
            }
        }
    }

    // Set cấp hành chính dựa vào phân loại đơn vị hành chính
    public void setCapHanhChinh() {
        if (this.phanLoai != null) {
            if (this.phanLoai == PhanLoaiDonViHanhChinh.TINH || this.phanLoai == PhanLoaiDonViHanhChinh.THANH_PHO_TRUNG_UONG) {
                this.capHanhChinh = CapHanhChinh.CAP_TINH;
            } else if (this.phanLoai == PhanLoaiDonViHanhChinh.THANH_PHO_THUOC_TINH || this.phanLoai == PhanLoaiDonViHanhChinh.THANH_PHO_THUOC_THANH_PHO) {
                this.capHanhChinh = CapHanhChinh.DAC_BIET;
            } else if (this.phanLoai == PhanLoaiDonViHanhChinh.QUAN || this.phanLoai == PhanLoaiDonViHanhChinh.HUYEN || this.phanLoai == PhanLoaiDonViHanhChinh.THI_XA) {
                this.capHanhChinh = CapHanhChinh.CAP_HUYEN;
            } else if (this.phanLoai == PhanLoaiDonViHanhChinh.PHUONG || this.phanLoai == PhanLoaiDonViHanhChinh.XA || this.phanLoai == PhanLoaiDonViHanhChinh.THI_TRAN) {
                this.capHanhChinh = CapHanhChinh.CAP_XA;
            }
        }
    }
}