package com.webgis.dsws.controller;

import com.webgis.dsws.model.CaBenh;
import com.webgis.dsws.model.NguoiDung;
import com.webgis.dsws.model.enums.TrangThaiEnum;
import com.webgis.dsws.service.CaBenhService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/ca-benh")
public class CaBenhController {
    @Autowired
    private CaBenhService caBenhService;

    // Trang để người dùng thay đổi thông tin ca bệnh
    @GetMapping("/change/{id}")
    public String hienThiFormThayDoi(@PathVariable Long id, Model model) {
        CaBenh caBenh = caBenhService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy ca bệnh"));
        model.addAttribute("caBenh", caBenh);
        return "ca-benh/thay-doi";
    }

    // Xử lý thay đổi ca bệnh
    @PostMapping("/change")
    public String thayDoiCaBenh(@ModelAttribute CaBenh caBenh,
                                @AuthenticationPrincipal NguoiDung nguoiDung) {
        caBenhService.thayDoiCaBenh(caBenh, nguoiDung);
        return "redirect:/ca-benh/danh-sach";
    }

    // Trang quản lý duyệt ca bệnh cho quản lý
    @GetMapping("/list")
    public String danhSachCaBenhChoDuyet(Model model) {
        List<CaBenh> danhSachCaBenh = caBenhService.findByTrangThai(TrangThaiEnum.PENDING);
        model.addAttribute("danhSachCaBenh", danhSachCaBenh);
        return "ca-benh/list";
    }

    // Xử lý duyệt ca bệnh
    @PostMapping("/quan-ly/duyet")
    public String duyetCaBenh(@RequestParam Long caBenhId,
                              @RequestParam boolean approved,
                              @AuthenticationPrincipal NguoiDung nguoiQuanLy) {
        caBenhService.duyetCaBenh(caBenhId, nguoiQuanLy, approved);
        return "redirect:/ca-benh/list";
    }
}