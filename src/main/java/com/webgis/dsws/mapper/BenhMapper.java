package com.webgis.dsws.mapper;

import com.webgis.dsws.domain.dto.BenhDto;
import com.webgis.dsws.domain.model.Benh;
import com.webgis.dsws.domain.model.LoaiVatNuoi;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class BenhMapper {
    
    public BenhDto toDto(Benh benh) {
        if (benh == null) return null;
        
        BenhDto dto = new BenhDto();
        dto.setId(benh.getId());
        dto.setTenBenh(benh.getTenBenh());
        dto.setMoTa(benh.getMoTa());
        dto.setMucDoBenhs(benh.getMucDoBenhs());
        dto.setLoaiVatNuoiIds(benh.getLoaiVatNuoi().stream()
            .map(LoaiVatNuoi::getId)
            .collect(Collectors.toSet()));
        return dto;
    }

    public Benh toEntity(BenhDto dto) {
        if (dto == null) return null;
        
        Benh benh = new Benh();
        benh.setId(dto.getId());
        benh.setTenBenh(dto.getTenBenh());
        benh.setMoTa(dto.getMoTa());
        benh.setMucDoBenhs(dto.getMucDoBenhs());
        return benh;
    }
}
