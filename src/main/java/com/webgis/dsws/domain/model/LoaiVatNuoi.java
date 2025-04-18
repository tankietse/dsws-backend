package com.webgis.dsws.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Set;
import java.util.HashSet;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "loai_vat_nuoi")
public class LoaiVatNuoi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên loại vật nuôi không được để trống")
    private String tenLoai;

    private String moTa;

    @OneToMany(mappedBy = "loaiVatNuoi", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BenhVatNuoi> benhVatNuois = new HashSet<>();

    @OneToMany(mappedBy = "loaiVatNuoi")
    private Set<TrangTraiVatNuoi> trangTraiVatNuois = new HashSet<>();
}
