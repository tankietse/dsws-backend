
package com.webgis.dsws.model;

import java.sql.Date;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "dien_bien_ca_benh")
public class DienBienCaBenh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ca_benh_id")
    private CaBenh caBenh;

    private Date ngayCapNhat;

    private Integer soCaNhiemMoi;

    private Integer soCaKhoi;

    private Integer soCaTuVong;

    private String bienPhapXuLy;

    private String ketQuaXuLy;

    private String chanDoanChiTiet;

    // TODO: Figure out how to store file in database
    @Lob
    @Column(columnDefinition = "BLOB")
    private byte[] fileDinhKem;

    @ManyToOne
    @JoinColumn(name = "nguoi_cap_nhat_id")
    private NguoiDung nguoiCapNhat;
}