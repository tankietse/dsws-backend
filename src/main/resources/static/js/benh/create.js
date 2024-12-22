document.addEventListener("DOMContentLoaded", function () {
  loadLoaiVatNuoi();
  setupForm();
});

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
    // Toggle options container
    this.selectedContainer.addEventListener("click", () => {
      this.toggleOptionsContainer();
    });

    // Handle search
    this.searchInput.addEventListener("input", (e) => {
      this.filterOptions(e.target.value);
    });

    // Close on click outside
    document.addEventListener("click", (e) => {
      if (!this.container.contains(e.target)) {
        this.optionsContainer.classList.add("hidden");
      }
    });
  }

  setOptions(options) {
    this.options = options;
    this.renderOptions();
  }

  toggleOptionsContainer() {
    this.optionsContainer.classList.toggle("hidden");
    if (!this.optionsContainer.classList.contains("hidden")) {
      this.searchInput.focus();
    }
  }

  filterOptions(searchTerm) {
    const filtered = this.options.filter((option) =>
      option.tenLoai.toLowerCase().includes(searchTerm.toLowerCase())
    );
    this.renderOptions(filtered);
  }

  renderOptions(options = this.options) {
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

  addOption(option) {
    this.selectedOptions.add(option.id);
    this.renderSelected();
    this.updateHiddenSelect();
    this.renderOptions();
  }

  removeOption(id) {
    this.selectedOptions.delete(id);
    this.renderSelected();
    this.updateHiddenSelect();
    this.renderOptions();
  }

  renderSelected() {
    this.selectedContainer.innerHTML = Array.from(this.selectedOptions)
      .map((id) => {
        const option = this.options.find((opt) => opt.id === id);
        return `
                    <span class="selected-item">
                        ${option.tenLoai}
                        <span class="remove-item" data-value="${option.id}">×</span>
                    </span>
                `;
      })
      .join("");

    // Add remove handlers
    this.selectedContainer.querySelectorAll(".remove-item").forEach((item) => {
      item.addEventListener("click", (e) => {
        e.stopPropagation();
        this.removeOption(parseInt(item.dataset.value));
      });
    });
  }

  updateHiddenSelect() {
    // Update the hidden select element for form submission
    Array.from(this.hiddenSelect.options).forEach((option) => {
      option.selected = this.selectedOptions.has(parseInt(option.value));
    });
  }
}

async function loadLoaiVatNuoi() {
  try {
    const response = await fetch("/api/v1/loai-vat-nuoi");
    const data = await response.json();

    const select = document.getElementById("loaiVatNuoi");
    // Clear existing options
    select.innerHTML = '';
    
    // Add new options
    data.forEach((loai) => {
      const option = document.createElement("option");
      option.value = loai.id;
      option.textContent = loai.tenLoai;
      select.appendChild(option);
    });

    // Initialize custom multi-select and store it globally
    window.customMultiSelect = new CustomMultiSelect(
      document.querySelector(".custom-multiselect")
    );
    window.customMultiSelect.setOptions(data);
    
    return data;
  } catch (error) {
    console.error("Error loading loai vat nuoi:", error);
    showError("Không thể tải danh sách loại vật nuôi");
  }
}

function setupForm() {
  const form = document.getElementById("createBenhForm");

  // Add event listeners for severity selection
  const severityOptions = document.querySelectorAll('input[name="mucDoBenhs"]');
  severityOptions.forEach((option) => {
    option.addEventListener("change", (e) => {
      // Remove selected class from all containers
      document.querySelectorAll(".severity-content").forEach((cont) => {
        cont.classList.remove("selected");
      });
      // Add selected class to chosen container
      e.target.parentElement
        .querySelector(".severity-content")
        .classList.add("selected");
    });
  });

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    const selectedMucDo = document.querySelector(
      'input[name="mucDoBenhs"]:checked'
    );
    if (!selectedMucDo) {
      showError("Vui lòng chọn mức độ bệnh");
      return;
    }

    const formData = {
      id: 0,
      tenBenh: document.getElementById("tenBenh").value.trim(),
      moTa: document.getElementById("moTa").value.trim() || "",
      mucDoBenhs: [selectedMucDo.value], // Make sure this is an array
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

    console.log("Submitting form data:", formData); // Debug log

    try {
      const response = await fetch("/api/v1/benh", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
        credentials: "include",
        body: JSON.stringify(formData),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Lỗi khi thêm bệnh");
      }

      const result = await response.json();
      showSuccess("Thêm bệnh thành công");
      setTimeout(() => {
        window.location.href = "/benh";
      }, 1500);
    } catch (error) {
      console.error("Error submitting form:", error);
      showError(error.message || "Có lỗi xảy ra khi thêm bệnh");
    }
  });
}

function validateForm() {
  const requiredFields = {
    tenBenh: "Tên bệnh",
    loaiVatNuoi: "Loại vật nuôi",
  };

  // Separate validation for radio buttons
  const mucDoBenh = document.querySelector('input[name="mucDoBenhs"]:checked');
  if (!mucDoBenh) {
    showError("Vui lòng chọn mức độ bệnh");
    return false;
  }

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

// Update notifications to use SweetAlert2
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
    alert(message); // Fallback if SweetAlert2 is not loaded
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
    alert(message); // Fallback if SweetAlert2 is not loaded
  }
}
