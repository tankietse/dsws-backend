package com.webgis.dsws.domain.model.alert;

import com.webgis.dsws.domain.model.Benh;
import com.webgis.dsws.domain.model.enums.MucDoVungDichEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "alert_configuration")
public class AlertConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private Boolean enabled;

    @ManyToMany(mappedBy = "configurations")
    private Set<AlertRecipient> recipients;
//    @ElementCollection
//    private Set<String> recipientEmails;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<MucDoVungDichEnum> mucDoVungDichs;

    private Integer minSoCaNhiem;
    private Integer minSoCaTuVong;
    private Double radiusKm;

    @ManyToOne
    @JoinColumn(name = "benh_id")
    private Benh benh;

    private Boolean daKetThuc;
}
