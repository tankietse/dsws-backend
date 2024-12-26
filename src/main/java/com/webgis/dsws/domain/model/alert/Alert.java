package com.webgis.dsws.domain.model.alert;

import com.webgis.dsws.domain.model.CaBenh;
import com.webgis.dsws.domain.model.VungDich;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "alert")
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "configuration_id")
    private AlertConfiguration configuration;

    @ManyToOne
    @JoinColumn(name = "ca_benh_id")
    private CaBenh caBenh;

    @ManyToOne
    @JoinColumn(name = "vung_dich_id")
    private VungDich vungDich;

    private Date createdAt;
    private String message;
    private Boolean emailSent;
    private Date emailSentAt;
}
