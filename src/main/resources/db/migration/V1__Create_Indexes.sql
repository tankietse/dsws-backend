CREATE EXTENSION IF NOT EXISTS unaccent;

-- Regular indexes
CREATE INDEX IF NOT EXISTS idx_dvhc_ten ON don_vi_hanh_chinh(ten);
CREATE INDEX IF NOT EXISTS idx_dvhc_cap_hanh_chinh ON don_vi_hanh_chinh(cap_hanh_chinh);
CREATE INDEX IF NOT EXISTS idx_dvhc_admin_level ON don_vi_hanh_chinh(admin_level);

-- Spatial indexes
CREATE INDEX IF NOT EXISTS idx_dvhc_ranh_gioi_gist ON don_vi_hanh_chinh USING GIST (ranh_gioi);
CREATE INDEX IF NOT EXISTS idx_dvhc_diem_trung_tam_gist ON don_vi_hanh_chinh USING GIST (diem_trung_tam);