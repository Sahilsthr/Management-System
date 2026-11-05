// ========================================
// EMPLOYEES MANAGEMENT
// ========================================

// Authentication
const token = sessionStorage.getItem("fmsToken");

const adminName = document.getElementById("adminName");
const logoutButton = document.getElementById("logoutButton");

// Page elements
const addEmployeeButton = document.getElementById("addEmployeeButton");
const searchEmployees = document.getElementById("searchEmployees");
const employeesTableBody = document.getElementById("employeesTableBody");
const employeesMessage = document.getElementById("employeesMessage");

// Statistics
const totalEmployeesElement = document.getElementById("totalEmployees");
const employeeAccountsElement = document.getElementById("employeeAccounts");
const pendingProfilesElement = document.getElementById("pendingProfiles");

// Modal elements
const employeeDialog = document.getElementById("employeeDialog");
const closeEmployeeModal = document.getElementById("closeEmployeeModal");
const cancelEmployee = document.getElementById("cancelEmployee");
const addEmployeeForm = document.getElementById("addEmployeeForm");

// Form elements
const employeeName = document.getElementById("employeeName");
const employeeEmail = document.getElementById("employeeEmail");
const employeePhone = document.getElementById("employeePhone");
const employeePassword = document.getElementById("employeePassword");
const employeeCode = document.getElementById("employeeCode");
const employeeDesignation = document.getElementById("employeeDesignation");
const employeeJoiningDate = document.getElementById("employeeJoiningDate");
const employeeFormMessage = document.getElementById("employeeFormMessage");

// Submit button
const saveEmployeeButton = addEmployeeForm.querySelector(
    ".save-employee-button"
);


// ========================================
// DATA
// ========================================

let allEmployees = [];
let allUsers = [];

// If user account creation succeeds but employee profile creation fails,
// keep this ID in memory so the next submission retries only the profile.
let pendingEmployeeUserId = null;


// ========================================
// API HELPER
// ========================================

async function apiRequest(url, options = {}) {

    const response = await fetch(url, {
        ...options,

        headers: {
            Authorization: `Bearer ${token}`,
            ...(options.body ? {
                "Content-Type": "application/json"
            } : {}),
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

    element.className =
        element === employeesMessage
            ? `employees-message ${type}`
            : `form-message ${type}`;
}


// ========================================
// DATE FORMATTER
// ========================================

function formatDate(dateValue) {

    if (!dateValue) {
        return "—";
    }

    const parts = String(dateValue).split("-");

    if (parts.length !== 3) {
        return dateValue;
    }

    return `${parts[2]}-${parts[1]}-${parts[0]}`;
}


// ========================================
// LOAD EMPLOYEES AND USER ACCOUNTS
// ========================================

async function loadEmployeeData() {

    showMessage(employeesMessage, "Loading employees...");

    try {

        const [employees, users] = await Promise.all([

            apiRequest("/api/employees"),
            apiRequest("/api/users")

        ]);

        allEmployees = Array.isArray(employees)
            ? employees
            : [];

        allUsers = Array.isArray(users)
            ? users
            : [];

        renderEmployees(allEmployees);

        updateStatistics();

        showMessage(
            employeesMessage,
            `${allEmployees.length} employee(s) loaded successfully.`,
            "success"
        );

    } catch (error) {

        console.error("Error loading employees:", error);

        employeesTableBody.replaceChildren();

        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 6;
        cell.className = "table-placeholder";
        cell.textContent = "Unable to load employee information.";

        row.appendChild(cell);
        employeesTableBody.appendChild(row);

        showMessage(
            employeesMessage,
            error.message,
            "error"
        );
    }
}


// ========================================
// UPDATE STATISTICS
// ========================================

function updateStatistics() {

    const employeeAccounts = allUsers.filter(
        user => user.role === "EMPLOYEE"
    );

    const employeeEmails = new Set(
        allEmployees.map(employee =>
            String(employee.email || "").toLowerCase()
        )
    );

    const pendingProfiles = employeeAccounts.filter(user =>
        !employeeEmails.has(
            String(user.email || "").toLowerCase()
        )
    );

    totalEmployeesElement.textContent = allEmployees.length;

    employeeAccountsElement.textContent =
        employeeAccounts.length;

    pendingProfilesElement.textContent =
        pendingProfiles.length;
}


// ========================================
// CREATE TABLE CELL
// ========================================

function createCell(value) {

    const cell = document.createElement("td");

    cell.textContent =
        value === null ||
        value === undefined ||
        value === ""
            ? "—"
            : String(value);

    return cell;
}


// ========================================
// RENDER EMPLOYEE TABLE
// ========================================

function renderEmployees(employees) {

    employeesTableBody.replaceChildren();

    if (employees.length === 0) {

        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 6;
        cell.className = "table-placeholder";
        cell.textContent = "No employees found.";

        row.appendChild(cell);
        employeesTableBody.appendChild(row);

        return;
    }

    employees.forEach(employee => {

        const row = document.createElement("tr");

        row.appendChild(createCell(`#${employee.id}`));

        row.appendChild(
            createCell(employee.employeeName)
        );

        row.appendChild(
            createCell(employee.employeeCode)
        );

        row.appendChild(
            createCell(employee.designation)
        );

        row.appendChild(
            createCell(employee.email)
        );

        row.appendChild(
            createCell(formatDate(employee.joiningDate))
        );

        employeesTableBody.appendChild(row);
    });
}


// ========================================
// SEARCH EMPLOYEES
// ========================================

function searchEmployeeRecords() {

    const query = searchEmployees.value
        .trim()
        .toLowerCase();

    const filteredEmployees = allEmployees.filter(employee => {

        const searchableText = [

            employee.id,
            employee.employeeName,
            employee.employeeCode,
            employee.designation,
            employee.email,
            employee.joiningDate

        ].join(" ").toLowerCase();

        return searchableText.includes(query);
    });

    renderEmployees(filteredEmployees);
}


// ========================================
// OPEN MODAL
// ========================================

function openEmployeeModal() {

    if (pendingEmployeeUserId !== null) {

        showMessage(
            employeeFormMessage,
            `Login account already created (User ID: ${pendingEmployeeUserId}). Complete the employee profile first.`,
            "error"
        );

        employeeDialog.classList.add("show");
        employeeDialog.setAttribute("aria-hidden", "false");

        return;
    }

    addEmployeeForm.reset();

    showMessage(employeeFormMessage, "");

    employeeDialog.classList.add("show");
    employeeDialog.setAttribute("aria-hidden", "false");
}


// ========================================
// CLOSE MODAL
// ========================================

function closeModal() {

    // Do not close while an account is waiting for its profile.
    if (pendingEmployeeUserId !== null) {

        showMessage(
            employeeFormMessage,
            "Please finish creating this employee profile before closing.",
            "error"
        );

        return;
    }

    employeeDialog.classList.remove("show");
    employeeDialog.setAttribute("aria-hidden", "true");

    addEmployeeForm.reset();

    showMessage(employeeFormMessage, "");
}


// ========================================
// CREATE EMPLOYEE PROFILE
// ========================================

async function createEmployeeProfile(userId) {

    const profilePayload = {

        employeeCode: employeeCode.value.trim(),

        designation: employeeDesignation.value.trim(),

        joiningDate: employeeJoiningDate.value,

        userId: userId

    };

    try {

        return await apiRequest("/api/employees", {

            method: "POST",

            body: JSON.stringify(profilePayload)

        });

    } catch (error) {

        // Check whether the profile was actually created in case
        // the server committed it but the response was interrupted.
        try {

            const latestEmployees =
                await apiRequest("/api/employees");

            const matchingEmployee = latestEmployees.find(employee =>

                String(employee.email || "").toLowerCase() ===
                employeeEmail.value.trim().toLowerCase()

            );

            if (matchingEmployee) {
                return matchingEmployee;
            }

        } catch (checkError) {

            console.error(
                "Could not verify employee profile:",
                checkError
            );

        }

        throw error;
    }
}


// ========================================
// CREATE EMPLOYEE ACCOUNT + PROFILE
// ========================================

async function createEmployee(event) {

    event.preventDefault();

    if (!addEmployeeForm.reportValidity()) {
        return;
    }

    // Additional password validation
    if (employeePassword.value.length < 12 &&
        pendingEmployeeUserId === null) {

        showMessage(
            employeeFormMessage,
            "Password must contain at least 12 characters.",
            "error"
        );

        return;
    }

    saveEmployeeButton.disabled = true;
    saveEmployeeButton.textContent = "Saving...";

    try {

        let userId = pendingEmployeeUserId;

        // Step 1: Create login account, unless it already exists
        // from an earlier partially completed submission.
        if (userId === null) {

            showMessage(
                employeeFormMessage,
                "Creating employee login account..."
            );

            const userPayload = {

                name: employeeName.value.trim(),

                email: employeeEmail.value.trim(),

                phone: employeePhone.value.trim() || null,

                password: employeePassword.value,

                role: "EMPLOYEE"

            };

            const createdUser = await apiRequest("/api/users", {

                method: "POST",

                body: JSON.stringify(userPayload)

            });

            if (!createdUser || !createdUser.id) {
                throw new Error(
                    "Login account was created but its user ID was not returned. Please check the Users API."
                );
            }

            userId = createdUser.id;

            // Keep the ID so a failed profile request can be retried
            // without creating a duplicate login account.
            pendingEmployeeUserId = userId;
        }


        // Step 2: Create employee profile
        showMessage(
            employeeFormMessage,
            `Creating employee profile for User ID ${userId}...`
        );

        await createEmployeeProfile(userId);

        // Both operations succeeded
        pendingEmployeeUserId = null;

        employeeDialog.classList.remove("show");
        employeeDialog.setAttribute("aria-hidden", "true");

        addEmployeeForm.reset();

        showMessage(employeeFormMessage, "");

        // Reload table and statistics
        await loadEmployeeData();

        showMessage(
            employeesMessage,
            "Employee account and profile created successfully.",
            "success"
        );

    } catch (error) {

        console.error("Employee creation error:", error);

        if (pendingEmployeeUserId !== null) {

            showMessage(
                employeeFormMessage,
                `Login account created (User ID: ${pendingEmployeeUserId}), but profile creation failed: ${error.message}. Keep this window open and click Save Employee again to retry the profile.`,
                "error"
            );

        } else {

            showMessage(
                employeeFormMessage,
                error.message || "Unable to create employee.",
                "error"
            );

        }

    } finally {

        saveEmployeeButton.disabled = false;
        saveEmployeeButton.textContent = "Save Employee";

    }
}


// ========================================
// LOGOUT
// ========================================

function logout() {

    if (pendingEmployeeUserId !== null) {

        alert(
            "An employee account has been created but its profile is pending. Complete the profile before logging out."
        );

        return;
    }

    sessionStorage.removeItem("fmsToken");
    sessionStorage.removeItem("fmsUser");

    window.location.href = "/login.html";
}


// ========================================
// EVENT LISTENERS
// ========================================

addEmployeeButton.addEventListener(
    "click",
    openEmployeeModal
);

closeEmployeeModal.addEventListener(
    "click",
    closeModal
);

cancelEmployee.addEventListener(
    "click",
    closeModal
);

logoutButton.addEventListener(
    "click",
    logout
);

searchEmployees.addEventListener(
    "input",
    searchEmployeeRecords
);

addEmployeeForm.addEventListener(
    "submit",
    createEmployee
);


// Close modal when clicking outside
employeeDialog.addEventListener("click", function(event) {

    if (event.target === employeeDialog) {
        closeModal();
    }

});


// Close modal using Escape key
document.addEventListener("keydown", function(event) {

    if (
        event.key === "Escape" &&
        employeeDialog.classList.contains("show")
    ) {
        closeModal();
    }

});


// ========================================
// INITIALIZE PAGE
// ========================================

async function initializeEmployeesPage() {

    const isAdmin = await verifyAdmin();

    if (!isAdmin) {
        return;
    }

    await loadEmployeeData();
}

initializeEmployeesPage();