package com.webgis.dsws.domain.model;

import java.util.Date;
import java.util.HashSet;

import org.locationtech.jts.geom.Geometry;

import com.webgis.dsws.domain.model.enums.MucDoVungDichEnum;
import com.webgis.dsws.domain.model.enums.TrangThaiVungDichEnum;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vung_dich", schema = "public")
public class VungDich {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String maVung;
    private String tenVung;

    @ManyToOne
    @JoinColumn(name = "benh_id")
    private Benh benh;

    @Column(columnDefinition = "geometry")
    private Geometry geom;

    private Float banKinh;
    private Date ngayBatDau;
    private Date ngayKetThuc;

    @Enumerated(EnumType.STRING)
    private TrangThaiVungDichEnum trangThai;

    @Enumerated(EnumType.STRING)
    private MucDoVungDichEnum mucDo;

    private Integer mucDoNghiemTrong;
    private String bienPhapXuLy;
    private String lyDoKetThuc;

    @ManyToOne
    @JoinColumn(name = "nguoi_tao_id")
    private NguoiDung nguoiTao;

    @JsonManagedReference
    @OneToMany(mappedBy = "vungDich", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VungDichTrangTrai> trangTrais = new HashSet<>();

    @OneToMany(mappedBy = "vungDich")
    private Set<CanhBao> canhBaos;

    @ManyToMany(cascade = CascadeType.MERGE) // Khi thêm mới vùng dịch, cập nhật thông tin biện pháp phòng chống
    @JoinTable(name = "vung_dich_bien_phap", joinColumns = @JoinColumn(name = "vung_dich_id"), inverseJoinColumns = @JoinColumn(name = "bien_phap_id"))
    private Set<BienPhapPhongChong> bienPhapPhongChongs;

    @OneToMany(mappedBy = "vungDich", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VungDichBienPhap> vungDichBienPhaps = new HashSet<>(); // Initialize empty set

    private String moTa;
    private String colorCode;
}