package com.webgis.dsws.domain.service;

import com.webgis.dsws.domain.dto.RegisterRequest;
import com.webgis.dsws.domain.model.NguoiDung;
import com.webgis.dsws.domain.model.NguoiDungVaiTro;
import com.webgis.dsws.domain.model.VaiTro;
import com.webgis.dsws.domain.dto.NguoiDungDTO;
import com.webgis.dsws.domain.model.enums.VaiTroEnum;
import com.webgis.dsws.domain.repository.NguoiDungRepository;
import com.webgis.dsws.domain.repository.NguoiDungVaiTroRepository;
import com.webgis.dsws.domain.repository.VaiTroRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional  
@CacheConfig(cacheNames = "users")
public class NguoiDungService implements UserDetailsService {
    @Autowired
    private NguoiDungRepository nguoiDungRepository;
    @Autowired
    private VaiTroRepository vaiTroRepository;
    @Autowired
    private NguoiDungVaiTroRepository nguoiDungVaiTroRepository;
    @Autowired
    private HttpSession session;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void save(com.webgis.dsws.domain.dto.NguoiDungDTO userDto) {
        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setTenDangNhap(userDto.getTenDangNhap());
        nguoiDung.setEmail(userDto.getEmail());
        nguoiDung.setHoTen(userDto.getHoTen());
        nguoiDung.setSoDienThoai(userDto.getSoDienThoai());
        nguoiDung.setChucVu(userDto.getChucVu());
        nguoiDung.setTrangThaiHoatDong(userDto.getTrangThaiHoatDong());
        nguoiDung.setNgayTao(LocalDateTime.now());
        nguoiDung.setMatKhauHash(passwordEncoder.encode(userDto.getMatKhauHash()));
        nguoiDungRepository.save(nguoiDung);
    }

    public void setDefaultRole(String username) {
        nguoiDungRepository.findByTenDangNhap(username).ifPresentOrElse(
                user -> {
                    // Kiểm tra xem người dùng đã có vai trò chưa
                    if (user.getVaiTros().stream()
                            .noneMatch(vt -> vt.getVaiTro().getMaVaiTro().equals(VaiTroEnum.USER.value))) {
                        // Tạo một đối tượng NguoiDungVaiTro mới
                        VaiTro defaultRole = vaiTroRepository.findVaiTroById(VaiTroEnum.USER.value);
                        NguoiDungVaiTro nguoiDungVaiTro = new NguoiDungVaiTro();
                        nguoiDungVaiTro.setNguoiDung(user); // Gắn người dùng
                        nguoiDungVaiTro.setVaiTro(defaultRole); // Gắn vai trò
                        nguoiDungVaiTro.setNgayBatDau(LocalDateTime.now()); // Gắn ngày bắt đầu

                        // Lưu đối tượng NguoiDungVaiTro vào cơ sở dữ liệu
                        nguoiDungVaiTroRepository.save(nguoiDungVaiTro);

                        // Cập nhật lại danh sách vai trò của người dùng
                        user.getVaiTros().add(nguoiDungVaiTro);
                        nguoiDungRepository.save(user);
                    }
                },
                () -> {
                    throw new UsernameNotFoundException("User not found");
                });
    }

    public void update(NguoiDungDTO nguoiDungDTO) {
        NguoiDung nguoiDung = nguoiDungRepository.findById(nguoiDungDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("Người dùng không tồn tại"));

        nguoiDung.setEmail(nguoiDungDTO.getEmail());
        nguoiDung.setHoTen(nguoiDungDTO.getHoTen());
        nguoiDung.setSoDienThoai(nguoiDungDTO.getSoDienThoai());
        nguoiDung.setChucVu(nguoiDungDTO.getChucVu());
        nguoiDung.setTrangThaiHoatDong(nguoiDungDTO.getTrangThaiHoatDong());

        if (nguoiDungDTO.getMatKhauHash() != null && !nguoiDungDTO.getMatKhauHash().isBlank()) {
            nguoiDung.setMatKhauHash(passwordEncoder.encode(nguoiDungDTO.getMatKhauHash()));
        }

        nguoiDungRepository.save(nguoiDung);
    }

    public List<NguoiDungDTO> findAll() {
        return nguoiDungRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<NguoiDungDTO> findById(Long id) {
        return nguoiDungRepository.findById(id).map(this::convertToDTO);
    }

    public void deleteById(Long id) {
        nguoiDungRepository.deleteById(id);
    }

    public NguoiDung getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String username = authentication.getName();
        return nguoiDungRepository.findByTenDangNhap(username).orElse(null);
    }

    private NguoiDungDTO convertToDTO(NguoiDung nguoiDung) {
        return new NguoiDungDTO(
                nguoiDung.getId(),
                nguoiDung.getTenDangNhap(),
                null, // Không trả về mật khẩu đã mã hóa
                nguoiDung.getEmail(),
                nguoiDung.getHoTen(),
                nguoiDung.getSoDienThoai(),
                nguoiDung.getChucVu(),
                nguoiDung.getTrangThaiHoatDong());
    }

    /**
     * Kiểm tra xem tên người dùng đã tồn tại trong hệ thống hay chưa.
     *
     * @param username Tên đăng nhập cần kiểm tra
     * @return true nếu đã tồn tại, false nếu chưa
     */
    public boolean existsByUsername(String username) {
        return nguoiDungRepository.existsByTenDangNhap(username);
    }

    /**
     * Đăng ký người dùng mới.
     *
     * @param request Đối tượng chứa thông tin đăng ký
     */
    public void registerNewUser(RegisterRequest request) {
        // Thực hiện việc lưu người dùng mới vào database
        // Mã hóa mật khẩu trước khi lưu
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        NguoiDung newUser = new NguoiDung();
        newUser.setTenDangNhap(request.getUsername());
        newUser.setMatKhauHash(encodedPassword);
        newUser.setEmail(request.getEmail());
        newUser.setNgayTao(LocalDateTime.now());
        newUser.setTrangThaiHoatDong(true);
        // TODO: Thêm các thông tin khác của người dùng

        nguoiDungRepository.save(newUser);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#username")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            var nguoiDung = nguoiDungRepository.findByTenDangNhap(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Người dùng không tồn tại"));
            return org.springframework.security.core.userdetails.User
                    .withUsername(nguoiDung.getTenDangNhap())
                    .password(nguoiDung.getMatKhauHash())
                    .authorities(nguoiDung.getVaiTros().stream()
                            .map(vt -> vt.getVaiTro().getTenVaiTro())
                            .toArray(String[]::new))
                    .accountLocked(false)
                    .disabled(false)
                    .build();
        } catch (Exception e) {
            log.error("Error loading user by username: {}", username, e);
            throw e;
        }
    }
}