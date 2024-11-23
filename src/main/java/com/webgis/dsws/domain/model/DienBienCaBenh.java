package com.webgis.dsws.domain.model;

import java.sql.Date;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
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
    // @Column(columnDefinition = "BLOB") Hibernate tự động ánh xạ trường kiểu
    // byte[] (khi có @Lob)
    private byte[] fileDinhKem;

    @ManyToOne
    @JoinColumn(name = "nguoi_cap_nhat_id")
    private NguoiDung nguoiCapNhat;
}