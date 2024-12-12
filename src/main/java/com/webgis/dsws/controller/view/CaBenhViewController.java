package com.webgis.dsws.controller.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.webgis.dsws.domain.model.CaBenh;
import com.webgis.dsws.domain.model.NguoiDung;
import com.webgis.dsws.domain.model.enums.TrangThaiEnum;
import com.webgis.dsws.domain.service.CaBenhService;

import java.util.List;

@Controller
@RequestMapping("/ca-benh")
public class CaBenhViewController {
    @Autowired
    private CaBenhService caBenhService;

    // Trang để người dùng thay đổi thông tin ca bệnh
    @GetMapping("/hienThiFormThayDoi/{id}")
    public String hienThiFormThayDoi(@PathVariable Long id, Model model) {
        CaBenh caBenh = caBenhService.findById(id);
        model.addAttribute("caBenh", caBenh);
        return "ca-benh/change";
    }

    // Xử lý thay đổi ca bệnh
    @PostMapping("/thayDoiCaBenh")
    public String thayDoiCaBenh(@ModelAttribute CaBenh caBenh,
            @AuthenticationPrincipal NguoiDung nguoiDung) {
        caBenhService.thayDoiCaBenh(caBenh, nguoiDung);
        return "redirect:/ca-benh/list";
    }

    // Cập nhật đường dẫn nếu cần thiết
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

    // @ModelAttribute("pendingCases")
    // public Long getPendingCasesCount() {
    // return caBenhService.countByTrangThai(TrangThaiEnum.PENDING);
    // }

    // // Add create case mapping
    // @GetMapping("/create")
    // public String showCreateForm(Model model) {
    // model.addAttribute("caBenh", new CaBenh());
    // return "ca-benh/create";
    // }

    // @PostMapping("/create")
    // public String createCaBenh(@ModelAttribute CaBenh caBenh,
    // @AuthenticationPrincipal NguoiDung nguoiDung) {
    // caBenh.setTrangThai(TrangThaiEnum.PENDING);
    // caBenhService.create(caBenh, nguoiDung);
    // return "redirect:/ca-benh/list";
    // }
}