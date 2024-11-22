
package com.webgis.dsws.model;

import java.sql.Date;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vung_dich_bien_phap")
public class VungDichBienPhap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vung_dich_id")
    private VungDich vungDich;

    @ManyToOne
    @JoinColumn(name = "bien_phap_id")
    private BienPhapPhongChong bienPhap;

    private Date ngayApDung;

    private Date ngayKetThuc;

    private String ketQuaThucHien;

    private Float chiPhiThucTe;

    @ManyToOne
    @JoinColumn(name = "nguoi_thuc_hien_id")
    private NguoiDung nguoiThucHien;
}