
package com.webgis.dsws.model;

import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "don_vi")
public class DonVi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String maDonVi;
    private String tenDonVi;
    private String moTa;
    private Boolean trangThaiHoatDong;

    @ManyToOne
    @JoinColumn(name = "don_vi_cha_id")
    private DonVi donViCha;

    @OneToMany(mappedBy = "donViCha")
    private Set<DonVi> donViCons;

    @OneToMany(mappedBy = "donVi")
    private Set<NguoiDung> nguoiDungs;
}