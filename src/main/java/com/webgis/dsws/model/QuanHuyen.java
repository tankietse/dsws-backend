package com.webgis.dsws.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "quan_huyen")
public class QuanHuyen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên quận không được để trống")
    private String tenQuan;

    @OneToMany(mappedBy = "quanHuyen")
    private Set<PhuongXa> phuongXas;
}
