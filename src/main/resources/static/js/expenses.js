// ========================================
// EXPENSES MANAGEMENT
// ========================================

const token = sessionStorage.getItem("fmsToken");

// Header
const adminName = document.getElementById("adminName");
const logoutButton = document.getElementById("logoutButton");

// Buttons
const addExpenseButton = document.getElementById("addExpenseButton");

// Statistics
const totalExpensesElement = document.getElementById("totalExpenses");
const materialExpensesElement = document.getElementById("materialExpenses");
const labourExpensesElement = document.getElementById("labourExpenses");
const transportExpensesElement = document.getElementById("transportExpenses");

// Filters
const searchExpenses = document.getElementById("searchExpenses");
const expenseProjectFilter =
    document.getElementById("expenseProjectFilter");
const expenseCategoryFilter =
    document.getElementById("expenseCategoryFilter");

// Table
const expensesTableBody =
    document.getElementById("expensesTableBody");
const expensesMessage = document.getElementById("expensesMessage");

// Modal
const expenseDialog = document.getElementById("expenseDialog");
const closeExpenseModal =
    document.getElementById("closeExpenseModal");
const cancelExpense = document.getElementById("cancelExpense");
const addExpenseForm = document.getElementById("addExpenseForm");

// Form fields
const expenseProject = document.getElementById("expenseProject");
const expenseAmount = document.getElementById("expenseAmount");
const expenseCategory = document.getElementById("expenseCategory");
const expenseDate = document.getElementById("expenseDate");
const expenseDescription =
    document.getElementById("expenseDescription");
const expenseFormMessage =
    document.getElementById("expenseFormMessage");

const saveExpenseButton =
    addExpenseForm.querySelector(".save-expense-button");


// ========================================
// DATA
// ========================================

let allExpenses = [];
let allProjects = [];
let isSavingExpense = false;


// ========================================
// FORMATTERS
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


function getToday() {

    const now = new Date();

    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const day = String(now.getDate()).padStart(2, "0");

    return `${year}-${month}-${day}`;
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
        element === expensesMessage
            ? `expenses-message ${type}`
            : `expense-form-message ${type}`;
}


// ========================================
// EXPENSE HELPERS
// ========================================

function getExpenseProjectId(expense) {

    return expense.project?.id ??
        expense.projectId ??
        null;
}


function getExpenseProjectName(expense) {

    return expense.project?.projectName ||
        expense.projectName ||
        "Unknown Project";
}


function getProjectById(projectId) {

    return allProjects.find(
        project => String(project.id) === String(projectId)
    );
}


function getCategoryLabel(category) {

    const labels = {
        WOOD: "Wood",
        HARDWARE: "Hardware",
        LABOUR: "Labour",
        TRANSPORT: "Transport",
        OTHER: "Other"
    };

    return labels[category] || category || "Unknown";
}


// ========================================
// LOAD EXPENSE DATA
// ========================================

async function loadExpenseData() {

    showMessage(expensesMessage, "Loading expense records...");

    try {

        const [expenses, projects] = await Promise.all([

            apiRequest("/api/expenses"),
            apiRequest("/api/projects")

        ]);

        allExpenses = Array.isArray(expenses)
            ? expenses
            : [];

        allProjects = Array.isArray(projects)
            ? projects
            : [];

        renderProjectFilters();
        renderExpenseProjectOptions();

        renderExpenses();
        updateStatistics();

        showMessage(
            expensesMessage,
            `${allExpenses.length} expense record(s) loaded successfully.`,
            "success"
        );

    } catch (error) {

        console.error("Expense loading error:", error);

        expensesTableBody.replaceChildren();

        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 6;
        cell.className = "table-placeholder";
        cell.textContent = "Unable to load expense records.";

        row.appendChild(cell);
        expensesTableBody.appendChild(row);

        showMessage(
            expensesMessage,
            error.message,
            "error"
        );
    }
}


// ========================================
// UPDATE STATISTICS
// ========================================

function updateStatistics() {

    const total = allExpenses.reduce(
        (sum, expense) => sum + Number(expense.amount || 0),
        0
    );

    const materials = allExpenses
        .filter(expense =>
            expense.category === "WOOD" ||
            expense.category === "HARDWARE"
        )
        .reduce(
            (sum, expense) => sum + Number(expense.amount || 0),
            0
        );

    const labour = allExpenses
        .filter(expense => expense.category === "LABOUR")
        .reduce(
            (sum, expense) => sum + Number(expense.amount || 0),
            0
        );

    const transport = allExpenses
        .filter(expense => expense.category === "TRANSPORT")
        .reduce(
            (sum, expense) => sum + Number(expense.amount || 0),
            0
        );

    totalExpensesElement.textContent = formatCurrency(total);

    materialExpensesElement.textContent = formatCurrency(materials);

    labourExpensesElement.textContent = formatCurrency(labour);

    transportExpensesElement.textContent = formatCurrency(transport);
}


// ========================================
// SAFE TABLE CELL
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
// CATEGORY BADGE
// ========================================

function createCategoryBadge(category) {

    const badge = document.createElement("span");

    badge.className = "expense-category-badge";

    const classes = {
        WOOD: "category-wood",
        HARDWARE: "category-hardware",
        LABOUR: "category-labour",
        TRANSPORT: "category-transport",
        OTHER: "category-other"
    };

    if (classes[category]) {
        badge.classList.add(classes[category]);
    }

    badge.textContent = getCategoryLabel(category);

    return badge;
}


// ========================================
// FILTER DROPDOWNS
// ========================================

function renderProjectFilters() {

    const previousValue = expenseProjectFilter.value || "ALL";

    expenseProjectFilter.replaceChildren();

    const defaultOption = document.createElement("option");
    defaultOption.value = "ALL";
    defaultOption.textContent = "All Projects";

    expenseProjectFilter.appendChild(defaultOption);

    allProjects.forEach(project => {

        const option = document.createElement("option");

        option.value = project.id;
        option.textContent = project.projectName;

        expenseProjectFilter.appendChild(option);
    });

    const exists = Array.from(expenseProjectFilter.options)
        .some(option => option.value === previousValue);

    expenseProjectFilter.value = exists
        ? previousValue
        : "ALL";
}


function renderExpenseProjectOptions() {

    const previousValue = expenseProject.value;

    expenseProject.replaceChildren();

    const defaultOption = document.createElement("option");

    defaultOption.value = "";
    defaultOption.textContent = allProjects.length
        ? "Select a project"
        : "No projects available";

    expenseProject.appendChild(defaultOption);

    allProjects.forEach(project => {

        const option = document.createElement("option");

        option.value = project.id;

        option.textContent =
            `${project.projectName} (#${project.id})`;

        expenseProject.appendChild(option);
    });

    expenseProject.disabled = allProjects.length === 0;

    if (
        previousValue &&
        allProjects.some(
            project => String(project.id) === previousValue
        )
    ) {
        expenseProject.value = previousValue;
    }
}


// ========================================
// SEARCH AND FILTERS
// ========================================

function getFilteredExpenses() {

    const query = searchExpenses.value
        .trim()
        .toLowerCase();

    const selectedProject = expenseProjectFilter.value;
    const selectedCategory = expenseCategoryFilter.value;

    return allExpenses.filter(expense => {

        const projectId = getExpenseProjectId(expense);
        const projectName = getExpenseProjectName(expense);

        const searchableText = [

            expense.id,
            projectName,
            projectId,
            expense.category,
            expense.amount,
            expense.expenseDate,
            expense.description

        ].join(" ").toLowerCase();

        const matchesSearch =
            searchableText.includes(query);

        const matchesProject =
            selectedProject === "ALL" ||
            String(projectId) === selectedProject;

        const matchesCategory =
            selectedCategory === "ALL" ||
            expense.category === selectedCategory;

        return matchesSearch &&
            matchesProject &&
            matchesCategory;
    });
}


// ========================================
// RENDER EXPENSE TABLE
// ========================================

function renderExpenses() {

    const expenses = getFilteredExpenses();

    expensesTableBody.replaceChildren();

    if (expenses.length === 0) {

        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 6;
        cell.className = "table-placeholder";

        cell.textContent = allExpenses.length === 0
            ? "No expense records available."
            : "No expenses match your search or filters.";

        row.appendChild(cell);
        expensesTableBody.appendChild(row);

        return;
    }

    expenses.forEach(expense => {

        const row = document.createElement("tr");

        // Expense ID
        row.appendChild(
            createCell(`#${expense.id}`)
        );

        // Project
        row.appendChild(
            createCell(getExpenseProjectName(expense))
        );

        // Category
        const categoryCell = document.createElement("td");

        categoryCell.appendChild(
            createCategoryBadge(expense.category)
        );

        row.appendChild(categoryCell);

        // Amount
        row.appendChild(
            createCell(formatCurrency(expense.amount))
        );

        // Date
        row.appendChild(
            createCell(formatDate(expense.expenseDate))
        );

        // Description
        const descriptionCell = document.createElement("td");
        const description = document.createElement("span");

        description.className = "expense-description";
        description.textContent = expense.description || "—";
        description.title = expense.description || "";

        descriptionCell.appendChild(description);
        row.appendChild(descriptionCell);

        expensesTableBody.appendChild(row);
    });
}


// ========================================
// OPEN EXPENSE MODAL
// ========================================

function openExpenseModal() {

    addExpenseForm.reset();

    showMessage(expenseFormMessage, "");

    expenseDate.max = getToday();
    expenseDate.value = getToday();

    renderExpenseProjectOptions();

    expenseDialog.classList.add("show");
    expenseDialog.setAttribute("aria-hidden", "false");
}


// ========================================
// CLOSE EXPENSE MODAL
// ========================================

function closeModal() {

    if (isSavingExpense) {
        return;
    }

    expenseDialog.classList.remove("show");
    expenseDialog.setAttribute("aria-hidden", "true");

    addExpenseForm.reset();

    showMessage(expenseFormMessage, "");
}


// ========================================
// CREATE EXPENSE
// ========================================

async function createExpense(event) {

    event.preventDefault();

    if (isSavingExpense) {
        return;
    }

    if (!addExpenseForm.reportValidity()) {
        return;
    }

    const projectId = Number(expenseProject.value);
    const amount = expenseAmount.value.trim();
    const category = expenseCategory.value;
    const date = expenseDate.value;

    if (!projectId || !getProjectById(projectId)) {

        showMessage(
            expenseFormMessage,
            "Please select a valid project.",
            "error"
        );

        return;
    }

    const numericAmount = Number(amount);

    if (
        !Number.isFinite(numericAmount) ||
        numericAmount <= 0 ||
        numericAmount > 9999999999.99 ||
        !/^\d+(\.\d{1,2})?$/.test(amount)
    ) {

        showMessage(
            expenseFormMessage,
            "Enter a valid positive amount with up to 2 decimal places.",
            "error"
        );

        return;
    }

    if (date > getToday()) {

        showMessage(
            expenseFormMessage,
            "Expense date cannot be in the future.",
            "error"
        );

        return;
    }

    const validCategories = [
        "WOOD",
        "HARDWARE",
        "LABOUR",
        "TRANSPORT",
        "OTHER"
    ];

    if (!validCategories.includes(category)) {

        showMessage(
            expenseFormMessage,
            "Please select a valid expense category.",
            "error"
        );

        return;
    }

    const payload = {

        projectId: projectId,

        // Send decimal as string to preserve precision.
        amount: amount,

        category: category,

        expenseDate: date,

        description: expenseDescription.value.trim() || null

    };

    isSavingExpense = true;

    saveExpenseButton.disabled = true;
    saveExpenseButton.textContent = "Saving...";

    showMessage(
        expenseFormMessage,
        "Saving expense..."
    );

    try {

        await apiRequest("/api/expenses", {

            method: "POST",

            body: JSON.stringify(payload)

        });

        expenseDialog.classList.remove("show");
        expenseDialog.setAttribute("aria-hidden", "true");

        addExpenseForm.reset();

        showMessage(expenseFormMessage, "");

        await loadExpenseData();

        showMessage(
            expensesMessage,
            "Expense recorded successfully.",
            "success"
        );

    } catch (error) {

        console.error("Expense creation error:", error);

        showMessage(
            expenseFormMessage,
            error.message || "Unable to record expense.",
            "error"
        );

    } finally {

        isSavingExpense = false;

        saveExpenseButton.disabled = false;
        saveExpenseButton.textContent = "Save Expense";
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

addExpenseButton.addEventListener(
    "click",
    openExpenseModal
);

closeExpenseModal.addEventListener(
    "click",
    closeModal
);

cancelExpense.addEventListener(
    "click",
    closeModal
);

logoutButton.addEventListener(
    "click",
    logout
);


// Search and filters
searchExpenses.addEventListener(
    "input",
    renderExpenses
);

expenseProjectFilter.addEventListener(
    "change",
    renderExpenses
);

expenseCategoryFilter.addEventListener(
    "change",
    renderExpenses
);


// Form submission
addExpenseForm.addEventListener(
    "submit",
    createExpense
);


// Close when clicking outside
expenseDialog.addEventListener("click", function(event) {

    if (event.target === expenseDialog) {
        closeModal();
    }

});


// Escape key
document.addEventListener("keydown", function(event) {

    if (
        event.key === "Escape" &&
        expenseDialog.classList.contains("show")
    ) {
        closeModal();
    }

});


// ========================================
// INITIALIZE PAGE
// ========================================

async function initializeExpensesPage() {

    const isAdmin = await verifyAdmin();

    if (!isAdmin) {
        return;
    }

    expenseDate.max = getToday();

    await loadExpenseData();
}

initializeExpensesPage();