// ===== Dashboard Elements =====
const adminName = document.getElementById("adminName");
const welcomeName = document.getElementById("welcomeName");
const logoutButton = document.getElementById("logoutButton");


// ===== Format Currency =====
function formatCurrency(amount) {
    return new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: "INR",
        maximumFractionDigits: 0
    }).format(Number(amount || 0));
}


// ===== Load Admin Dashboard =====
async function loadDashboard() {

    const token = sessionStorage.getItem("fmsToken");

    if (!token) {
        window.location.replace("/login.html");
        return;
    }

    try {

        // Verify logged-in user
        const userResponse = await fetch("/api/auth/me", {
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });

        if (!userResponse.ok) {
            sessionStorage.removeItem("fmsToken");
            sessionStorage.removeItem("fmsUser");
            window.location.replace("/login.html");
            return;
        }

        const user = await userResponse.json();

        // Only Admin can access this dashboard
        if (user.role !== "ADMIN") {
            window.location.replace("/login.html");
            return;
        }

        adminName.textContent = user.name;
        welcomeName.textContent = user.name;

        // Fetch dashboard statistics
        const response = await fetch("/api/dashboard", {
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error("Unable to load dashboard statistics.");
        }

        const data = await response.json();

        // Display statistics
        document.getElementById("totalProjects").textContent =
            data.totalProjects ?? 0;

        document.getElementById("activeProjects").textContent =
            data.activeProjects ?? 0;

        document.getElementById("totalClients").textContent =
            data.totalClients ?? 0;

        document.getElementById("totalEmployees").textContent =
            data.totalEmployees ?? 0;

        document.getElementById("pendingAttendanceRequests").textContent =
            data.pendingAttendanceRequests ?? 0;

        document.getElementById("totalRevenue").textContent =
            formatCurrency(data.totalRevenue);

        document.getElementById("totalExpenses").textContent =
            formatCurrency(data.totalExpenses);

        document.getElementById("totalProfit").textContent =
            formatCurrency(data.totalProfit);

        document.getElementById("pendingPayments").textContent =
            formatCurrency(data.pendingPayments);

    } catch (error) {

        console.error("Dashboard error:", error);

        const message = document.querySelector(".dashboard-panel p");

        if (message) {
            message.textContent = error.message;
        }

    }
}


// ===== Logout =====
logoutButton.addEventListener("click", () => {

    sessionStorage.removeItem("fmsToken");
    sessionStorage.removeItem("fmsUser");

    window.location.replace("/login.html");

});


// ===== Initialize Dashboard =====
loadDashboard();