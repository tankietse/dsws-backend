package com.webgis.dsws.service;

import com.webgis.dsws.dto.TrangTraiImportDTO;
import com.webgis.dsws.mapper.TrangTraiMapper;
import com.webgis.dsws.model.TrangTrai;
import com.webgis.dsws.repository.TrangTraiRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrangTraiImportBatchProcessor implements BatchProcessor<TrangTraiImportDTO, TrangTrai> {
    private static final int BATCH_SIZE = 50;
    private final TrangTraiMapper mapper;
    private final TransactionService transactionService;
    private final TrangTraiRepository trangTraiRepository;

    @Override
    public List<TrangTrai> processBatch(List<TrangTraiImportDTO> batch, StringBuilder errors) {
        List<TrangTrai> entities = new ArrayList<>();

        // Kiểm tra mã số đã tồn tại trong database
        List<String> maSoList = batch.stream()
                .map(TrangTraiImportDTO::getMaSo)
                .collect(Collectors.toList());

        List<String> existingMaSo = trangTraiRepository.findExistingMaTrangTrai(maSoList);

        transactionService.executeInTransaction(batch, dtos -> {
            for (TrangTraiImportDTO dto : dtos) {
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
            return entities;
        }, e -> {
            String error = "Lỗi khi xử lý batch: " + e.getMessage();
            log.error(error, e);
            errors.append(error).append("\n");
        });

        return entities;
    }

    @Override
    public int getBatchSize() {
        return BATCH_SIZE;
    }
}