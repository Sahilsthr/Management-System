// ========================================
// TASK MANAGEMENT
// ========================================

// Authentication
const token = sessionStorage.getItem("fmsToken");

const adminName = document.getElementById("adminName");
const logoutButton = document.getElementById("logoutButton");

// Page elements
const addTaskButton = document.getElementById("addTaskButton");
const searchTasks = document.getElementById("searchTasks");
const filterTaskStatus = document.getElementById("filterTaskStatus");
const tasksTableBody = document.getElementById("tasksTableBody");
const tasksMessage = document.getElementById("tasksMessage");

// Statistics
const totalTasksElement = document.getElementById("totalTasks");
const pendingTasksElement = document.getElementById("pendingTasks");
const inProgressTasksElement = document.getElementById("inProgressTasks");
const completedTasksElement = document.getElementById("completedTasks");

// Modal
const taskDialog = document.getElementById("taskDialog");
const closeTaskModal = document.getElementById("closeTaskModal");
const cancelTask = document.getElementById("cancelTask");
const addTaskForm = document.getElementById("addTaskForm");

// Form fields
const taskName = document.getElementById("taskName");
const taskDescription = document.getElementById("taskDescription");
const taskProject = document.getElementById("taskProject");
const taskEmployee = document.getElementById("taskEmployee");
const taskPriority = document.getElementById("taskPriority");
const taskDueDate = document.getElementById("taskDueDate");
const taskFormMessage = document.getElementById("taskFormMessage");

// Submit button
const saveTaskButton = addTaskForm.querySelector(".save-task-button");


// ========================================
// DATA
// ========================================

let allTasks = [];
let allProjects = [];
let isSavingTask = false;
let isUpdatingStatus = false;


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
        element === tasksMessage
            ? `tasks-message ${type}`
            : `task-form-message ${type}`;
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
        return String(dateValue);
    }

    return `${parts[2]}-${parts[1]}-${parts[0]}`;
}


// ========================================
// LOAD PROJECTS
// ========================================

async function loadProjects() {

    try {

        const projects = await apiRequest("/api/projects");

        allProjects = Array.isArray(projects)
            ? projects
            : [];

        renderProjectDropdown();

    } catch (error) {

        console.error("Error loading projects:", error);

        taskProject.replaceChildren();

        const option = document.createElement("option");
        option.value = "";
        option.textContent = "Unable to load projects";

        taskProject.appendChild(option);

        taskProject.disabled = true;
    }
}


// ========================================
// PROJECT DROPDOWN
// ========================================

function renderProjectDropdown() {

    taskProject.replaceChildren();

    const defaultOption = document.createElement("option");

    defaultOption.value = "";
    defaultOption.textContent = allProjects.length
        ? "Select a project"
        : "No projects available";

    taskProject.appendChild(defaultOption);

    allProjects.forEach(project => {

        const option = document.createElement("option");

        option.value = project.id;
        option.textContent =
            `${project.projectName} (#${project.id})`;

        taskProject.appendChild(option);
    });

    taskProject.disabled = allProjects.length === 0;

    resetEmployeeDropdown();
}


// ========================================
// RESET EMPLOYEE DROPDOWN
// ========================================

function resetEmployeeDropdown(message = "First select a project") {

    taskEmployee.replaceChildren();

    const option = document.createElement("option");

    option.value = "";
    option.textContent = message;

    taskEmployee.appendChild(option);

    taskEmployee.disabled = true;
}


// ========================================
// LOAD PROJECT-ASSIGNED EMPLOYEES
// ========================================

async function loadAssignedEmployees(projectId) {

    resetEmployeeDropdown("Loading assigned employees...");

    if (!projectId) {
        resetEmployeeDropdown();
        return;
    }

    try {

        const assignments = await apiRequest(
            `/api/projects/${encodeURIComponent(projectId)}/employees`
        );

        const employeeOptions = [];
        const seenEmployeeIds = new Set();

        const assignmentList = Array.isArray(assignments)
            ? assignments
            : [];

        assignmentList.forEach(assignment => {

            // API returns ProjectEmployee objects with nested employee.
            const employee =
                assignment.employee ||
                assignment.assignedEmployee ||
                assignment;

            const employeeId =
                employee?.id ??
                assignment.employeeId;

            if (
                employeeId === null ||
                employeeId === undefined ||
                seenEmployeeIds.has(String(employeeId))
            ) {
                return;
            }

            seenEmployeeIds.add(String(employeeId));

            const employeeName =
                employee.user?.name ||
                employee.employeeName ||
                employee.name ||
                employee.employeeCode ||
                `Employee #${employeeId}`;

            const employeeCode = employee.employeeCode
                ? ` - ${employee.employeeCode}`
                : "";

            employeeOptions.push({
                id: employeeId,
                label: `${employeeName}${employeeCode}`
            });
        });


        taskEmployee.replaceChildren();

        const defaultOption = document.createElement("option");

        defaultOption.value = "";
        defaultOption.textContent = employeeOptions.length
            ? "Select assigned employee"
            : "No employees assigned to this project";

        taskEmployee.appendChild(defaultOption);


        employeeOptions.forEach(employee => {

            const option = document.createElement("option");

            option.value = employee.id;
            option.textContent = employee.label;

            taskEmployee.appendChild(option);
        });

        taskEmployee.disabled = employeeOptions.length === 0;

    } catch (error) {

        console.error("Error loading assigned employees:", error);

        resetEmployeeDropdown("Unable to load employees");

        showMessage(
            taskFormMessage,
            error.message,
            "error"
        );
    }
}


// ========================================
// LOAD ALL TASKS
// ========================================

async function loadTasks() {

    showMessage(tasksMessage, "Loading tasks...");

    try {

        const tasks = await apiRequest("/api/tasks");

        allTasks = Array.isArray(tasks)
            ? tasks
            : [];

        renderTasks();

        updateStatistics();

        showMessage(
            tasksMessage,
            `${allTasks.length} task(s) loaded successfully.`,
            "success"
        );

    } catch (error) {

        console.error("Error loading tasks:", error);

        tasksTableBody.replaceChildren();

        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 8;
        cell.className = "table-placeholder";
        cell.textContent = "Unable to load tasks.";

        row.appendChild(cell);
        tasksTableBody.appendChild(row);

        showMessage(
            tasksMessage,
            error.message,
            "error"
        );
    }
}


// ========================================
// UPDATE STATISTICS
// ========================================

function updateStatistics() {

    const pendingCount = allTasks.filter(
        task => task.status === "PENDING"
    ).length;

    const inProgressCount = allTasks.filter(
        task => task.status === "IN_PROGRESS"
    ).length;

    const completedCount = allTasks.filter(
        task => task.status === "COMPLETED"
    ).length;

    totalTasksElement.textContent = allTasks.length;
    pendingTasksElement.textContent = pendingCount;
    inProgressTasksElement.textContent = inProgressCount;
    completedTasksElement.textContent = completedCount;
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

function createStatusBadge(status) {

    const badge = document.createElement("span");

    badge.classList.add("task-status-badge");

    const labels = {
        PENDING: "Pending",
        IN_PROGRESS: "In Progress",
        COMPLETED: "Completed"
    };

    const classes = {
        PENDING: "status-pending",
        IN_PROGRESS: "status-in-progress",
        COMPLETED: "status-completed"
    };

    badge.classList.add(
        classes[status] || "status-pending"
    );

    badge.textContent = labels[status] || status || "Unknown";

    return badge;
}


// ========================================
// PRIORITY BADGE
// ========================================

function createPriorityBadge(priority) {

    const badge = document.createElement("span");

    badge.classList.add("task-priority-badge");

    const labels = {
        LOW: "Low",
        MEDIUM: "Medium",
        HIGH: "High"
    };

    const classes = {
        LOW: "priority-low",
        MEDIUM: "priority-medium",
        HIGH: "priority-high"
    };

    badge.classList.add(
        classes[priority] || "priority-medium"
    );

    badge.textContent =
        labels[priority] || priority || "Medium";

    return badge;
}


// ========================================
// STATUS UPDATE DROPDOWN
// ========================================

function createStatusSelect(task) {

    const select = document.createElement("select");

    select.className = "task-status-select";

    select.dataset.taskId = task.id;
    select.dataset.originalStatus = task.status;

    const statuses = [
        { value: "PENDING", label: "Pending" },
        { value: "IN_PROGRESS", label: "In Progress" },
        { value: "COMPLETED", label: "Completed" }
    ];

    statuses.forEach(status => {

        const option = document.createElement("option");

        option.value = status.value;
        option.textContent = status.label;
        option.selected = task.status === status.value;

        select.appendChild(option);
    });

    select.disabled =
        isUpdatingStatus ||
        !task.status ||
        !statuses.some(status => status.value === task.status);

    return select;
}


// ========================================
// RENDER TASKS TABLE
// ========================================

function renderTasks(tasks = getFilteredTasks()) {

    tasksTableBody.replaceChildren();

    if (tasks.length === 0) {

        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 8;
        cell.className = "table-placeholder";
        cell.textContent = allTasks.length === 0
            ? "No tasks created yet."
            : "No tasks match your search or filter.";

        row.appendChild(cell);
        tasksTableBody.appendChild(row);

        return;
    }

    tasks.forEach(task => {

        const row = document.createElement("tr");

        // Task ID
        row.appendChild(createCell(`#${task.id}`));

        // Task name
        row.appendChild(createCell(task.taskName));

        // Project
        const projectName =
            task.project?.projectName ||
            task.projectName ||
            "Unknown Project";

        row.appendChild(createCell(projectName));


        // Assigned employee
        const assignedEmployee =
            task.assignedEmployee ||
            task.employee ||
            null;

        const employeeName =
            assignedEmployee?.user?.name ||
            assignedEmployee?.employeeName ||
            assignedEmployee?.name ||
            assignedEmployee?.employeeCode ||
            "Unassigned";

        row.appendChild(createCell(employeeName));


        // Priority
        const priorityCell = document.createElement("td");

        priorityCell.appendChild(
            createPriorityBadge(task.priority)
        );

        row.appendChild(priorityCell);


        // Due date
        row.appendChild(
            createCell(formatDate(task.dueDate))
        );


        // Status badge
        const statusCell = document.createElement("td");

        statusCell.appendChild(
            createStatusBadge(task.status)
        );

        row.appendChild(statusCell);


        // Status update dropdown
        const updateCell = document.createElement("td");

        updateCell.appendChild(
            createStatusSelect(task)
        );

        row.appendChild(updateCell);

        tasksTableBody.appendChild(row);
    });
}


// ========================================
// SEARCH AND FILTER
// ========================================

function getFilteredTasks() {

    const query = searchTasks.value
        .trim()
        .toLowerCase();

    const selectedStatus = filterTaskStatus.value;

    return allTasks.filter(task => {

        const projectName =
            task.project?.projectName ||
            task.projectName ||
            "";

        const employee =
            task.assignedEmployee ||
            task.employee ||
            {};

        const employeeName =
            employee.user?.name ||
            employee.employeeName ||
            employee.name ||
            employee.employeeCode ||
            "";

        const searchableText = [

            task.id,
            task.taskName,
            task.description,
            projectName,
            employeeName,
            task.priority,
            task.status,
            task.dueDate

        ].join(" ").toLowerCase();

        const matchesSearch =
            searchableText.includes(query);

        const matchesStatus =
            selectedStatus === "ALL" ||
            task.status === selectedStatus;

        return matchesSearch && matchesStatus;
    });
}


function applyTaskFilters() {

    renderTasks();
}


// ========================================
// OPEN MODAL
// ========================================

function openTaskModal() {

    addTaskForm.reset();

    showMessage(taskFormMessage, "");

    renderProjectDropdown();

    taskDialog.classList.add("show");
    taskDialog.setAttribute("aria-hidden", "false");

}


// ========================================
// CLOSE MODAL
// ========================================

function closeModal() {

    if (isSavingTask) {
        return;
    }

    taskDialog.classList.remove("show");
    taskDialog.setAttribute("aria-hidden", "true");

    addTaskForm.reset();

    resetEmployeeDropdown();

    showMessage(taskFormMessage, "");
}


// ========================================
// CREATE NEW TASK
// ========================================

async function createTask(event) {

    event.preventDefault();

    if (isSavingTask) {
        return;
    }

    if (!addTaskForm.reportValidity()) {
        return;
    }

    const projectId = Number(taskProject.value);
    const assignedEmployeeId = Number(taskEmployee.value);

    if (!projectId || !assignedEmployeeId) {

        showMessage(
            taskFormMessage,
            "Select a project and an assigned employee.",
            "error"
        );

        return;
    }

    const selectedEmployeeExists =
        !taskEmployee.disabled &&
        Array.from(taskEmployee.options).some(option =>
            Number(option.value) === assignedEmployeeId
        );

    if (!selectedEmployeeExists) {

        showMessage(
            taskFormMessage,
            "Please select a valid employee assigned to this project.",
            "error"
        );

        return;
    }


    const payload = {

        taskName: taskName.value.trim(),

        description: taskDescription.value.trim() || null,

        priority: taskPriority.value,

        dueDate: taskDueDate.value || null,

        projectId: projectId,

        assignedEmployeeId: assignedEmployeeId

    };


    isSavingTask = true;

    saveTaskButton.disabled = true;
    saveTaskButton.textContent = "Creating...";

    showMessage(
        taskFormMessage,
        "Creating task..."
    );


    try {

        await apiRequest("/api/tasks", {

            method: "POST",

            body: JSON.stringify(payload)

        });

        taskDialog.classList.remove("show");
        taskDialog.setAttribute("aria-hidden", "true");

        addTaskForm.reset();
        resetEmployeeDropdown();

        showMessage(taskFormMessage, "");

        await loadTasks();

        showMessage(
            tasksMessage,
            "Task created successfully.",
            "success"
        );

    } catch (error) {

        console.error("Task creation error:", error);

        showMessage(
            taskFormMessage,
            error.message || "Unable to create task.",
            "error"
        );

    } finally {

        isSavingTask = false;

        saveTaskButton.disabled = false;
        saveTaskButton.textContent = "Create Task";

    }
}


// ========================================
// UPDATE TASK STATUS
// ========================================

async function updateTaskStatus(select) {

    if (isUpdatingStatus) {
        return;
    }

    const taskId = select.dataset.taskId;
    const previousStatus = select.dataset.originalStatus;
    const newStatus = select.value;

    if (!taskId || !newStatus || newStatus === previousStatus) {
        return;
    }

    isUpdatingStatus = true;

    // Prevent concurrent status changes
    tasksTableBody.querySelectorAll(".task-status-select")
        .forEach(control => {
            control.disabled = true;
        });

    select.value = newStatus;

    try {

        // API expects a raw JSON enum string, e.g. "COMPLETED".
        await apiRequest(
            `/api/tasks/${encodeURIComponent(taskId)}/status`,
            {
                method: "PUT",
                body: JSON.stringify(newStatus)
            }
        );

        showMessage(
            tasksMessage,
            `Task #${taskId} status updated successfully.`,
            "success"
        );

        await loadTasks();

    } catch (error) {

        console.error("Task status update error:", error);

        // Restore old status in the current row
        select.value = previousStatus;

        showMessage(
            tasksMessage,
            error.message || "Unable to update task status.",
            "error"
        );

    } finally {

        isUpdatingStatus = false;

        // Re-render to restore correct enabled/disabled state
        renderTasks();
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

addTaskButton.addEventListener(
    "click",
    openTaskModal
);

closeTaskModal.addEventListener(
    "click",
    closeModal
);

cancelTask.addEventListener(
    "click",
    closeModal
);

logoutButton.addEventListener(
    "click",
    logout
);

searchTasks.addEventListener(
    "input",
    applyTaskFilters
);

filterTaskStatus.addEventListener(
    "change",
    applyTaskFilters
);

taskProject.addEventListener(
    "change",
    function () {

        showMessage(taskFormMessage, "");

        loadAssignedEmployees(taskProject.value);

    }
);

addTaskForm.addEventListener(
    "submit",
    createTask
);


// Status update using event delegation
tasksTableBody.addEventListener("change", function(event) {

    const select = event.target.closest(".task-status-select");

    if (!select) {
        return;
    }

    updateTaskStatus(select);

});


// Close modal when clicking outside
taskDialog.addEventListener("click", function(event) {

    if (event.target === taskDialog) {
        closeModal();
    }

});


// Close modal using Escape key
document.addEventListener("keydown", function(event) {

    if (
        event.key === "Escape" &&
        taskDialog.classList.contains("show")
    ) {
        closeModal();
    }

});


// ========================================
// INITIALIZE PAGE
// ========================================

async function initializeTasksPage() {

    const isAdmin = await verifyAdmin();

    if (!isAdmin) {
        return;
    }

    await Promise.all([
        loadProjects(),
        loadTasks()
    ]);

}

initializeTasksPage();