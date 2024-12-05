package com.webgis.dsws.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.webgis.dsws.domain.model.VaiTro;
import com.webgis.dsws.domain.repository.VaiTroRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VaiTroService {
    private final VaiTroRepository vaiTroRepository;

    public List<VaiTro> findAll() {
        return vaiTroRepository.findAll();
    }

    public VaiTro findRoleByName(String name) {
        return vaiTroRepository.findVaiTroByTenVaiTro(name);
    }
}