package com.webgis.dsws.domain.service.impl;

import com.webgis.dsws.domain.model.Benh;
import com.webgis.dsws.domain.model.CaBenh;
import com.webgis.dsws.domain.model.TrangTrai;
import com.webgis.dsws.domain.repository.BenhRepository;
import com.webgis.dsws.domain.service.BenhService;
import com.webgis.dsws.domain.service.processor.BenhProcessor;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Triển khai dịch vụ cho Benh.
 */
@Service
@RequiredArgsConstructor
public class BenhServiceImpl implements BenhService {
    private final BenhRepository benhRepository;
    private final BenhProcessor benhProcessor;

    @Override
    public List<Benh> findAll() {
        return benhRepository.findAll();
    }

    @Override
    public Optional<Benh> findById(Long id) {
        return benhRepository.findById(id);
    }

    @Override
    @Transactional
    public Benh save(Benh benh) {
        benhProcessor.processBenh(benh);
        return benhRepository.save(benh);
    }

    @Override
    @Transactional
    public Benh update(Long id, Benh benhDetails) {
        Benh existing = findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bệnh với ID: " + id));
        
        benhDetails.setId(id);
        benhProcessor.processBenh(benhDetails);
        return benhRepository.save(benhDetails);
    }

    /**
     * Xử lý danh sách bệnh từ chuỗi và gán cho trang trại.
     *
     * @param benhListStr chuỗi danh sách bệnh
     * @param trangTrai   trang trại cần gán bệnh
     * @return tập hợp các ca bệnh
     */
    public Set<CaBenh> processBenhList(String benhListStr, TrangTrai trangTrai) {
        if (benhListStr == null || benhListStr.trim().isEmpty()) {
            return Collections.emptySet();
        }

        Set<CaBenh> danhSachCaBenh = new HashSet<>();
        String[] benhNames = benhListStr.split(",");

        for (String benhName : benhNames) {
            String cleanBenhName = benhName.trim();
            if (!cleanBenhName.isEmpty()) {
                Benh benh = findOrCreateBenh(cleanBenhName);

                CaBenh newCabenh = new CaBenh();
                newCabenh.setBenh(benh);
                newCabenh.setTrangTrai(trangTrai);
                danhSachCaBenh.add(newCabenh);
            }
        }

        return danhSachCaBenh;
    }

    private Benh findOrCreateBenh(String tenBenh) {
        return benhRepository.findByTenBenh(tenBenh)
                .orElseGet(() -> save(new Benh(null, tenBenh)));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        benhRepository.deleteById(id);
    }
}