export default function loadHeatmap() {
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
        const heatData = data.map((d) => [
          d.latitude,
          d.longitude,
          d.intensity * 100, // Scale up intensity for better visualization
        ]);
        this.currentLayer = L.heatLayer(heatData, {
          radius: 25,
          blur: 15,
          maxZoom: 15,
          pane: "points",
          gradient: {
            0.2: "#4dff4d",
            0.4: "#ffff4d",
            0.6: "#ffa64d",
            0.8: "#ff4d4d",
          },
        });
        this.currentLayer.addTo(this.map);
        resolve();
      })
      .catch((error) => {
        console.error("Error loading heatmap:", error);
        if (error.status === 401) {
          window.location.href = "/auth/login";
        }
        reject(error);
      });
  });
}