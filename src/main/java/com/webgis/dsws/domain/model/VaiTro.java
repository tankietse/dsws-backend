package com.webgis.dsws.domain.model;

import java.util.Set;

import com.webgis.dsws.domain.model.enums.QuyenHanEnum;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
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
    private Set<NguoiDungVaiTro> danhSachNguoiDung;

    @ElementCollection(targetClass = QuyenHanEnum.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "vai_tro_quyen_han", joinColumns = @JoinColumn(name = "vai_tro_id"))
    @Column(name = "quyen_han")
    private Set<QuyenHanEnum> danhSachQuyenHan;
}
