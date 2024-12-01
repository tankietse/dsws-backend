var ETogglePassword = document.querySelector("#toggle-password");
var EPassword = document.querySelector("#password");
var ELogout = document.querySelector("#dashboard-logout");
var EProfile = document.querySelector("#dashboard-profile");
var EProfileContent = document.querySelector("#profile-content");
var EProfileName = document.querySelector("#profile-name");
var EProfilePosition = document.querySelector("#profile-job");
var EAddEmployeeBtn = document.querySelector("#info-add-employee");
var ESettingMenuBtn = document.querySelector("#setting-menu-btn");
var ESettingMenuDropdown = document.querySelector("#setting-menu-dropdown");


$(document).ready(function () {
    let employeeName = decodeURIComponent(getUserCookie('EmployeeName')).replace(/\+/g, ' ');
    let positionName = decodeURIComponent(getUserCookie('PositionName')).replace(/\+/g, ' ');

    EProfileName.textContent = employeeName;
    EProfilePosition.textContent = positionName;
});

//$(document).ready(function () {
//    const idnv = getUserCookie('IDNV');
//    const url = `/Info/Display?id=${idnv}`;
//    EProfile?.addEventListener("click", function () {
//        window.location.href = url;
//    })
//    EProfileContent?.addEventListener("click", function () {
//        window.location.href = url;
//    })
//});

$(document).ready(function () {
    const idnv = getUserCookie('IDNV');
    const url = `/profile`;

    EProfile?.addEventListener("click", function () {
        window.location.href = url;
    });

    EProfileContent?.addEventListener("click", function () {
        window.location.href = url;
    });
});

ETogglePassword?.addEventListener("click", function () {
    console.log("click");
    if (EPassword.type == "text") {
        EPassword.type = "password"; 
    } else {
        EPassword.type = "text";
    }
});

ESettingMenuBtn?.addEventListener("click", function () {
    if (ESettingMenuDropdown.classList.contains("show")) {
        ESettingMenuDropdown.classList.remove("show");
        ESettingMenuDropdown.classList.add("hide");
    } else {
        ESettingMenuDropdown.classList.remove("hide");
        ESettingMenuDropdown.classList.add("show");
    }
});

EAddEmployeeBtn?.addEventListener("click", function () {
    window.location.href = "/Info/Add";
});

ELogout?.addEventListener("click", function () {
    document.cookie = "UserId=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    document.cookie = "UserName=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    document.cookie = "IDNV=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    document.cookie = "EmployeeName=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    document.cookie = "PositionName=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    window.location.href = "/OAuth/Login";
});

function getUserCookie(cookieName) {
    const cookies = document.cookie.split(';');
    for (let i = 0; i < cookies.length; i++) {
        const cookie = cookies[i].trim();
        if (cookie?.startsWith(cookieName + '=')) {
            return cookie?.substring(cookieName.length + 1);
        }
    }
    return null;
}