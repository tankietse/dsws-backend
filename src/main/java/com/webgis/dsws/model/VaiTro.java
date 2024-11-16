package com.webgis.dsws.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vai_tro")
public class VaiTro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên vai trò không được để trống")
    private String tenVaiTro;

    private String moTa;

    @ManyToMany(mappedBy = "vaiTros")
    private Set<NguoiDung> nguoiDungs;

    @ManyToMany
    @JoinTable(
            name = "vai_tro_quyen_han",
            joinColumns = @JoinColumn(name = "vai_tro_id"),
            inverseJoinColumns = @JoinColumn(name = "quyen_han_id")
    )
    private Set<QuyenHan> quyenHans;
}
