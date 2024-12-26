package com.webgis.dsws.domain.service;

import com.webgis.dsws.domain.model.CaBenh;
import com.webgis.dsws.domain.model.VungDich;
import com.webgis.dsws.domain.model.alert.Alert;
import com.webgis.dsws.domain.model.alert.AlertConfiguration;
import com.webgis.dsws.domain.model.alert.AlertRecipient;
import com.webgis.dsws.domain.repository.AlertConfigurationRepository;
import com.webgis.dsws.domain.repository.AlertRepository;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {
    private final JavaMailSender mailSender;
    private final AlertConfigurationRepository alertConfigRepository;
    private final AlertRepository alertRepository;
    private final CaBenhService caBenhService;
    private final VungDichService vungDichService;
    private final EntityManager entityManager;

    @Scheduled(fixedDelay = 60000) // Run every 5 minutes
    public void checkAndSendAlerts() {
//        List<AlertConfiguration> activeConfigs = alertConfigRepository.findByEnabled(true);
        List<AlertConfiguration> activeConfigs = alertConfigRepository.findByEnabledWithMucDoVungDichs(true);
//        Date now = new Date();
        System.out.println("Đã check");
        for (AlertConfiguration config : activeConfigs) {
            // Check for new disease cases matching criteria
            List<CaBenh> matchingCases = findMatchingCases(config);
            for (CaBenh caBenh : matchingCases) {
                // Pass null for vungDich when checking CaBenh alerts
                if (!hasExistingAlert(config, caBenh, null)) {
                    createAndSendAlert(config, caBenh, null);
                }
            }

            // Check for outbreak zones matching criteria
            List<VungDich> matchingZones = findMatchingZones(config);
            for (VungDich vungDich : matchingZones) {
                // Pass null for caBenh when checking VungDich alerts
                if (!hasExistingAlert(config, null, vungDich)) {
                    createAndSendAlert(config, null, vungDich);
                }
            }
        }
    }

//    private List<CaBenh> findMatchingCases(AlertConfiguration config) {
//        Date now = new Date(System.currentTimeMillis() - 86400000);
//        Timestamp afterDate = new Timestamp(now.getTime());
//        return caBenhService.findByCriteria(
//                config.getBenh(),
//                config.getMinSoCaNhiem(),
//                config.getMinSoCaTuVong(),
//                afterDate, //24 hours
//                Boolean.FALSE
//        );
//    }

    private List<CaBenh> findMatchingCases(AlertConfiguration config) {
        Date now = new Date(System.currentTimeMillis() - 86400000);
        Timestamp afterDate = new Timestamp(now.getTime());
        return caBenhService.findByCriteria(
                config.getBenh(),
                config.getMinSoCaNhiem(),
                config.getMinSoCaTuVong()
        );
    }

    private List<VungDich> findMatchingZones(AlertConfiguration config) {
        return vungDichService.findByCriteria(
                config.getBenh(),
                config.getMucDoVungDichs(),
                config.getRadiusKm()
        );
    }

    private Boolean hasExistingAlert(AlertConfiguration config, CaBenh caBenh, VungDich vungDich) {
        if (caBenh != null) {
            return alertRepository.existsByConfigurationAndCaBenh(config, caBenh);
        } else if (vungDich != null) {
            return alertRepository.existsByConfigurationAndVungDich(config, vungDich);
        }
        return false;
    }

    private void createAndSendAlert(AlertConfiguration config, CaBenh caBenh, VungDich vungDich) {
        Alert alert = new Alert();
        alert.setConfiguration(config);
        alert.setCaBenh(caBenh);
        alert.setVungDich(vungDich);
        alert.setCreatedAt(new Date());
        alert.setMessage(generateAlertMessage(config, caBenh, vungDich));

        try {
            sendAlertEmail(alert);
            alert.setEmailSent(true);
            alert.setEmailSentAt(new Date());
        } catch (Exception e) {
            alert.setEmailSent(false);
        }

        alertRepository.save(alert);
    }

    private String generateAlertMessage(AlertConfiguration config, CaBenh caBenh, VungDich vungDich) {
        StringBuilder message = new StringBuilder();
        message.append("CẢNH BÁO DỊCH BỆNH\n\n");
        System.out.println("CẢNH BÁO DỊCH BỆNH");
        if (caBenh != null) {
            message.append("Ca bệnh mới được phát hiện:\n");
            message.append("- Bệnh: ").append(caBenh.getBenh().getTenBenh()).append("\n");
            message.append("- Ngày phát hiện: ").append(caBenh.getNgayPhatHien()).append("\n");
            message.append("- Số ca nhiễm: ").append(caBenh.getSoCaNhiemBanDau()).append("\n");
            message.append("- Số ca tử vong: ").append(caBenh.getSoCaTuVongBanDau()).append("\n");
//            message.append("- Trang trại: ").append(caBenh.getTrangTrai().getTenTrangTrai()).append("\n");
        }

        if (vungDich != null) {
            message.append("Vùng dịch cảnh báo:\n");
            message.append("- Tên vùng: ").append(vungDich.getTenVung()).append("\n");
            message.append("- Mức độ: ").append(vungDich.getMucDo()).append("\n");
            message.append("- Ngày bắt đầu: ").append(vungDich.getNgayBatDau()).append("\n");
            message.append("- Bán kính ảnh hưởng: ").append(vungDich.getBanKinh()).append(" km\n");
        }

        return message.toString();
    }

    private void sendAlertEmail(Alert alert) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setSubject("Cảnh báo dịch bệnh");
        helper.setText(alert.getMessage(), true);

        for (AlertRecipient recipient : alert.getConfiguration().getRecipients()) {
            System.out.println("Recipient: " + recipient.getEmail() + ", active: " + recipient.isActive());
            if (recipient.isActive()) {
                try {
                    System.out.println("Sending email to: " + recipient.getEmail());
                    helper.setTo(recipient.getEmail());
                    mailSender.send(message);
                    System.out.println("Email sent successfully to: " + recipient.getEmail());
                } catch (Exception e) {
                    System.err.println("Failed to send email to: " + recipient.getEmail());
                    e.printStackTrace();
                }
            }
        }
    }
}
