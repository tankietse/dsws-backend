package com.webgis.dsws.service.importer;

import com.webgis.dsws.dto.TrangTraiImportDTO;
import com.webgis.dsws.exception.DataImportException;
import com.webgis.dsws.mapper.TrangTraiMapper;
import com.webgis.dsws.model.TrangTrai;
import com.webgis.dsws.repository.TrangTraiRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrangTraiImportService {
    private final TrangTraiMapper trangTraiMapper;
    private final TrangTraiRepository trangTraiRepository;
    private final TrangTraiImportBatchProcessor batchProcessor;
    
    @Transactional
    public void importTrangTrai(List<TrangTraiImportDTO> dtos) {
        StringBuilder errors = new StringBuilder();
        List<TrangTraiImportDTO> currentBatch = new ArrayList<>();
        int count = 0;

        for (TrangTraiImportDTO dto : dtos) {
            try {
                currentBatch.add(dto);
                if (currentBatch.size() >= batchProcessor.getBatchSize()) {
                    batchProcessor.processBatch(currentBatch, errors);
                    currentBatch = new ArrayList<>();
                }
                count++;
            } catch (Exception e) {
                logImportError(dto, count, e, errors);
            }
        }

        processRemainingBatch(currentBatch, errors);
        throwIfErrors(errors);

        List<TrangTrai> trangTrais = dtos.stream()
                .map(trangTraiMapper::toEntity)
                .collect(Collectors.toList());
        trangTraiRepository.saveAll(trangTrais);
    }

    private void logImportError(TrangTraiImportDTO dto, int count, Exception e, StringBuilder errors) {
        String error = String.format("Dòng %d (Mã số: %s): %s", count + 1, dto.getMaSo(), e.getMessage());
        log.error(error, e);
        errors.append(error).append("\n");
    }

    private void processRemainingBatch(List<TrangTraiImportDTO> batch, StringBuilder errors) {
        if (!batch.isEmpty()) {
            batchProcessor.processBatch(batch, errors);
        }
    }

    private void throwIfErrors(StringBuilder errors) {
        if (!errors.isEmpty()) {
            throw new DataImportException("Lỗi khi import dữ liệu:\n" + errors);
        }
    }
}