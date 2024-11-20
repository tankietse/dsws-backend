
package com.webgis.dsws.model;

import java.sql.Date;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vung_dich_trang_trai")
public class VungDichTrangTrai {
    @Id
    @ManyToOne
    @JoinColumn(name = "vung_dich_id")
    private VungDich vungDich;

    @Id
    @ManyToOne
    @JoinColumn(name = "trang_trai_id")
    private TrangTrai trangTrai;

    private Float khoangCach;
    private String mucDoAnhHuong;
    private Date ngayBatDauAnhHuong;
    private Date ngayKetThucAnhHuong;
}