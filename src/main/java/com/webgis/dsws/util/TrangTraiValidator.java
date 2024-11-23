
package com.webgis.dsws.util;

import com.webgis.dsws.dto.TrangTraiImportDTO;
import org.springframework.stereotype.Component;

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