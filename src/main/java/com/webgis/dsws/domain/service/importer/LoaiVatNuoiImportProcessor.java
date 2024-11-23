package com.webgis.dsws.domain.service.importer;

import com.webgis.dsws.util.ImportEntityProcessor;
import com.webgis.dsws.domain.model.LoaiVatNuoi;
import com.webgis.dsws.domain.repository.LoaiVatNuoiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoaiVatNuoiImportProcessor implements ImportEntityProcessor<LoaiVatNuoi> {
    private final LoaiVatNuoiRepository repository;

    @Override
    @Transactional
    public Set<LoaiVatNuoi> processAndSave(Set<String> names) {
        return names.stream()
                .map(this::findOrCreate)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public LoaiVatNuoi findOrCreate(String name) {
        return repository.findByTenLoai(name)
                .orElseGet(() -> {
                    LoaiVatNuoi loaiVatNuoi = new LoaiVatNuoi();
                    loaiVatNuoi.setTenLoai(name);
                    return repository.save(loaiVatNuoi);
                });
    }
}