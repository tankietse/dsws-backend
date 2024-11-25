package com.webgis.dsws.domain.service.impl;

import com.webgis.dsws.domain.model.Benh;
import com.webgis.dsws.domain.model.CaBenh;
import com.webgis.dsws.domain.model.TrangTrai;
import com.webgis.dsws.domain.model.TrangTraiVatNuoi;
import com.webgis.dsws.domain.model.constant.BenhRegistry;
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
import java.util.stream.Collectors;

/**
 * Triển khai dịch vụ xử lý nghiệp vụ liên quan đến thực thể Benh.
 * Cung cấp các chức năng quản lý bệnh như thêm, sửa, xóa, tìm kiếm
 * và xử lý danh sách bệnh cho trang trại.
 */
@Service
@RequiredArgsConstructor
public class BenhServiceImpl implements BenhService {
    private final BenhRepository benhRepository;
    private final BenhProcessor benhProcessor;

    private String standardizeDiseaseNameAndCheck(String tenBenh) {
        BenhRegistry.DiseaseInfo info = BenhRegistry.getInfo(tenBenh);
        if (info != null) {
            return info.getVietnameseName();
        }
        return tenBenh;
    }

    /**
     * Lấy danh sách tất cả các bệnh trong hệ thống.
     *
     * @return Danh sách các thực thể Benh
     */
    @Override
    public List<Benh> findAll() {
        return benhRepository.findAll();
    }

    /**
     * Tìm kiếm bệnh theo tên hoặc tạo mới nếu chưa tồn tại.
     *
     * @param tenBenh Tên bệnh cần tìm hoặc tạo mới
     * @return Thực thể Benh tương ứng (đã tồn tại hoặc mới tạo)
     */
    @Override
    public Benh findOrCreateBenh(String tenBenh) {
        String standardName = standardizeDiseaseNameAndCheck(tenBenh);
        return benhRepository.findByTenBenh(standardName)
                .orElseGet(() -> {
                    Benh newBenh = new Benh(null, standardName);
                    // Lấy thông tin bổ sung từ registry nếu có
                    BenhRegistry.DiseaseInfo info = BenhRegistry.getInfo(tenBenh);
                    if (info != null) {
                        // Có thể thêm các thông tin bổ sung vào đây nếu cần
                    }
                    return save(newBenh);
                });
    }

    /**
     * Tìm kiếm bệnh theo mã định danh.
     *
     * @param id Mã định danh của bệnh cần tìm
     * @return Optional chứa thực thể Benh nếu tìm thấy, ngược lại là Optional rỗng
     */
    @Override
    public Optional<Benh> findById(Long id) {
        return benhRepository.findById(id);
    }

    /**
     * Lưu thông tin một bệnh mới vào hệ thống.
     * Kiểm tra trùng lặp tên bệnh trước khi lưu.
     *
     * @param benh Thông tin bệnh cần lưu
     * @return Thực thể Benh đã được lưu
     * @throws IllegalArgumentException khi tên bệnh đã tồn tại trong hệ thống
     */
    @Override
    @Transactional
    public Benh save(Benh benh) {
        String standardName = standardizeDiseaseNameAndCheck(benh.getTenBenh());
        benh.setTenBenh(standardName);

        Optional<Benh> existingBenh = benhRepository.findByTenBenh(standardName);
        if (existingBenh.isPresent()) {
            return existingBenh.get(); // Trả về bệnh đã tồn tại thay vì ném exception
        }

        benhProcessor.processBenh(benh);
        return benhRepository.save(benh);
    }

    /**
     * Cập nhật thông tin của một bệnh đã tồn tại.
     * Kiểm tra sự tồn tại của bệnh và tính duy nhất của tên bệnh.
     *
     * @param id          Mã định danh của bệnh cần cập nhật
     * @param benhDetails Thông tin bệnh mới
     * @return Thực thể Benh đã được cập nhật
     * @throws EntityNotFoundException  khi không tìm thấy bệnh với ID tương ứng
     * @throws IllegalArgumentException khi tên bệnh mới đã tồn tại cho một bệnh
     *                                  khác
     */
    @Override
    @Transactional
    public Benh update(Long id, Benh benhDetails) {
        String standardName = standardizeDiseaseNameAndCheck(benhDetails.getTenBenh());
        benhDetails.setTenBenh(standardName);

        Optional<Benh> existingBenh = benhRepository.findByTenBenh(standardName);
        if (existingBenh.isPresent() && !existingBenh.get().getId().equals(id)) {
            throw new IllegalArgumentException("Bệnh đã tồn tại: " + standardName);
        }

        benhDetails.setId(id);
        benhProcessor.processBenh(benhDetails);
        return benhRepository.save(benhDetails);
    }

    /**
     * Xử lý và chuyển đổi danh sách bệnh từ chuỗi thành tập hợp các ca bệnh.
     * Tự động tạo mới bệnh nếu chưa tồn tại trong hệ thống.
     *
     * @param benhListStr Chuỗi chứa danh sách tên các bệnh, phân cách bởi dấu phẩy
     * @param trangTrai   Trang trại được gán các ca bệnh
     * @return Tập hợp các ca bệnh đã được tạo
     */
    @Transactional
    public Set<CaBenh> processBenhList(String benhListStr, TrangTrai trangTrai) {
        if (benhListStr == null || benhListStr.trim().isEmpty()) {
            return Collections.emptySet();
        }

        Set<CaBenh> danhSachCaBenh = new HashSet<>();
        String[] benhNames = benhListStr.split(",");

        try {
            for (String benhName : benhNames) {
                String cleanBenhName = benhName.trim();
                if (!cleanBenhName.isEmpty()) {
                    Benh benh = findOrCreateBenh(cleanBenhName);

                    for (TrangTraiVatNuoi vatNuoi : trangTrai.getTrangTraiVatNuois()) {
                        CaBenh newCabenh = benhProcessor.createInitialCaBenh(benh, vatNuoi);
                        danhSachCaBenh.add(newCabenh);
                    }
                }
            }
            return danhSachCaBenh;
        } catch (Exception e) {
            throw new RuntimeException("Error processing disease list", e);
        }
    }

    public Set<Benh> findOrCreateBenhBatch(Set<String> benhNames) {
        Set<Benh> benhSet = new HashSet<>();
        List<Benh> existingBenhs = benhRepository.findByTenBenhIn(benhNames);

        Set<String> existingNames = existingBenhs.stream()
                .map(Benh::getTenBenh)
                .collect(Collectors.toSet());

        benhSet.addAll(existingBenhs);

        Set<String> newBenhNames = benhNames.stream()
                .filter(name -> !existingNames.contains(name))
                .collect(Collectors.toSet());

        if (!newBenhNames.isEmpty()) {
            List<Benh> newBenhs = newBenhNames.stream()
                    .map(name -> {
                        Benh benh = new Benh();
                        benh.setTenBenh(name);
                        // benh.set
                        return benh;
                    })
                    .collect(Collectors.toList());

            benhRepository.saveAll(newBenhs);
            benhSet.addAll(newBenhs);
        }

        return benhSet;
    }

    /**
     * Xóa một bệnh khỏi hệ thống theo mã định danh.
     *
     * @param id Mã định danh của bệnh cần xóa
     */
    @Override
    @Transactional
    public void deleteById(Long id) {
        benhRepository.deleteById(id);
    }
}