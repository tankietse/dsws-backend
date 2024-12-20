
package com.webgis.dsws.util;

import org.springframework.stereotype.Component;

import com.webgis.dsws.domain.dto.TrangTraiImportDTO;

@Component
public class TrangTraiValidator {
    public void validateDTO(TrangTraiImportDTO dto) {
        validateNotEmpty(dto.getMaSo(), "Mã số");
        validateNotEmpty(dto.getChuCoSo(), "Chủ cơ sở");
        validateNotEmpty(dto.getGeomWKB(), "Geometry");
        validateNotEmpty(dto.getTenXaPhuong(), "Tên phường");
    }

    private void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " không được để trống");
        }
    }
}