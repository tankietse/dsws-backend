package com.webgis.dsws.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "benh")
public class Benh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên bệnh không được để trống")
    private String tenBenh;
    private String moTa;
    private String tacNhanGayBenh;
    private String trieuChung;
    private Integer thoiGianUBenh;
    private String phuongPhapChanDoan;
    private String bienPhapPhongNgua;
//    private Boolean trangThaiHoatDong;

    @OneToMany(mappedBy = "benh")
    private List<BenhVatNuoi> benhVatNuoi = new ArrayList<>();
}