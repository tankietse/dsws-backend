package com.webgis.dsws.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Set;

@Entity
@Data
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

    @OneToMany(mappedBy = "loaiVatNuoi")
    private Set<TrangTraiVatNuoi> trangTraiVatNuois;
}

