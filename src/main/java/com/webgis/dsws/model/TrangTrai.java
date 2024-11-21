package com.webgis.dsws.model;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trang_trai")
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
    private DonViHanhChinh donViHanhChinh;

    private Float dienTich;
    private Integer tongDan;
    private String phuongThucChanNuoi;

    @ManyToOne
    @JoinColumn(name = "nguoi_quan_ly_id")
    private NguoiDung nguoiQuanLy;

    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private Boolean trangThaiHoatDong;

    // Relationships
    @OneToMany(mappedBy = "trangTrai")
    private Set<TrangTraiVatNuoi> danhSachVatNuoi;

    @OneToMany(mappedBy = "trangTrai")
    private Set<CaBenh> danhSachCaBenh;

    @OneToMany(mappedBy = "trangTrai")
    private Set<VungDichTrangTrai> danhSachVungDich;

    @OneToMany(mappedBy = "trangTrai")
    private Set<CanhBaoTrangTrai> danhSachCanhBao;

    @OneToMany(mappedBy = "trangTrai")
    private Set<VungDichTrangTrai> vungDichs;

    @OneToMany(mappedBy = "trangTrai")
    private Set<CanhBaoTrangTrai> canhBaos;
}
