package com.webgis.dsws.dto;

import com.webgis.dsws.model.Benh;
import com.webgis.dsws.model.CaBenh;
import com.webgis.dsws.model.NguoiDung;
import com.webgis.dsws.model.TrangTrai;
import com.webgis.dsws.model.enums.TrangThaiEnum;
import lombok.Data;

import java.sql.Date;
import java.time.LocalDate;

@Data
public class CaBenhDTO {
    private Long id;
    private LocalDate ngayPhatHien;
    private String moTaBanDau;
    private Integer soCaNhiemBanDau;
    private Integer soCaTuVongBanDau;
    private String nguyenNhanDuDoan;
    private TrangThaiEnum trangThai = TrangThaiEnum.PENDING;
    private NguoiDung nguoiTao;
    private Benh benh;
    private TrangTrai trangTrai;

    // Chuyển đổi từ entity sang DTO
    public static CaBenhDTO fromEntity(CaBenh caBenh) {
        CaBenhDTO dto = new CaBenhDTO();
        dto.setId(caBenh.getId());
        dto.setNgayPhatHien(caBenh.getNgayPhatHien().toLocalDate());
        dto.setMoTaBanDau(caBenh.getMoTaBanDau());
        dto.setSoCaNhiemBanDau(caBenh.getSoCaNhiemBanDau());
        dto.setSoCaTuVongBanDau(caBenh.getSoCaTuVongBanDau());
        dto.setNguyenNhanDuDoan(caBenh.getNguyenNhanDuDoan());
        dto.setNguoiTao(caBenh.getNguoiTao());
        dto.setBenh(caBenh.getBenh());
        dto.setTrangTrai(caBenh.getTrangTrai());
        return dto;
    }

    // Chuyển đổi từ DTO sang entity
    public CaBenh toEntity() {
        CaBenh caBenh = new CaBenh();
        caBenh.setId(this.id);
        caBenh.setNgayPhatHien(Date.valueOf(this.ngayPhatHien));
        caBenh.setMoTaBanDau(this.moTaBanDau);
        caBenh.setSoCaNhiemBanDau(this.soCaNhiemBanDau);
        caBenh.setSoCaTuVongBanDau(this.soCaTuVongBanDau);
        caBenh.setNguyenNhanDuDoan(this.nguyenNhanDuDoan);
        caBenh.setNguoiTao(this.nguoiTao);
        caBenh.setBenh(this.benh);
        caBenh.setTrangTrai(this.trangTrai);
        return caBenh;
    }
}