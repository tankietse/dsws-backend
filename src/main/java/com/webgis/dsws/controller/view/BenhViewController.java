package com.webgis.dsws.controller.view;

import com.webgis.dsws.domain.dto.BenhDTO;
import com.webgis.dsws.domain.service.impl.BenhServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.webgis.dsws.domain.model.Benh;
import com.webgis.dsws.domain.repository.BenhRepository;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

//@Controller
//@RequestMapping("/benh")
//public class BenhViewController {
//
//    @Autowired
//    private BenhRepository benhRepository;
//
//    // Hiển thị danh sách bệnh
////    @GetMapping
////    public String listBenh(Model model) {
////        List<Benh> dsBenh = benhRepository.findAll();
////        model.addAttribute("dsBenh", dsBenh);
////        return "benh/list";
////    }
//    @GetMapping
//    public String listBenh(@RequestParam(defaultValue = "0") int page,
//                           @RequestParam(defaultValue = "10") int size,
//                           Model model) {
//        Pageable pageable = PageRequest.of(page, size);
//        Page<Benh> benhPage = benhRepository.findAll(pageable);
//
//        model.addAttribute("dsBenh", benhPage.getContent());
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", benhPage.getTotalPages());
//        return "benh/list";
//    }
//
//    // Cập nhật đường dẫn cho form tạo bệnh
//    @GetMapping("/create")
//    public String createBenhForm(Model model) {
//        model.addAttribute("benh", new Benh());
//        return "benh/create";
//    }
//
//    // Xử lý thêm bệnh
//    @PostMapping("/create")
//    public String createBenh(@Valid @ModelAttribute("benh") Benh benh, BindingResult result) {
//        if (result.hasErrors()) {
//            return "benh/create";
//        }
//        benhRepository.save(benh);
//        return "redirect:/benh";
//    }
//
//    // Hiển thị form chỉnh sửa
//    @GetMapping("/edit/{id}")
//    public String editBenhForm(@PathVariable Long id, Model model) {
//        Benh benh = benhRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid Benh ID: " + id));
//        model.addAttribute("benh", benh);
//        return "benh/edit";
//    }
//
//    // Xử lý chỉnh sửa
//    @PostMapping("/edit/{id}")
//    public String editBenh(@PathVariable Long id, @Valid @ModelAttribute("benh") Benh benh, BindingResult result) {
//        if (result.hasErrors()) {
//            return "benh/edit";
//        }
//        benhRepository.save(benh);
//        return "redirect:/benh";
//    }
//
//    // Xóa bệnh
//    @GetMapping("/delete/{id}")
//    public String deleteBenh(@PathVariable Long id) {
//        Benh benh = benhRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid Benh ID: " + id));
//        benhRepository.delete(benh);
//        return "redirect:/benh";
//    }
//}
@Controller
@RequestMapping("/benh")
public class BenhViewController {

    private final BenhServiceImpl benhService;

    // Constructor injection (nên dùng thay vì @Autowired)
    public BenhViewController(BenhServiceImpl benhService) {
        this.benhService = benhService;
    }

//    @GetMapping
//    public String listBenh(@RequestParam(defaultValue = "0") int page,
//                           @RequestParam(defaultValue = "10") int size,
//                           Model model) {
//        Pageable pageable = PageRequest.of(page, size);
//        Page<Benh> benhPage = benhService.findAll(pageable);
//
//        model.addAttribute("dsBenh", benhPage.getContent());
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", benhPage.getTotalPages());
//        return "benh/list";
//    }

    @GetMapping("/create")
    public String createBenhForm(Model model) {
        model.addAttribute("benh", new Benh());
        return "benh/create";
    }

    @PostMapping("/create")
    public String createBenh(@Valid @ModelAttribute("benh") Benh benh,
                             BindingResult result) {
        if (result.hasErrors()) {
            return "benh/create";
        }
        benhService.save(benh);
        return "redirect:/benh";
    }

    @GetMapping("/edit/{id}")
    public String editBenhForm(@PathVariable Long id, Model model) {
        Benh benh = benhService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Benh ID: " + id));
        System.out.print(benh.toString());
        model.addAttribute("benh", benh);
        return "benh/edit";
    }

//    @GetMapping("/edit/{id}")
//    public String editBenh(@PathVariable Long id, Model model) {
//        Optional<BenhDTO> benhDTO = benhService.findDTOById(id);
//        if (benhDTO.isPresent()) {
//            model.addAttribute("benhDTO", benhDTO.get()); // Thêm `.get()` để truyền đúng object
//            return "benh/edit"; // View chỉnh sửa bệnh
//        }
//        return "redirect:/benh/all"; // Điều hướng lại nếu không tìm thấy
//    }

    @PostMapping("/edit/{id}")
    public String editBenh(@PathVariable Long id, @Valid @ModelAttribute("benh") Benh benh, BindingResult result) {
        System.out.print(benh.toString());
        if (result.hasErrors()) {
            return "benh/edit";
        }
        benhService.update(id, benh);
        return "redirect:/benh";
    }

//    @PostMapping("/edit/{id}")
//    public String updateBenh(@PathVariable("id") Long id,
//                             @ModelAttribute("benh") @Valid BenhDTO benhDTO,
//                             BindingResult result,
//                             RedirectAttributes redirectAttributes) {
//        if (result.hasErrors()) {
//            // Nếu có lỗi validation, redirect lại trang sửa bệnh và giữ lại thông tin lỗi và dữ liệu
//            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.benhDTO", result);
//            redirectAttributes.addFlashAttribute("benhDTO", benhDTO);
//            return "redirect:/benh/edit/" + id; // Quay lại trang sửa bệnh
//        }
//        // Tìm bệnh theo ID
//        Benh existingBenh = benhService.findById(id).orElseThrow(() ->
//                new EntityNotFoundException("Không tìm thấy bệnh với ID: " + id));
//
//        // Cập nhật thông tin bệnh từ BenhDTO
//        benhService.updateDTO(id, benhDTO);
//        // Lưu bệnh đã cập nhật
//        benhService.update(id, existingBenh);
//
//        // Sau khi cập nhật xong, chuyển hướng về trang danh sách bệnh
//        return "redirect:/benh"; // Quay lại trang danh sách bệnh
//    }


    @GetMapping("/delete/{id}")
    public String deleteBenh(@PathVariable Long id) {
        benhService.deleteById(id);
        return "redirect:/benh";
    }

    @GetMapping
    public String listBenh(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Benh> benhPage;

        if (keyword != null && !keyword.isEmpty()) {
            benhPage = benhService.searchBenh(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            benhPage = benhService.findAll(pageable);
        }

        model.addAttribute("dsBenh", benhPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", benhPage.getTotalPages());
        return "benh/list";
    }
}
