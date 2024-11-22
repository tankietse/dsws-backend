package com.webgis.dsws.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TrangTraiImportDTO {
    private Integer id;
    private String maSo;
    private String chuCoSo;
    private String dienThoai;
    private String soNha;
    private String tenDuong;
    private String khuPho;
    // private Double lat;
    // private Double lng;
    private String diaChi;
    private String geomWKB;
    private String loaiBenh; // raw string of comma-separated diseases
    private String tenXaPhuong;
    private String tenQuan;
    private String chungLoai; // raw string of comma-separated animal types
    private Integer soLuong;
}