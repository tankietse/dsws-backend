package com.webgis.dsws.importer;

import com.webgis.dsws.dto.TrangTraiImportDTO;
import com.webgis.dsws.exception.DataImportException;
import com.webgis.dsws.domain.service.importer.TrangTraiImportBatchProcessor;
import com.webgis.dsws.domain.service.importer.TrangTraiImportService;
import com.webgis.dsws.util.DataValidator;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrangtraiDataImporter {
    private static final int PARALLEL_THRESHOLD = 1000;

    private final TrangTraiImportService importService;
    private final DataValidator dataValidator;
    private final TrangTraiImportBatchProcessor batchProcessor;

    public void importData(String excelFilePath) {
        try {
            // Đọc dữ liệu với progress tracking
            List<TrangTraiImportDTO> dtos = readExcelFileOptimized(excelFilePath);
            processImport(dtos);
        } catch (Exception e) {
            log.error("Lỗi import dữ liệu: ", e);
            throw new DataImportException("Không thể import dữ liệu", e);
        }
    }

    private List<TrangTraiImportDTO> readExcelFileOptimized(String excelFilePath) throws Exception {
        List<TrangTraiImportDTO> dtos;

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(excelFilePath));
                Workbook workbook = WorkbookFactory.create(bis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();

            // Pre-allocate ArrayList size
            dtos = new ArrayList<>(rowCount - 1);

            // Use parallel processing for large datasets
            if (rowCount > PARALLEL_THRESHOLD) {
                processSheetInParallel(sheet, dtos);
            } else {
                processSheetSequential(sheet, dtos);
            }
        }

        return dtos;
    }

    private void processSheetInParallel(Sheet sheet, List<TrangTraiImportDTO> dtos) {
        StreamSupport.stream(sheet.spliterator(), true)
                .skip(1) // Skip header
                .forEach(row -> {
                    try {
                        TrangTraiImportDTO dto = mapRowToDTO(row);
                        synchronized (dtos) {
                            dtos.add(dto);
                        }
                    } catch (Exception e) {
                        log.error("Lỗi xử lý dòng {}: {}", row.getRowNum(), e.getMessage());
                    }
                });
    }

    private void processSheetSequential(Sheet sheet, List<TrangTraiImportDTO> dtos) {
        sheet.forEach(row -> {
            if (row.getRowNum() == 0)
                return; // Skip header
            try {
                dtos.add(mapRowToDTO(row));
            } catch (Exception e) {
                log.error("Lỗi xử lý dòng {}: {}", row.getRowNum(), e.getMessage());
            }
        });
    }

    private void processImport(List<TrangTraiImportDTO> dtos) {
        StringBuilder errors = new StringBuilder();
        int totalRecords = dtos.size();
        int processedRecords = 0;

        // Process in batches
        for (int i = 0; i < dtos.size(); i += batchProcessor.getBatchSize()) {
            int end = Math.min(i + batchProcessor.getBatchSize(), dtos.size());
            List<TrangTraiImportDTO> batch = dtos.subList(i, end);

            batchProcessor.processBatch(batch, errors);

            processedRecords += batch.size();
            logProgress(processedRecords, totalRecords);
        }

        if (errors.length() > 0) {
            log.warn("Import completed with warnings:\n{}", errors);
        }
    }

    private void logProgress(int processed, int total) {
        if (processed % 1000 == 0 || processed == total) {
            log.info("Đã xử lý {}/{} records ({}%)",
                    processed, total,
                    Math.round((float) processed / total * 100));
        }
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
        dto.setLoaiBenh(dataValidator.getOptionalCellValueAsString(row.getCell(11))); // Dữ liệu bệnh

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
