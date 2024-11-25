package com.webgis.dsws.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

import com.webgis.dsws.domain.model.enums.MucDoBenhEnum;

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

    @OneToMany(mappedBy = "benh", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BenhVatNuoi> benhVatNuois = new HashSet<>();

    @OneToMany(mappedBy = "benh", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VungDich> vungDichs = new HashSet<>();

    @ElementCollection(targetClass = MucDoBenhEnum.class)
    @CollectionTable(name = "benh_phan_loai", joinColumns = @JoinColumn(name = "benh_id"))
    @Column(name = "muc_do")
    @Enumerated(EnumType.STRING)
    private Set<MucDoBenhEnum> mucDoBenhs = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "benh_loai_vat_nuoi", joinColumns = @JoinColumn(name = "benh_id"), inverseJoinColumns = @JoinColumn(name = "loai_vat_nuoi_id"))
    private Set<LoaiVatNuoi> loaiVatNuoi = new HashSet<>();

    @Column(name = "can_cong_bo_dich")
    private Boolean canCongBoDich = false;

    @Column(name = "can_phong_benh_bat_buoc")
    private Boolean canPhongBenhBatBuoc = false;

    // Simple constructor
    public Benh(Long id, String tenBenh) {
        this.id = id;
        this.tenBenh = tenBenh;
    }
}