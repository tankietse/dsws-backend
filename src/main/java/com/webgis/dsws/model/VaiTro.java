package com.webgis.dsws.model;

import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vai_tro")
public class VaiTro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String maVaiTro;
    private String tenVaiTro;
    private String moTa;
    private Boolean trangThaiHoatDong;

    @OneToMany(mappedBy = "vaiTro")
    private Set<NguoiDungVaiTro> nguoiDungs;
}
