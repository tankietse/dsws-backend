var currentPage = 1;
var pageSize = 10; // Số bản ghi trên mỗi trang mặc định

//  Đảm bảo pageSize có giá trị hợp lệ
function validatePageSize() {
  const entriesElement = document.getElementById("entries");
  if (entriesElement) {
    pageSize = Number(entriesElement.value);
    if (!pageSize || isNaN(pageSize) || pageSize <= 0) {
      pageSize = 10; // Default page size
    }
  }
}

//  Hàm cập nhật thanh điều hướng phân trang
function updatePaginationControls(data) {
  //  Kiểm tra xem tất cả các phần tử cần thiết có tồn tại không
  const startItemEl = document.getElementById("startItem");
  const endItemEl = document.getElementById("endItem");
  const totalItemsEl = document.getElementById("totalItems");
  const paginationContainer = document.getElementById("paginationContainer");

  if (!startItemEl || !endItemEl || !totalItemsEl || !paginationContainer) {
    console.warn(
      "Pagination elements not found - pagination will not be updated"
    );
    return;
  }

  var pageNum = data.number + 1;
  const totalPages = data.totalPages;
  const totalItems = data.totalElements;
  const pageSize = data.size;
  const startItem = (pageNum - 1) * pageSize + 1;
  const endItem = Math.min(pageNum * pageSize, totalItems);

  startItemEl.textContent = startItem;
  endItemEl.textContent = endItem;
  totalItemsEl.textContent = totalItems;

  paginationContainer.innerHTML = "";

  var paginationHtml = "";

  // Previous button
  if (pageNum > 1) {
    paginationHtml += `
      <a href="#" data-page="${
        pageNum - 1
      }" class="relative inline-flex items-center rounded-l-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50">
        <span class="sr-only">Previous</span>
        <svg class="size-5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
          <path fill-rule="evenodd" d="M11.78 5.22a.75.75 0 0 1 0 1.06L8.06 10l3.72 3.72a.75.75 0 1 1-1.06 1.06l-4.25-4.25a.75.75 0 0 1 0-1.06l4.25-4.25a.75.75 0 0 1 1.06 0Z" clip-rule="evenodd" />
        </svg>
      </a>`;
  } else {
    paginationHtml += `
      <span class="relative inline-flex items-center rounded-l-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 cursor-not-allowed">
        <span class="sr-only">Previous</span>
        <svg class="size-5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
          <path fill-rule="evenodd" d="M11.78 5.22a.75.75 0 0 1 0 1.06L8.06 10l3.72 3.72a.75.75 0 1 1-1.06 1.06l-4.25-4.25a.75.75 0 0 1 0-1.06l4.25-4.25a.75.75 0 0 1 1.06 0Z" clip-rule="evenodd" />
        </svg>
      </span>`;
  }

  // Page numbers
  const maxPagesToShow = 5;
  var startPage = Math.max(1, pageNum - 2);
  var endPage = Math.min(totalPages, pageNum + 2);

  if (startPage > 1) {
    paginationHtml += `
      <a href="#" data-page="1" class="relative inline-flex items-center px-4 py-2 text-sm font-semibold text-gray-900 hover:bg-gray-50">1</a>`;
    if (startPage > 2) {
      paginationHtml += `
        <span class="relative inline-flex items-center px-4 py-2 text-sm font-semibold text-gray-700">...</span>`;
    }
  }

  for (var i = startPage; i <= endPage; i++) {
    if (i === pageNum) {
      paginationHtml += `
        <span class="relative z-10 inline-flex items-center bg-indigo-600 px-4 py-2 text-sm font-semibold text-white">${i}</span>`;
    } else {
      paginationHtml += `
        <a href="#" data-page="${i}" class="relative inline-flex items-center px-4 py-2 text-sm font-semibold text-gray-900 hover:bg-gray-50">${i}</a>`;
    }
  }

  if (endPage < totalPages) {
    if (endPage < totalPages - 1) {
      paginationHtml += `
        <span class="relative inline-flex items-center px-4 py-2 text-sm font-semibold text-gray-700">...</span>`;
    }
    paginationHtml += `
      <a href="#" data-page="${totalPages}" class="relative inline-flex items-center px-4 py-2 text-sm font-semibold text-gray-900 hover:bg-gray-50">${totalPages}</a>`;
  }

  // Next button
  if (pageNum < totalPages) {
    paginationHtml += `
      <a href="#" data-page="${
        pageNum + 1
      }" class="relative inline-flex items-center rounded-r-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50">
        <span class="sr-only">Next</span>
        <svg class="size-5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
          <path fill-rule="evenodd" d="M8.22 5.22a.75.75 0 0 1 1.06 0l4.25 4.25a.75.75 0 0 1 0 1.06l-4.25 4.25a.75.75 0 0 1-1.06-1.06L11.94 10 8.22 6.28a.75.75 0 0 1 0-1.06Z" clip-rule="evenodd" />
        </svg>
      </a>`;
  } else {
    paginationHtml += `
      <span class="relative inline-flex items-center rounded-r-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 cursor-not-allowed">
        <span class="sr-only">Next</span>
        <svg class="size-5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
          <path fill-rule="evenodd" d="M8.22 5.22a.75.75 0 0 1 1.06 0l4.25 4.25a.75.75 0 0 1 0 1.06l-4.25 4.25a.75.75 0 0 1-1.06-1.06L11.94 10 8.22 6.28a.75.75 0 0 1 0-1.06Z" clip-rule="evenodd" />
        </svg>
      </span>`;
  }

  paginationContainer.innerHTML = paginationHtml;

  // Add event listeners
  paginationContainer.querySelectorAll("a[data-page]").forEach((link) => {
    link.addEventListener("click", function (e) {
      e.preventDefault();
      const newPage = parseInt(this.getAttribute("data-page"));
      if (!isNaN(newPage)) {
        // Sử dụng biến toàn cục
        currentPage = newPage;
        loadTrangTraiData(currentPage, pageSize);
      }
    });
  });
}
// THêm hàm xử lý khi click vào từng dòng
function loadTrangTraiData(page = 1, size = pageSize) {
  validatePageSize();
  const nameFilter = document.getElementById("nameFilter")?.value || "";
  const donViFilter = document.getElementById("donViFilter")?.value || "";
  const searchTerm = document.getElementById("searchInput")?.value || "";
  var url = `/api/v1/trang-trai/paged?page=${page - 1}&size=${size}`;

  if (nameFilter) {
    url += `&tenTrangTrai=${encodeURIComponent(nameFilter)}`;
  }
  if (donViFilter) {
    url += `&donViHanhChinh=${encodeURIComponent(donViFilter)}`;
  }
  if (searchTerm) {
    url += `&tenTrangTrai=${encodeURIComponent(searchTerm)}`;
  }

  fetch(url)
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.text(); // Get as text first
    })
    .then((text) => {
      try {
        return JSON.parse(text);
      } catch (error) {
        console.error("JSON Parse Error:", error);
        console.log("Response text:", text);
        throw new Error("Invalid JSON response from server");
      }
    })
    .then((data) => {
      const tableBody = document.getElementById("trangTraiTableBody");
      tableBody.innerHTML = "";

      data.content.forEach((trangTrai) => {
        const row = document.createElement("tr");
        row.className = "hover:bg-gray-50 cursor-pointer";
        row.dataset.id = trangTrai.id;
        row.addEventListener("click", () => loadEditForm(trangTrai.id));
        row.innerHTML = `
                  <td class="border px-4 py-2" data-id="${trangTrai.id}">${
          trangTrai.maTrangTrai || ""
        }</td>
                  <td class="border px-4 py-2" data-field="tenTrangTrai" ondblclick="makeEditable(this)">${
                    trangTrai.tenTrangTrai || ""
                  }</td>
                  <td class="border px-4 py-2" data-field="tenChu" ondblclick="makeEditable(this)">${
                    trangTrai.tenChu || ""
                  }</td>
                  <td class="border px-4 py-2" data-field="soDienThoai" ondblclick="makeEditable(this)">
                    ${trangTrai.soDienThoai || ""}<br>
                    ${trangTrai.email || ""}
                  </td>
                  <td class="border px-4 py-2" data-field="diaChiDayDu" ondblclick="makeEditable(this)">${
                    trangTrai.diaChiDayDu || ""
                  }</td>
                  <td class="border px-4 py-2" data-field="dienTich" ondblclick="makeEditable(this)">${
                    trangTrai.dienTich || ""
                  }</td>
                  <td class="border px-4 py-2" data-field="tongDan" ondblclick="makeEditable(this)">${
                    trangTrai.tongDan || ""
                  }</td>
                  <td class="border px-4 py-2">
                    ${
                      trangTrai.trangTraiVatNuois
                        ?.map(
                          (vn) =>
                            `${vn.loaiVatNuoi?.tenLoai} (${vn.soLuong || 0})`
                        )
                        .join(", ") || ""
                    }
                  </td>
                  <td class="border px-4 py-2">
                    <button class="px-3 py-1 rounded-full text-sm font-medium ${
                      trangTrai.trangThaiHoatDong
                        ? "bg-green-100 text-green-800"
                        : "bg-red-100 text-red-800"
                    }" data-status="${
          trangTrai.trangThaiHoatDong
        }" onclick="toggleStatus(this)">
                      ${
                        trangTrai.trangThaiHoatDong
                          ? "Hoạt động"
                          : "Ngừng hoạt động"
                      }
                    </button>
                  </td>
                `;
        tableBody.appendChild(row);
      });

      updatePaginationControls(data);
    })
    .catch((error) => {
      console.error("Error:", error);
      showToast("Lỗi khi tải dữ liệu", "error");
    });
}

// Function to load and show edit form
// Load và hiển thị lên form sửa
async function loadEditForm(farmId) {
  try {
    const response = await fetch(`/api/v1/trang-trai/${farmId}`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    let farm;
    try {
      const text = await response.text(); // Get response as text first
      farm = JSON.parse(text); // Then try to parse it
    } catch (parseError) {
      console.error("JSON Parse Error:", parseError);
      console.log("Response text:", text); // Log the problematic response
      throw new Error("Invalid JSON response from server");
    }

    if (!farm || !farm.id) {
      throw new Error("Invalid farm data received");
    }

    document.getElementById("editFarmId").value = farm.id;
    document.getElementById("editMaTrangTrai").value = farm.maTrangTrai;
    document.getElementById("editTenTrangTrai").value = farm.tenTrangTrai;
    document.getElementById("editTenChu").value = farm.tenChu;
    document.getElementById("editSoDienThoai").value = farm.soDienThoai;
    document.getElementById("editEmail").value = farm.email;
    document.getElementById("editDienTich").value = farm.dienTich;
    document.getElementById("editTongDan").value = farm.tongDan;
    document.getElementById("editPhuongThucChanNuoi").value =
      farm.phuongThucChanNuoi;

    await loadAdminUnitsForEdit(farm.donViHanhChinh);

    document.getElementById("editFarmModal").classList.remove("hidden");
  } catch (error) {
    console.error("Error loading farm data:", error);
    showToast("Lỗi khi tải thông tin trang trại", "error");

    // Add more detailed error logging
    if (error.message.includes("JSON")) {
      console.error("JSON parsing error. Please check server response format.");
    } else if (error.message.includes("HTTP")) {
      console.error("Network error. Please check server connectivity.");
    }

    // Prevent modal from showing on error
    const modal = document.getElementById("editFarmModal");
    if (modal) modal.classList.add("hidden");
  }
}

// Update / sửa trang trại
document
  .getElementById("editFarmForm")
  .addEventListener("submit", async function (e) {
    e.preventDefault();

    const formData = new FormData(this);
    const farmId = formData.get("id");

    // Build update DTO
    const updateData = {
      tenTrangTrai: formData.get("tenTrangTrai"),
      tenChu: formData.get("tenChu"),
      soDienThoai: formData.get("soDienThoai"),
      email: formData.get("email"),
      soNha: formData.get("soNha"),
      tenDuong: formData.get("tenDuong"),
      khuPho: formData.get("khuPho"),
      donViHanhChinhId: parseInt(formData.get("donViHanhChinhId")),
      dienTich: parseFloat(formData.get("dienTich")),
      tongDan: parseInt(formData.get("tongDan")),
      phuongThucChanNuoi: formData.get("phuongThucChanNuoi"),

      // Get coordinates if they exist
      longitude: formData.get("longitude")
        ? parseFloat(formData.get("longitude"))
        : null,
      latitude: formData.get("latitude")
        ? parseFloat(formData.get("latitude"))
        : null,

      // Chuyển đổi dữ liệu vật nuôi từ form
      vatNuoi: Array.from(
        document.querySelectorAll("#editVatNuoiContainer .flex")
      )
        .map((row) => ({
          loaiVatNuoiId: parseInt(row.querySelector("select").value),
          soLuong: parseInt(row.querySelector('input[type="number"]').value),
        }))
        .filter((item) => item.loaiVatNuoiId && item.soLuong > 0),
    };

    try {
      const response = await fetch(`/api/v1/trang-trai/${farmId}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
        body: JSON.stringify(updateData),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Update failed");
      }

      const updatedFarm = await response.json();

      // Update table row with new data
      updateTableRow(updatedFarm);

      // Close modal and show success message
      closeEditModal();
      showToast("Cập nhật thành công", "success");

      // Reload table data to ensure consistency
      loadTrangTraiData(currentPage, pageSize);
    } catch (error) {
      console.error("Error updating farm:", error);
      showToast(error.message || "Lỗi khi cập nhật", "error");
    }
  });

// Helper function to update table row
function updateTableRow(farm) {
  const row = document.querySelector(`tr[data-id="${farm.id}"]`);
  if (row) {
    const cells = row.getElementsByTagName("td");
    cells[0].textContent = farm.maTrangTrai;
    cells[1].textContent = farm.tenTrangTrai;
    cells[2].textContent = farm.tenChu;
    cells[3].innerHTML = `${farm.soDienThoai}<br>${farm.email}`;
    cells[4].textContent = farm.diaChiDayDu;
    cells[5].textContent = farm.dienTich;
    cells[6].textContent = farm.tongDan;
    cells[7].textContent = farm.trangTraiVatNuois
      ?.map((vn) => `${vn.loaiVatNuoi?.tenLoai} (${vn.soLuong || 0})`)
      .join(", ");

    // Update status button
    const statusBtn = cells[8].querySelector("button");
    updateStatusButton(statusBtn, farm.trangThaiHoatDong);
  }
}

document.addEventListener("DOMContentLoaded", function () {
  // Ensure edit modal is hidden on page load
  const editModal = document.getElementById("editFarmModal");
  if (editModal) {
    editModal.classList.add("hidden");
  }

  loadDonViHanhChinh();
  loadTrangTraiData(currentPage, pageSize);

  const entriesElement = document.getElementById("entries");
  if (entriesElement) {
    entriesElement.addEventListener("change", function (e) {
      pageSize = Number(e.target.value);
      currentPage = 1;
      loadTrangTraiData(currentPage, pageSize);
    });
  }

  const nameFilterElement = document.getElementById("nameFilter");
  const donViFilterElement = document.getElementById("donViFilter");
  const searchInputElement = document.getElementById("searchInput");
  const prevBtnElement = document.getElementById("prevBtn");
  const nextBtnElement = document.getElementById("nextBtn");

  if (nameFilterElement) {
    nameFilterElement.addEventListener("input", function () {
      currentPage = 1;
      loadTrangTraiData(currentPage, pageSize);
    });
  }

  if (donViFilterElement) {
    donViFilterElement.addEventListener("change", function () {
      currentPage = 1;
      loadTrangTraiData(currentPage, pageSize);
    });
  }

  if (searchInputElement) {
    searchInputElement.addEventListener("input", function () {
      currentPage = 1;
      loadTrangTraiData(currentPage, pageSize);
    });
  }

  if (prevBtnElement) {
    prevBtnElement.addEventListener("click", function (e) {
      e.preventDefault();
      if (currentPage > 1) {
        currentPage--;
        loadTrangTraiData(currentPage, pageSize);
      }
    });
  }

  if (nextBtnElement) {
    nextBtnElement.addEventListener("click", function (e) {
      e.preventDefault();
      const totalPages = Number(
        document.getElementById("totalPages").textContent
      );
      if (currentPage < totalPages) {
        currentPage++;
        loadTrangTraiData(currentPage, pageSize);
      }
    });
  }

  const prevBtnMobile = document.getElementById("prevBtnMobile");
  const nextBtnMobile = document.getElementById("nextBtnMobile");

  if (prevBtnMobile) {
    prevBtnMobile.addEventListener("click", function (e) {
      e.preventDefault();
      if (currentPage > 1) {
        currentPage--;
        loadTrangTraiData(currentPage, pageSize);
      }
    });
  }

  if (nextBtnMobile) {
    nextBtnMobile.addEventListener("click", function (e) {
      e.preventDefault();
      if (currentPage < data.totalPages) {
        currentPage++;
        loadTrangTraiData(currentPage, pageSize);
      }
    });
  }

  initializeVatNuoiControls();
});

// Add new function to load administrative units
function loadDonViHanhChinh() {
  fetch("/api/v1/don-vi-hanh-chinh")
    .then((response) => response.json())
    .then((data) => {
      const donViFilter = document.getElementById("donViFilter");
      if (donViFilter) {
        // Keep the "Tất cả" option
        donViFilter.innerHTML = '<option value="">Tất cả</option>';

        // Add options for each administrative unit
        data.forEach((donVi) => {
          const option = document.createElement("option");
          option.value = donVi.id;
          option.textContent = donVi.ten;
          donViFilter.appendChild(option);
        });
      }
    })
    .catch((error) =>
      console.error("Error loading administrative units:", error)
    );
}

// Modify initial load to include administrative units
function initializePage() {
  loadDonViHanhChinh();
  loadTrangTraiData(currentPage, pageSize);
}

// Replace the existing initial load call with initializePage
initializePage();

// Modal functionality
const addFarmBtn = document.getElementById("addFarmBtn");
const addFarmModal = document.getElementById("addFarmModal");
const closeModalBtn = document.getElementById("closeModalBtn");

addFarmBtn.addEventListener("click", () => {
  addFarmModal.classList.remove("hidden");
});

closeModalBtn.addEventListener("click", () => {
  addFarmModal.classList.add("hidden");
});

// Handle form submission
document
  .getElementById("addFarmForm")
  .addEventListener("submit", function (event) {
    event.preventDefault();
    const formData = new FormData(this);

    fetch("/api/v1/trang-trai", {
      method: "POST",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
      body: JSON.stringify(Object.fromEntries(formData)),
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error("Lỗi khi thêm trang trại");
        }
        return response.json();
      })
      .then((data) => {
        // Close the modal
        addFarmModal.classList.add("hidden");
        // Reload the data or update the table
        loadTrangTraiData(currentPage, pageSize);
      })
      .catch((error) => {
        console.error("Error:", error);
        // Display error message if needed
      });
  });

// Load administrative units for cascading selects
// Load đơn vị hành chính cho các select phụ thuộc
async function loadTinhThanh() {
  try {
    const response = await fetch("/api/v1/don-vi-hanh-chinh/cap/4");
    if (!response.ok) throw new Error("Failed to fetch tinh/thanh");
    const data = await response.json();

    const select = document.getElementById("editTinhThanh");
    select.innerHTML = '<option value="">Chọn Tỉnh/Thành phố</option>';
    data.forEach((tinh) => {
      select.innerHTML += `<option value="${tinh.id}">${tinh.ten}</option>`;
    });
    return data;
  } catch (error) {
    console.error("Error loading tinh/thanh:", error);
    throw error;
  }
}

async function loadQuanHuyen(tinhId) {
  if (!tinhId) {
    const quanHuyenSelect = document.getElementById("editQuanHuyen");
    const phuongXaSelect = document.getElementById("editPhuongXa");
    quanHuyenSelect.innerHTML = '<option value="">Chọn Quận/Huyện</option>';
    phuongXaSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';
    quanHuyenSelect.disabled = true;
    phuongXaSelect.disabled = true;
    return;
  }

  try {
    const response = await fetch(`/api/v1/don-vi-hanh-chinh/parent/${tinhId}`);
    if (!response.ok) throw new Error("Failed to fetch quan/huyen");
    const data = await response.json();

    const select = document.getElementById("editQuanHuyen");
    select.disabled = false;
    select.innerHTML = '<option value="">Chọn Quận/Huyện</option>';
    data.forEach((huyen) => {
      select.innerHTML += `<option value="${huyen.id}">${huyen.ten}</option>`;
    });
    return data;
  } catch (error) {
    console.error("Error loading quan/huyen:", error);
    throw error;
  }
}

async function loadPhuongXa(huyenId) {
  if (!huyenId) {
    const phuongXaSelect = document.getElementById("editPhuongXa");
    phuongXaSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';
    phuongXaSelect.disabled = true;
    return;
  }

  try {
    const response = await fetch(`/api/v1/don-vi-hanh-chinh/parent/${huyenId}`);
    if (!response.ok) throw new Error("Failed to fetch phuong/xa");
    const data = await response.json();

    const select = document.getElementById("editPhuongXa");
    select.disabled = false;
    select.innerHTML = '<option value="">Chọn Phường/Xã</option>';
    data.forEach((xa) => {
      select.innerHTML += `<option value="${xa.id}">${xa.ten}</option>`;
    });
    return data;
  } catch (error) {
    console.error("Error loading phuong/xa:", error);
    throw error;
  }
}

document.getElementById("tinhThanh").addEventListener("change", function () {
  loadQuanHuyen(this.value);
  document.getElementById("donViHanhChinhId").value = this.value;
});

document.getElementById("quanHuyen").addEventListener("change", function () {
  loadPhuongXa(this.value);
  document.getElementById("donViHanhChinhId").value = this.value;
});

document.getElementById("phuongXa").addEventListener("change", function () {
  document.getElementById("donViHanhChinhId").value = this.value;
});

// Initialize tỉnh/thành select on modal open
document.getElementById("addFarmBtn").addEventListener("click", function () {
  loadTinhThanh();
});

// Add new functions for inline editing 
// 
function makeEditable(element) {
  const currentValue = element.textContent;
  const field = element.getAttribute("data-field");
  const farmId = element.closest("tr").getAttribute("data-id");

  element.innerHTML = `
        <input type="text" 
               class="w-full p-1 border rounded"
               value="${currentValue}"
               onblur="updateField(thisLINH - TRANG, '${field}', ${farmId})"
               onkeypress="handleEnterKey(event, this, '${field}', ${farmId})">
    `;
  element.querySelector("input").focus();
}

function handleEnterKey(event, element, field, farmId) {
  if (event.key === "Enter") {
    updateField(element, field, farmId);
  }
}

function updateField(element, field, farmId) {
  const newValue = element.value;
  const parent = element.parentElement;

  fetch(`/api/v1/trang-trai/${farmId}`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ [field]: newValue }),
  })
    .then((response) => response.json())
    .then((data) => {
      parent.innerHTML = newValue;
      showToast("Cập nhật thành công!", "success");
    })
    .catch((error) => {
      showToast("Lỗi cập nhật!", "error");
      parent.innerHTML = element.defaultValue;
    });
}

function toggleStatus(button) {
  const farmId = button.closest("tr").getAttribute("data-id");
  const currentStatus = button.getAttribute("data-status") === "true";

  fetch(`/api/v1/trang-trai/${farmId}/toggle-status`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ trangThaiHoatDong: !currentStatus }),
  })
    .then((response) => response.json())
    .then((data) => {
      updateStatusButton(button, data.trangThaiHoatDong);
      showToast("Cập nhật trạng thái thành công!", "success");
    })
    .catch((error) => showToast("Lỗi cập nhật trạng thái!", "error"));
}

function updateStatusButton(button, status) {
  button.setAttribute("data-status", status);
  button.className = `px-3 py-1 rounded-full text-sm font-medium ${
    status ? "bg-green-100 text-green-800" : "bg-red-100 text-red-800"
  }`;
  button.textContent = status ? "Hoạt động" : "Ngừng hoạt động";
}

function showEditModal(farmData) {
  const modal = document.getElementById("editFarmModal");
  // Populate form with farm data
  // ...implement form population logic...
  modal.classList.remove("hidden");
}

function showToast(message, type) {
  // Implement toast notification logic
  console.log(`${type}: ${message}`);
}

// Edit farm functionality
function editFarm(button) {
  const row = button.closest("tr");
  const farmId = row.querySelector("td").dataset.id;

  // Fetch farm data
  fetch(`/api/v1/trang-trai/${farmId}`)
    .then((response) => response.json())
    .then((farm) => {
      // Populate edit form
      document.getElementById("editFarmId").value = farm.id;
      document.getElementById("editMaTrangTrai").value = farm.maTrangTrai;
      document.getElementById("editTenTrangTrai").value = farm.tenTrangTrai;
      document.getElementById("editTenChu").value = farm.tenChu;
      document.getElementById("editSoDienThoai").value = farm.soDienThoai;
      document.getElementById("editEmail").value = farm.email;
      document.getElementById("editDienTich").value = farm.dienTich;
      document.getElementById("editTongDan").value = farm.tongDan;
      document.getElementById("editPhuongThucChanNuoi").value =
        farm.phuongThucChanNuoi;

      //todo: lOAD LOẠI VẬT NUÔI KÈM SỐ LƯỢNG

      // Load administrative units
      if (farm.donViHanhChinh) {
        loadAdminUnitsForEdit(farm.donViHanhChinh);
      }

      // Show modal
      document.getElementById("editFarmModal").classList.remove("hidden");
    })
    .catch((error) => showToast("Lỗi khi tải thông tin trang trại", "error"));
}

// Handle edit form submission
document
  .getElementById("editFarmForm")
  .addEventListener("submit", function (e) {
    e.preventDefault();
    const farmId = document.getElementById("editFarmId").value;
    const formData = new FormData(this);

    fetch(`/api/v1/trang-trai/${farmId}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(Object.fromEntries(formData)),
    })
      .then((response) => response.json())
      .then((data) => {
        // Update table row with new data
        updateTableRow(data);
        // Close modal
        closeEditModal();
        showToast("Cập nhật thành công", "success");
      })
      .catch((error) => showToast("Lỗi khi cập nhật", "error"));
  });

function closeEditModal() {
  document.getElementById("editFarmModal").classList.add("hidden");
}

function updateTableRow(farm) {
  const row = document.querySelector(`td[data-id="${farm.id}"]`).closest("tr");

  // Update text fields
  row.querySelector('[data-field="maTrangTrai"]').textContent =
    farm.maTrangTrai;
  row.querySelector('[data-field="tenTrangTrai"]').textContent =
    farm.tenTrangTrai;
  // Update other fields...
}

// Update event listeners for address selects
document
  .getElementById("editTinhThanh")
  ?.addEventListener("change", async function () {
    try {
      await loadQuanHuyen(this.value);
      document.getElementById("editQuanHuyen").value = "";
      document.getElementById("editPhuongXa").value = "";
      document.getElementById("editDonViHanhChinhId").value = "";
    } catch (error) {
      console.error("Error handling province change:", error);
    }
  });

document
  .getElementById("editQuanHuyen")
  ?.addEventListener("change", async function () {
    try {
      await loadPhuongXa(this.value);
      document.getElementById("editPhuongXa").value = "";
      document.getElementById("editDonViHanhChinhId").value = "";
    } catch (error) {
      console.error("Error handling district change:", error);
    }
  });

document
  .getElementById("editPhuongXa")
  ?.addEventListener("change", function () {
    document.getElementById("editDonViHanhChinhId").value = this.value;
  });

// Update modal selectors to match new fragment IDs
const editFarmModal = document.getElementById("editFarmModal");
const closeEditModalBtn = document.getElementById("closeEditModalBtn");

closeEditModalBtn.addEventListener("click", () => {
  closeEditModal();
});

function closeEditModal() {
  editFarmModal.classList.add("hidden");
}

// Update edit form submission handler
document
  .getElementById("editFarmForm")
  .addEventListener("submit", function (e) {
    e.preventDefault();
    const farmId = document.getElementById("editFarmId").value;
    const formData = new FormData(this);

    fetch(`/api/v1/trang-trai/${farmId}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(Object.fromEntries(formData)),
    })
      .then((response) => response.json())
      .then((data) => {
        updateTableRow(data);
        closeEditModal();
        showToast("Cập nhật thành công", "success");
      })
      .catch((error) => showToast("Lỗi khi cập nhật", "error"));
  });

// Update the functions to handle both add and edit forms
async function loadTinhThanh(formPrefix = "") {
  try {
    const response = await fetch("/api/v1/don-vi-hanh-chinh/cap/4");
    if (!response.ok) throw new Error("Failed to fetch tinh/thanh");
    const data = await response.json();

    const select = document.getElementById(formPrefix + "tinhThanh");
    if (select) {
      select.innerHTML = '<option value="">Chọn Tỉnh/Thành phố</option>';
      data.forEach((tinh) => {
        select.innerHTML += `<option value="${tinh.id}">${tinh.ten}</option>`;
      });
    }
    return data;
  } catch (error) {
    console.error("Error loading tinh/thanh:", error);
    showToast("Lỗi khi tải danh sách tỉnh/thành", "error");
    throw error;
  }
}

async function loadQuanHuyen(tinhId, formPrefix = "") {
  const quanHuyenSelect = document.getElementById(formPrefix + "quanHuyen");
  const phuongXaSelect = document.getElementById(formPrefix + "phuongXa");
  const donViIdInput = document.getElementById(formPrefix + "donViHanhChinhId");

  if (!tinhId) {
    if (quanHuyenSelect) {
      quanHuyenSelect.innerHTML = '<option value="">Chọn Quận/Huyện</option>';
      quanHuyenSelect.disabled = true;
    }
    if (phuongXaSelect) {
      phuongXaSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';
      phuongXaSelect.disabled = true;
    }
    if (donViIdInput) donViIdInput.value = "";
    return;
  }

  try {
    const response = await fetch(`/api/v1/don-vi-hanh-chinh/parent/${tinhId}`);
    if (!response.ok) throw new Error("Failed to fetch quan/huyen");
    const data = await response.json();

    if (quanHuyenSelect) {
      quanHuyenSelect.disabled = false;
      quanHuyenSelect.innerHTML = '<option value="">Chọn Quận/Huyện</option>';
      data.forEach((huyen) => {
        quanHuyenSelect.innerHTML += `<option value="${huyen.id}">${huyen.ten}</option>`;
      });
    }
    return data;
  } catch (error) {
    console.error("Error loading quan/huyen:", error);
    showToast("Lỗi khi tải danh sách quận/huyện", "error");
    throw error;
  }
}

async function loadPhuongXa(huyenId, formPrefix = "") {
  const phuongXaSelect = document.getElementById(formPrefix + "phuongXa");
  const donViIdInput = document.getElementById(formPrefix + "donViHanhChinhId");

  if (!huyenId) {
    if (phuongXaSelect) {
      phuongXaSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';
      phuongXaSelect.disabled = true;
    }
    if (donViIdInput) donViIdInput.value = "";
    return;
  }

  try {
    const response = await fetch(`/api/v1/don-vi-hanh-chinh/parent/${huyenId}`);
    if (!response.ok) throw new Error("Failed to fetch phuong/xa");
    const data = await response.json();

    if (phuongXaSelect) {
      phuongXaSelect.disabled = false;
      phuongXaSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';
      data.forEach((xa) => {
        phuongXaSelect.innerHTML += `<option value="${xa.id}">${xa.ten}</option>`;
      });
    }
    return data;
  } catch (error) {
    console.error("Error loading phuong/xa:", error);
    showToast("Lỗi khi tải danh sách phường/xã", "error");
    throw error;
  }
}

// Update event listeners for add form
document
  .getElementById("addFarmBtn")
  .addEventListener("click", async function () {
    try {
      await loadTinhThanh(""); // Load for add form
      const quanHuyenSelect = document.getElementById("quanHuyen");
      const phuongXaSelect = document.getElementById("phuongXa");
      if (quanHuyenSelect) quanHuyenSelect.disabled = true;
      if (phuongXaSelect) phuongXaSelect.disabled = true;
    } catch (error) {
      console.error("Error initializing add form:", error);
    }
  });

// Add form event listeners
document.getElementById("tinhThanh")?.addEventListener("change", function () {
  loadQuanHuyen(this.value, "");
});

document.getElementById("quanHuyen")?.addEventListener("change", function () {
  loadPhuongXa(this.value, "");
});

document.getElementById("phuongXa")?.addEventListener("change", function () {
  document.getElementById("donViHanhChinhId").value = this.value;
});

async function loadAdminUnitsForEdit(donViHanhChinh) {
  if (!donViHanhChinh) {
    console.warn("No administrative unit provided");
    return;
  }

  try {
    const tinhSelect = document.getElementById("editTinhThanh");
    const huyenSelect = document.getElementById("editQuanHuyen");
    const xaSelect = document.getElementById("editPhuongXa");

    // Get the ward level unit (cấp xã) and its parent chain
    let donViXa, donViHuyen, donViTinh;

    const wardResponse = await fetch(
      `/api/v1/don-vi-hanh-chinh/${donViHanhChinh}`
    );
    donViXa = await wardResponse.json();
    // Get district (parent of ward)
    const districtResponse = await fetch(
      `/api/v1/don-vi-hanh-chinh/${donViXa.donViCha.id}`
    );
    donViHuyen = await districtResponse.json();
    // Get province (parent of district)
    const provinceResponse = await fetch(
      `/api/v1/don-vi-hanh-chinh/${donViHuyen.donViCha.id}`
    );
    donViTinh = await provinceResponse.json();

    // Load and populate province select
    const provinces = await fetch("/api/v1/don-vi-hanh-chinh/cap/4");
    const provinceData = await provinces.json();

    tinhSelect.innerHTML = '<option value="">Chọn Tỉnh/Thành phố</option>';
    provinceData.forEach((tinh) => {
      tinhSelect.innerHTML += `<option value="${tinh.id}" ${
        donViTinh && tinh.id === donViTinh.id ? "selected" : ""
      }>${tinh.ten}</option>`;
    });

    // If we have province info, load districts
    if (donViTinh) {
      huyenSelect.disabled = false;
      const districts = await fetch(
        `/api/v1/don-vi-hanh-chinh/parent/${donViTinh.id}`
      );
      const districtData = await districts.json();

      huyenSelect.innerHTML = '<option value="">Chọn Quận/Huyện</option>';
      districtData.forEach((huyen) => {
        huyenSelect.innerHTML += `<option value="${huyen.id}" ${
          donViHuyen && huyen.id === donViHuyen.id ? "selected" : ""
        }>${huyen.ten}</option>`;
      });

      // If we have district info, load wards
      if (donViHuyen) {
        xaSelect.disabled = false;
        const wards = await fetch(
          `/api/v1/don-vi-hanh-chinh/parent/${donViHuyen.id}`
        );
        const wardData = await wards.json();

        xaSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';
        wardData.forEach((xa) => {
          xaSelect.innerHTML += `<option value="${xa.id}" ${
            donViXa && xa.id === donViXa.id ? "selected" : ""
          }>${xa.ten}</option>`;
        });
      }
    }

    // Set the final selected value for donViHanhChinhId
    if (donViXa) {
      document.getElementById("editDonViHanhChinhId").value = donViXa.id;
    } else if (donViHuyen) {
      document.getElementById("editDonViHanhChinhId").value = donViHuyen.id;
    } else if (donViTinh) {
      document.getElementById("editDonViHanhChinhId").value = donViTinh.id;
    }
  } catch (error) {
    console.error("Error loading administrative units:", error);
    showToast("Lỗi khi tải thông tin đơn vị hành chính", "error");
  }
}

// Enhanced function to load farm data into edit form
async function loadEditForm(farmId) {
  try {
    const response = await fetch(`/api/v1/trang-trai/${farmId}`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    let farm;
    try {
      const text = await response.text(); // Get response as text first
      farm = JSON.parse(text); // Then try to parse it
    } catch (parseError) {
      console.error("JSON Parse Error:", parseError);
      console.log("Response text:", text); // Log the problematic response
      throw new Error("Invalid JSON response from server");
    }

    if (!farm || !farm.id) {
      throw new Error("Invalid farm data received");
    }

    // Basic information
    document.getElementById("editFarmId").value = farm.id;
    document.getElementById("editMaTrangTrai").value = farm.maTrangTrai;
    document.getElementById("editTenTrangTrai").value = farm.tenTrangTrai;
    document.getElementById("editTenChu").value = farm.tenChu;
    document.getElementById("editSoDienThoai").value = farm.soDienThoai;
    document.getElementById("editEmail").value = farm.email;

    // Address information
    document.getElementById("editSoNha").value = farm.soNha || "";
    document.getElementById("editTenDuong").value = farm.tenDuong || "";
    document.getElementById("editKhuPho").value = farm.khuPho || "";
    document.getElementById("editDiaChiDayDu").value = farm.diaChiDayDu || "";

    // Operation information
    document.getElementById("editDienTich").value = farm.dienTich;
    document.getElementById("editTongDan").value = farm.tongDan;
    document.getElementById("editPhuongThucChanNuoi").value =
      farm.phuongThucChanNuoi;

    // Load and set administrative units
    if (farm.donViHanhChinh) {
      await loadAdminUnitsForEdit(farm.donViHanhChinh);
    }

    // Populate animal data
    const container = document.getElementById("editVatNuoiContainer");
    container.innerHTML = ""; // Clear existing rows

    const animals = await loadLoaiVatNuoi();
    const options = animals
      .map(
        (animal) => `<option value="${animal.id}">${animal.tenLoai}</option>`
      )
      .join("");

    farm.trangTraiVatNuois?.forEach((vatNuoi, index) => {
      addVatNuoiRow(container, "editVatNuoi", options);
      const select = container.querySelector(
        `[name="editVatNuoi[${index}].loaiVatNuoi"]`
      );
      const input = container.querySelector(
        `[name="editVatNuoi[${index}].soLuong"]`
      );
      if (select) select.value = vatNuoi.loaiVatNuoi.id;
      if (input) input.value = vatNuoi.soLuong;
    });

    // Show modal after data is loaded
    document.getElementById("editFarmModal").classList.remove("hidden");
  } catch (error) {
    console.error("Error loading farm data:", error);
    showToast("Lỗi khi tải thông tin trang trại", "error");

    // Add more detailed error logging
    if (error.message.includes("JSON")) {
      console.error("JSON parsing error. Please check server response format.");
    } else if (error.message.includes("HTTP")) {
      console.error("Network error. Please check server connectivity.");
    }

    // Prevent modal from showing on error
    const modal = document.getElementById("editFarmModal");
    if (modal) modal.classList.add("hidden");
  }
}

async function loadAdminUnitsForEdit(donViHanhChinh) {
  if (!donViHanhChinh) {
    console.warn("No administrative unit provided");
    return;
  }

  try {
    const tinhSelect = document.getElementById("editTinhThanh");
    const huyenSelect = document.getElementById("editQuanHuyen");
    const xaSelect = document.getElementById("editPhuongXa");

    // Get the ward level unit (cấp xã) and its parent chain
    let donViXa, donViHuyen, donViTinh;

    const wardResponse = await fetch(
      `/api/v1/don-vi-hanh-chinh/${donViHanhChinh}`
    );
    donViXa = await wardResponse.json();
    // Get district (parent of ward)
    const districtResponse = await fetch(
      `/api/v1/don-vi-hanh-chinh/${donViXa.donViCha.id}`
    );
    donViHuyen = await districtResponse.json();
    // Get province (parent of district)
    const provinceResponse = await fetch(
      `/api/v1/don-vi-hanh-chinh/${donViHuyen.donViCha.id}`
    );
    donViTinh = await provinceResponse.json();

    // Load and populate province select
    const provinces = await fetch("/api/v1/don-vi-hanh-chinh/cap/4");
    const provinceData = await provinces.json();

    tinhSelect.innerHTML = '<option value="">Chọn Tỉnh/Thành phố</option>';
    provinceData.forEach((tinh) => {
      tinhSelect.innerHTML += `<option value="${tinh.id}" ${
        donViTinh && tinh.id === donViTinh.id ? "selected" : ""
      }>${tinh.ten}</option>`;
    });

    // If we have province info, load districts
    if (donViTinh) {
      huyenSelect.disabled = false;
      const districts = await fetch(
        `/api/v1/don-vi-hanh-chinh/parent/${donViTinh.id}`
      );
      const districtData = await districts.json();

      huyenSelect.innerHTML = '<option value="">Chọn Quận/Huyện</option>';
      districtData.forEach((huyen) => {
        huyenSelect.innerHTML += `<option value="${huyen.id}" ${
          donViHuyen && huyen.id === donViHuyen.id ? "selected" : ""
        }>${huyen.ten}</option>`;
      });

      // If we have district info, load wards
      if (donViHuyen) {
        xaSelect.disabled = false;
        const wards = await fetch(
          `/api/v1/don-vi-hanh-chinh/parent/${donViHuyen.id}`
        );
        const wardData = await wards.json();

        xaSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';
        wardData.forEach((xa) => {
          xaSelect.innerHTML += `<option value="${xa.id}" ${
            donViXa && xa.id === donViXa.id ? "selected" : ""
          }>${xa.ten}</option>`;
        });
      }
    }

    // Set the final selected value for donViHanhChinhId
    if (donViXa) {
      document.getElementById("editDonViHanhChinhId").value = donViXa.id;
    } else if (donViHuyen) {
      document.getElementById("editDonViHanhChinhId").value = donViHuyen.id;
    } else if (donViTinh) {
      document.getElementById("editDonViHanhChinhId").value = donViTinh.id;
    }
  } catch (error) {
    console.error("Error loading administrative units:", error);
    showToast("Lỗi khi tải thông tin đơn vị hành chính", "error");
  }
}

// Update event listeners for edit form address selects
document
  .getElementById("editTinhThanh")
  ?.addEventListener("change", async function () {
    const huyenSelect = document.getElementById("editQuanHuyen");
    const xaSelect = document.getElementById("editPhuongXa");

    try {
      // Reset dependent selects
      huyenSelect.innerHTML = '<option value="">Chọn Quận/Huyện</option>';
      xaSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';

      if (this.value) {
        // Load districts for selected province
        const response = await fetch(
          `/api/v1/don-vi-hanh-chinh/parent/${this.value}`
        );
        const districts = await response.json();

        huyenSelect.disabled = false;
        districts.forEach((huyen) => {
          huyenSelect.innerHTML += `<option value="${huyen.id}">${huyen.ten}</option>`;
        });

        xaSelect.disabled = true;
        document.getElementById("editDonViHanhChinhId").value = this.value;
      } else {
        huyenSelect.disabled = true;
        xaSelect.disabled = true;
        document.getElementById("editDonViHanhChinhId").value = "";
      }
    } catch (error) {
      console.error("Error loading districts:", error);
      showToast("Lỗi khi tải danh sách quận/huyện", "error");
    }
  });

document
  .getElementById("editQuanHuyen")
  ?.addEventListener("change", async function () {
    const xaSelect = document.getElementById("editPhuongXa");

    try {
      xaSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';

      if (this.value) {
        // Load wards for selected district
        const response = await fetch(
          `/api/v1/don-vi-hanh-chinh/parent/${this.value}`
        );
        const wards = await response.json();

        xaSelect.disabled = false;
        wards.forEach((xa) => {
          xaSelect.innerHTML += `<option value="${xa.id}">${xa.ten}</option>`;
        });

        document.getElementById("editDonViHanhChinhId").value = this.value;
      } else {
        xaSelect.disabled = true;
        document.getElementById("editDonViHanhChinhId").value =
          document.getElementById("editTinhThanh").value;
      }
    } catch (error) {
      console.error("Error loading wards:", error);
      showToast("Lỗi khi tải danh sách phường/xã", "error");
    }
  });

// Load animal types from API
async function loadLoaiVatNuoi() {
  try {
    const response = await fetch("/api/v1/loai-vat-nuoi");
    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Error loading animal types:", error);
    return [];
  }
}

// Add new animal row to form
function addVatNuoiRow(container, prefix, loaiVatNuoiOptions) {
  const index = container.children.length;
  const div = document.createElement("div");
  div.className = "flex gap-2 items-center";
  div.innerHTML = `
    <select
      name="${prefix}[${index}].loaiVatNuoi"
      class="flex-1 border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
      required
    >
      <option value="">Chọn loại vật nuôi</option>
      ${loaiVatNuoiOptions}
    </select>
    <input
      type="number"
      name="${prefix}[${index}].soLuong"
      placeholder="Số lượng"
      class="w-32 border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
      required
    />
    <button type="button" class="text-red-500 hover:text-red-700" onclick="removeVatNuoiRow(this)">
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
      </svg>
    </button>
  `;
  container.appendChild(div);
}

function removeVatNuoiRow(button) {
  button.closest(".flex").remove();
}

// Initialize animal type controls
async function initializeVatNuoiControls() {
  const animals = await loadLoaiVatNuoi();
  const options = animals
    .map((animal) => `<option value="${animal.id}">${animal.tenLoai}</option>`)
    .join("");

  // Add animal row handlers
  document.getElementById("addVatNuoiBtn")?.addEventListener("click", () => {
    addVatNuoiRow(
      document.getElementById("vatNuoiContainer"),
      "vatNuoi",
      options
    );
  });

  document
    .getElementById("editAddVatNuoiBtn")
    ?.addEventListener("click", () => {
      addVatNuoiRow(
        document.getElementById("editVatNuoiContainer"),
        "editVatNuoi",
        options
      );
    });

  // Populate initial row for add form
  const firstSelect = document.querySelector('[name="vatNuoi[0].loaiVatNuoi"]');
  if (firstSelect) {
    animals.forEach((animal) => {
      firstSelect.add(new Option(animal.tenLoai, animal.id));
    });
  }
}
