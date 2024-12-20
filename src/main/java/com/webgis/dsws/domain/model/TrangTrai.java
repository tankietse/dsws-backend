package com.webgis.dsws.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIdentityReference;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trang_trai")
@JsonIgnoreProperties({ "ranhGioi", "centerPoint", "dsDonViCon" })
public class TrangTrai {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String maTrangTrai;
    private String tenTrangTrai;
    private String tenChu;
    private String soDienThoai;
    private String email;

    private String soNha;
    private String tenDuong;
    private String khuPho;
    private String diaChiDayDu;

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point point;

    @ManyToOne
    @JoinColumn(name = "don_vi_hanh_chinh_id")
    @JsonIdentityReference(alwaysAsId = true)
    private DonViHanhChinh donViHanhChinh;

    private Float dienTich;
    private Integer tongDan;
    private String phuongThucChanNuoi;

    @ManyToOne
    @JoinColumn(name = "nguoi_quan_ly_id")
    private NguoiDung nguoiQuanLy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ngayTao;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ngayCapNhat;
    private Boolean trangThaiHoatDong;

    // Relationships
    @OneToMany(mappedBy = "trangTrai")
    private Set<TrangTraiVatNuoi> danhSachVatNuoi;

    @OneToMany(mappedBy = "trangTrai")
    private Set<CaBenh> danhSachCaBenh;

    @JsonManagedReference
    @OneToMany(mappedBy = "trangTrai")
    private Set<VungDichTrangTrai> danhSachVungDich;

    @OneToMany(mappedBy = "trangTrai")
    private Set<CanhBaoTrangTrai> danhSachCanhBao;

    @OneToMany(mappedBy = "trangTrai")
    @JsonManagedReference
    private Set<VungDichTrangTrai> vungDichs;

    @OneToMany(mappedBy = "trangTrai")
    private Set<CanhBaoTrangTrai> canhBaos;

    @OneToMany(mappedBy = "trangTrai", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<CaBenh> caBenhs = new HashSet<>();

    @OneToMany(mappedBy = "trangTrai", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<TrangTraiVatNuoi> trangTraiVatNuois = new HashSet<>();
}
