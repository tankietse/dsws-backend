package com.webgis.dsws.model;

import java.sql.Date;
import org.locationtech.jts.geom.Geometry;
import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Data
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

    @ManyToOne
    @JoinColumn(name = "trang_thai_id")
    private TrangThaiVungDich trangThai;

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

    @ManyToMany
    @JoinTable(name = "vung_dich_bien_phap", joinColumns = @JoinColumn(name = "vung_dich_id"), inverseJoinColumns = @JoinColumn(name = "bien_phap_id"))
    private Set<BienPhapPhongChong> bienPhapPhongChongs;
}