// ========================================
// ATTENDANCE MANAGEMENT
// ========================================

const token = sessionStorage.getItem("fmsToken");

// Header and navigation
const adminName = document.getElementById("adminName");
const logoutButton = document.getElementById("logoutButton");

// Buttons
const addAttendanceRequestButton =
    document.getElementById("addAttendanceRequestButton");

// Statistics
const totalAttendanceElement = document.getElementById("totalAttendance");
const pendingRequestsElement = document.getElementById("pendingRequests");
const approvedRequestsElement = document.getElementById("approvedRequests");
const rejectedRequestsElement = document.getElementById("rejectedRequests");
const pendingTabCount = document.getElementById("pendingTabCount");

// Tabs
const attendanceTabButton = document.getElementById("attendanceTabButton");
const requestsTabButton = document.getElementById("requestsTabButton");
const attendanceRecordsPanel =
    document.getElementById("attendanceRecordsPanel");
const attendanceRequestsPanel =
    document.getElementById("attendanceRequestsPanel");

// Tables
const attendanceTableBody =
    document.getElementById("attendanceTableBody");
const requestsTableBody =
    document.getElementById("requestsTableBody");

// Filters
const searchAttendance = document.getElementById("searchAttendance");
const attendanceDateFilter =
    document.getElementById("attendanceDateFilter");
const attendanceStatusFilter =
    document.getElementById("attendanceStatusFilter");

// Calendar
const attendanceCalendarMonth =
    document.getElementById("attendanceCalendarMonth");
const attendanceCalendarGrid =
    document.getElementById("attendanceCalendarGrid");
const previousCalendarMonth =
    document.getElementById("previousCalendarMonth");
const nextCalendarMonth =
    document.getElementById("nextCalendarMonth");
const calendarMarkedDays =
    document.getElementById("calendarMarkedDays");
const calendarMonthEntries =
    document.getElementById("calendarMonthEntries");
const calendarSelectedDate =
    document.getElementById("calendarSelectedDate");
const calendarSelectedHint =
    document.getElementById("calendarSelectedHint");
const calendarDayTotal =
    document.getElementById("calendarDayTotal");
const calendarDayPresent =
    document.getElementById("calendarDayPresent");
const calendarDayAbsent =
    document.getElementById("calendarDayAbsent");
const calendarDayRecordCount =
    document.getElementById("calendarDayRecordCount");
const calendarDayRecords =
    document.getElementById("calendarDayRecords");
const clearCalendarDate =
    document.getElementById("clearCalendarDate");

const searchRequests = document.getElementById("searchRequests");
const requestStatusFilter =
    document.getElementById("requestStatusFilter");

// Messages
const attendanceRecordsMessage =
    document.getElementById("attendanceRecordsMessage");
const requestsMessage = document.getElementById("requestsMessage");

// Request modal
const requestDialog = document.getElementById("requestDialog");
const closeRequestModal = document.getElementById("closeRequestModal");
const cancelRequest = document.getElementById("cancelRequest");
const createRequestForm = document.getElementById("createRequestForm");

const requestProject = document.getElementById("requestProject");
const requestEmployee = document.getElementById("requestEmployee");
const requestDate = document.getElementById("requestDate");
const requestFormMessage =
    document.getElementById("requestFormMessage");

const requestSubmitButton =
    createRequestForm.querySelector('[type="submit"]');

// Review modal
const reviewDialog = document.getElementById("reviewDialog");
const reviewRequestForm =
    document.getElementById("reviewRequestForm");

const reviewTitle = document.getElementById("reviewTitle");
const reviewDescription =
    document.getElementById("reviewDescription");
const reviewSummary = document.getElementById("reviewSummary");
const reviewRemark = document.getElementById("reviewRemark");
const reviewFormMessage =
    document.getElementById("reviewFormMessage");

const closeReviewModal = document.getElementById("closeReviewModal");
const cancelReview = document.getElementById("cancelReview");
const confirmReviewButton =
    document.getElementById("confirmReviewButton");


// ========================================
// DATA
// ========================================

let allAttendance = [];
let allRequests = [];
let allProjects = [];

let reviewRequestId = null;
let reviewAction = null;

let isCreatingRequest = false;
let isReviewingRequest = false;

// Calendar state (uses the browser's local month and date).
const calendarToday = new Date();
let calendarViewYear = calendarToday.getFullYear();
let calendarViewMonth = calendarToday.getMonth();
let selectedCalendarDate = null;


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
        element === attendanceRecordsMessage ||
        element === requestsMessage
            ? `attendance-message ${type}`
            : `attendance-form-message ${type}`;
}


// ========================================
// DATE FORMATTERS
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


function formatDateTime(value) {

    if (!value) {
        return "—";
    }

    const parts = String(value).split("T");

    if (parts.length !== 2) {
        return String(value);
    }

    const date = formatDate(parts[0]);
    const time = parts[1].substring(0, 5);

    return `${date}, ${time}`;
}


function getToday() {

    const now = new Date();

    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const day = String(now.getDate()).padStart(2, "0");

    return `${year}-${month}-${day}`;
}


// ========================================
// LOAD ALL PAGE DATA
// ========================================

async function loadAttendanceData() {

    showMessage(
        attendanceRecordsMessage,
        "Loading attendance records..."
    );

    showMessage(
        requestsMessage,
        "Loading attendance requests..."
    );

    try {

        const [attendance, requests, projects] = await Promise.all([

            apiRequest("/api/attendances"),
            apiRequest("/api/attendance-requests"),
            apiRequest("/api/projects")

        ]);

        allAttendance = Array.isArray(attendance)
            ? attendance
            : [];

        allRequests = Array.isArray(requests)
            ? requests
            : [];

        allProjects = Array.isArray(projects)
            ? projects
            : [];

        renderAttendanceRecords();
        renderAttendanceRequests();
        renderAttendanceCalendar();
        renderCalendarDayDetails();

        updateStatistics();
        renderProjectOptions();

        showMessage(
            attendanceRecordsMessage,
            `${allAttendance.length} attendance record(s) loaded.`,
            "success"
        );

        showMessage(
            requestsMessage,
            `${allRequests.length} attendance request(s) loaded.`,
            "success"
        );

    } catch (error) {

        console.error("Attendance loading error:", error);

        showMessage(
            attendanceRecordsMessage,
            error.message,
            "error"
        );

        showMessage(
            requestsMessage,
            error.message,
            "error"
        );
    }
}


// ========================================
// UPDATE STATISTICS
// ========================================

function updateStatistics() {

    const pending = allRequests.filter(
        request => request.status === "PENDING"
    ).length;

    const approved = allRequests.filter(
        request => request.status === "APPROVED"
    ).length;

    const rejected = allRequests.filter(
        request => request.status === "REJECTED"
    ).length;

    totalAttendanceElement.textContent = allAttendance.length;

    pendingRequestsElement.textContent = pending;

    approvedRequestsElement.textContent = approved;

    rejectedRequestsElement.textContent = rejected;

    pendingTabCount.textContent = pending;
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
// STATUS BADGE
// ========================================

function createAttendanceBadge(status) {

    const badge = document.createElement("span");

    badge.className = "attendance-status";

    const values = {
        PRESENT: ["Present", "attendance-present"],
        ABSENT: ["Absent", "attendance-absent"],
        PENDING: ["Pending", "request-pending"],
        APPROVED: ["Approved", "request-approved"],
        REJECTED: ["Rejected", "request-rejected"]
    };

    const config = values[status] || [status || "Unknown", ""];

    badge.textContent = config[0];

    if (config[1]) {
        badge.classList.add(config[1]);
    }

    return badge;
}



// ========================================
// ATTENDANCE CALENDAR
// ========================================

function toIsoDate(year, monthIndex, day) {
    const month = String(monthIndex + 1).padStart(2, "0");
    const date = String(day).padStart(2, "0");
    return `${year}-${month}-${date}`;
}

function getCalendarMonthRecords() {
    return allAttendance.filter(record => {
        if (!record.attendanceDate) return false;
        const parts = String(record.attendanceDate).split("-");
        return parts.length === 3 &&
            Number(parts[0]) === calendarViewYear &&
            Number(parts[1]) === calendarViewMonth + 1;
    });
}

function renderAttendanceCalendar() {
    const firstDay = new Date(calendarViewYear, calendarViewMonth, 1);
    const daysInMonth = new Date(
        calendarViewYear,
        calendarViewMonth + 1,
        0
    ).getDate();

    // Monday-first calendar: convert JS Sunday=0 to Monday=0.
    const leadingEmptyDays = (firstDay.getDay() + 6) % 7;
    const totalCalendarCells = Math.ceil(
        (leadingEmptyDays + daysInMonth) / 7
    ) * 7;
    const monthRecords = getCalendarMonthRecords();
    const recordsByDate = new Map();

    monthRecords.forEach(record => {
        const date = String(record.attendanceDate);
        if (!recordsByDate.has(date)) recordsByDate.set(date, []);
        recordsByDate.get(date).push(record);
    });

    attendanceCalendarMonth.textContent = firstDay.toLocaleDateString(
        "en-IN",
        { month: "long", year: "numeric" }
    );

    calendarMarkedDays.textContent = recordsByDate.size;
    calendarMonthEntries.textContent = monthRecords.length;
    attendanceCalendarGrid.replaceChildren();

    for (let cellIndex = 0; cellIndex < totalCalendarCells; cellIndex++) {
        const dayNumber = cellIndex - leadingEmptyDays + 1;

        if (dayNumber < 1 || dayNumber > daysInMonth) {
            const emptyCell = document.createElement("div");
            emptyCell.className = "calendar-day is-empty";
            emptyCell.setAttribute("aria-hidden", "true");
            attendanceCalendarGrid.appendChild(emptyCell);
            continue;
        }

        const dateKey = toIsoDate(
            calendarViewYear,
            calendarViewMonth,
            dayNumber
        );
        const dayRecords = recordsByDate.get(dateKey) || [];
        const presentCount = dayRecords.filter(
            record => record.attendanceStatus === "PRESENT"
        ).length;
        const absentCount = dayRecords.filter(
            record => record.attendanceStatus === "ABSENT"
        ).length;

        const dayButton = document.createElement("button");
        dayButton.type = "button";
        dayButton.className = "calendar-day";
        dayButton.dataset.date = dateKey;
        dayButton.setAttribute("aria-label", `${formatDate(dateKey)}: ${dayRecords.length} attendance record(s)`);
        dayButton.setAttribute("aria-pressed", String(selectedCalendarDate === dateKey));

        if (dayRecords.length > 0) {
            dayButton.classList.add("has-records");

            if (presentCount > 0 && absentCount > 0) {
                dayButton.classList.add("has-mixed");
            } else if (presentCount > 0) {
                dayButton.classList.add("has-present");
            } else if (absentCount > 0) {
                dayButton.classList.add("has-absent");
            }
        }

        if (selectedCalendarDate === dateKey) {
            dayButton.classList.add("is-selected");
        }

        if (dateKey === getToday()) {
            dayButton.classList.add("is-today");
        }

        const number = document.createElement("span");
        number.className = "calendar-day-number";
        number.textContent = dayNumber;
        dayButton.appendChild(number);

        if (dayRecords.length > 0) {
            const count = document.createElement("span");
            count.className = "calendar-day-count";
            count.textContent = `${dayRecords.length} ${dayRecords.length === 1 ? "entry" : "entries"}`;
            dayButton.appendChild(count);
        }

        attendanceCalendarGrid.appendChild(dayButton);
    }
}

function renderCalendarDayDetails() {
    calendarDayRecords.replaceChildren();

    if (!selectedCalendarDate) {
        calendarSelectedDate.textContent = "Select a date";
        calendarSelectedHint.textContent = "Choose a date in the calendar to view attendance.";
        calendarDayTotal.textContent = "0";
        calendarDayPresent.textContent = "0";
        calendarDayAbsent.textContent = "0";
        calendarDayRecordCount.textContent = "0 records";

        const empty = document.createElement("div");
        empty.className = "calendar-day-empty";
        empty.textContent = "Select a calendar date to see its employee attendance records.";
        calendarDayRecords.appendChild(empty);
        return;
    }

    const dayRecords = allAttendance
        .filter(record => record.attendanceDate === selectedCalendarDate)
        .slice()
        .sort((a, b) => String(a.employeeName || "").localeCompare(String(b.employeeName || "")));

    const presentCount = dayRecords.filter(
        record => record.attendanceStatus === "PRESENT"
    ).length;
    const absentCount = dayRecords.filter(
        record => record.attendanceStatus === "ABSENT"
    ).length;

    calendarSelectedDate.textContent = formatDate(selectedCalendarDate);
    calendarSelectedHint.textContent = dayRecords.length
        ? "Attendance recorded for this date."
        : "No attendance has been recorded for this date.";

    calendarDayTotal.textContent = dayRecords.length;
    calendarDayPresent.textContent = presentCount;
    calendarDayAbsent.textContent = absentCount;
    calendarDayRecordCount.textContent = `${dayRecords.length} ${dayRecords.length === 1 ? "record" : "records"}`;

    if (dayRecords.length === 0) {
        const empty = document.createElement("div");
        empty.className = "calendar-day-empty";
        empty.textContent = "No attendance records for this date.";
        calendarDayRecords.appendChild(empty);
        return;
    }

    dayRecords.forEach(record => {
        const row = document.createElement("div");
        row.className = "calendar-person-row";

        const main = document.createElement("div");
        main.className = "calendar-person-main";

        const name = document.createElement("strong");
        name.textContent = record.employeeName || "Unknown employee";

        const meta = document.createElement("small");
        const code = record.employeeCode ? ` · ${record.employeeCode}` : "";
        const project = record.projectName || "No project";
        meta.textContent = `${project}${code}`;

        main.append(name, meta);
        row.append(main, createAttendanceBadge(record.attendanceStatus));
        calendarDayRecords.appendChild(row);
    });
}

function selectCalendarDate(date) {
    selectedCalendarDate = date;
    attendanceDateFilter.value = date;
    renderAttendanceCalendar();
    renderCalendarDayDetails();
    renderAttendanceRecords();
}

function changeCalendarMonth(offset) {
    const nextMonth = new Date(
        calendarViewYear,
        calendarViewMonth + offset,
        1
    );

    calendarViewYear = nextMonth.getFullYear();
    calendarViewMonth = nextMonth.getMonth();

    // Clear the selected date when browsing to another month.
    selectedCalendarDate = null;
    attendanceDateFilter.value = "";

    renderAttendanceCalendar();
    renderCalendarDayDetails();
    renderAttendanceRecords();
}

function syncCalendarWithDateFilter() {
    const selectedDate = attendanceDateFilter.value;

    if (!selectedDate) {
        selectedCalendarDate = null;
    } else {
        selectedCalendarDate = selectedDate;
        const [year, month] = selectedDate.split("-").map(Number);
        calendarViewYear = year;
        calendarViewMonth = month - 1;
    }

    renderAttendanceCalendar();
    renderCalendarDayDetails();
    renderAttendanceRecords();
}

// ========================================
// ATTENDANCE RECORD FILTERS
// ========================================

function getFilteredAttendance() {

    const query = searchAttendance.value
        .trim()
        .toLowerCase();

    const selectedStatus = attendanceStatusFilter.value;
    const selectedDate = attendanceDateFilter.value;

    return allAttendance.filter(record => {

        const searchableText = [

            record.id,
            record.employeeName,
            record.employeeCode,
            record.projectName,
            record.attendanceStatus,
            record.attendanceDate

        ].join(" ").toLowerCase();

        const matchesSearch = searchableText.includes(query);

        const matchesStatus =
            selectedStatus === "ALL" ||
            record.attendanceStatus === selectedStatus;

        const matchesDate =
            !selectedDate ||
            record.attendanceDate === selectedDate;

        return matchesSearch && matchesStatus && matchesDate;
    });
}


// ========================================
// RENDER ATTENDANCE RECORDS
// ========================================

function renderAttendanceRecords() {

    const records = getFilteredAttendance();

    attendanceTableBody.replaceChildren();

    if (records.length === 0) {

        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 7;
        cell.className = "table-placeholder";

        cell.textContent = allAttendance.length === 0
            ? "No attendance records available."
            : "No records match your filters.";

        row.appendChild(cell);
        attendanceTableBody.appendChild(row);

        return;
    }

    records.forEach(record => {

        const row = document.createElement("tr");

        row.appendChild(createCell(`#${record.id}`));
        row.appendChild(createCell(record.employeeName));
        row.appendChild(createCell(record.employeeCode));
        row.appendChild(createCell(record.projectName));
        row.appendChild(createCell(formatDate(record.attendanceDate)));

        const statusCell = document.createElement("td");

        statusCell.appendChild(
            createAttendanceBadge(record.attendanceStatus)
        );

        row.appendChild(statusCell);

        row.appendChild(createCell(formatDateTime(record.markedAt)));

        attendanceTableBody.appendChild(row);
    });
}


// ========================================
// REQUEST FILTERS
// ========================================

function getFilteredRequests() {

    const query = searchRequests.value
        .trim()
        .toLowerCase();

    const selectedStatus = requestStatusFilter.value;

    return allRequests.filter(request => {

        const searchableText = [

            request.id,
            request.employeeName,
            request.employeeCode,
            request.projectName,
            request.attendanceDate,
            request.status,
            request.remark

        ].join(" ").toLowerCase();

        const matchesSearch = searchableText.includes(query);

        const matchesStatus =
            selectedStatus === "ALL" ||
            request.status === selectedStatus;

        return matchesSearch && matchesStatus;
    });
}


// ========================================
// RENDER ATTENDANCE REQUESTS
// ========================================

function renderAttendanceRequests() {

    const requests = getFilteredRequests();

    requestsTableBody.replaceChildren();

    if (requests.length === 0) {

        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 9;
        cell.className = "table-placeholder";

        cell.textContent = allRequests.length === 0
            ? "No attendance requests available."
            : "No requests match your filters.";

        row.appendChild(cell);
        requestsTableBody.appendChild(row);

        return;
    }

    requests.forEach(request => {

        const row = document.createElement("tr");

        row.appendChild(createCell(`#${request.id}`));
        row.appendChild(createCell(request.employeeName));
        row.appendChild(createCell(request.employeeCode));
        row.appendChild(createCell(request.projectName));
        row.appendChild(createCell(formatDate(request.attendanceDate)));
        row.appendChild(createCell(formatDateTime(request.requestedAt)));

        const statusCell = document.createElement("td");

        statusCell.appendChild(
            createAttendanceBadge(request.status)
        );

        row.appendChild(statusCell);

        row.appendChild(createCell(request.remark));

        const actionCell = document.createElement("td");

        if (request.status === "PENDING") {

            const actions = document.createElement("div");
            actions.className = "request-actions";

            const approveButton = document.createElement("button");

            approveButton.type = "button";
            approveButton.className =
                "request-action-button approve-button";

            approveButton.textContent = "Approve";
            approveButton.dataset.action = "approve";
            approveButton.dataset.requestId = request.id;

            const rejectButton = document.createElement("button");

            rejectButton.type = "button";
            rejectButton.className =
                "request-action-button reject-button";

            rejectButton.textContent = "Reject";
            rejectButton.dataset.action = "reject";
            rejectButton.dataset.requestId = request.id;

            actions.append(approveButton, rejectButton);
            actionCell.appendChild(actions);

        } else {

            const reviewedLabel = document.createElement("span");

            reviewedLabel.className = "reviewed-label";
            reviewedLabel.textContent = "Reviewed";

            actionCell.appendChild(reviewedLabel);
        }

        row.appendChild(actionCell);

        requestsTableBody.appendChild(row);
    });
}


// ========================================
// PROJECT DROPDOWN
// ========================================

function renderProjectOptions() {

    requestProject.replaceChildren();

    const defaultOption = document.createElement("option");

    defaultOption.value = "";
    defaultOption.textContent = allProjects.length
        ? "Select a project"
        : "No projects available";

    requestProject.appendChild(defaultOption);

    allProjects.forEach(project => {

        const option = document.createElement("option");

        option.value = project.id;
        option.textContent =
            `${project.projectName} (#${project.id})`;

        requestProject.appendChild(option);
    });

    requestProject.disabled = allProjects.length === 0;

    resetRequestEmployee();
}


// ========================================
// RESET EMPLOYEE DROPDOWN
// ========================================

function resetRequestEmployee(
    message = "First select a project"
) {

    requestEmployee.replaceChildren();

    const option = document.createElement("option");

    option.value = "";
    option.textContent = message;

    requestEmployee.appendChild(option);

    requestEmployee.disabled = true;
}


// ========================================
// LOAD PROJECT ASSIGNED EMPLOYEES
// ========================================

async function loadProjectEmployees(projectId) {

    resetRequestEmployee("Loading employees...");

    if (!projectId) {
        resetRequestEmployee();
        return;
    }

    try {

        const assignments = await apiRequest(
            `/api/projects/${encodeURIComponent(projectId)}/employees`
        );

        const employeeOptions = [];
        const seenIds = new Set();

        (Array.isArray(assignments) ? assignments : [])
            .forEach(assignment => {

                const employee = assignment.employee || assignment;

                const employeeId =
                    employee?.id ??
                    assignment.employeeId;

                if (
                    employeeId === undefined ||
                    employeeId === null ||
                    seenIds.has(String(employeeId))
                ) {
                    return;
                }

                seenIds.add(String(employeeId));

                const employeeName =
                    employee.user?.name ||
                    employee.employeeName ||
                    employee.name ||
                    employee.employeeCode ||
                    `Employee #${employeeId}`;

                employeeOptions.push({
                    id: employeeId,
                    name: employeeName,
                    code: employee.employeeCode || ""
                });
            });


        requestEmployee.replaceChildren();

        const defaultOption = document.createElement("option");

        defaultOption.value = "";

        defaultOption.textContent = employeeOptions.length
            ? "Select an employee"
            : "No employees assigned to this project";

        requestEmployee.appendChild(defaultOption);


        employeeOptions.forEach(employee => {

            const option = document.createElement("option");

            option.value = employee.id;

            option.textContent =
                employee.code
                    ? `${employee.name} - ${employee.code}`
                    : employee.name;

            requestEmployee.appendChild(option);
        });

        requestEmployee.disabled = employeeOptions.length === 0;

    } catch (error) {

        console.error("Employee loading error:", error);

        resetRequestEmployee("Unable to load employees");

        showMessage(
            requestFormMessage,
            error.message,
            "error"
        );
    }
}


// ========================================
// OPEN/CLOSE REQUEST MODAL
// ========================================

function openRequestModal() {

    createRequestForm.reset();

    showMessage(requestFormMessage, "");

    requestDate.max = getToday();
    requestDate.value = getToday();

    renderProjectOptions();

    requestDialog.classList.add("show");
    requestDialog.setAttribute("aria-hidden", "false");
}


function closeRequestModalWindow() {

    if (isCreatingRequest) {
        return;
    }

    requestDialog.classList.remove("show");
    requestDialog.setAttribute("aria-hidden", "true");

    createRequestForm.reset();

    resetRequestEmployee();

    showMessage(requestFormMessage, "");
}


// ========================================
// CREATE ATTENDANCE REQUEST
// ========================================

async function createAttendanceRequest(event) {

    event.preventDefault();

    if (isCreatingRequest) {
        return;
    }

    if (!createRequestForm.reportValidity()) {
        return;
    }

    const projectId = Number(requestProject.value);
    const employeeId = Number(requestEmployee.value);
    const attendanceDate = requestDate.value;

    if (!projectId || !employeeId || !attendanceDate) {

        showMessage(
            requestFormMessage,
            "Please select a project, employee and date.",
            "error"
        );

        return;
    }

    if (attendanceDate > getToday()) {

        showMessage(
            requestFormMessage,
            "Future attendance dates are not allowed.",
            "error"
        );

        return;
    }

    const validEmployee = Array.from(requestEmployee.options)
        .some(option => Number(option.value) === employeeId);

    if (!validEmployee || requestEmployee.disabled) {

        showMessage(
            requestFormMessage,
            "Select a valid employee assigned to this project.",
            "error"
        );

        return;
    }

    const payload = {
        employeeId: employeeId,
        projectId: projectId,
        attendanceDate: attendanceDate
    };

    isCreatingRequest = true;

    requestSubmitButton.disabled = true;
    requestSubmitButton.textContent = "Submitting...";

    showMessage(requestFormMessage, "Submitting attendance request...");

    try {

        await apiRequest("/api/attendance-requests", {
            method: "POST",
            body: JSON.stringify(payload)
        });

        requestDialog.classList.remove("show");
        requestDialog.setAttribute("aria-hidden", "true");
        createRequestForm.reset();
        resetRequestEmployee();

        await loadAttendanceData();

        showMessage(
            requestsMessage,
            "Attendance request created successfully.",
            "success"
        );

        switchTab("requests");

    } catch (error) {

        console.error("Request creation error:", error);

        showMessage(
            requestFormMessage,
            error.message || "Unable to create attendance request.",
            "error"
        );

    } finally {

        isCreatingRequest = false;

        requestSubmitButton.disabled = false;
        requestSubmitButton.textContent = "Submit Request";
    }
}


// ========================================
// OPEN REVIEW MODAL
// ========================================

function openReviewModal(requestId, action) {

    const request = allRequests.find(
        item => String(item.id) === String(requestId)
    );

    if (!request || request.status !== "PENDING") {
        return;
    }

    reviewRequestId = requestId;
    reviewAction = action;

    reviewTitle.textContent =
        action === "approve"
            ? "Approve Attendance Request"
            : "Reject Attendance Request";

    reviewDescription.textContent =
        action === "approve"
            ? "Confirm this attendance request."
            : "Confirm rejection of this attendance request.";

    reviewSummary.replaceChildren();

    const details = [
        ["Employee", request.employeeName],
        ["Employee Code", request.employeeCode],
        ["Project", request.projectName],
        ["Attendance Date", formatDate(request.attendanceDate)]
    ];

    details.forEach(([label, value]) => {

        const wrapper = document.createElement("div");
        wrapper.className = "review-detail";

        const title = document.createElement("span");
        title.textContent = label;

        const content = document.createElement("strong");
        content.textContent = value || "—";

        wrapper.append(title, content);
        reviewSummary.appendChild(wrapper);
    });

    reviewRemark.value = "";

    confirmReviewButton.textContent =
        action === "approve" ? "Approve Request" : "Reject Request";

    confirmReviewButton.classList.toggle(
        "reject-confirm-button",
        action === "reject"
    );

    showMessage(reviewFormMessage, "");

    reviewDialog.classList.add("show");
    reviewDialog.setAttribute("aria-hidden", "false");
}


// ========================================
// CLOSE REVIEW MODAL
// ========================================

function closeReviewModalWindow() {

    if (isReviewingRequest) {
        return;
    }

    reviewDialog.classList.remove("show");
    reviewDialog.setAttribute("aria-hidden", "true");

    reviewRequestId = null;
    reviewAction = null;

    reviewRequestForm.reset();

    showMessage(reviewFormMessage, "");
}


// ========================================
// APPROVE / REJECT REQUEST
// ========================================

async function reviewAttendanceRequest(event) {

    event.preventDefault();

    if (
        isReviewingRequest ||
        !reviewRequestId ||
        !reviewAction
    ) {
        return;
    }

    const endpointAction =
        reviewAction === "approve" ? "approve" : "reject";

    const remark = reviewRemark.value.trim();

    const payload = {
        remark: remark || null
    };

    const requestId = reviewRequestId;

    isReviewingRequest = true;

    confirmReviewButton.disabled = true;
    confirmReviewButton.textContent = "Processing...";

    showMessage(reviewFormMessage, "Processing request...");

    try {

        await apiRequest(
            `/api/attendance-requests/${encodeURIComponent(requestId)}/${endpointAction}`,
            {
                method: "PUT",
                body: JSON.stringify(payload)
            }
        );

        reviewDialog.classList.remove("show");
        reviewDialog.setAttribute("aria-hidden", "true");

        reviewRequestId = null;
        reviewAction = null;

        await loadAttendanceData();

        showMessage(
            requestsMessage,
            endpointAction === "approve"
                ? "Request approved. Attendance record created successfully."
                : "Attendance request rejected successfully.",
            "success"
        );

    } catch (error) {

        console.error("Review request error:", error);

        showMessage(
            reviewFormMessage,
            error.message || "Unable to review attendance request.",
            "error"
        );

    } finally {

        isReviewingRequest = false;

        confirmReviewButton.disabled = false;
    }
}


// ========================================
// TAB SWITCHING
// ========================================

function switchTab(tab) {

    const showRecords = tab === "records";

    attendanceRecordsPanel.classList.toggle(
        "hidden",
        !showRecords
    );

    attendanceRequestsPanel.classList.toggle(
        "hidden",
        showRecords
    );

    attendanceTabButton.classList.toggle(
        "active",
        showRecords
    );

    requestsTabButton.classList.toggle(
        "active",
        !showRecords
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

addAttendanceRequestButton.addEventListener(
    "click",
    openRequestModal
);

logoutButton.addEventListener("click", logout);


// Tabs
attendanceTabButton.addEventListener(
    "click",
    () => switchTab("records")
);

requestsTabButton.addEventListener(
    "click",
    () => switchTab("requests")
);


// Attendance filters
searchAttendance.addEventListener(
    "input",
    renderAttendanceRecords
);

attendanceDateFilter.addEventListener(
    "change",
    syncCalendarWithDateFilter
);

attendanceCalendarGrid.addEventListener("click", function(event) {
    const dayButton = event.target.closest("button[data-date]");
    if (!dayButton) return;
    selectCalendarDate(dayButton.dataset.date);
});

previousCalendarMonth.addEventListener(
    "click",
    () => changeCalendarMonth(-1)
);

nextCalendarMonth.addEventListener(
    "click",
    () => changeCalendarMonth(1)
);

clearCalendarDate.addEventListener("click", function() {
    selectedCalendarDate = null;
    attendanceDateFilter.value = "";
    renderAttendanceCalendar();
    renderCalendarDayDetails();
    renderAttendanceRecords();
});

attendanceStatusFilter.addEventListener(
    "change",
    renderAttendanceRecords
);


// Request filters
searchRequests.addEventListener(
    "input",
    renderAttendanceRequests
);

requestStatusFilter.addEventListener(
    "change",
    renderAttendanceRequests
);


// Project change
requestProject.addEventListener(
    "change",
    () => {
        showMessage(requestFormMessage, "");
        loadProjectEmployees(requestProject.value);
    }
);


// Create request
createRequestForm.addEventListener(
    "submit",
    createAttendanceRequest
);


// Review request
reviewRequestForm.addEventListener(
    "submit",
    reviewAttendanceRequest
);


// Review actions using event delegation
requestsTableBody.addEventListener("click", function(event) {

    const button = event.target.closest("[data-action]");

    if (!button) {
        return;
    }

    openReviewModal(
        button.dataset.requestId,
        button.dataset.action
    );
});


// Close request modal
closeRequestModal.addEventListener(
    "click",
    closeRequestModalWindow
);

cancelRequest.addEventListener(
    "click",
    closeRequestModalWindow
);


// Close review modal
closeReviewModal.addEventListener(
    "click",
    closeReviewModalWindow
);

cancelReview.addEventListener(
    "click",
    closeReviewModalWindow
);


// Click outside modal
requestDialog.addEventListener("click", function(event) {

    if (event.target === requestDialog) {
        closeRequestModalWindow();
    }
});

reviewDialog.addEventListener("click", function(event) {

    if (event.target === reviewDialog) {
        closeReviewModalWindow();
    }
});


// Escape key
document.addEventListener("keydown", function(event) {

    if (event.key !== "Escape") {
        return;
    }

    if (requestDialog.classList.contains("show")) {
        closeRequestModalWindow();
    }

    if (reviewDialog.classList.contains("show")) {
        closeReviewModalWindow();
    }
});


// ========================================
// INITIALIZE PAGE
// ========================================

async function initializeAttendancePage() {

    const isAdmin = await verifyAdmin();

    if (!isAdmin) {
        return;
    }

    requestDate.max = getToday();

    await loadAttendanceData();
}

initializeAttendancePage();