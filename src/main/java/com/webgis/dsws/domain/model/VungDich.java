package com.webgis.dsws.domain.model;

import java.util.Date;
import java.util.HashSet;

import org.locationtech.jts.geom.Geometry;

import com.webgis.dsws.domain.model.enums.MucDoVungDichEnum;
import com.webgis.dsws.domain.model.enums.TrangThaiVungDichEnum;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vung_dich")
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

    @OneToMany(mappedBy = "vungDich")
    private Set<VungDichTrangTrai> trangTrais;

    @OneToMany(mappedBy = "vungDich")
    private Set<CanhBao> canhBaos;

    @ManyToMany(cascade = CascadeType.MERGE) // Khi thêm mới vùng dịch, cập nhật thông tin biện pháp phòng chống
    @JoinTable(name = "vung_dich_bien_phap", joinColumns = @JoinColumn(name = "vung_dich_id"), inverseJoinColumns = @JoinColumn(name = "bien_phap_id"))
    private Set<BienPhapPhongChong> bienPhapPhongChongs;

    @OneToMany(mappedBy = "vungDich", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VungDichBienPhap> vungDichBienPhaps = new HashSet<>(); // Initialize empty set

    // Helper method để quản lý quan hệ 2 chiều
    public void addVungDichBienPhap(VungDichBienPhap vungDichBienPhap) {
        vungDichBienPhaps.add(vungDichBienPhap);
        vungDichBienPhap.setVungDich(this);
    }

    public void removeVungDichBienPhap(VungDichBienPhap vungDichBienPhap) {
        vungDichBienPhaps.remove(vungDichBienPhap);
        vungDichBienPhap.setVungDich(null);
    }
}