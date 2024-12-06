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

  // Sử dụng var thay vì const cho biến currentPage
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

// Add row click handler when loading data
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
        throw new Error(`Server error: ${response.status}`);
      }
      return response.json();
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
                        ?.map((vn) => vn.loaiVatNuoi?.tenLoai)
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
    .catch((error) => console.error("Error:", error));
}

// Function to load and show edit form
async function loadEditForm(farmId) {
  try {
    const response = await fetch(`/api/v1/trang-trai/${farmId}`);
    if (!response.ok) throw new Error("Farm not found");
    const farm = await response.json();

    // Populate form fields
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

    // Load and set administrative units
    await loadAdminUnitsForEdit(farm.donViHanhChinh);

    // Show modal
    document.getElementById("editFarmModal").classList.remove("hidden");
  } catch (error) {
    console.error("Error loading farm data:", error);
    showToast("Lỗi khi tải thông tin trang trại", "error");
  }
}

// Enhanced update function with validation
document
  .getElementById("editFarmForm")
  .addEventListener("submit", async function (e) {
    e.preventDefault();

    const formData = new FormData(this);
    const farmId = formData.get("id");

    try {
      // Basic validation
      if (!formData.get("tenTrangTrai").trim()) {
        throw new Error("Tên trang trại không được để trống");
      }

      const response = await fetch(`/api/v1/trang-trai/${farmId}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(Object.fromEntries(formData)),
      });

      if (!response.ok) throw new Error("Update failed");

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
      ?.map((vn) => vn.loaiVatNuoi?.tenLoai)
      .join(", ");

    // Update status button
    const statusBtn = cells[8].querySelector("button");
    updateStatusButton(statusBtn, farm.trangThaiHoatDong);
  }
}

document.addEventListener("DOMContentLoaded", function () {
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
      // Populate other fields...

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
  if (!donViHanhChinh) return;

  try {
    // First load all provinces
    await loadTinhThanh("edit");

    // Get the compvare hierarchy
    const hierarchy = await fetchAdminHierarchy(donViHanhChinh);

    // Set values and enable/disable selects appropriately
    const tinhSelect = document.getElementById("editTinhThanh");
    const huyenSelect = document.getElementById("editQuanHuyen");
    const xaSelect = document.getElementById("editPhuongXa");

    if (hierarchy.province) {
      tinhSelect.value = hierarchy.province.id;
      await loadQuanHuyen(hierarchy.province.id, "edit");
      huyenSelect.disabled = false;

      if (hierarchy.district) {
        huyenSelect.value = hierarchy.district.id;
        await loadPhuongXa(hierarchy.district.id, "edit");
        xaSelect.disabled = false;

        if (hierarchy.ward) {
          xaSelect.value = hierarchy.ward.id;
          document.getElementById("editDonViHanhChinhId").value =
            hierarchy.ward.id;
        } else {
          document.getElementById("editDonViHanhChinhId").value =
            hierarchy.district.id;
        }
      } else {
        document.getElementById("editDonViHanhChinhId").value =
          hierarchy.province.id;
      }
    }
  } catch (error) {
    console.error("Error in loadAdminUnitsForEdit:", error);
    showToast("Lỗi khi tải thông tin địa chỉ", "error");
  }
}

async function fetchAdminHierarchy(currentUnit) {
  const hierarchy = {
    ward: null,
    district: null,
    province: null,
  };

  try {
    var current = currentUnit;

    // Determine the current level and set appropriate hierarchy
    switch (current.capHanhChinh) {
      case "8": // Ward level
        hierarchy.ward = current;
        current = await fetchParentUnit(current.donViCha.id);
      // falls through
      case "6": // District level
        hierarchy.district = hierarchy.district || current;
        current = await fetchParentUnit(current.donViCha.id);
      // falls through
      case "4": // Province level
        hierarchy.province = hierarchy.province || current;
        break;
    }

    return hierarchy;
  } catch (error) {
    console.error("Error fetching hierarchy:", error);
    throw error;
  }
}

async function fetchParentUnit(parentId) {
  if (!parentId) return null;
  const response = await fetch(`/api/v1/don-vi-hanh-chinh/${parentId}`);
  if (!response.ok) throw new Error(`Failed to fetch unit ${parentId}`);
  return await response.json();
}
