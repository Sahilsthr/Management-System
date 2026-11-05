// ========================================
// SITES MANAGEMENT
// ========================================

// Authentication
const token = sessionStorage.getItem("fmsToken");

const adminName = document.getElementById("adminName");
const logoutButton = document.getElementById("logoutButton");

// Page elements
const addSiteButton = document.getElementById("addSiteButton");
const searchSites = document.getElementById("searchSites");
const sitesTableBody = document.getElementById("sitesTableBody");
const sitesMessage = document.getElementById("sitesMessage");

// Statistics
const totalSitesElement = document.getElementById("totalSites");
const sitesWithCoordinatesElement =
    document.getElementById("sitesWithCoordinates");
const projectsWithoutSitesElement =
    document.getElementById("projectsWithoutSites");

// Modal elements
const siteDialog = document.getElementById("siteDialog");
const closeSiteModal = document.getElementById("closeSiteModal");
const cancelSite = document.getElementById("cancelSite");
const addSiteForm = document.getElementById("addSiteForm");

// Form elements
const siteProject = document.getElementById("siteProject");
const siteAddress = document.getElementById("siteAddress");
const siteCity = document.getElementById("siteCity");
const siteState = document.getElementById("siteState");
const sitePincode = document.getElementById("sitePincode");
const siteLatitude = document.getElementById("siteLatitude");
const siteLongitude = document.getElementById("siteLongitude");
const siteFormMessage = document.getElementById("siteFormMessage");


// ========================================
// DATA
// ========================================

let allSites = [];
let allProjects = [];
let eligibleProjects = [];


// ========================================
// API HELPER
// ========================================

async function apiRequest(url, options = {}) {

    const response = await fetch(url, {
        ...options,

        headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
            ...(options.headers || {})
        }
    });

    const responseText = await response.text();

    let data = null;

    if (responseText) {
        try {
            data = JSON.parse(responseText);
        } catch {
            data = null;
        }
    }

    if (!response.ok) {

        const errorMessage =
            data?.message ||
            data?.detail ||
            data?.title ||
            `Request failed with status ${response.status}`;

        throw new Error(errorMessage);
    }

    return data;
}


// ========================================
// ADMIN VERIFICATION
// ========================================

async function verifyAdmin() {

    if (!token) {
        window.location.href = "/login.html";
        return false;
    }

    try {

        const user = await apiRequest("/api/auth/me");

        if (user.role !== "ADMIN") {
            alert("Access denied. Admin account required.");
            sessionStorage.removeItem("fmsToken");
            sessionStorage.removeItem("fmsUser");
            window.location.href = "/login.html";
            return false;
        }

        adminName.textContent =
            user.fullName ||
            user.name ||
            user.email ||
            "Admin";

        return true;

    } catch (error) {

        console.error("Authentication error:", error);

        sessionStorage.removeItem("fmsToken");
        sessionStorage.removeItem("fmsUser");

        window.location.href = "/login.html";

        return false;
    }
}


// ========================================
// MESSAGE HELPER
// ========================================

function showMessage(element, message, type = "") {

    element.textContent = message;
    element.className = element === sitesMessage
        ? `sites-message ${type}`
        : `form-message ${type}`;
}


// ========================================
// LOAD ALL DATA
// ========================================

async function loadSiteData() {

    showMessage(sitesMessage, "Loading sites...");

    try {

        const [sites, projects] = await Promise.all([
            apiRequest("/api/sites"),
            apiRequest("/api/projects")
        ]);

        allSites = Array.isArray(sites) ? sites : [];
        allProjects = Array.isArray(projects) ? projects : [];

        renderProjectDropdown();
        renderSites(allSites);
        updateStatistics();

        showMessage(
            sitesMessage,
            `${allSites.length} site(s) loaded successfully.`,
            "success"
        );

    } catch (error) {

        console.error("Error loading site data:", error);

        sitesTableBody.replaceChildren();

        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 7;
        cell.className = "table-placeholder";
        cell.textContent = "Unable to load site information.";

        row.appendChild(cell);
        sitesTableBody.appendChild(row);

        showMessage(sitesMessage, error.message, "error");
    }
}


// ========================================
// PROJECT DROPDOWN
// ========================================

function renderProjectDropdown() {

    siteProject.replaceChildren();

    const defaultOption = document.createElement("option");

    defaultOption.value = "";
    defaultOption.textContent = "Select a project";

    siteProject.appendChild(defaultOption);

    // Projects that already have a site
    const existingProjectIds = new Set(
        allSites.map(site => String(site.project?.id))
    );

    // Only projects without a site can be selected
    eligibleProjects = allProjects.filter(project =>
        !existingProjectIds.has(String(project.id))
    );

    eligibleProjects.forEach(project => {

        const option = document.createElement("option");

        option.value = project.id;

        option.textContent =
            `${project.projectName} (#${project.id})`;

        siteProject.appendChild(option);
    });

    if (eligibleProjects.length === 0) {

        defaultOption.textContent =
            allProjects.length === 0
                ? "No projects available"
                : "All projects already have sites";

    }

}


// ========================================
// UPDATE STATISTICS
// ========================================

function updateStatistics() {

    totalSitesElement.textContent = allSites.length;

    const sitesWithCoordinates = allSites.filter(site => {

        return site.latitude !== null &&
            site.latitude !== undefined &&
            site.longitude !== null &&
            site.longitude !== undefined;

    });

    sitesWithCoordinatesElement.textContent =
        sitesWithCoordinates.length;

    const projectIdsWithSites = new Set(
        allSites.map(site => String(site.project?.id))
    );

    const projectsWithoutSites = allProjects.filter(project =>
        !projectIdsWithSites.has(String(project.id))
    );

    projectsWithoutSitesElement.textContent =
        projectsWithoutSites.length;
}


// ========================================
// RENDER SITES TABLE
// ========================================

function createCell(value) {

    const cell = document.createElement("td");

    cell.textContent =
        value === null || value === undefined || value === ""
            ? "—"
            : String(value);

    return cell;
}


function renderSites(sites) {

    sitesTableBody.replaceChildren();

    if (sites.length === 0) {

        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 7;
        cell.className = "table-placeholder";
        cell.textContent = "No sites found.";

        row.appendChild(cell);
        sitesTableBody.appendChild(row);

        return;
    }

    sites.forEach(site => {

        const row = document.createElement("tr");

        const projectName =
            site.project?.projectName || "Unknown Project";

        const projectId =
            site.project?.id || "—";

        const projectLabel =
            `${projectName} (#${projectId})`;

        // Check GPS coordinates
        const hasCoordinates =
            site.latitude !== null &&
            site.latitude !== undefined &&
            site.longitude !== null &&
            site.longitude !== undefined;

        // Site ID
        row.appendChild(createCell(`#${site.id}`));

        // Project
        row.appendChild(createCell(projectLabel));

        // Address
        row.appendChild(createCell(site.address));

        // City
        row.appendChild(createCell(site.city));

        // State
        row.appendChild(createCell(site.state));

        // Pincode
        row.appendChild(createCell(site.pincode));

        // GPS Location status
        const locationCell = document.createElement("td");

        const locationBadge = document.createElement("span");

        locationBadge.className = hasCoordinates
            ? "location-status"
            : "location-status location-missing";

        locationBadge.textContent = hasCoordinates
            ? "GPS Added"
            : "Not Added";

        locationCell.appendChild(locationBadge);
        row.appendChild(locationCell);

        sitesTableBody.appendChild(row);
    });
}


// ========================================
// SEARCH SITES
// ========================================

function searchSiteRecords() {

    const query = searchSites.value
        .trim()
        .toLowerCase();

    const filteredSites = allSites.filter(site => {

        const projectName =
            site.project?.projectName || "";

        const searchableText = [
            site.id,
            projectName,
            site.project?.id,
            site.address,
            site.city,
            site.state,
            site.pincode
        ].join(" ").toLowerCase();

        return searchableText.includes(query);
    });

    renderSites(filteredSites);
}


// ========================================
// OPEN MODAL
// ========================================

function openSiteModal() {

    addSiteForm.reset();

    showMessage(siteFormMessage, "");

    siteDialog.classList.add("show");
    siteDialog.setAttribute("aria-hidden", "false");

    if (eligibleProjects.length === 0) {

        showMessage(
            siteFormMessage,
            "All existing projects already have a site, or no projects are available.",
            "error"
        );

    }
}


// ========================================
// CLOSE MODAL
// ========================================

function closeModal() {

    siteDialog.classList.remove("show");
    siteDialog.setAttribute("aria-hidden", "true");

    addSiteForm.reset();

    showMessage(siteFormMessage, "");
}


// ========================================
// CREATE NEW SITE
// ========================================

async function createSite(event) {

    event.preventDefault();

    if (!addSiteForm.reportValidity()) {
        return;
    }

    if (eligibleProjects.length === 0) {

        showMessage(
            siteFormMessage,
            "There are no projects available for a new site.",
            "error"
        );

        return;
    }

    const latitudeValue = siteLatitude.value.trim();
    const longitudeValue = siteLongitude.value.trim();

    const latitude = latitudeValue === ""
        ? null
        : Number(latitudeValue);

    const longitude = longitudeValue === ""
        ? null
        : Number(longitudeValue);

    // Coordinate validation
    if (latitude !== null &&
        (!Number.isFinite(latitude) ||
            latitude < -90 ||
            latitude > 90)) {

        showMessage(
            siteFormMessage,
            "Latitude must be between -90 and 90.",
            "error"
        );

        return;
    }

    if (longitude !== null &&
        (!Number.isFinite(longitude) ||
            longitude < -180 ||
            longitude > 180)) {

        showMessage(
            siteFormMessage,
            "Longitude must be between -180 and 180.",
            "error"
        );

        return;
    }

    // Both coordinates should be entered together
    if ((latitude === null) !== (longitude === null)) {

        showMessage(
            siteFormMessage,
            "Please enter both latitude and longitude, or leave both empty.",
            "error"
        );

        return;
    }

    const projectId = Number(siteProject.value);

    const payload = {

        address: siteAddress.value.trim(),

        city: siteCity.value.trim(),

        state: siteState.value.trim(),

        pincode: sitePincode.value.trim(),

        latitude: latitude,

        longitude: longitude,

        projectId: projectId

    };

    const saveButton = addSiteForm.querySelector(
        ".save-site-button"
    );

    saveButton.disabled = true;
    saveButton.textContent = "Saving...";

    showMessage(siteFormMessage, "Saving site...");

    try {

        await apiRequest("/api/sites", {

            method: "POST",

            body: JSON.stringify(payload)

        });

        closeModal();

        // Refresh table and statistics
        await loadSiteData();

        showMessage(
            sitesMessage,
            "New site created successfully.",
            "success"
        );

    } catch (error) {

        console.error("Error creating site:", error);

        showMessage(
            siteFormMessage,
            error.message || "Unable to create site.",
            "error"
        );

    } finally {

        saveButton.disabled = false;
        saveButton.textContent = "Save Site";

    }
}


// ========================================
// LOGOUT
// ========================================

function logout() {

    sessionStorage.removeItem("fmsToken");
    sessionStorage.removeItem("fmsUser");

    window.location.href = "/login.html";
}


// ========================================
// EVENT LISTENERS
// ========================================

addSiteButton.addEventListener("click", openSiteModal);

closeSiteModal.addEventListener("click", closeModal);

cancelSite.addEventListener("click", closeModal);

logoutButton.addEventListener("click", logout);

searchSites.addEventListener("input", searchSiteRecords);

addSiteForm.addEventListener("submit", createSite);


// Close modal when clicking outside
siteDialog.addEventListener("click", function(event) {

    if (event.target === siteDialog) {
        closeModal();
    }

});


// Close modal using Escape key
document.addEventListener("keydown", function(event) {

    if (event.key === "Escape" &&
        siteDialog.classList.contains("show")) {

        closeModal();
    }

});


// ========================================
// INITIALIZE PAGE
// ========================================

async function initializeSitesPage() {

    const isAdmin = await verifyAdmin();

    if (!isAdmin) {
        return;
    }

    await loadSiteData();

}

initializeSitesPage();