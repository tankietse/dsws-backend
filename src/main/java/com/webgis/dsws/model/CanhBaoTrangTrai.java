
package com.webgis.dsws.model;

import java.sql.Date;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "canh_bao_trang_trai")
public class CanhBaoTrangTrai {
    @Id
    @ManyToOne
    @JoinColumn(name = "canh_bao_id")
    private CanhBao canhBao;

    @Id
    @ManyToOne
    @JoinColumn(name = "trang_trai_id")
    private TrangTrai trangTrai;

    private Date ngayGui;
    private String phuongThucGui;
    private Boolean daXem;
    private Date ngayXem;
}