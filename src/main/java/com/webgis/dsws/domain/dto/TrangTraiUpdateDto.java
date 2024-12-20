package com.webgis.dsws.domain.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class TrangTraiUpdateDto {
    private String tenTrangTrai;
    private String tenChu;
    
    @Pattern(regexp = "^\\d{10}$", message = "Số điện thoại phải có 10 chữ số")
    private String soDienThoai;
    
    @Email(message = "Email không hợp lệ")
    private String email;

    private String soNha;
    private String tenDuong;
    private String khuPho;
    private Integer donViHanhChinhId;

    @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
    private Double longitude;
    
    @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
    private Double latitude;

    @Positive(message = "Diện tích phải lớn hơn 0")
    private Float dienTich;

    @Positive(message = "Tổng đàn phải lớn hơn 0")
    private Integer tongDan;
    
    private String phuongThucChanNuoi;
    private List<TrangTraiCreateDto.VatNuoiDto> vatNuoi;
    private Boolean trangThaiHoatDong;
}
