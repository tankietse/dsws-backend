package com.webgis.dsws.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "phuong_xa")
public class PhuongXa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên phường không được để trống")
    private String tenPhuong;

    @ManyToOne
    @JoinColumn(name = "quan_huyen_id")
    private QuanHuyen quanHuyen;

    @OneToMany(mappedBy = "phuong")
    private Set<TrangTrai> trangTrais;
}

