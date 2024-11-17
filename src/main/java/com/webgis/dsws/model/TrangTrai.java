package com.webgis.dsws.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import java.util.Set;

@Entity
@Getter
@Setter
@ToString(exclude = {"donViHanhChinh", "nguoiDung", "trangTraiVatNuois", "trangTraiBenhs"})
@Table(name = "trang_trai")
public class TrangTrai {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên chủ trang trại không được để trống")
    private String tenChuTrangTrai;

//    @NotBlank(message = "Số điện thoại không được để trống")
    private String soDienThoai;

//    @NotBlank(message = "Số nhà không được để trống")
    private String soNha;

    private Float tongDienTich;

//    @NotBlank(message = "Khu phố không được để trống")
    private String khuPho;

//    @NotBlank(message = "Địa chỉ đầy đủ không được để trống")
    private String diaChiDayDu;

    @Column(unique = true)
    @NotBlank(message = "Mã số không được để trống")
    private String maSo;

    // Vị trí địa lý của trang trại (kiểu Point với SRID 4326)
    @Column(columnDefinition = "geometry(Point,4326)")
    private Point geom;

    private Integer tongDan;

    // Quan hệ với đơn vị hành chính
    @ManyToOne
    @JoinColumn(name = "don_vi_hanh_chinh_id")
    private DonViHanhChinh donViHanhChinh;

    @ManyToOne
    @JoinColumn(name = "nguoi_dung_id")
    private NguoiDung nguoiDung;

    // Các quan hệ một-nhiều
    @OneToMany(mappedBy = "trangTrai")
    private Set<TrangTraiVatNuoi> trangTraiVatNuois;

    @OneToMany(mappedBy = "trangTrai")
    private Set<TrangTraiBenh> trangTraiBenhs;
}
