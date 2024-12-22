package com.webgis.dsws.domain.model;

import java.util.Date;

import com.webgis.dsws.domain.model.ids.VungDichTrangTraiId;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(VungDichTrangTraiId.class) // Khóa chính phức hợp
@Table(name = "vung_dich_trang_trai")
public class VungDichTrangTrai implements Serializable {
    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vung_dich_id")
    @JsonBackReference("vungDich-trangTrais")
    private VungDich vungDich;

    @Id
    @JsonBackReference("trangTrai-vungDich")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trang_trai_id")
    private TrangTrai trangTrai;

    @Column(name = "khoang_cach")
    private Float khoangCach;
    /**
     * Mức độ ảnh hưởng của vùng dịch đến trang trại
     */
    private String mucDoAnhHuong;
    private Date ngayBatDauAnhHuong;
    private Date ngayKetThucAnhHuong;
}
