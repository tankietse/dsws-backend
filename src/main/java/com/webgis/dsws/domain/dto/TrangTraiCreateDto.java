package com.webgis.dsws.domain.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class TrangTraiCreateDto {
    @Pattern(regexp = "^[A-Z0-9]{3,10}$", message = "Mã trang trại phải từ 3-10 ký tự chữ hoa hoặc số")
    private String maTrangTrai;
    
    @NotBlank(message = "Tên trang trại không được để trống")
    private String tenTrangTrai;
    
    @NotBlank(message = "Tên chủ không được để trống")
    private String tenChu;
    
    @Pattern(regexp = "^\\d{10}$", message = "Số điện thoại phải có 10 chữ số")
    private String soDienThoai;
    
    @Email(message = "Email không hợp lệ")
    private String email;

    private String soNha;
    private String tenDuong;
    private String khuPho;
    
    @NotNull(message = "ID đơn vị hành chính không được để trống")
    private Integer donViHanhChinhId;

    @NotNull(message = "Kinh độ không được để trống")
    @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
    private Double longitude;
    
    @NotNull(message = "Vĩ độ không được để trống")
    @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
    private Double latitude;

    @NotNull(message = "Diện tích không được để trống")
    @Positive(message = "Diện tích phải lớn hơn 0")
    private Float dienTich;

    @NotNull(message = "Tổng đàn không được để trống")
    @Positive(message = "Tổng đàn phải lớn hơn 0")
    private Integer tongDan;

    @NotBlank(message = "Phương thức chăn nuôi không được để trống")
    private String phuongThucChanNuoi;

    @NotEmpty(message = "Phải có ít nhất một loại vật nuôi")
    private List<VatNuoiDto> vatNuoi;

    @Data
    public static class VatNuoiDto {
        @NotNull(message = "ID loại vật nuôi không được để trống")
        private Long loaiVatNuoiId;
        
        @NotNull(message = "Số lượng không được để trống")
        @Positive(message = "Số lượng phải lớn hơn 0")
        private Integer soLuong;
    }
}