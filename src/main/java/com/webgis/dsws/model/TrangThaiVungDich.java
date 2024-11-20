
package com.webgis.dsws.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trang_thai_vung_dich")
public class TrangThaiVungDich {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String maTrangThai;
    private String tenTrangThai;
    private String moTa;
    private String mauHienThi;
}