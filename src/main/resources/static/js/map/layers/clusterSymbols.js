export default function loadClusterSymbols() {
  return new Promise((resolve, reject) => {
    const mapLoading = document.getElementById("map-loading");
    if (mapLoading) {
      mapLoading.style.display = "flex";
    }

    this.clearCurrentLayer();

    fetch("/api/v1/vung-dich/markers", {
      credentials: "include",
      headers: {
        Accept: "application/json",
      },
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })
      .then((data) => {
        const markers = L.markerClusterGroup();
        data.forEach((d) => {
          // Validate coordinates
          if (
            typeof d.latitude !== "number" ||
            typeof d.longitude !== "number"
          ) {
            console.warn(
              `Invalid coordinates for cluster point: ${JSON.stringify(d)}`
            );
            return; // Skip this point
          }

          const marker = L.marker([d.latitude, d.longitude]);
          markers.addLayer(marker);
        });
        this.currentLayer = markers;
        this.currentLayer.addTo(this.map);
        resolve();
      })
      .catch((error) => {
        console.error("Error loading clusters:", error);
        if (mapLoading) {
          mapLoading.style.display = "none";
        }
        if (error.status === 401) {
          window.location.href = "/auth/login";
        }
        reject(error);
      })
      .finally(() => {
        if (mapLoading) {
          mapLoading.style.display = "none";
        }
      });
  });
}
