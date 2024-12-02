package com.webgis.dsws.domain.model;

import java.io.Serializable;
import java.util.Objects;

public class VungDichTrangTraiId implements Serializable {
    private Long vungDich;
    private Long trangTrai;

    // Default constructor
    public VungDichTrangTraiId() {}

    // Getters and setters
    public Long getVungDich() {
        return vungDich;
    }

    public void setVungDich(Long vungDich) {
        this.vungDich = vungDich;
    }

    public Long getTrangTrai() {
        return trangTrai;
    }

    public void setTrangTrai(Long trangTrai) {
        this.trangTrai = trangTrai;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VungDichTrangTraiId that = (VungDichTrangTraiId) o;
        return Objects.equals(vungDich, that.vungDich) &&
               Objects.equals(trangTrai, that.trangTrai);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vungDich, trangTrai);
    }
}