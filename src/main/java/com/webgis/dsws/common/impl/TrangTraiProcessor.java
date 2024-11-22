
package com.webgis.dsws.common.impl;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.webgis.dsws.model.TrangTrai;
import com.webgis.dsws.repository.TrangTraiRepository;

@Component
public class TrangTraiProcessor implements ImportEntityProcessor<TrangTrai> {
    private final TrangTraiRepository trangTraiRepository;

    public TrangTraiProcessor(TrangTraiRepository trangTraiRepository) {
        this.trangTraiRepository = trangTraiRepository;
    }

    @Override
    public Set<TrangTrai> processAndSave(Set<String> names) {
        Set<TrangTrai> result = new HashSet<>();
        for (String name : names) {
            result.add(findOrCreate(name));
        }
        return result;
    }

    @Override
    public TrangTrai findOrCreate(String name) {
        return trangTraiRepository.findByTenTrangTrai(name)
                .orElseGet(() -> {
                    TrangTrai newTrangTrai = new TrangTrai();
                    newTrangTrai.setTenTrangTrai(name);
                    return trangTraiRepository.save(newTrangTrai);
                });
    }
}