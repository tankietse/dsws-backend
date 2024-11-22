package com.webgis.dsws.model;

import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VungDichTrangTraiId implements Serializable {
    private Long vungDich;
    private Long trangTrai;
}