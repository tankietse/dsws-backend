package com.webgis.dsws.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bien_phap_phong_chong")
public class BienPhapPhongChong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String maBienPhap;

    private String tenBienPhap;

    private String moTa;

    private String huongDanThucHien;

    private Boolean batBuoc;

    private Integer thuTuUuTien;

    private Float chiPhiDuKien;

    private Boolean trangThaiHoatDong;

    @ManyToMany(mappedBy = "bienPhapPhongChongs")
    private Set<VungDich> vungDichs;
}