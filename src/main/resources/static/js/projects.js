// ===== Get HTML Elements =====

const adminName = document.getElementById("adminName");
const projectsTableBody = document.getElementById("projectsTableBody");
const searchProjects = document.getElementById("searchProjects");
const projectsMessage = document.getElementById("projectsMessage");

const totalProjects = document.getElementById("totalProjects");
const activeProjects = document.getElementById("activeProjects");
const completedProjects = document.getElementById("completedProjects");

const addProjectButton = document.getElementById("addProjectButton");
const projectDialog = document.getElementById("projectDialog");
const addProjectForm = document.getElementById("addProjectForm");

const closeProjectModal = document.getElementById("closeProjectModal");
const cancelProject = document.getElementById("cancelProject");
const projectFormMessage = document.getElementById("projectFormMessage");

const projectClient = document.getElementById("projectClient");
const logoutButton = document.getElementById("logoutButton");

let allProjects = [];
let allClients = [];


// ===== Get JWT Token =====

function getToken() {
  return sessionStorage.getItem("fmsToken");
}


// ===== Currency Formatter =====

function formatCurrency(amount) {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0
  }).format(Number(amount || 0));
}


// ===== Show Message =====

function showMessage(message, color = "#c96b3d") {
  if (projectsMessage) {
    projectsMessage.textContent = message;
    projectsMessage.style.color = color;
  }
}


// ===== Verify Admin =====

async function verifyAdmin() {
  const token = getToken();
  if (!token) {
    window.location.replace("/login.html");
    return false;
  }

  try {
    const response = await fetch("/api/auth/me", {
      headers: {
        "Authorization": `Bearer ${token}`
      }
    });

    if (!response.ok) {
      throw new Error("Session expired");
    }

    const user = await response.json();

    if (user.role !== "ADMIN") {
      throw new Error("Admin access required");
    }

    if (adminName) adminName.textContent = user.name;
    return true;

  } catch (error) {
    sessionStorage.removeItem("fmsToken");
    sessionStorage.removeItem("fmsUser");
    window.location.replace("/login.html");
    return false;
  }
}


// ===== Load Clients for Dropdown =====

async function loadClients() {
  const response = await fetch("/api/clients", {
    headers: {
      "Authorization": `Bearer ${getToken()}`
    }
  });

  if (response.status === 401 || response.status === 403) {
    throw new Error("You are not authorized to access clients.");
  }

  if (!response.ok) {
    throw new Error("Unable to load clients.");
  }

  allClients = await response.json();

  if (projectClient) {
    projectClient.replaceChildren();

    const defaultOption = document.createElement("option");
    defaultOption.value = "";
    defaultOption.textContent = "Select a client";
    projectClient.appendChild(defaultOption);

    allClients.forEach(client => {
      const option = document.createElement("option");
      option.value = client.id;
      option.textContent = `${client.name} (#${client.id})`;
      projectClient.appendChild(option);
    });
  }
}


// ===== Get Project Status Class =====

function getStatusClass(status) {
  switch (status) {
    case "PLANNED":
      return "status-planned";
    case "IN_PROGRESS":
      return "status-in-progress";
    case "COMPLETED":
      return "status-completed";
    case "CANCELLED":
      return "status-cancelled";
    default:
      return "";
  }
}


// ===== Format Project Status =====

function formatStatus(status) {
  const statusNames = {
    PLANNED: "Planned",
    IN_PROGRESS: "In Progress",
    COMPLETED: "Completed",
    CANCELLED: "Cancelled"
  };
  return statusNames[status] || status;
}


// ===== Create Table Cell =====

function createCell(value) {
  const cell = document.createElement("td");
  cell.textContent = value ?? "-";
  return cell;
}


// ===== Render Projects =====

function renderProjects(projects) {
  if (!projectsTableBody) return;

  projectsTableBody.replaceChildren();

  if (projects.length === 0) {
    const row = document.createElement("tr");
    const cell = document.createElement("td");
    cell.colSpan = 7;
    cell.textContent = "No projects found.";
    row.appendChild(cell);
    projectsTableBody.appendChild(row);
    return;
  }

  projects.forEach(project => {
    const row = document.createElement("tr");

    row.appendChild(createCell(`#${project.id}`));
    row.appendChild(createCell(project.projectName));
    row.appendChild(createCell(project.client?.name || "Unknown"));
    row.appendChild(createCell(project.startDate));
    row.appendChild(createCell(project.expectedEndDate));
    row.appendChild(createCell(formatCurrency(project.totalAmount)));

    const statusCell = document.createElement("td");
    const statusBadge = document.createElement("span");
    statusBadge.className = `status-badge ${getStatusClass(project.projectStatus)}`;
    statusBadge.textContent = formatStatus(project.projectStatus);

    statusCell.appendChild(statusBadge);
    row.appendChild(statusCell);
    projectsTableBody.appendChild(row);
  });
}


// ===== Update Statistics =====

function updateStatistics() {
  if (totalProjects) totalProjects.textContent = allProjects.length;
  if (activeProjects) {
    activeProjects.textContent = allProjects.filter(
      project => project.projectStatus === "IN_PROGRESS"
    ).length;
  }
  if (completedProjects) {
    completedProjects.textContent = allProjects.filter(
      project => project.projectStatus === "COMPLETED"
    ).length;
  }
}


// ===== Load Projects from Backend =====

async function loadProjects() {
  try {
    showMessage("Loading projects...");
    const response = await fetch("/api/projects", {
      headers: {
        "Authorization": `Bearer ${getToken()}`
      }
    });

    if (response.status === 401 || response.status === 403) {
      throw new Error("You are not authorized to view projects.");
    }

    if (!response.ok) {
      throw new Error("Unable to load projects.");
    }

    allProjects = await response.json();
    updateStatistics();
    renderProjects(allProjects);
    showMessage(`${allProjects.length} project(s) loaded successfully.`, "#27864a");

  } catch (error) {
    showMessage(error.message, "#d9534f");
  }
}


// ===== Search Projects =====

if (searchProjects) {
  searchProjects.addEventListener("input", () => {
    const searchValue = searchProjects.value.trim().toLowerCase();

    const filteredProjects = allProjects.filter(project => {
      return [
        project.projectName,
        project.description,
        project.client?.name,
        project.projectStatus,
        project.id
      ].some(value =>
        String(value ?? "").toLowerCase().includes(searchValue)
      );
    });

    renderProjects(filteredProjects);
  });
}


// ===== Open Add Project Modal =====

if (addProjectButton) {
  addProjectButton.addEventListener("click", () => {
    if (addProjectForm) addProjectForm.reset();
    if (projectFormMessage) projectFormMessage.textContent = "";

    if (allClients.length === 0 && projectFormMessage) {
      projectFormMessage.textContent = "Please add a client before creating a project.";
    }

    if (projectDialog) projectDialog.showModal();
  });
}


// ===== Close Modal =====

if (closeProjectModal) {
  closeProjectModal.addEventListener("click", () => {
    if (projectDialog) projectDialog.close();
  });
}

if (cancelProject) {
  cancelProject.addEventListener("click", () => {
    if (projectDialog) projectDialog.close();
  });
}


// ===== Create New Project =====

if (addProjectForm) {
  addProjectForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const startDate = document.getElementById("startDate")?.value;
    const expectedEndDate = document.getElementById("expectedEndDate")?.value;
    const totalAmount = document.getElementById("totalAmount")?.value;

    if (expectedEndDate < startDate) {
      if (projectFormMessage) projectFormMessage.textContent = "Expected end date cannot be before start date.";
      return;
    }

    if (Number(totalAmount) < 0 || totalAmount === "") {
      if (projectFormMessage) projectFormMessage.textContent = "Please enter a valid project budget.";
      return;
    }

    if (projectClient && !projectClient.value) {
      if (projectFormMessage) projectFormMessage.textContent = "Please select a client.";
      return;
    }

    const projectData = {
      projectName: document.getElementById("projectName")?.value.trim(),
      description: document.getElementById("projectDescription")?.value.trim(),
      startDate: startDate,
      expectedEndDate: expectedEndDate,
      projectStatus: document.getElementById("projectStatus")?.value,
      totalAmount: Number(totalAmount),
      clientId: Number(projectClient?.value)
    };

    const saveButton = addProjectForm.querySelector(".save-project-button");

    if (saveButton) {
      saveButton.disabled = true;
      saveButton.textContent = "Saving...";
    }
    if (projectFormMessage) projectFormMessage.textContent = "";

    try {
      const response = await fetch("/api/projects", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${getToken()}`
        },
        body: JSON.stringify(projectData)
      });

      const data = await response.json().catch(() => ({}));

      // FIX: Changed from (response.ok) to (!response.ok)
      if (!response.ok) {
        throw new Error(
          data.message || "Unable to create project."
        );
      }

      if (projectDialog) projectDialog.close();
      addProjectForm.reset();
      await loadProjects();
      showMessage(`${projectData.projectName} created successfully!`, "#27864a");

    } catch (error) {
      if (projectFormMessage) projectFormMessage.textContent = error.message;
    } finally {
      if (saveButton) {
        saveButton.disabled = false;
        saveButton.textContent = "Save Project";
      }
    }
  });
}


// ===== Logout =====

if (logoutButton) {
  logoutButton.addEventListener("click", () => {
    sessionStorage.removeItem("fmsToken");
    sessionStorage.removeItem("fmsUser");
    window.location.replace("/login.html");
  });
}


// ===== Initialize Projects Page =====

async function initializeProjectsPage() {
  const isAdmin = await verifyAdmin();

  if (!isAdmin) {
    return;
  }

  try {
    await loadClients();
    await loadProjects();
  } catch (error) {
    showMessage(error.message, "#d9534f");
  }
}

initializeProjectsPage();