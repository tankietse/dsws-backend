package com.webgis.dsws.domain.model;

import java.util.Date;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(VungDichTrangTraiId.class) // Khóa chính phức hợp
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
    /**
     * Mức độ ảnh hưởng của vùng dịch đến trang trại
     */
    private String mucDoAnhHuong;
    private Date ngayBatDauAnhHuong;
    private Date ngayKetThucAnhHuong;
}
