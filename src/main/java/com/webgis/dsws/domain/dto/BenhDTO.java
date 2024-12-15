package com.webgis.dsws.domain.dto;

import com.webgis.dsws.domain.model.enums.MucDoBenhEnum;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BenhDTO {
    private Long id;
    @NotEmpty(message = "Tên bệnh không được để trống")
    private String tenBenh;

    @NotEmpty(message = "Mô tả không được để trống")
    private String moTa;

    @NotEmpty(message = "Tác nhân gây bệnh không được để trống")
    private String tacNhanGayBenh;
    private String trieuChung;
    private Integer thoiGianUBenh;
    private String phuongPhapChanDoan;
    private String bienPhapPhongNgua;
    private Set<MucDoBenhEnum> mucDoBenhs;
    private Boolean canCongBoDich;
    private Boolean canPhongBenhBatBuoc;
}
