package com.webgis.dsws.service.impl;

import com.webgis.dsws.model.Benh;
import com.webgis.dsws.model.TrangTrai;
import com.webgis.dsws.model.TrangTraiBenh;
import com.webgis.dsws.repository.BenhRepository;
import com.webgis.dsws.repository.TrangTraiBenhRepository;
import com.webgis.dsws.service.BenhService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BenhServiceImpl implements BenhService {
    private final BenhRepository benhRepository;
    private final TrangTraiBenhRepository trangTraiBenhRepository;

    @Override
    public List<Benh> findAll() {
        return benhRepository.findAll();
    }

    @Override
    public Optional<Benh> findById(Long id) {
        return benhRepository.findById(id);
    }

    @Override
    public Benh save(Benh benh) {
        return benhRepository.save(benh);
    }

    public Set<TrangTraiBenh> processBenhList(String benhListStr, TrangTrai trangTrai) {
        if (benhListStr == null || benhListStr.trim().isEmpty()) {
            return Collections.emptySet();
        }

        Set<TrangTraiBenh> trangTraiBenhs = new HashSet<>();
        String[] benhNames = benhListStr.split(",");

        for (String benhName : benhNames) {
            String cleanBenhName = benhName.trim();
            if (!cleanBenhName.isEmpty()) {
                Benh benh = benhRepository.findByTenBenh(cleanBenhName)
                        .orElseGet(() -> save(new Benh(null, cleanBenhName)));

                TrangTraiBenh trangTraiBenh = new TrangTraiBenh();
                trangTraiBenh.setBenh(benh);
                trangTraiBenh.setTrangTrai(trangTrai);
                trangTraiBenhs.add(trangTraiBenh);
            }
        }

        return trangTraiBenhs;
    }

    @Override
    public void deleteById(Long id) {
        benhRepository.deleteById(id);
    }
}