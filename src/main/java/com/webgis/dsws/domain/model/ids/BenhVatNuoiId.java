package com.webgis.dsws.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BenhVatNuoiId implements Serializable {

    @Column(name = "benh_id")
    private Long benhId;

    @Column(name = "loai_vatnuoi_id")
    private Long loaiVatNuoiId;

}