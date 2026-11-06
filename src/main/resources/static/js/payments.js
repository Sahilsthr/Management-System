// ========================================
// PAYMENTS MANAGEMENT
// ========================================

const token = sessionStorage.getItem("fmsToken");

// Header
const adminName = document.getElementById("adminName");
const logoutButton = document.getElementById("logoutButton");

// Buttons
const addPaymentButton = document.getElementById("addPaymentButton");

// Statistics
const totalCollectedElement = document.getElementById("totalCollected");
const outstandingBalanceElement =
    document.getElementById("outstandingBalance");
const pendingPaymentCountElement =
    document.getElementById("pendingPaymentCount");
const totalPaymentRecordsElement =
    document.getElementById("totalPaymentRecords");

// Filters
const searchPayments = document.getElementById("searchPayments");
const paymentProjectFilter =
    document.getElementById("paymentProjectFilter");
const paymentStatusFilter =
    document.getElementById("paymentStatusFilter");

// Table
const paymentsTableBody =
    document.getElementById("paymentsTableBody");
const paymentsMessage = document.getElementById("paymentsMessage");

// Modal
const paymentDialog = document.getElementById("paymentDialog");
const closePaymentModal =
    document.getElementById("closePaymentModal");
const cancelPayment = document.getElementById("cancelPayment");
const addPaymentForm = document.getElementById("addPaymentForm");

// Form fields
const paymentProject = document.getElementById("paymentProject");
const paymentAmount = document.getElementById("paymentAmount");
const paymentDate = document.getElementById("paymentDate");
const paymentMethod = document.getElementById("paymentMethod");
const paymentStatus = document.getElementById("paymentStatus");
const paymentRemark = document.getElementById("paymentRemark");
const projectBalanceInfo =
    document.getElementById("projectBalanceInfo");
const paymentFormMessage =
    document.getElementById("paymentFormMessage");

const savePaymentButton =
    addPaymentForm.querySelector(".save-payment-button");


// ========================================
// DATA
// ========================================

let allPayments = [];
let allProjects = [];
let projectSummaries = new Map();

let isSavingPayment = false;
let isUpdatingPayment = false;


// ========================================
// CURRENCY FORMATTER
// ========================================

const currencyFormatter = new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 2
});

function formatCurrency(value) {

    const amount = Number(value);

    return currencyFormatter.format(
        Number.isFinite(amount) ? amount : 0
    );
}


// ========================================
// API HELPER
// ========================================

async function apiRequest(url, options = {}) {

    const response = await fetch(url, {
        ...options,

        headers: {
            Authorization: `Bearer ${token}`,

            ...(options.body !== undefined ? {
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

        throw new Error(
            data?.message ||
            data?.detail ||
            data?.title ||
            `Request failed with status ${response.status}`
        );
    }

    return data;
}


// ========================================
// ADMIN AUTHENTICATION
// ========================================

async function verifyAdmin() {

    if (!token) {
        window.location.href = "/login.html";
        return false;
    }

    try {

        const user = await apiRequest("/api/auth/me");

        if (user.role !== "ADMIN") {

            alert("Admin access required.");

            sessionStorage.removeItem("fmsToken");
            sessionStorage.removeItem("fmsUser");

            window.location.href = "/login.html";

            return false;
        }

        adminName.textContent =
            user.fullName ||
            user.name ||
            user.email ||
            "Administrator";

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
        element === paymentsMessage
            ? `payments-message ${type}`
            : `payment-form-message ${type}`;
}


// ========================================
// DATE FORMATTER
// ========================================

function formatDate(value) {

    if (!value) {
        return "—";
    }

    const parts = String(value).split("-");

    if (parts.length !== 3) {
        return String(value);
    }

    return `${parts[2]}-${parts[1]}-${parts[0]}`;
}


// ========================================
// PROJECT HELPERS
// ========================================

function getPaymentProjectId(payment) {

    return payment.project?.id ??
        payment.projectId ??
        null;
}


function getPaymentProjectName(payment) {

    return payment.project?.projectName ||
        payment.projectName ||
        "Unknown Project";
}


function getProjectById(projectId) {

    return allProjects.find(
        project => String(project.id) === String(projectId)
    );
}


// ========================================
// LOAD PAYMENT DATA
// ========================================

async function loadPaymentData() {

    showMessage(paymentsMessage, "Loading payment records...");

    try {

        const [payments, projects] = await Promise.all([

            apiRequest("/api/payments"),
            apiRequest("/api/projects")

        ]);

        allPayments = Array.isArray(payments) ? payments : [];
        allProjects = Array.isArray(projects) ? projects : [];

        await loadProjectSummaries();

        renderProjectFilters();
        renderPaymentProjectOptions();

        renderPayments();
        updateStatistics();

        showMessage(
            paymentsMessage,
            `${allPayments.length} payment record(s) loaded successfully.`,
            "success"
        );

    } catch (error) {

        console.error("Payment loading error:", error);

        paymentsTableBody.replaceChildren();

        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 8;
        cell.className = "table-placeholder";
        cell.textContent = "Unable to load payment records.";

        row.appendChild(cell);
        paymentsTableBody.appendChild(row);

        showMessage(paymentsMessage, error.message, "error");
    }
}


// ========================================
// LOAD PROJECT PAYMENT SUMMARIES
// ========================================

async function loadProjectSummaries() {

    projectSummaries = new Map();

    const results = await Promise.allSettled(

        allProjects.map(project =>
            apiRequest(`/api/projects/${encodeURIComponent(project.id)}/payment-summary`)
        )

    );

    results.forEach((result, index) => {

        const project = allProjects[index];

        if (result.status === "fulfilled") {

            projectSummaries.set(
                String(project.id),
                result.value
            );

        } else {

            console.warn(
                `Payment summary unavailable for project ${project.id}:`,
                result.reason
            );

            // Fallback calculation using available project and payment data.
            const totalAmount = Number(project.totalAmount || 0);

            const paidAmount = allPayments
                .filter(payment =>
                    String(getPaymentProjectId(payment)) === String(project.id) &&
                    payment.paymentStatus === "PAID"
                )
                .reduce(
                    (sum, payment) => sum + Number(payment.amount || 0),
                    0
                );

            projectSummaries.set(String(project.id), {
                projectId: project.id,
                totalAmount: totalAmount,
                paidAmount: paidAmount,
                pendingAmount: totalAmount - paidAmount
            });
        }
    });
}


// ========================================
// UPDATE STATISTICS
// ========================================

function updateStatistics() {

    const paidAmount = allPayments
        .filter(payment => payment.paymentStatus === "PAID")
        .reduce(
            (sum, payment) => sum + Number(payment.amount || 0),
            0
        );

    const pendingCount = allPayments.filter(
        payment => payment.paymentStatus === "PENDING"
    ).length;

    const outstandingAmount = Array.from(
        projectSummaries.values()
    ).reduce(
        (sum, summary) => sum + Number(summary.pendingAmount || 0),
        0
    );

    totalCollectedElement.textContent =
        formatCurrency(paidAmount);

    outstandingBalanceElement.textContent =
        formatCurrency(outstandingAmount);

    pendingPaymentCountElement.textContent =
        pendingCount;

    totalPaymentRecordsElement.textContent =
        allPayments.length;
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
// STATUS BADGE
// ========================================

function createPaymentStatusBadge(status) {

    const badge = document.createElement("span");

    badge.className = "payment-status-badge";

    const config = {
        PAID: ["Paid", "payment-paid"],
        PENDING: ["Pending", "payment-pending"],
        CANCELLED: ["Cancelled", "payment-cancelled"]
    };

    const item = config[status] || [status || "Unknown", ""];

    badge.textContent = item[0];

    if (item[1]) {
        badge.classList.add(item[1]);
    }

    return badge;
}


// ========================================
// PAYMENT METHOD LABEL
// ========================================

function getPaymentMethodLabel(method) {

    const labels = {
        CASH: "Cash",
        UPI: "UPI",
        BANK_TRANSFER: "Bank Transfer",
        CHEQUE: "Cheque"
    };

    return labels[method] || method || "—";
}


// ========================================
// STATUS UPDATE DROPDOWN
// ========================================

function createPaymentStatusSelect(payment) {

    const select = document.createElement("select");

    select.className = "payment-status-select";

    select.dataset.paymentId = payment.id;
    select.dataset.originalStatus = payment.paymentStatus;

    const statuses = [
        { value: "PAID", label: "Paid" },
        { value: "PENDING", label: "Pending" },
        { value: "CANCELLED", label: "Cancelled" }
    ];

    statuses.forEach(item => {

        const option = document.createElement("option");

        option.value = item.value;
        option.textContent = item.label;
        option.selected = payment.paymentStatus === item.value;

        select.appendChild(option);
    });

    select.disabled =
        isUpdatingPayment ||
        !statuses.some(
            item => item.value === payment.paymentStatus
        );

    return select;
}


// ========================================
// PAYMENT FILTERS
// ========================================

function getFilteredPayments() {

    const query = searchPayments.value.trim().toLowerCase();

    const selectedProject = paymentProjectFilter.value;
    const selectedStatus = paymentStatusFilter.value;

    return allPayments.filter(payment => {

        const projectName = getPaymentProjectName(payment);
        const projectId = getPaymentProjectId(payment);

        const searchableText = [

            payment.id,
            projectName,
            projectId,
            payment.amount,
            payment.paymentDate,
            payment.paymentMethod,
            payment.paymentStatus,
            payment.remark

        ].join(" ").toLowerCase();

        const matchesSearch =
            searchableText.includes(query);

        const matchesProject =
            selectedProject === "ALL" ||
            String(projectId) === selectedProject;

        const matchesStatus =
            selectedStatus === "ALL" ||
            payment.paymentStatus === selectedStatus;

        return matchesSearch && matchesProject && matchesStatus;
    });
}


// ========================================
// RENDER PAYMENTS TABLE
// ========================================

function renderPayments() {

    const payments = getFilteredPayments();

    paymentsTableBody.replaceChildren();

    if (payments.length === 0) {

        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 8;
        cell.className = "table-placeholder";

        cell.textContent = allPayments.length === 0
            ? "No payment records available."
            : "No payments match your search or filters.";

        row.appendChild(cell);
        paymentsTableBody.appendChild(row);

        return;
    }

    payments.forEach(payment => {

        const row = document.createElement("tr");

        row.appendChild(
            createCell(`#${payment.id}`)
        );

        row.appendChild(
            createCell(getPaymentProjectName(payment))
        );

        row.appendChild(
            createCell(formatCurrency(payment.amount))
        );

        row.appendChild(
            createCell(formatDate(payment.paymentDate))
        );

        row.appendChild(
            createCell(getPaymentMethodLabel(payment.paymentMethod))
        );


        const statusCell = document.createElement("td");

        statusCell.appendChild(
            createPaymentStatusBadge(payment.paymentStatus)
        );

        row.appendChild(statusCell);


        const remarkCell = document.createElement("td");
        const remark = document.createElement("span");

        remark.className = "payment-remark";
        remark.textContent = payment.remark || "—";
        remark.title = payment.remark || "";

        remarkCell.appendChild(remark);
        row.appendChild(remarkCell);


        const updateCell = document.createElement("td");

        updateCell.appendChild(
            createPaymentStatusSelect(payment)
        );

        row.appendChild(updateCell);

        paymentsTableBody.appendChild(row);
    });
}


// ========================================
// PROJECT FILTER DROPDOWN
// ========================================

function renderProjectFilters() {

    const previousValue = paymentProjectFilter.value || "ALL";

    paymentProjectFilter.replaceChildren();

    const allOption = document.createElement("option");

    allOption.value = "ALL";
    allOption.textContent = "All Projects";

    paymentProjectFilter.appendChild(allOption);

    allProjects.forEach(project => {

        const option = document.createElement("option");

        option.value = project.id;
        option.textContent = project.projectName;

        paymentProjectFilter.appendChild(option);
    });

    const optionExists = Array.from(paymentProjectFilter.options)
        .some(option => option.value === previousValue);

    paymentProjectFilter.value =
        optionExists ? previousValue : "ALL";
}


// ========================================
// ADD PAYMENT PROJECT DROPDOWN
// ========================================

function renderPaymentProjectOptions() {

    const previousValue = paymentProject.value;

    paymentProject.replaceChildren();

    const defaultOption = document.createElement("option");

    defaultOption.value = "";
    defaultOption.textContent = allProjects.length
        ? "Select a project"
        : "No projects available";

    paymentProject.appendChild(defaultOption);

    allProjects.forEach(project => {

        const option = document.createElement("option");

        option.value = project.id;

        option.textContent =
            `${project.projectName} (#${project.id})`;

        paymentProject.appendChild(option);
    });

    paymentProject.disabled = allProjects.length === 0;

    if (
        previousValue &&
        allProjects.some(
            project => String(project.id) === previousValue
        )
    ) {
        paymentProject.value = previousValue;
        updateProjectBalanceInfo(previousValue);
    }
}


// ========================================
// SHOW PROJECT BALANCE
// ========================================

function updateProjectBalanceInfo(projectId) {

    if (!projectId) {

        projectBalanceInfo.textContent =
            "Select a project to view its payment balance.";

        return;
    }

    const project = getProjectById(projectId);

    const summary = projectSummaries.get(String(projectId));

    if (!project) {
        projectBalanceInfo.textContent = "Project not found.";
        return;
    }

    if (summary) {

        projectBalanceInfo.textContent =
            `Project budget: ${formatCurrency(summary.totalAmount)} | Outstanding: ${formatCurrency(summary.pendingAmount)}`;

    } else {

        projectBalanceInfo.textContent =
            `Project budget: ${formatCurrency(project.totalAmount || 0)}`;
    }
}


// ========================================
// SEARCH AND FILTER EVENTS
// ========================================

function applyPaymentFilters() {
    renderPayments();
}


// ========================================
// OPEN PAYMENT MODAL
// ========================================

function openPaymentModal() {

    addPaymentForm.reset();

    showMessage(paymentFormMessage, "");

    renderPaymentProjectOptions();

    updateProjectBalanceInfo("");

    paymentDialog.classList.add("show");
    paymentDialog.setAttribute("aria-hidden", "false");
}


// ========================================
// CLOSE PAYMENT MODAL
// ========================================

function closeModal() {

    if (isSavingPayment) {
        return;
    }

    paymentDialog.classList.remove("show");
    paymentDialog.setAttribute("aria-hidden", "true");

    addPaymentForm.reset();

    updateProjectBalanceInfo("");

    showMessage(paymentFormMessage, "");
}


// ========================================
// CREATE PAYMENT
// ========================================

async function createPayment(event) {

    event.preventDefault();

    if (isSavingPayment) {
        return;
    }

    if (!addPaymentForm.reportValidity()) {
        return;
    }

    const projectId = Number(paymentProject.value);
    const amount = paymentAmount.value.trim();

    if (!projectId || !amount || Number(amount) <= 0) {

        showMessage(
            paymentFormMessage,
            "Select a project and enter a valid payment amount.",
            "error"
        );

        return;
    }

    const projectExists = allProjects.some(
        project => Number(project.id) === projectId
    );

    if (!projectExists) {

        showMessage(
            paymentFormMessage,
            "Please select a valid project.",
            "error"
        );

        return;
    }

    const payload = {

        projectId: projectId,

        // Keep decimal precision by sending the input string.
        amount: amount,

        paymentDate: paymentDate.value,

        paymentMethod: paymentMethod.value,

        paymentStatus: paymentStatus.value,

        remarks: paymentRemark.value.trim() || null

    };

    isSavingPayment = true;

    savePaymentButton.disabled = true;
    savePaymentButton.textContent = "Recording...";

    showMessage(
        paymentFormMessage,
        "Recording payment..."
    );

    try {

        await apiRequest("/api/payments", {

            method: "POST",

            body: JSON.stringify(payload)

        });

        paymentDialog.classList.remove("show");
        paymentDialog.setAttribute("aria-hidden", "true");

        addPaymentForm.reset();

        showMessage(paymentFormMessage, "");

        await loadPaymentData();

        showMessage(
            paymentsMessage,
            "Payment recorded successfully.",
            "success"
        );

    } catch (error) {

        console.error("Payment creation error:", error);

        showMessage(
            paymentFormMessage,
            error.message || "Unable to record payment.",
            "error"
        );

    } finally {

        isSavingPayment = false;

        savePaymentButton.disabled = false;
        savePaymentButton.textContent = "Record Payment";
    }
}


// ========================================
// UPDATE PAYMENT STATUS
// ========================================

async function updatePaymentStatus(select) {

    if (isUpdatingPayment) {
        return;
    }

    const paymentId = select.dataset.paymentId;
    const previousStatus = select.dataset.originalStatus;
    const newStatus = select.value;

    if (!paymentId || !newStatus || newStatus === previousStatus) {
        return;
    }

    isUpdatingPayment = true;

    paymentsTableBody.querySelectorAll(".payment-status-select")
        .forEach(control => {
            control.disabled = true;
        });

    try {

        await apiRequest(
            `/api/payments/${encodeURIComponent(paymentId)}/status?status=${encodeURIComponent(newStatus)}`,
            {
                method: "PUT"
            }
        );

        await loadPaymentData();

        showMessage(
            paymentsMessage,
            `Payment #${paymentId} status updated successfully.`,
            "success"
        );

    } catch (error) {

        console.error("Payment status update error:", error);

        select.value = previousStatus;

        showMessage(
            paymentsMessage,
            error.message || "Unable to update payment status.",
            "error"
        );

    } finally {

        isUpdatingPayment = false;

        renderPayments();
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

addPaymentButton.addEventListener(
    "click",
    openPaymentModal
);

closePaymentModal.addEventListener(
    "click",
    closeModal
);

cancelPayment.addEventListener(
    "click",
    closeModal
);

logoutButton.addEventListener(
    "click",
    logout
);


searchPayments.addEventListener(
    "input",
    applyPaymentFilters
);

paymentProjectFilter.addEventListener(
    "change",
    applyPaymentFilters
);

paymentStatusFilter.addEventListener(
    "change",
    applyPaymentFilters
);


paymentProject.addEventListener(
    "change",
    function () {
        updateProjectBalanceInfo(paymentProject.value);
        showMessage(paymentFormMessage, "");
    }
);


addPaymentForm.addEventListener(
    "submit",
    createPayment
);


// Status update using event delegation
paymentsTableBody.addEventListener("change", function(event) {

    const select = event.target.closest(".payment-status-select");

    if (!select) {
        return;
    }

    updatePaymentStatus(select);

});


// Click outside modal
paymentDialog.addEventListener("click", function(event) {

    if (event.target === paymentDialog) {
        closeModal();
    }

});


// Escape key
document.addEventListener("keydown", function(event) {

    if (
        event.key === "Escape" &&
        paymentDialog.classList.contains("show")
    ) {
        closeModal();
    }

});


// ========================================
// INITIALIZE PAGE
// ========================================

async function initializePaymentsPage() {

    const isAdmin = await verifyAdmin();

    if (!isAdmin) {
        return;
    }

    await loadPaymentData();
}

initializePaymentsPage();