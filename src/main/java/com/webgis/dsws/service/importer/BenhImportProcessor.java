
package com.webgis.dsws.service.importer;

import com.webgis.dsws.common.impl.ImportEntityProcessor;
import com.webgis.dsws.model.Benh;
import com.webgis.dsws.repository.BenhRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BenhImportProcessor implements ImportEntityProcessor<Benh> {
    private final BenhRepository repository;

    @Override
    @Transactional
    public Set<Benh> processAndSave(Set<String> names) {
        return names.stream()
                .map(this::findOrCreate)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public Benh findOrCreate(String name) {
        return repository.findByTenBenh(name)
                .orElseGet(() -> {
                    Benh benh = new Benh();
                    benh.setTenBenh(name);
                    return repository.save(benh);
                });
    }
}