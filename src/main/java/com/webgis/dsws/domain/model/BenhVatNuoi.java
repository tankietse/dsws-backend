package com.webgis.dsws.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "benh_vatnuoi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BenhVatNuoi {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "benh_id", nullable = false)
    private Benh benh;

    @ManyToOne
    @JoinColumn(name = "loai_vatnuoi_id", nullable = false)
    private LoaiVatNuoi loaiVatNuoi;
}