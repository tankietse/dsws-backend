package com.webgis.dsws.service;

import com.webgis.dsws.model.DonViHanhChinh;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddressService {

    public String generateFullAddress(String soNha, String tenDuong, String khuPho, DonViHanhChinh donViHanhChinh) {
        StringBuilder address = new StringBuilder();

        // Add house number and street
        if (soNha != null && !soNha.trim().isEmpty()) {
            address.append(soNha.trim());
            if (tenDuong != null && !tenDuong.trim().isEmpty()) {
                address.append(" ").append(tenDuong.trim());
            }
        }

        // Add quarter/neighborhood
        if (khuPho != null && !khuPho.trim().isEmpty()) {
            if (!address.isEmpty())
                address.append(", ");
            address.append("KP. ").append(khuPho.trim());
        }

        // Add administrative unit info
        if (donViHanhChinh != null) {
            if (!address.isEmpty())
                address.append(", ");
            address.append(donViHanhChinh.getTen());

            // Add parent administrative units if available
            DonViHanhChinh parent = donViHanhChinh.getDonViCha();
            while (parent != null) {
                address.append(", ").append(parent.getTen());
                parent = parent.getDonViCha();
            }
        }

        return address.toString();
    }

    public String updateAddressWithPoint(String existingAddress, Point point, DonViHanhChinh donViHanhChinh) {
        // Here you can implement reverse geocoding using the point
        // For now we'll just append coordinates if no address exists
        if (existingAddress == null || existingAddress.trim().isEmpty()) {
            StringBuilder address = new StringBuilder();

            if (point != null) {
                address.append(String.format("(%.6f, %.6f)", point.getY(), point.getX()));
            }

            if (donViHanhChinh != null) {
                if (!address.isEmpty())
                    address.append(", ");
                address.append(donViHanhChinh.getTen());
            }

            return address.toString();
        }

        return existingAddress;
    }
}