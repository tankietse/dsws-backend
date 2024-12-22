package com.webgis.dsws.domain.dto;

import com.webgis.dsws.domain.model.enums.MucDoBenhEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BenhDto {
    private Long id;
    private String tenBenh;
    private String moTa;
    private Set<MucDoBenhEnum> mucDoBenhs;
    private Set<Long> loaiVatNuoiIds;
    private String tacNhanGayBenh;
    private String trieuChung;
    private Integer thoiGianUBenh;
    private String phuongPhapChanDoan;
    private String bienPhapPhongNgua;
    private Boolean canCongBoDich;
    private Boolean canPhongBenhBatBuoc;
}
