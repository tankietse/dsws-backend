package com.webgis.dsws.service;

import com.webgis.dsws.model.VaiTro;
import com.webgis.dsws.repository.VaiTroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
