// ========================================
// REPORTS & ANALYTICS
// ========================================

const token = sessionStorage.getItem("fmsToken");

// Header
const adminName = document.getElementById("adminName");
const logoutButton = document.getElementById("logoutButton");

// Buttons and filters
const refreshReportsButton =
    document.getElementById("refreshReportsButton");

const exportReportButton =
    document.getElementById("exportReportButton");

const reportProjectFilter =
    document.getElementById("reportProjectFilter");

// Generated date
const reportGeneratedAt =
    document.getElementById("reportGeneratedAt");

// KPI elements
const reportTotalBudget =
    document.getElementById("reportTotalBudget");

const reportTotalRevenue =
    document.getElementById("reportTotalRevenue");

const reportTotalExpenses =
    document.getElementById("reportTotalExpenses");

const reportTotalProfit =
    document.getElementById("reportTotalProfit");

const reportOutstanding =
    document.getElementById("reportOutstanding");

const reportTaskCompletion =
    document.getElementById("reportTaskCompletion");

// Charts
const monthlyTrendChart =
    document.getElementById("monthlyTrendChart");

const expenseDonut =
    document.getElementById("expenseDonut");

const categoryExpenseTotal =
    document.getElementById("categoryExpenseTotal");

const expenseLegend =
    document.getElementById("expenseLegend");

const projectComparisonChart =
    document.getElementById("projectComparisonChart");

const projectProgressList =
    document.getElementById("projectProgressList");

// Business summary
const summaryBudget =
    document.getElementById("summaryBudget");

const summaryRevenue =
    document.getElementById("summaryRevenue");

const summaryExpenses =
    document.getElementById("summaryExpenses");

const summaryOutstanding =
    document.getElementById("summaryOutstanding");

const summaryProfit =
    document.getElementById("summaryProfit");

// Project table
const projectReportTableBody =
    document.getElementById("projectReportTableBody");

const projectReportCount =
    document.getElementById("projectReportCount");

const reportsMessage =
    document.getElementById("reportsMessage");


// ========================================
// DATA
// ========================================

let dashboardData = {};
let allProjects = [];
let allPayments = [];
let allExpenses = [];
let allTasks = [];
let projectReports = [];

let isLoadingReports = false;


// ========================================
// CONSTANTS
// ========================================

const CATEGORY_CONFIG = {
    WOOD: {
        label: "Wood",
        color: "#c79a68"
    },
    HARDWARE: {
        label: "Hardware",
        color: "#8c9cac"
    },
    LABOUR: {
        label: "Labour",
        color: "#d97745"
    },
    TRANSPORT: {
        label: "Transport",
        color: "#e7b65f"
    },
    OTHER: {
        label: "Other",
        color: "#9a8bb9"
    }
};


// ========================================
// FORMATTERS
// ========================================

const currencyFormatter = new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 2
});

const compactCurrencyFormatter = new Intl.NumberFormat("en-IN", {
    notation: "compact",
    maximumFractionDigits: 1
});


function formatCurrency(value) {

    const amount = Number(value);

    return currencyFormatter.format(
        Number.isFinite(amount) ? amount : 0
    );
}


function formatCompactCurrency(value) {

    const amount = Number(value);

    return "₹" + compactCurrencyFormatter.format(
        Number.isFinite(amount) ? amount : 0
    );
}


function formatPercent(value) {

    const number = Number(value);

    if (!Number.isFinite(number)) {
        return "0%";
    }

    return `${number.toFixed(1).replace(/\.0$/, "")}%`;
}


function formatDateTime(value) {

    return new Date(value).toLocaleString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    });
}


// ========================================
// API HELPER
// ========================================

async function apiRequest(url) {

    const response = await fetch(url, {

        headers: {
            Authorization: `Bearer ${token}`
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

function showMessage(message, type = "") {

    reportsMessage.textContent = message;
    reportsMessage.className = `reports-message ${type}`;
}


// ========================================
// RECORD HELPERS
// ========================================

function getProjectId(record) {

    return record.project?.id ??
        record.projectId ??
        null;
}


function getProjectName(record) {

    return record.project?.projectName ||
        record.projectName ||
        "Unknown Project";
}


function getPaymentAmount(payment) {

    return Number(payment.amount || 0);
}


function getExpenseAmount(expense) {

    return Number(expense.amount || 0);
}


function getProjectTasks(projectId) {

    return allTasks.filter(task =>
        String(getProjectId(task)) === String(projectId)
    );
}


// ========================================
// BUILD PROJECT REPORTS
// ========================================

function buildProjectReports() {

    projectReports = allProjects.map(project => {

        const projectPayments = allPayments.filter(payment =>
            String(getProjectId(payment)) === String(project.id)
        );

        const projectExpenses = allExpenses.filter(expense =>
            String(getProjectId(expense)) === String(project.id)
        );

        const tasks = getProjectTasks(project.id);

        const paidRevenue = projectPayments
            .filter(payment => payment.paymentStatus === "PAID")
            .reduce(
                (sum, payment) => sum + getPaymentAmount(payment),
                0
            );

        const totalExpense = projectExpenses.reduce(
            (sum, expense) => sum + getExpenseAmount(expense),
            0
        );

        const totalBudget = Number(project.totalAmount || 0);

        const completedTasks = tasks.filter(
            task => task.status === "COMPLETED"
        ).length;

        const totalTasks = tasks.length;

        const progress = totalTasks > 0
            ? (completedTasks / totalTasks) * 100
            : 0;

        return {

            id: project.id,
            name: project.projectName,
            status: project.projectStatus || "—",

            budget: totalBudget,
            revenue: paidRevenue,
            expenses: totalExpense,

            outstanding: totalBudget - paidRevenue,

            profit: paidRevenue - totalExpense,

            totalTasks: totalTasks,
            completedTasks: completedTasks,
            progress: progress

        };
    });
}


// ========================================
// GET SELECTED PROJECT SCOPE
// ========================================

function getSelectedProjectScope() {

    const selectedId = reportProjectFilter.value || "ALL";

    if (selectedId === "ALL") {

        return {
            projects: projectReports,
            payments: allPayments,
            expenses: allExpenses,
            tasks: allTasks
        };
    }

    const projects = projectReports.filter(
        project => String(project.id) === selectedId
    );

    const payments = allPayments.filter(
        payment => String(getProjectId(payment)) === selectedId
    );

    const expenses = allExpenses.filter(
        expense => String(getProjectId(expense)) === selectedId
    );

    const tasks = allTasks.filter(
        task => String(getProjectId(task)) === selectedId
    );

    return {
        projects,
        payments,
        expenses,
        tasks
    };
}


// ========================================
// LOAD REPORT DATA
// ========================================

async function loadReports() {

    if (isLoadingReports) {
        return;
    }

    isLoadingReports = true;

    refreshReportsButton.disabled = true;
    refreshReportsButton.textContent = "Refreshing...";

    showMessage("Loading business reports...");

    try {

        const results = await Promise.allSettled([

            apiRequest("/api/dashboard"),
            apiRequest("/api/projects"),
            apiRequest("/api/payments"),
            apiRequest("/api/expenses"),
            apiRequest("/api/tasks")

        ]);

        const names = [
            "Dashboard",
            "Projects",
            "Payments",
            "Expenses",
            "Tasks"
        ];

        results.forEach((result, index) => {

            if (result.status === "rejected") {

                console.error(
                    `${names[index]} API failed:`,
                    result.reason
                );
            }
        });

        // Projects, payments, expenses and tasks are required
        // for building the detailed report.
        const requiredIndexes = [1, 2, 3, 4];

        const failedRequired = requiredIndexes.filter(
            index => results[index].status === "rejected"
        );

        if (failedRequired.length > 0) {

            throw new Error(
                "Some report data could not be loaded. Check the API response and try refreshing."
            );
        }

        dashboardData =
            results[0].status === "fulfilled"
                ? results[0].value || {}
                : {};

        allProjects = Array.isArray(results[1].value)
            ? results[1].value
            : [];

        allPayments = Array.isArray(results[2].value)
            ? results[2].value
            : [];

        allExpenses = Array.isArray(results[3].value)
            ? results[3].value
            : [];

        allTasks = Array.isArray(results[4].value)
            ? results[4].value
            : [];


        buildProjectReports();

        renderProjectFilter();

        renderAllReports();

        reportGeneratedAt.textContent =
            `Last updated: ${formatDateTime(new Date())}`;

        showMessage(
            "Reports refreshed successfully.",
            "success"
        );

    } catch (error) {

        console.error("Reports loading error:", error);

        showMessage(error.message, "error");

    } finally {

        isLoadingReports = false;

        refreshReportsButton.disabled = false;
        refreshReportsButton.textContent = "Refresh Data";
    }
}


// ========================================
// PROJECT FILTER
// ========================================

function renderProjectFilter() {

    const previousValue = reportProjectFilter.value || "ALL";

    reportProjectFilter.replaceChildren();

    const allOption = document.createElement("option");

    allOption.value = "ALL";
    allOption.textContent = "All Projects";

    reportProjectFilter.appendChild(allOption);

    allProjects.forEach(project => {

        const option = document.createElement("option");

        option.value = String(project.id);
        option.textContent =
            `${project.projectName} (#${project.id})`;

        reportProjectFilter.appendChild(option);
    });

    const exists = Array.from(reportProjectFilter.options)
        .some(option => option.value === previousValue);

    reportProjectFilter.value =
        exists ? previousValue : "ALL";
}


// ========================================
// RENDER ALL REPORT SECTIONS
// ========================================

function renderAllReports() {

    const scope = getSelectedProjectScope();

    const budget = scope.projects.reduce(
        (sum, project) => sum + project.budget,
        0
    );

    const revenue = scope.payments
        .filter(payment => payment.paymentStatus === "PAID")
        .reduce(
            (sum, payment) => sum + getPaymentAmount(payment),
            0
        );

    const expenses = scope.expenses.reduce(
        (sum, expense) => sum + getExpenseAmount(expense),
        0
    );

    const outstanding = budget - revenue;
    const profit = revenue - expenses;

    const completedTasks = scope.tasks.filter(
        task => task.status === "COMPLETED"
    ).length;

    const totalTasks = scope.tasks.length;

    const completion = totalTasks > 0
        ? (completedTasks / totalTasks) * 100
        : 0;


    renderBusinessStatistics({
        budget,
        revenue,
        expenses,
        outstanding,
        profit,
        completion
    });

    renderMonthlyChart(scope);
    renderExpenseBreakdown(scope);
    renderProjectComparison(scope);
    renderProjectProgress(scope);
    renderProjectTable(scope);
}


// ========================================
// BUSINESS STATISTICS
// ========================================

function renderBusinessStatistics(data) {

    reportTotalBudget.textContent =
        formatCurrency(data.budget);

    reportTotalRevenue.textContent =
        formatCurrency(data.revenue);

    reportTotalExpenses.textContent =
        formatCurrency(data.expenses);

    reportTotalProfit.textContent =
        formatCurrency(data.profit);

    reportOutstanding.textContent =
        formatCurrency(data.outstanding);

    reportTaskCompletion.textContent =
        formatPercent(data.completion);


    reportTotalProfit.classList.toggle(
        "report-positive",
        data.profit >= 0
    );

    reportTotalProfit.classList.toggle(
        "report-negative",
        data.profit < 0
    );


    summaryBudget.textContent =
        formatCurrency(data.budget);

    summaryRevenue.textContent =
        formatCurrency(data.revenue);

    summaryExpenses.textContent =
        formatCurrency(data.expenses);

    summaryOutstanding.textContent =
        formatCurrency(data.outstanding);

    summaryProfit.textContent =
        formatCurrency(data.profit);

    summaryProfit.classList.toggle(
        "report-positive",
        data.profit >= 0
    );

    summaryProfit.classList.toggle(
        "report-negative",
        data.profit < 0
    );
}


// ========================================
// MONTHLY TREND DATA
// ========================================

function getLastSixMonths() {

    const months = [];
    const today = new Date();

    for (let offset = 5; offset >= 0; offset--) {

        const date = new Date(
            today.getFullYear(),
            today.getMonth() - offset,
            1
        );

        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");

        months.push({

            key: `${year}-${month}`,

            label: date.toLocaleDateString("en-IN", {
                month: "short"
            }),

            revenue: 0,
            expenses: 0

        });
    }

    return months;
}


function getRecordMonth(value) {

    if (!value) {
        return null;
    }

    return String(value).substring(0, 7);
}


function getMonthlyTrend(scope) {

    const months = getLastSixMonths();

    const monthMap = new Map(
        months.map(month => [month.key, month])
    );

    scope.payments
        .filter(payment => payment.paymentStatus === "PAID")
        .forEach(payment => {

            const month = monthMap.get(
                getRecordMonth(payment.paymentDate)
            );

            if (month) {
                month.revenue += getPaymentAmount(payment);
            }
        });

    scope.expenses.forEach(expense => {

        const month = monthMap.get(
            getRecordMonth(expense.expenseDate)
        );

        if (month) {
            month.expenses += getExpenseAmount(expense);
        }
    });

    return months;
}


// ========================================
// SVG LINE CHART
// ========================================

function renderMonthlyChart(scope) {

    const data = getMonthlyTrend(scope);

    const width = 760;
    const height = 270;

    const left = 65;
    const right = 20;
    const top = 20;
    const bottom = 42;

    const plotWidth = width - left - right;
    const plotHeight = height - top - bottom;

    const maxValue = Math.max(
        1,
        ...data.map(item => Math.max(item.revenue, item.expenses))
    );

    const roundedMax = Math.ceil(maxValue / 1000) * 1000 || 1000;

    const step = roundedMax / 4;

    const xPosition = index =>
        left + (plotWidth / (data.length - 1)) * index;

    const yPosition = value =>
        top + plotHeight - (value / roundedMax) * plotHeight;


    const revenuePoints = data.map((item, index) => ({
        x: xPosition(index),
        y: yPosition(item.revenue),
        value: item.revenue,
        label: item.label
    }));

    const expensePoints = data.map((item, index) => ({
        x: xPosition(index),
        y: yPosition(item.expenses),
        value: item.expenses,
        label: item.label
    }));


    function makePath(points) {

        return points.map((point, index) =>

            `${index === 0 ? "M" : "L"} ${point.x} ${point.y}`

        ).join(" ");
    }


    function makeArea(points) {

        return [
            `M ${points[0].x} ${top + plotHeight}`,
            ...points.map(point => `L ${point.x} ${point.y}`),
            `L ${points[points.length - 1].x} ${top + plotHeight}`,
            "Z"
        ].join(" ");
    }


    let gridLines = "";

    for (let index = 0; index <= 4; index++) {

        const value = step * index;
        const y = yPosition(value);

        gridLines += `
            <line
                x1="${left}"
                y1="${y}"
                x2="${width - right}"
                y2="${y}"
                class="chart-grid-line"
            />

            <text
                x="${left - 10}"
                y="${y + 3}"
                text-anchor="end"
                class="chart-axis-label"
            >${formatCompactCurrency(value)}</text>
        `;
    }


    let monthLabels = "";

    data.forEach((item, index) => {

        monthLabels += `
            <text
                x="${xPosition(index)}"
                y="${height - 12}"
                text-anchor="middle"
                class="chart-axis-label"
            >${item.label}</text>
        `;
    });


    const revenueCircles = revenuePoints.map(point => `
        <circle
            cx="${point.x}"
            cy="${point.y}"
            r="4"
            class="chart-revenue-point"
        >
            <title>${point.label} Revenue: ${formatCurrency(point.value)}</title>
        </circle>
    `).join("");


    const expenseCircles = expensePoints.map(point => `
        <circle
            cx="${point.x}"
            cy="${point.y}"
            r="4"
            class="chart-expense-point"
        >
            <title>${point.label} Expenses: ${formatCurrency(point.value)}</title>
        </circle>
    `).join("");


    monthlyTrendChart.innerHTML = `
        <svg
            viewBox="0 0 ${width} ${height}"
            role="img"
            aria-label="Monthly revenue and expenses chart"
        >

            ${gridLines}

            <path
                d="${makeArea(expensePoints)}"
                class="chart-expense-area"
            />

            <path
                d="${makeArea(revenuePoints)}"
                class="chart-revenue-area"
            />

            <path
                d="${makePath(expensePoints)}"
                class="chart-expense-line"
            />

            <path
                d="${makePath(revenuePoints)}"
                class="chart-revenue-line"
            />

            ${expenseCircles}
            ${revenueCircles}
            ${monthLabels}

        </svg>
    `;
}


// ========================================
// EXPENSE CATEGORY DONUT
// ========================================

function renderExpenseBreakdown(scope) {

    const categories = Object.keys(CATEGORY_CONFIG).map(category => {

        const amount = scope.expenses
            .filter(expense => expense.category === category)
            .reduce(
                (sum, expense) => sum + getExpenseAmount(expense),
                0
            );

        return {
            key: category,
            label: CATEGORY_CONFIG[category].label,
            color: CATEGORY_CONFIG[category].color,
            amount
        };
    });

    const total = categories.reduce(
        (sum, category) => sum + category.amount,
        0
    );

    categoryExpenseTotal.textContent =
        formatCurrency(total);


    if (total <= 0) {

        expenseDonut.style.background = "#f3f0ec";

    } else {

        let currentPercent = 0;

        const segments = categories
            .filter(category => category.amount > 0)
            .map(category => {

                const start = currentPercent;

                const percent =
                    (category.amount / total) * 100;

                currentPercent += percent;

                return `${category.color} ${start}% ${currentPercent}%`;
            });

        expenseDonut.style.background =
            `conic-gradient(${segments.join(", ")})`;
    }


    expenseLegend.replaceChildren();

    categories.forEach(category => {

        const item = document.createElement("div");
        item.className = "expense-legend-item";

        const color = document.createElement("span");
        color.className = "expense-legend-color";
        color.style.background = category.color;

        const label = document.createElement("span");
        label.className = "expense-legend-label";
        label.textContent = category.label;

        const value = document.createElement("strong");
        value.className = "expense-legend-value";
        value.textContent = formatCurrency(category.amount);

        item.append(color, label, value);

        expenseLegend.appendChild(item);
    });
}


// ========================================
// PROJECT FINANCIAL COMPARISON
// ========================================

function renderProjectComparison(scope) {

    projectComparisonChart.replaceChildren();

    if (scope.projects.length === 0) {

        showEmptyState(
            projectComparisonChart,
            "No project data available."
        );

        return;
    }

    const maxValue = Math.max(
        1,
        ...scope.projects.flatMap(project => [
            project.revenue,
            project.expenses
        ])
    );

    scope.projects.forEach(project => {

        const row = document.createElement("div");
        row.className = "project-comparison-row";

        const name = document.createElement("div");
        name.className = "project-comparison-name";
        name.textContent = project.name;

        const bars = document.createElement("div");
        bars.className = "project-comparison-bars";

        bars.append(
            createComparisonBar(
                "Revenue",
                project.revenue,
                maxValue,
                "revenue-fill"
            ),

            createComparisonBar(
                "Expenses",
                project.expenses,
                maxValue,
                "expense-fill"
            )
        );

        row.append(name, bars);

        projectComparisonChart.appendChild(row);
    });
}


function createComparisonBar(label, amount, maxValue, className) {

    const line = document.createElement("div");
    line.className = "comparison-bar-line";

    const text = document.createElement("span");
    text.className = "comparison-bar-label";
    text.textContent = label;

    const track = document.createElement("div");
    track.className = "comparison-bar-track";

    const fill = document.createElement("div");
    fill.className = `comparison-bar-fill ${className}`;

    fill.style.width =
        `${Math.max(0, (amount / maxValue) * 100)}%`;

    track.appendChild(fill);

    const value = document.createElement("strong");
    value.className = "comparison-bar-value";
    value.textContent = formatCurrency(amount);

    line.append(text, track, value);

    return line;
}


// ========================================
// PROJECT PROGRESS
// ========================================

function renderProjectProgress(scope) {

    projectProgressList.replaceChildren();

    if (scope.projects.length === 0) {

        showEmptyState(
            projectProgressList,
            "No project progress data available."
        );

        return;
    }

    scope.projects.forEach(project => {

        const wrapper = document.createElement("div");
        wrapper.className = "project-progress-item";

        const top = document.createElement("div");
        top.className = "project-progress-top";

        const name = document.createElement("span");
        name.className = "project-progress-name";
        name.textContent = project.name;

        const percent = document.createElement("strong");
        percent.className = "project-progress-percent";
        percent.textContent = formatPercent(project.progress);

        top.append(name, percent);


        const track = document.createElement("div");
        track.className = "project-progress-track";

        const fill = document.createElement("div");
        fill.className = "project-progress-fill";

        fill.style.width =
            `${Math.max(0, Math.min(100, project.progress))}%`;

        track.appendChild(fill);


        const caption = document.createElement("span");
        caption.className = "project-progress-caption";

        caption.textContent =
            `${project.completedTasks} of ${project.totalTasks} tasks completed`;

        wrapper.append(top, track, caption);

        projectProgressList.appendChild(wrapper);
    });
}


// ========================================
// PROJECT REPORT TABLE
// ========================================

function renderProjectTable(scope) {

    projectReportTableBody.replaceChildren();

    projectReportCount.textContent =
        `${scope.projects.length} Projects`;

    if (scope.projects.length === 0) {

        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 7;
        cell.className = "table-placeholder";
        cell.textContent = "No project reports available.";

        row.appendChild(cell);
        projectReportTableBody.appendChild(row);

        return;
    }

    scope.projects.forEach(project => {

        const row = document.createElement("tr");


        const nameCell = createCell(project.name);
        nameCell.classList.add("report-project-name");

        row.appendChild(nameCell);


        const statusCell = document.createElement("td");

        const status = document.createElement("span");
        status.className = "report-project-status";
        status.textContent = project.status.replaceAll("_", " ");

        statusCell.appendChild(status);
        row.appendChild(statusCell);


        row.appendChild(createCell(formatCurrency(project.budget)));
        row.appendChild(createCell(formatCurrency(project.revenue)));
        row.appendChild(createCell(formatCurrency(project.expenses)));


        const profitCell = createCell(
            formatCurrency(project.profit)
        );

        profitCell.classList.add(
            project.profit >= 0
                ? "report-profit-positive"
                : "report-profit-negative"
        );

        row.appendChild(profitCell);


        const progressCell = document.createElement("td");
        progressCell.className = "report-progress-cell";

        const progressText = document.createElement("strong");
        progressText.textContent = formatPercent(project.progress);

        const progressTrack = document.createElement("div");
        progressTrack.className = "report-progress-mini";

        const progressFill = document.createElement("span");

        progressFill.style.width =
            `${Math.max(0, Math.min(100, project.progress))}%`;

        progressTrack.appendChild(progressFill);

        progressCell.append(progressText, progressTrack);

        row.appendChild(progressCell);

        projectReportTableBody.appendChild(row);
    });
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
// EMPTY STATE
// ========================================

function showEmptyState(container, message) {

    const element = document.createElement("div");

    element.className = "chart-empty";
    element.textContent = message;

    container.appendChild(element);
}


// ========================================
// CSV EXPORT
// ========================================

function escapeCsv(value) {

    const text = String(value ?? "");

    return `"${text.replaceAll('"', '""')}"`;
}


function exportReportCsv() {

    const scope = getSelectedProjectScope();

    const rows = [

        [
            "Project",
            "Status",
            "Budget",
            "Revenue",
            "Expenses",
            "Profit/Loss",
            "Outstanding",
            "Completed Tasks",
            "Total Tasks",
            "Progress (%)"
        ],

        ...scope.projects.map(project => [

            project.name,
            project.status,
            project.budget,
            project.revenue,
            project.expenses,
            project.profit,
            project.outstanding,
            project.completedTasks,
            project.totalTasks,
            project.progress.toFixed(2)

        ])

    ];

    const csv = rows
        .map(row => row.map(escapeCsv).join(","))
        .join("\r\n");

    const blob = new Blob(
        ["\uFEFF" + csv],
        { type: "text/csv;charset=utf-8;" }
    );

    const url = URL.createObjectURL(blob);

    const link = document.createElement("a");

    link.href = url;
    link.download = "furniture-business-report.csv";

    document.body.appendChild(link);

    link.click();
    link.remove();

    URL.revokeObjectURL(url);

    showMessage(
        "CSV report exported successfully.",
        "success"
    );
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

logoutButton.addEventListener("click", logout);

refreshReportsButton.addEventListener(
    "click",
    loadReports
);

exportReportButton.addEventListener(
    "click",
    exportReportCsv
);

reportProjectFilter.addEventListener(
    "change",
    renderAllReports
);


// ========================================
// INITIALIZE
// ========================================

async function initializeReportsPage() {

    const isAdmin = await verifyAdmin();

    if (!isAdmin) {
        return;
    }

    await loadReports();
}

initializeReportsPage();