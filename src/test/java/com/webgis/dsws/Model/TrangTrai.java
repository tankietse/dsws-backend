package com.webgis.dsws.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.locationtech.jts.geom.Geometry;

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

    @NotBlank(message = "Tên chủ trang trại không được để trống")
    private String tenChuTrangTrai;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String soDienThoai;

    @NotBlank(message = "Số nhà không được để trống")
    private String soNha;

    private Float tongDienTich;

    @NotBlank(message = "Khu phố không được để trống")
    private String khuPho;

    private Float lat;
    private Float lng;

    @NotBlank(message = "Địa chỉ đầy đủ không được để trống")
    private String diaChiDayDu;

    @Column(columnDefinition = "geometry")
    private Geometry geom;

    private Integer tongDan;

    @ManyToOne
    @JoinColumn(name = "phuong_id")
    private PhuongXa phuong;

    @ManyToOne
    @JoinColumn(name = "nguoi_dung_id")
    private NguoiDung nguoiDung;

    @OneToMany(mappedBy = "trangTrai")
    private Set<TrangTraiVatNuoi> trangTraiVatNuois;

    @OneToMany(mappedBy = "trangTrai")
    private Set<TrangTraiBenh> trangTraiBenhs;
}

