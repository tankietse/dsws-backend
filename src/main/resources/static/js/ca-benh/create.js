document.addEventListener("DOMContentLoaded", function () {
  setupFarmSearch();
  setupBenhSelect();
  setupForm();
});

// Farm search with debounce
function setupFarmSearch() {
  const searchInput = document.getElementById("farmSearch");
  const resultsContainer = document.getElementById("farmSearchResults");
  let selectedFarm = null;
  let timeoutId;

  searchInput.addEventListener("input", (e) => {
    clearTimeout(timeoutId);
    const query = e.target.value.trim();

    if (query.length < 2) {
      resultsContainer.classList.add("hidden");
      return;
    }

    timeoutId = setTimeout(async () => {
      try {
        const response = await fetch(
          `/api/v1/trang-trai/search?q=${encodeURIComponent(query)}`
        );
        const farms = await response.json();

        if (farms.length > 0) {
          resultsContainer.innerHTML = farms
            .map(
              (farm) => `
            <div class="farm-result p-3 hover:bg-gray-100 cursor-pointer border-b last:border-0" 
                 data-farm-id="${farm.id}">
              <div class="font-medium">${farm.tenChu}</div>
              <div class="text-sm text-gray-600">Mã: ${
                farm.maTrangTrai || "N/A"
              }</div>
              <div class="text-sm text-gray-600 truncate">${
                farm.diaChiDayDu || "Chưa có địa chỉ"
              }</div>
            </div>
          `
            )
            .join("");

          resultsContainer.classList.remove("hidden");

          // Add click handlers
          resultsContainer.querySelectorAll(".farm-result").forEach((el) => {
            el.addEventListener("click", () =>
              selectFarm(
                farms.find((f) => f.id === parseInt(el.dataset.farmId))
              )
            );
          });
        } else {
          resultsContainer.innerHTML =
            '<div class="p-3 text-gray-500">Không tìm thấy trang trại</div>';
          resultsContainer.classList.remove("hidden");
        }
      } catch (error) {
        console.error("Farm search error:", error);
        showError("Lỗi tìm kiếm trang trại");
      }
    }, 300);
  });

  // Close results when clicking outside
  document.addEventListener("click", (e) => {
    if (
      !searchInput.contains(e.target) &&
      !resultsContainer.contains(e.target)
    ) {
      resultsContainer.classList.add("hidden");
    }
  });
}

function selectFarm(farm) {
  const selectedFarmInfo = document.getElementById("selectedFarmInfo");

  // Add animal type selection dropdown
  const animalTypeOptions =
    farm.danhSachVatNuoi
      ?.map(
        (vn) => `
    <option value="${vn.loaiVatNuoi.id}" data-count="${vn.soLuong}">
      ${vn.loaiVatNuoi.tenLoai} (${vn.soLuong} con)
    </option>
  `
      )
      .join("") || "";

  selectedFarmInfo.innerHTML = `
    <div class="grid grid-cols-2 gap-6">
      <div class="space-y-4">
        <div class="info-group">
          <label class="text-sm font-medium text-gray-600">Chủ trang trại</label>
          <div class="p-3 bg-white rounded-lg border font-medium">
            ${farm.tenChu}
          </div>
        </div>
        <div class="info-group">
          <label class="text-sm font-medium text-gray-600">Tổng đàn</label>
          <div class="p-3 bg-white rounded-lg border font-medium">
            ${farm.tongDan || 0}
          </div>
        </div>
      </div>
      <div class="space-y-4">
        <div class="info-group">
          <label class="text-sm font-medium text-gray-600">Loại vật nuôi</label>
          <div class="p-3 bg-white rounded-lg border font-medium">
            ${
              farm.danhSachVatNuoi
                ?.map((vn) => `${vn.loaiVatNuoi.tenLoai} (${vn.soLuong})`)
                .join(", ") || "Không có thông tin"
            }
          </div>
        </div>
        <div class="info-group">
          <label class="text-sm font-medium text-gray-600">Địa chỉ</label>
          <div class="p-3 bg-white rounded-lg border font-medium">
            ${farm.diaChiDayDu || "Chưa có địa chỉ"}
          </div>
        </div>
      </div>
      <div class="col-span-2 mt-4">
        <div class="form-group">
          <label class="block text-sm font-medium text-gray-700">Chọn loại vật nuôi bị bệnh</label>
          <select id="animalTypeSelect" class="mt-1 block w-full rounded-lg border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500">
            <option value="">-- Chọn loại vật nuôi --</option>
            ${animalTypeOptions}
          </select>
        </div>
      </div>
    </div>
  `;

  selectedFarmInfo.classList.remove("hidden");
  document.getElementById("farmSearchResults").classList.add("hidden");
  document.getElementById("selectedFarmId").value = farm.id;

  // Add event listener for animal type selection
  const animalTypeSelect = document.getElementById("animalTypeSelect");
  animalTypeSelect.addEventListener("change", (e) => {
    const selectedAnimalTypeId = e.target.value;
    if (selectedAnimalTypeId) {
      loadCompatibleDiseases(selectedAnimalTypeId);

      // Store selected animal count for validation
      const selectedCount = parseInt(e.target.selectedOptions[0].dataset.count);
      document.getElementById("soCaNhiemBanDau").max = selectedCount;
      document.getElementById("soCaTuVongBanDau").max = selectedCount;
    } else {
      const select = document.getElementById("benhSelect");
      select.innerHTML =
        '<option value="">-- Chọn loại vật nuôi trước --</option>';
    }
  });
}

async function setupBenhSelect() {
  try {
    const response = await fetch("/api/v1/benh");
    const diseases = await response.json();

    const select = document.getElementById("benhSelect");
    select.innerHTML = '<option value="">-- Chọn bệnh --</option>';

    diseases.forEach((disease) => {
      const option = document.createElement("option");
      option.value = disease.id;
      option.textContent = disease.tenBenh;
      select.appendChild(option);
    });

    // Add change handler
    select.addEventListener("change", (e) => {
      const selectedDisease = diseases.find(
        (d) => d.id === parseInt(e.target.value)
      );
      if (selectedDisease) {
        showDiseaseInfo(selectedDisease);
      }
    });
  } catch (error) {
    console.error("Error loading diseases:", error);
    showNotification("Không thể tải danh sách bệnh", "error");
  }
}

function loadCompatibleDiseases(animalTypeId) {
  if (!animalTypeId) {
    console.log("No animal type ID provided");
    return;
  }

  console.log("Loading diseases for animal type:", animalTypeId);

  fetch(`/api/v1/benh/by-loai-vat-nuoi/${animalTypeId}`)
    .then((res) => {
      if (!res.ok) {
        throw new Error(`HTTP error! status: ${res.status}`);
      }
      return res.json();
    })
    .then((diseases) => {
      console.log("Loaded diseases:", diseases);

      const select = document.getElementById("benhSelect");
      select.innerHTML = '<option value="">-- Chọn bệnh --</option>';

      if (diseases && diseases.length > 0) {
        diseases.forEach((disease) => {
          const option = document.createElement("option");
          option.value = disease.id;
          option.textContent = disease.tenBenh;
          select.appendChild(option);
        });

        select.disabled = false;
      } else {
        select.innerHTML =
          '<option value="" disabled>Không có bệnh phù hợp cho loại vật nuôi này</option>';
        select.disabled = true;
      }

      const benhInfo = document.getElementById("benhInfo");
      benhInfo.classList.add("hidden");
    })
    .catch((err) => {
      console.error("Error loading compatible diseases:", err);
      const select = document.getElementById("benhSelect");
      select.innerHTML =
        '<option value="">-- Lỗi tải danh sách bệnh --</option>';
      select.disabled = true;
      showNotification("Không thể tải danh sách bệnh tương thích", "error");
    });
}

function showDiseaseInfo(disease) {
  const infoDiv = document.getElementById("benhInfo");
  infoDiv.innerHTML = `
    <div class="p-4 bg-blue-50 rounded-lg border border-blue-200">
      <h4 class="font-medium text-blue-800">${disease.tenBenh}</h4>
      <div class="mt-2 text-sm text-blue-700">
        ${disease.canCongBoDich ? "<p>• Bệnh cần công bố dịch</p>" : ""}
        ${disease.canPhongBenhBatBuoc ? "<p>• Bệnh cần phòng bắt buộc</p>" : ""}
      </div>
      <div class="mt-2 flex gap-2">
        ${
          disease.mucDoBenhs
            ?.map(
              (mucDo) => `
          <span class="px-2 py-1 text-xs font-medium rounded-full ${getMucDoStyle(
            mucDo
          )}">
            ${getMucDoLabel(mucDo)}
          </span>
        `
            )
            .join("") || ""
        }
      </div>
    </div>
  `;
  infoDiv.classList.remove("hidden");
}

function getMucDoStyle(mucDo) {
  const styles = {
    NANG: "bg-red-100 text-red-800",
    TRUNG_BINH: "bg-yellow-100 text-yellow-800",
    NHE: "bg-green-100 text-green-800",
  };
  return styles[mucDo] || "bg-gray-100 text-gray-800";
}

function getMucDoLabel(mucDo) {
  const labels = {
    NANG: "Nặng",
    TRUNG_BINH: "Trung bình",
    NHE: "Nhẹ",
  };
  return labels[mucDo] || mucDo;
}

async function setupForm() {
  const form = document.getElementById("createCaBenhForm");
  const cancelButton = document.getElementById("cancelButton");

  // Set default date value to now
  const now = new Date();
  now.setMinutes(now.getMinutes() - now.getTimezoneOffset()); // Fix timezone offset
  document.getElementById("ngayPhatHien").value = now
    .toISOString()
    .slice(0, 16);

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const formData = {
      trangTraiId: parseInt(document.getElementById("selectedFarmId").value),
      loaiVatNuoiId: parseInt(
        document.getElementById("animalTypeSelect").value
      ), // Add animal type ID
      benhId: parseInt(document.getElementById("benhSelect").value),
      ngayPhatHien:
        document.getElementById("ngayPhatHien").value.replace("T", " ") + ":00",
      soCaNhiemBanDau:
        parseInt(document.getElementById("soCaNhiemBanDau").value) || 0,
      soCaTuVongBanDau:
        parseInt(document.getElementById("soCaTuVongBanDau").value) || 0,
      moTaBanDau: document.getElementById("moTaBanDau").value.trim(),
      nguyenNhanDuDoan: document
        .getElementById("nguyenNhanDuDoan")
        .value.trim(),
    };

    // Validate required fields
    if (!formData.trangTraiId) {
      showNotification("Vui lòng chọn trang trại", "error");
      return;
    }
    if (!formData.loaiVatNuoiId) {
      showNotification("Vui lòng chọn loại vật nuôi", "error");
      return;
    }
    if (!formData.benhId) {
      showNotification("Vui lòng chọn bệnh", "error");
      return;
    }

    try {
      const response = await fetch("/api/v1/ca-benh", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(formData),
      });

      if (!response.ok) {
        throw new Error("Lỗi khi tạo ca bệnh");
      }

      showNotification("Tạo ca bệnh thành công", "success");
      window.location.href = "/ca-benh/list";
    } catch (error) {
      console.error("Error:", error);
      showNotification(error.message, "error");
    }
  });

  cancelButton.addEventListener("click", () => {
    window.location.href = "/ca-benh/list";
  });
}

function showNotification(message, type) {
  alert(message); // Replace with your notification system
}
