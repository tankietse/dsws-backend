package com.webgis.dsws.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "quyen_han")
public class QuyenHan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên quyền không được để trống")
    private String tenQuyen;

    private String moTa;

    @ManyToMany(mappedBy = "quyenHan")
    private Set<VaiTro> danhSachVaiTro;
}
