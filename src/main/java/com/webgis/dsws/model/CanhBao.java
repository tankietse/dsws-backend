package com.webgis.dsws.model;

import java.sql.Date;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "canh_bao")
public class CanhBao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String maCanhBao;

    @ManyToOne
    @JoinColumn(name = "vung_dich_id")
    private VungDich vungDich;

    private String tieuDe;
    private String noiDung;
    private String mucDoKhanCap;
    private Date ngayTao;
    private Date ngayHetHieuLuc;
    private Boolean daGui;

    @ManyToOne
    @JoinColumn(name = "nguoi_tao_id")
    private NguoiDung nguoiTao;
}