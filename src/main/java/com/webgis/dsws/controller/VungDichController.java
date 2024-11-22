package com.webgis.dsws.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.locationtech.jts.geom.Coordinate;
import com.webgis.dsws.service.VungDichService;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/v1/vung-dich")  // Update to a more specific path
public class VungDichController {

    @Autowired
    private VungDichService vungDichService;

    @Value("${springdoc.swagger-ui.path:}")
    private String baseUrl;

    /**
     * API kiểm tra tọa độ có nằm trong vùng dịch hay không.
     * 
     * @param id ID của vùng dịch.
     * @param x  Tọa độ x.
     * @param y  Tọa độ y.
     * @return true nếu nằm trong vùng dịch, ngược lại false.
     */
    @GetMapping("/{id}/contains")
    public boolean contains(@PathVariable Long id, @RequestParam double x, @RequestParam double y) {
        Coordinate coordinate = new Coordinate(x, y);
        return vungDichService.contains(id, coordinate);
    }

    /**
     * API lấy cảnh báo mức độ vùng dịch.
     * 
     * @param id ID của vùng dịch.
     * @return Thông báo cảnh báo.
     */
    @GetMapping("/{id}/canh-bao")
    public String canhBaoMucDo(@PathVariable Long id) {
        return vungDichService.canhBaoMucDo(id);
    }
}