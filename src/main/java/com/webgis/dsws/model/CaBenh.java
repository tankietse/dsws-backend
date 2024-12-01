package com.webgis.dsws.model;

import java.sql.Date;
import java.util.Set;

import com.webgis.dsws.model.enums.TrangThaiEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import com.webgis.dsws.model.VungDich;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ca_benh")
public class CaBenh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "trang_trai_id")
    private TrangTrai trangTrai;

    @ManyToOne
    @JoinColumn(name = "benh_id")
    private Benh benh;

    @NotNull
    private Date ngayPhatHien;

    private String moTaBanDau;

    private Integer soCaNhiemBanDau;

    private Integer soCaTuVongBanDau;

    private String nguyenNhanDuDoan;

    // @Column(columnDefinition = "geometry")
    // private Geometry viTriPhatHien;

    @ManyToOne
    @JoinColumn(name = "nguoi_tao_id")
    private NguoiDung nguoiTao;

    private Date ngayTao;

    @ManyToOne
    @JoinColumn(name = "nguoi_duyet_id")
    private NguoiDung nguoiDuyet;

    private Date ngayDuyet;

    private Boolean daKetThuc;

    @Enumerated(EnumType.STRING)
    private TrangThaiEnum trangThai;

    @OneToMany(mappedBy = "caBenh")
    private Set<DienBienCaBenh> dienBienCaBenhs;

    @PrePersist
    protected void onCreate() {
        ngayTao = new Date(System.currentTimeMillis());
        if (ngayPhatHien == null) {
            ngayPhatHien = ngayTao;
        }
        daKetThuc = false;
        // Mặc định khi tạo mới sẽ ở trạng thái PENDING
        trangThai = TrangThaiEnum.PENDING;
    }

    /**
     * Tạo vùng dịch mới dựa trên thông tin ca bệnh
     * 
     * @return Vùng dịch mới được tạo
     */
    public VungDich taoVungDichMoi() {
        VungDich vungDich = new VungDich();
        // Thiết lập các thuộc tính cho vùng dịch
        // vungDich.setBenh(this.benh);
        // vungDich.setGeom(this.trangTrai.getGeom());
        // vungDich.setNgayBatDau(this.ngayPhatHien);
        // Lưu vùng dịch vào cơ sở dữ liệu
        // ...
        return vungDich;
    }
}
