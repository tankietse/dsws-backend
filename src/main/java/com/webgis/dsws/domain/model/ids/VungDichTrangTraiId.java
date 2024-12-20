package com.webgis.dsws.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;

public class VungDichTrangTraiId implements Serializable {
    private Long vungDich;
    private Long trangTrai;

    public VungDichTrangTraiId() {
    }

    public VungDichTrangTraiId(Long vungDich, Long trangTrai) {
        this.vungDich = vungDich;
        this.trangTrai = trangTrai;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        VungDichTrangTraiId that = (VungDichTrangTraiId) o;
        return Objects.equals(vungDich, that.vungDich) &&
                Objects.equals(trangTrai, that.trangTrai);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vungDich, trangTrai);
    }
}