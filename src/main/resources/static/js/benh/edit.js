class CustomMultiSelect {
  constructor(container) {
    this.container = container;
    this.selectedOptions = new Set();
    this.options = [];
    this.selectedContainer = container.querySelector(".selected-options");
    this.optionsContainer = container.querySelector(".options-container");
    this.optionsList = container.querySelector(".options-list");
    this.searchInput = container.querySelector("#loaiVatNuoiSearch");
    this.hiddenSelect = container.querySelector("select");
    this.init();
  }

  init() {
    if (!this.selectedContainer || !this.optionsContainer) {
      console.error("Required elements not found in container");
      return;
    }

    // Add click handler to selected container
    this.selectedContainer.addEventListener("click", () => {
      this.optionsContainer.classList.toggle("hidden");
      if (!this.optionsContainer.classList.contains("hidden")) {
        this.searchInput?.focus();
      }
    });

    // Add search handler
    if (this.searchInput) {
      this.searchInput.addEventListener("input", (e) => {
        this.filterOptions(e.target.value);
      });
    }

    // Close on click outside
    document.addEventListener("click", (e) => {
      if (!this.container.contains(e.target)) {
        this.optionsContainer.classList.add("hidden");
      }
    });
  }

  createCustomSelect() {
    this.selectedContainer = this.container.querySelector(".selected-options");
    this.optionsContainer = this.container.querySelector(".options-container");
    this.optionsList = this.container.querySelector(".options-list");
    this.searchInput = this.container.querySelector("#loaiVatNuoiSearch");
  }

  addEventListeners() {
    this.customSelect.addEventListener("click", () => {
      this.dropdown.classList.toggle("show");
    });

    document.addEventListener("click", (e) => {
      if (!this.customSelect.contains(e.target)) {
        this.dropdown.classList.remove("show");
      }
    });
  }

  // Update setOptions to handle null checks
  setOptions(options) {
    if (!options) return;
    this.options = options;
    this.renderOptions();
  }

  renderOptions(options = this.options) {
    if (!this.optionsList || !options) return;

    this.optionsList.innerHTML = options
      .map(
        (option) => `
        <div class="option-item ${
          this.selectedOptions.has(option.id) ? "selected" : ""
        }"
             data-value="${option.id}">
          ${option.tenLoai}
        </div>
      `
      )
      .join("");

    // Add click handlers
    this.optionsList.querySelectorAll(".option-item").forEach((item) => {
      item.addEventListener("click", () => {
        const id = parseInt(item.dataset.value);
        const option = this.options.find((opt) => opt.id === id);
        if (this.selectedOptions.has(id)) {
          this.removeOption(id);
        } else {
          this.addOption(option);
        }
      });
    });
  }

  toggleOption(id, text) {
    const index = Array.from(this.selectedOptions).findIndex(
      (opt) => opt.id === id
    );
    if (index > -1) {
      this.selectedOptions.delete(id);
    } else {
      this.selectedOptions.add({ id, text });
    }
    this.updateSelectedItems();
    this.updateOriginalSelect();
  }

  updateSelectedItems() {
    this.selectedItemsContainer.innerHTML = "";
    this.selectedOptions.forEach((option) => {
      const item = document.createElement("div");
      item.classList.add("selected-item");
      item.textContent = option.text;
      this.selectedItemsContainer.appendChild(item);
    });
  }

  updateOriginalSelect() {
    if (!this.hiddenSelect) return;

    Array.from(this.hiddenSelect.options).forEach((option) => {
      const id = parseInt(option.value);
      option.selected = Array.from(this.selectedOptions).some(
        (opt) => opt.id === id
      );
    });
  }

  updateFromSelect() {
    if (!this.hiddenSelect || !this.options.length) return;

    // Clear existing selections
    this.selectedOptions.clear();

    // Get all selected values from the hidden select
    Array.from(this.hiddenSelect.selectedOptions).forEach((option) => {
      const id = parseInt(option.value);
      const opt = this.options.find((o) => o.id === id);
      if (opt) {
        this.addOption(opt);
      }
    });
  }

  addOption(option) {
    if (!option) return;
    this.selectedOptions.add(option);
    this.renderSelected();
    this.updateOriginalSelect();
    this.renderOptions();
  }

  removeOption(id) {
    this.selectedOptions.delete(
      [...this.selectedOptions].find((opt) => opt.id === id)
    );
    this.renderSelected();
    this.updateOriginalSelect();
    this.renderOptions();
  }

  renderSelected() {
    if (!this.selectedContainer) return;

    this.selectedContainer.innerHTML = Array.from(this.selectedOptions)
      .map(
        (option) => `
            <span class="selected-item">
                ${option.tenLoai}
                <span class="remove-item" data-value="${option.id}">×</span>
            </span>
        `
      )
      .join("");

    // Add remove handlers
    this.selectedContainer.querySelectorAll(".remove-item").forEach((item) => {
      item.addEventListener("click", (e) => {
        e.stopPropagation();
        this.removeOption(parseInt(item.dataset.value));
      });
    });
  }

  // Add a method to check if an option is selected
  isSelected(id) {
    return Array.from(this.selectedOptions).some((opt) => opt.id === id);
  }
}

document.addEventListener("DOMContentLoaded", async function () {
  const benhId = window.location.pathname.split("/").pop();
  try {
    // First load benh data
    const benhData = await loadBenhData(benhId);
    // Then load loai vat nuoi with the benh data
    await loadLoaiVatNuoi(benhData);
    // Finally setup form
    setupForm(benhId);
  } catch (error) {
    console.error("Initialization error:", error);
    showError("Có lỗi khi khởi tạo form");
  }
});

async function loadBenhData(id) {
  try {
    const response = await fetch(`/api/v1/benh/${id}`, {
      headers: {
        Accept: "application/json",
      },
      credentials: "include",
    });

    if (!response.ok) {
      throw new Error("Failed to fetch benh data");
    }

    const benh = await response.json();
    return benh; // Return the data instead of populating form directly
  } catch (error) {
    console.error("Error loading benh:", error);
    showError("Không thể tải thông tin bệnh");
    throw error;
  }
}

// Update loadLoaiVatNuoi to check for container
async function loadLoaiVatNuoi(benhData) {
  try {
    const response = await fetch("/api/v1/loai-vat-nuoi");
    const data = await response.json();

    const select = document.getElementById("loaiVatNuoi");
    if (!select) {
      throw new Error("Select element not found");
    }

    select.innerHTML = ""; // Clear existing options

    // Add new options
    data.forEach((loai) => {
      const option = document.createElement("option");
      option.value = loai.id;
      option.textContent = loai.tenLoai;
      if (benhData.loaiVatNuoiIds?.includes(loai.id)) {
        option.selected = true;
      }
      select.appendChild(option);
    });

    const container = document.querySelector(".custom-multiselect");
    if (!container) {
      throw new Error("Multiselect container not found");
    }

    // Initialize custom multi-select
    window.customMultiSelect = new CustomMultiSelect(container);
    window.customMultiSelect.setOptions(data);

    // Add selected options
    if (benhData.loaiVatNuoiIds) {
      benhData.loaiVatNuoiIds.forEach((id) => {
        const loai = data.find((l) => l.id === id);
        if (loai) {
          window.customMultiSelect.addOption(loai);
        }
      });
    }

    // Now populate the rest of the form
    populateForm(benhData);
  } catch (error) {
    console.error("Error loading loai vat nuoi:", error);
    showError("Không thể tải danh sách loại vật nuôi: " + error.message);
    throw error;
  }
}

function populateForm(benh) {
  // Basic fields
  document.getElementById("tenBenh").value = benh.tenBenh || "";
  document.getElementById("moTa").value = benh.moTa || "";
  document.getElementById("tacNhanGayBenh").value = benh.tacNhanGayBenh || "";
  document.getElementById("trieuChung").value = benh.trieuChung || "";
  document.getElementById("thoiGianUBenh").value = benh.thoiGianUBenh || "";
  document.getElementById("phuongPhapChanDoan").value =
    benh.phuongPhapChanDoan || "";
  document.getElementById("bienPhapPhongNgua").value =
    benh.bienPhapPhongNgua || "";

  // Set mucDoBenh radio button
  if (benh.mucDoBenhs?.length > 0) {
    const mucDoRadio = document.querySelector(
      `input[name="mucDoBenhs"][value="${benh.mucDoBenhs[0]}"]`
    );
    if (mucDoRadio) {
      mucDoRadio.checked = true;
      mucDoRadio.dispatchEvent(new Event("change"));
    }
  }

  // Set checkboxes with null check
  document.getElementById("canCongBoDich").checked =
    benh.canCongBoDich || false;
  document.getElementById("canPhongBenhBatBuoc").checked =
    benh.canPhongBenhBatBuoc || false;
}

// Add validateForm function before setupForm
function validateForm() {
  const requiredFields = {
    tenBenh: "Tên bệnh",
    loaiVatNuoi: "Loại vật nuôi",
  };

  // Validate radio buttons for mucDoBenhs
  const mucDoBenh = document.querySelector('input[name="mucDoBenhs"]:checked');
  if (!mucDoBenh) {
    showError("Vui lòng chọn mức độ bệnh");
    return false;
  }

  // Validate other required fields
  for (const [fieldId, fieldName] of Object.entries(requiredFields)) {
    const field = document.getElementById(fieldId);
    if (
      !field ||
      !field.value ||
      (field.multiple && field.selectedOptions.length === 0)
    ) {
      showError(`Vui lòng nhập ${fieldName}`);
      return false;
    }
  }
  return true;
}

function setupForm(benhId) {
  const form = document.getElementById("editBenhForm");

  // Add console log to verify handler attachment
  console.log("Setting up form submit handler for editBenhForm");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    // Add console log to verify event trigger
    console.log("Form submit event triggered");

    if (!validateForm()) {
      console.log("Form validation failed");
      return;
    }

    const formData = {
      id: parseInt(benhId),
      tenBenh: document.getElementById("tenBenh").value.trim(),
      moTa: document.getElementById("moTa").value.trim() || "",
      mucDoBenhs: [
        document.querySelector('input[name="mucDoBenhs"]:checked').value,
      ],
      loaiVatNuoiIds: Array.from(
        document.getElementById("loaiVatNuoi").selectedOptions
      ).map((option) => parseInt(option.value)),
      tacNhanGayBenh:
        document.getElementById("tacNhanGayBenh").value.trim() || "",
      trieuChung: document.getElementById("trieuChung").value.trim() || "",
      thoiGianUBenh:
        parseInt(document.getElementById("thoiGianUBenh").value) || 0,
      phuongPhapChanDoan:
        document.getElementById("phuongPhapChanDoan").value.trim() || "",
      bienPhapPhongNgua:
        document.getElementById("bienPhapPhongNgua").value.trim() || "",
      canCongBoDich: document.getElementById("canCongBoDich").checked,
      canPhongBenhBatBuoc: document.getElementById("canPhongBenhBatBuoc")
        .checked,
    };

    console.log("Sending update request:", formData); // Debug log

    try {
      const response = await fetch(`/api/v1/benh/${benhId}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
        credentials: "include",
        body: JSON.stringify(formData),
      });

      console.log("Response status:", response.status); // Debug log

      if (!response.ok) {
        const errorData = await response.json();
        console.error("Error response:", errorData); // Debug log
        throw new Error(errorData.message || "Lỗi khi cập nhật bệnh");
      }

      const result = await response.json();
      console.log("Update successful:", result); // Debug log

      showSuccess("Cập nhật bệnh thành công");
      setTimeout(() => {
        window.location.href = "/benh";
      }, 1500);
    } catch (error) {
      console.error("Error updating benh:", error);
      showError(error.message || "Có lỗi xảy ra khi cập nhật bệnh");
    }
  });
}

// Reuse the existing loadLoaiVatNuoi, validateForm, showError, and showSuccess functions from create.js

function showError(message) {
  if (typeof Swal !== "undefined") {
    Swal.fire({
      icon: "error",
      title: "Lỗi",
      text: message,
      confirmButtonText: "Đóng",
      confirmButtonColor: "#3085d6",
    });
  } else {
    alert(message);
  }
}

function showSuccess(message) {
  if (typeof Swal !== "undefined") {
    Swal.fire({
      icon: "success",
      title: "Thành công",
      text: message,
      timer: 1500,
    });
  } else {
    alert(message);
  }
}
