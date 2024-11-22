package com.webgis.dsws.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
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
    // private Boolean trangThaiHoatDong;

    // Sử dụng Set thay vì List để tránh trường hợp trùng lặp
    // cascade = CascadeType.ALL: Khi thêm, sửa, xóa bệnh thì các bệnh vật nuôi liên
    // quan cũng thay đổi theo
    @OneToMany(mappedBy = "benh", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BenhVatNuoi> benhVatNuois = new HashSet<>();

    // Tương tự
    @OneToMany(mappedBy = "benh", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VungDich> vungDichs = new HashSet<>();

    // Constructor
    public Benh(Long id, String tenBenh) {
        this.id = id;
        this.tenBenh = tenBenh;
    }
}