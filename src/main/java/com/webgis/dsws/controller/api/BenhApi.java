package com.webgis.dsws.controller.api;


import com.webgis.dsws.domain.model.Benh;
import com.webgis.dsws.domain.repository.BenhRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/benh")
public class BenhApi {
    @Autowired
    private BenhRepository benhRepository;

    @GetMapping
    public Page<Benh> listBenh(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return benhRepository.findAll(pageable);
    }

    @GetMapping("/{id}")
    public Optional<Benh> getBenhById(@PathVariable Long id) {
        return benhRepository.findById(id);
    }

    @PostMapping
    public Benh createBenh(@Valid @RequestBody Benh benh) {
        return benhRepository.save(benh);
    }

    @PutMapping("/{id}")
    public Benh updateBenh(@PathVariable Long id, @Valid @RequestBody Benh benhDetails) {
        Benh benh = benhRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Benh ID: " + id));
        benh.setTenBenh(benhDetails.getTenBenh());
        benh.setMoTa(benhDetails.getMoTa());
        return benhRepository.save(benh);
    }

    @DeleteMapping("/{id}")
    public void deleteBenh(@PathVariable Long id) {
        Benh benh = benhRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Benh ID: " + id));
        benhRepository.delete(benh);
    }
}
