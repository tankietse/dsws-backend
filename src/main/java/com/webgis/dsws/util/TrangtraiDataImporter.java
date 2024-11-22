package com.webgis.dsws.util;

import com.webgis.dsws.common.DataValidator;
import com.webgis.dsws.dto.TrangTraiImportDTO;
import com.webgis.dsws.exception.DataImportException;
import com.webgis.dsws.service.importer.TrangTraiImportService;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TrangtraiDataImporter {
    // Sử dụng service layer để xử lý logic import
    private final TrangTraiImportService importService;
    private final DataValidator dataValidator;

    // Phương thức chính để import dữ liệu từ file Excel
    public void importData(String excelFilePath) {
        try {
            List<TrangTraiImportDTO> dtos = readExcelFile(excelFilePath);
            importService.importTrangTrai(dtos);
        } catch (Exception e) {
            throw new DataImportException("Không thể import dữ liệu", e);
        }
    }

    // Đọc file Excel và chuyển đổi thành danh sách DTOs
    private List<TrangTraiImportDTO> readExcelFile(String excelFilePath) throws Exception {
        List<TrangTraiImportDTO> dtos = new ArrayList<>();

        //
        try (Workbook workbook = WorkbookFactory.create(new File(excelFilePath))) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;
                dtos.add(mapRowToDTO(row));
            }
        }
        return dtos;
    }

    // Chuyển đổi một dòng trong Excel thành đối tượng DTO
    private TrangTraiImportDTO mapRowToDTO(Row row) throws Exception {
        TrangTraiImportDTO dto = new TrangTraiImportDTO();

        // Bắt buộc các trường dữ liệu cơ bản
        dto.setMaSo(dataValidator.getCellValueAsString(row.getCell(1), "Mã số", row.getRowNum()));
        dto.setChuCoSo(dataValidator.getCellValueAsString(row.getCell(2), "Chủ cơ sở", row.getRowNum()));

        // Dữ liêu không bắt buộc
        dto.setDienThoai(dataValidator.getOptionalCellValueAsString(row.getCell(3)));
        dto.setSoNha(dataValidator.getOptionalCellValueAsString(row.getCell(4)));
        dto.setTenDuong(dataValidator.getOptionalCellValueAsString(row.getCell(5)));
        dto.setKhuPho(dataValidator.getOptionalCellValueAsString(row.getCell(6)));
        dto.setDiaChi(dataValidator.getOptionalCellValueAsString(row.getCell(9)));
        dto.setLoaiBenh(dataValidator.getOptionalCellValueAsString(row.getCell(11)));

        // Lấy geometry WKT thay vì lat/long
        dto.setGeomWKB(dataValidator.getCellValueAsString(row.getCell(10), "Geometry", row.getRowNum()));

        // Dữ liệu bắt buộc khác
        dto.setTenXaPhuong(dataValidator.getCellValueAsString(row.getCell(12), "Tên phường", row.getRowNum()));
        dto.setTenQuan(dataValidator.getOptionalCellValueAsString(row.getCell(13)));
        dto.setChungLoai(dataValidator.getCellValueAsString(row.getCell(14), "Chủng loại", row.getRowNum()));
        dto.setSoLuong(
                Integer.valueOf(dataValidator.getCellValueAsString(row.getCell(15), "Tổng đàn", row.getRowNum())));

        return dto;
    }
}
