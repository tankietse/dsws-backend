export default function loadOutbreakZones() {
  return new Promise((resolve, reject) => {
    fetch("/api/v1/vung-dich/map-data", {
      credentials: "include",
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })
      .then((data) => {
        if (!data || !Array.isArray(data)) {
          throw new Error("Invalid data format received");
        }

        const outbreakLayer = L.layerGroup();

        data.forEach((zone) => {
          // Add administrative boundary if exists
          if (zone.donViHanhChinh?.boundary) {
            const boundary = L.geoJSON(zone.donViHanhChinh.boundary, {
              style: {
                color: zone.colorCode,
                weight: 2,
                opacity: 0.6,
                fillColor: zone.colorCode,
                fillOpacity: 0.2,
              },
            });

            boundary.bindPopup(`
              <div class="popup-content">
                <h3>${zone.tenTrangTrai}</h3>
                <h4>${zone.donViHanhChinh.ten}</h4>
                <p>Vùng dịch: ${zone.tenVung}</p>
                <p>Mức độ: ${zone.mucDo}</p>
              </div>
            `);

            outbreakLayer.addLayer(boundary);
          }

          // Add center point marker
          const centerMarker = L.circleMarker(
            [zone.centerPoint.coordinates[1], zone.centerPoint.coordinates[0]],
            {
              radius: 8,
              color: zone.colorCode,
              fillColor: zone.colorCode,
              fillOpacity: 0.8,
              weight: 2,
            }
          ).bindPopup(`
            <div class="popup-content">
              <h4>Trung tâm vùng dịch</h4>
              <p>${zone.tenVung}</p>
              <p>Mức độ: ${zone.mucDo}</p>
            </div>
          `);

          outbreakLayer.addLayer(centerMarker);

          // Use default radius of 1000m if banKinh is 0 or invalid
          const radius = zone.banKinh <= 0 ? 1000 : zone.banKinh;

          // Create circle for outbreak zone
          const circle = L.circle(
            [zone.centerPoint.coordinates[1], zone.centerPoint.coordinates[0]],
            {
              radius: radius,
              color: zone.colorCode,
              fillColor: zone.colorCode,
              fillOpacity: 0.2,
              weight: 2,
            }
          );

          // Create markers for affected farms
          if (zone.trangTrais && Array.isArray(zone.trangTrais)) {
            zone.trangTrais.forEach((farm) => {
              // Only create marker if farm has valid location
              if (farm.location && Array.isArray(farm.location.coordinates)) {
                const farmMarker = L.circleMarker(
                  [farm.location.coordinates[1], farm.location.coordinates[0]],
                  {
                    radius: 5,
                    color: "#000",
                    fillColor: zone.colorCode,
                    fillOpacity: 0.6,
                    weight: 1,
                  }
                );

                farmMarker.bindPopup(`
                  <div class="popup-content">
                    <h4>${farm.tenTrangTrai || "Trang trại không tên"}</h4>
                    <p>Khoảng cách: ${farm.khoangCach}m</p>
                    <p>Thuộc vùng dịch: ${zone.tenVung}</p>
                  </div>
                `);

                outbreakLayer.addLayer(farmMarker);
              }
            });
          }

          // Add popup for zone info
          circle.bindPopup(`
            <div class="popup-content">
              <h3>${zone.tenVung}</h3>
              <p>Mức độ: ${zone.mucDo}</p>
              <p>Số trang trại: ${zone.trangTrais?.length || 0}</p>
              <p>Đơn vị hành chính: ${
                zone.donViHanhChinh?.ten || "Chưa cập nhật"
              }</p>
            </div>
          `);

          outbreakLayer.addLayer(circle);
        });

        resolve(outbreakLayer);
      })
      .catch((error) => {
        console.error("Error loading outbreak zones:", error);
        if (error.status === 401) {
          window.location.href = "/auth/login";
        }
        reject(error);
      });
  });
}
