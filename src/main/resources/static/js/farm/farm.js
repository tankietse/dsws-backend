let currentPage = 1;
let pageSize = 10; // Default page size

// Ensure pageSize has a valid value
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
  // Check if all required elements exist
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

  // Sử dụng let thay vì const cho biến currentPage
  let pageNum = data.number + 1;
  const totalPages = data.totalPages;
  const totalItems = data.totalElements;
  const pageSize = data.size;
  const startItem = (pageNum - 1) * pageSize + 1;
  const endItem = Math.min(pageNum * pageSize, totalItems);

  startItemEl.textContent = startItem;
  endItemEl.textContent = endItem;
  totalItemsEl.textContent = totalItems;

  paginationContainer.innerHTML = "";

  let paginationHtml = "";

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
  let startPage = Math.max(1, pageNum - 2);
  let endPage = Math.min(totalPages, pageNum + 2);

  if (startPage > 1) {
    paginationHtml += `
      <a href="#" data-page="1" class="relative inline-flex items-center px-4 py-2 text-sm font-semibold text-gray-900 hover:bg-gray-50">1</a>`;
    if (startPage > 2) {
      paginationHtml += `
        <span class="relative inline-flex items-center px-4 py-2 text-sm font-semibold text-gray-700">...</span>`;
    }
  }

  for (let i = startPage; i <= endPage; i++) {
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

function loadTrangTraiData(page = 1, size = pageSize) {
  validatePageSize();
  const nameFilter = document.getElementById("nameFilter")?.value || "";
  const donViFilter = document.getElementById("donViFilter")?.value || "";
  const searchTerm = document.getElementById("searchInput")?.value || "";
  let url = `/api/v1/trang-trai/paged?page=${page - 1}&size=${size}`;

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
        row.innerHTML = `
                  <td class="border px-4 py-2">${
                    trangTrai.maTrangTrai || ""
                  }</td>
                  <td class="border px-4 py-2" data-field="tenTrangTrai" ondblclick="makeEditable(this)">${
                    trangTrai.tenTrangTrai || ""
                  }</td>
                  <td class="border px-4 py-2" data-field="tenChu" ondblclick="makeEditable(this)">${trangTrai.tenChu || ""}</td>
                  <td class="border px-4 py-2" data-field="soDienThoai" ondblclick="makeEditable(this)">
                    ${trangTrai.soDienThoai || ""}<br>
                    ${trangTrai.email || ""}
                  </td>
                  <td class="border px-4 py-2" data-field="diaChiDayDu" ondblclick="makeEditable(this)">${
                    trangTrai.diaChiDayDu || ""
                  }</td>
                  <td class="border px-4 py-2" data-field="dienTich" ondblclick="makeEditable(this)">${trangTrai.dienTich || ""}</td>
                  <td class="border px-4 py-2" data-field="tongDan" ondblclick="makeEditable(this)">${trangTrai.tongDan || ""}</td>
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
                    }" data-status="${trangTrai.trangThaiHoatDong}" onclick="toggleStatus(this)">
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

// Ensure elements exist before adding event listeners
document.addEventListener("DOMContentLoaded", function () {
  // Move all initialization code here
  loadDonViHanhChinh();
  loadTrangTraiData(currentPage, pageSize);

  // Add event listeners here
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
function loadTinhThanh() {
  fetch("/api/v1/don-vi-hanh-chinh/cap/tinh")
    .then((response) => response.json())
    .then((data) => {
      const select = document.getElementById("tinhThanh");
      select.innerHTML = '<option value="">Chọn Tỉnh/Thành phố</option>';
      data.forEach((tinh) => {
        select.innerHTML += `<option value="${tinh.id}">${tinh.ten}</option>`;
      });
    });
}

function loadQuanHuyen(tinhId) {
  const quanHuyenSelect = document.getElementById("quanHuyen");
  const phuongXaSelect = document.getElementById("phuongXa");

  quanHuyenSelect.innerHTML = '<option value="">Chọn Quận/Huyện</option>';
  phuongXaSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';

  if (!tinhId) {
    quanHuyenSelect.disabled = true;
    phuongXaSelect.disabled = true;
    return;
  }

  fetch(`/api/v1/don-vi-hanh-chinh/parent/${tinhId}`)
    .then((response) => response.json())
    .then((data) => {
      quanHuyenSelect.disabled = false;
      data.forEach((huyen) => {
        quanHuyenSelect.innerHTML += `<option value="${huyen.id}">${huyen.ten}</option>`;
      });
    });
}

function loadPhuongXa(huyenId) {
  const phuongXaSelect = document.getElementById("phuongXa");
  phuongXaSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';

  if (!huyenId) {
    phuongXaSelect.disabled = true;
    return;
  }

  fetch(`/api/v1/don-vi-hanh-chinh/parent/${huyenId}`)
    .then((response) => response.json())
    .then((data) => {
      phuongXaSelect.disabled = false;
      data.forEach((xa) => {
        phuongXaSelect.innerHTML += `<option value="${xa.id}">${xa.ten}</option>`;
      });
    });
}

// Add event listeners for cascading selects
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
    const field = element.getAttribute('data-field');
    const farmId = element.closest('tr').getAttribute('data-id');
    
    element.innerHTML = `
        <input type="text" 
               class="w-full p-1 border rounded"
               value="${currentValue}"
               onblur="updateField(this, '${field}', ${farmId})"
               onkeypress="handleEnterKey(event, this, '${field}', ${farmId})">
    `;
    element.querySelector('input').focus();
}

function handleEnterKey(event, element, field, farmId) {
    if (event.key === 'Enter') {
        updateField(element, field, farmId);
    }
}

function updateField(element, field, farmId) {
    const newValue = element.value;
    const parent = element.parentElement;
    
    fetch(`/api/v1/trang-trai/${farmId}`, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ [field]: newValue })
    })
    .then(response => response.json())
    .then(data => {
        parent.innerHTML = newValue;
        showToast('Cập nhật thành công!', 'success');
    })
    .catch(error => {
        showToast('Lỗi cập nhật!', 'error');
        parent.innerHTML = element.defaultValue;
    });
}

function toggleStatus(button) {
    const farmId = button.closest('tr').getAttribute('data-id');
    const currentStatus = button.getAttribute('data-status') === 'true';
    
    fetch(`/api/v1/trang-trai/${farmId}/toggle-status`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ trangThaiHoatDong: !currentStatus })
    })
    .then(response => response.json())
    .then(data => {
        updateStatusButton(button, data.trangThaiHoatDong);
        showToast('Cập nhật trạng thái thành công!', 'success');
    })
    .catch(error => showToast('Lỗi cập nhật trạng thái!', 'error'));
}

function updateStatusButton(button, status) {
    button.setAttribute('data-status', status);
    button.className = `px-3 py-1 rounded-full text-sm font-medium ${
        status ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
    }`;
    button.textContent = status ? 'Hoạt động' : 'Ngừng hoạt động';
}

function editFarm(button) {
    const farmId = button.closest('tr').getAttribute('data-id');
    fetch(`/api/v1/trang-trai/${farmId}`)
        .then(response => response.json())
        .then(data => {
            showEditModal(data);
        })
        .catch(error => showToast('Lỗi tải thông tin trang trại!', 'error'));
}

function showEditModal(farmData) {
    const modal = document.getElementById('editFarmModal');
    // Populate form with farm data
    // ...implement form population logic...
    modal.classList.remove('hidden');
}

function showToast(message, type) {
    // Implement toast notification logic
    console.log(`${type}: ${message}`);
}
