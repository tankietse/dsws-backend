export default function loadMarkers() {
  return new Promise((resolve, reject) => {
    this.clearCurrentLayer();
    fetch("/api/v1/vung-dich/heatmap", {
      credentials: "include",
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error("Network response was not ok");
        }
        return response.json();
      })
      .then((data) => {
        const markers = L.markerClusterGroup({
          iconCreateFunction: (cluster) => {
            return this.createCustomClusterIcon(cluster);
          },
          showCoverageOnHover: false,
          spiderfyOnMaxZoom: true,
          maxClusterRadius: 40,
          zoomToBoundsOnClick: true,

          // Add custom hover functionality for clusters
          chunkedLoading: true,
          singleMarkerMode: false,

          // Custom popup settings
          spiderfyDistanceMultiplier: 2,

          // Configure popup behavior for clusters
          spiderfyShapePositions: function (count, centerPt) {
            var distanceFromCenter = 35,
              markerDistance = 45,
              lineLength = markerDistance * (count - 1),
              lineStart = centerPt.y - lineLength / 2;

            return [...Array(count)].map((_, index) => {
              return L.point(
                centerPt.x + distanceFromCenter,
                lineStart + markerDistance * index
              );
            });
          },
        });

        data.forEach((point) => {
          const marker = L.marker([point.latitude, point.longitude], {
            // ...existing marker options...
          });

          // Create detailed popup content
          const formattedDate = new Date(point.ngayBatDau).toLocaleDateString(
            "vi-VN",
            {
              day: "2-digit",
              month: "2-digit",
              year: "numeric",
              hour: "2-digit",
              minute: "2-digit",
            }
          );

          const status = point.ngayKetThuc ? "Đã kết thúc" : "Đang hoạt động";
          const statusClass = point.ngayKetThuc ? "inactive" : "active";

          const popupContent = `
              <div class="disease-zone-popup">
                <div class="disease-zone-header" style="background-color: ${
                  point.color
                }">
                  <h3>${point.tenVung}</h3>
                  <span class="status-badge ${statusClass}">${status}</span>
                </div>
                <div class="disease-zone-content">
                  <div class="info-row">
                    <span class="label">Loại bệnh:</span>
                    <span class="value">${point.tenBenh}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">Mức độ:</span>
                    <span class="value severity-badge">${point.mucDo.replace(
                      "CAP_DO_",
                      "Cấp độ "
                    )}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">Bắt đầu:</span>
                    <span class="value">${formattedDate}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">Bán kính:</span>
                    <span class="value">${(point.banKinh * 1000).toFixed(
                      0
                    )}m</span>
                  </div>
                  <div class="info-row description">
                    <p>${point.moTa}</p>
                  </div>
                </div>
              </div>
            `;

          marker.bindPopup(popupContent, {
            maxWidth: 300,
            className: "disease-zone-popup-container",
          });
          markers.addLayer(marker);
        });

        this.currentLayer = markers;
        this.currentLayer.addTo(this.map);
        resolve();
      })
      .catch((error) => {
        console.error("Error loading markers:", error);
        if (error.status === 401) {
          window.location.href = "/auth/login";
        }
        reject(error);
      });
  });
}
