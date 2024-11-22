package com.webgis.dsws.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "nguoi_dung_vai_tro")
public class NguoiDungVaiTro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "nguoi_dung_id")
    private NguoiDung nguoiDung;

    @ManyToOne
    @JoinColumn(name = "vai_tro_id")
    private VaiTro vaiTro;

    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;

    @PrePersist
    protected void onCreate() {
        if (ngayBatDau == null) {
            ngayBatDau = LocalDateTime.now();
        }
    }
}