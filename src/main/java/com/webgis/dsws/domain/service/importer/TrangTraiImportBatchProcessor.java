package com.webgis.dsws.domain.service.importer;

import com.webgis.dsws.util.BatchProcessor;
import com.webgis.dsws.dto.TrangTraiImportDTO;
import com.webgis.dsws.mapper.TrangTraiMapper;
import com.webgis.dsws.domain.model.TrangTrai;
import com.webgis.dsws.domain.repository.TrangTraiRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bộ xử lý batch import dữ liệu trang trại.
 * Lớp này thực hiện việc xử lý và import dữ liệu trang trại theo lô,
 * bao gồm kiểm tra trùng lặp và validate dữ liệu trước khi lưu vào database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrangTraiImportBatchProcessor implements BatchProcessor<TrangTraiImportDTO, TrangTrai> {

    /** Kích thước lô xử lý mặc định */
    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private int BATCH_SIZE;

    /** Mapper để chuyển đổi giữa DTO và entity */
    private final TrangTraiMapper mapper;

    /** Repository để thao tác với dữ liệu trang trại */
    private final TrangTraiRepository trangTraiRepository;

    /**
     * Xử lý một lô dữ liệu trang trại cần import.
     *
     * @param batch  Danh sách các DTO chứa thông tin trang trại cần import
     * @param errors StringBuilder để lưu trữ các lỗi phát sinh trong quá trình xử
     *               lý
     * @return Danh sách các entity TrangTrai đã được xử lý và lưu thành công
     *
     *         Quy trình xử lý:
     *         1. Kiểm tra mã số trang trại đã tồn tại trong database
     *         2. Với mỗi DTO trong lô:
     *         - Bỏ qua nếu mã số đã tồn tại
     *         - Chuyển đổi từ DTO sang entity
     *         - Kiểm tra tính hợp lệ của đơn vị hành chính
     *         - Thêm vào danh sách cần lưu nếu hợp lệ
     *         3. Lưu tất cả entities hợp lệ vào database
     */
    @Override
    public List<TrangTrai> processBatch(List<TrangTraiImportDTO> batch, StringBuilder errors) {
        List<TrangTrai> entities = new ArrayList<>();

        // Kiểm tra mã số đã tồn tại trong database
        List<String> maSoList = batch.stream()
                .map(TrangTraiImportDTO::getMaSo)
                .collect(Collectors.toList());

        List<String> existingMaSo = trangTraiRepository.findExistingMaTrangTrai(maSoList);

        for (TrangTraiImportDTO dto : batch) {
            try {
                // Skip if maSo already exists
                if (existingMaSo.contains(dto.getMaSo())) {
                    String warning = String.format(
                            "Bỏ qua record với Mã số: %s - Đã tồn tại trong database",
                            dto.getMaSo());
                    log.warn(warning);
                    errors.append(warning).append("\n");
                    continue;
                }

                TrangTrai entity = mapper.toEntity(dto);
                if (entity.getDonViHanhChinh() == null) {
                    String warning = String.format(
                            "Bỏ qua record với Mã số: %s - Không tìm thấy đơn vị hành chính: %s",
                            dto.getMaSo(), dto.getTenXaPhuong());
                    log.warn(warning);
                    errors.append(warning).append("\n");
                    continue;
                }

                entities.add(entity);
            } catch (Exception e) {
                String error = String.format("Lỗi xử lý record với Mã số: %s - %s",
                        dto.getMaSo(), e.getMessage());
                log.error(error, e);
                errors.append(error).append("\n");
            }
        }

        // Save all entities at once to minimize database calls
        trangTraiRepository.saveAll(entities);

        return entities;
    }

    /**
     * Lấy kích thước lô xử lý.
     *
     * @return Kích thước lô xử lý (số lượng records được xử lý trong một lần)
     */
    @Override
    public int getBatchSize() {
        return BATCH_SIZE;
    }
}