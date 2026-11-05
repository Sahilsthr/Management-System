// ===== Elements =====
const clientsTableBody = document.getElementById("clientsTableBody");
const totalClients = document.getElementById("totalClients");
const searchInput = document.getElementById("searchClients");
const adminName = document.getElementById("adminName");
const logoutButton = document.getElementById("logoutButton");
const clientsMessage = document.getElementById("clientsMessage");
const addClientButton = document.getElementById("addClientButton");
const clientDialog = document.getElementById("clientDialog");
const addClientForm = document.getElementById("addClientForm");

const closeClientModal = document.getElementById("closeClientModal");
const cancelClient = document.getElementById("cancelClient");
const formMessage = document.getElementById("formMessage");

let allClients = [];


// ===== Show Message =====
function showMessage(message, color = "#c96b3d") {
    clientsMessage.textContent = message;
    clientsMessage.style.color = color;
}


// ===== Verify Admin =====
async function verifyAdmin() {

    const token = sessionStorage.getItem("fmsToken");

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

        adminName.textContent = user.name;

        return true;

    } catch (error) {

        sessionStorage.removeItem("fmsToken");
        sessionStorage.removeItem("fmsUser");

        window.location.replace("/login.html");

        return false;
    }
}


// ===== Render Client Table =====
function renderClients(clients) {

    clientsTableBody.replaceChildren();

    if (clients.length === 0) {

        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 5;
        cell.textContent = "No clients found.";

        row.appendChild(cell);
        clientsTableBody.appendChild(row);

        return;
    }

    clients.forEach(client => {

        const row = document.createElement("tr");

        const values = [
            `#${client.id}`,
            client.name,
            client.phone,
            client.email || "Not provided",
            client.address || "Not provided"
        ];

        values.forEach(value => {

            const cell = document.createElement("td");

            cell.textContent = value;

            row.appendChild(cell);

        });

        clientsTableBody.appendChild(row);

    });

}


// ===== Load Clients from Backend =====
async function loadClients() {

    const token = sessionStorage.getItem("fmsToken");

    try {

        showMessage("Loading clients...");

        const response = await fetch("/api/clients", {

            headers: {
                "Authorization": `Bearer ${token}`
            }

        });

        if (response.status === 401 || response.status === 403) {
            throw new Error("You are not authorized to view clients.");
        }

        if (!response.ok) {
            throw new Error("Unable to load clients.");
        }

        allClients = await response.json();

        totalClients.textContent = allClients.length;

        renderClients(allClients);

        showMessage(
            `${allClients.length} client(s) loaded successfully.`,
            "#27864a"
        );

    } catch (error) {

        renderClients([]);

        showMessage(error.message, "#d9534f");

    }

}


// ===== Search Clients =====
searchInput.addEventListener("input", () => {

    const searchValue = searchInput.value.trim().toLowerCase();

    const filteredClients = allClients.filter(client => {

        return [
            client.name,
            client.email,
            client.phone,
            client.address,
            client.id
        ].some(value =>
            String(value ?? "").toLowerCase().includes(searchValue)
        );

    });

    renderClients(filteredClients);

});


// ===== Logout =====
logoutButton.addEventListener("click", () => {

    sessionStorage.removeItem("fmsToken");
    sessionStorage.removeItem("fmsUser");

    window.location.replace("/login.html");

});


// ===== Open Add Client Modal =====
addClientButton.addEventListener("click", () => {

    addClientForm.reset();
    formMessage.textContent = "";

    clientDialog.showModal();

});


// ===== Close Modal =====
closeClientModal.addEventListener("click", () => {
    clientDialog.close();
});

cancelClient.addEventListener("click", () => {
    clientDialog.close();
});


// ===== Create New Client =====
addClientForm.addEventListener("submit", async (event) => {

    event.preventDefault();

    const token = sessionStorage.getItem("fmsToken");

    const clientData = {
        name: document.getElementById("clientName").value.trim(),
        phone: document.getElementById("clientPhone").value.trim(),
        email: document.getElementById("clientEmail").value.trim(),
        address: document.getElementById("clientAddress").value.trim()
    };

    const saveButton = addClientForm.querySelector(".save-client-button");

    saveButton.disabled = true;
    saveButton.textContent = "Saving...";

    try {

        const response = await fetch("/api/clients", {

            method: "POST",

            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },

            body: JSON.stringify(clientData)

        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || "Unable to create client.");
        }

        clientDialog.close();

        await loadClients();

        showMessage(
            `${data.name} added successfully!`,
            "#27864a"
        );

    } catch (error) {

        formMessage.textContent = error.message;

    } finally {

        saveButton.disabled = false;
        saveButton.textContent = "Save Client";

    }

});


// ===== Initialize =====
async function initializeClientsPage() {

    const isAdmin = await verifyAdmin();

    if (!isAdmin) {
        return;
    }

    await loadClients();

}

initializeClientsPage();