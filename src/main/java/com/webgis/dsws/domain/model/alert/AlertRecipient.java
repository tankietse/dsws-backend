package com.webgis.dsws.domain.model.alert;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "alert_recipient")
@Getter
@Setter
public class AlertRecipient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private Boolean active = true;
    @ManyToMany
    @JoinTable(
            name = "alert_configuration_recipient_emails",
            joinColumns = @JoinColumn(name = "recipient"),
            inverseJoinColumns = @JoinColumn(name = "config_id"))
    private Set<AlertConfiguration> configurations;

    public boolean isActive() {
        return active;
    }
}
