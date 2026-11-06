// ===== Universal Mobile Navigation Drawer Handler =====
function initMobileNavigation() {
    const menuToggle = document.getElementById("menuToggle");
    const openMobileMenu = document.getElementById("openMobileMenu");
    const sidebar = document.getElementById("sidebar");
    const sidebarOverlay = document.getElementById("sidebarOverlay");

    function toggleSidebar(e) {
        if (e) e.preventDefault();
        if (sidebar && sidebarOverlay) {
            sidebar.classList.toggle("open");
            sidebarOverlay.classList.toggle("active");
        }
    }

    if (menuToggle) menuToggle.onclick = toggleSidebar;
    if (openMobileMenu) openMobileMenu.onclick = toggleSidebar;
    if (sidebarOverlay) sidebarOverlay.onclick = toggleSidebar;
}

// ===== Format Currency Helper =====
function formatCurrency(amount) {
    return new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: "INR",
        maximumFractionDigits: 0
    }).format(Number(amount || 0));
}

// ===== Load Admin Dashboard Stats =====
async function loadDashboard() {
    // Agar Dashboard page ke elements present nahi hain toh return karein
    const totalProjectsEl = document.getElementById("totalProjects");
    if (!totalProjectsEl) return;

    const token = sessionStorage.getItem("fmsToken");

    if (!token) {
        window.location.replace("/login.html");
        return;
    }

    try {
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

        if (user.role !== "ADMIN") {
            window.location.replace("/login.html");
            return;
        }

        // Safe DOM updates for names
        const adminName = document.getElementById("adminName");
        const welcomeName = document.getElementById("welcomeName");
        if (adminName) adminName.textContent = user.name || "Administrator";
        if (welcomeName) welcomeName.textContent = user.name || "Admin";

        const response = await fetch("/api/dashboard", {
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error("Unable to load dashboard statistics.");
        }

        const data = await response.json();

        // Safe DOM updates for statistics
        if (document.getElementById("totalProjects")) document.getElementById("totalProjects").textContent = data.totalProjects ?? 0;
        if (document.getElementById("activeProjects")) document.getElementById("activeProjects").textContent = data.activeProjects ?? 0;
        if (document.getElementById("totalClients")) document.getElementById("totalClients").textContent = data.totalClients ?? 0;
        if (document.getElementById("totalEmployees")) document.getElementById("totalEmployees").textContent = data.totalEmployees ?? 0;
        if (document.getElementById("pendingAttendanceRequests")) document.getElementById("pendingAttendanceRequests").textContent = data.pendingAttendanceRequests ?? 0;
        if (document.getElementById("totalRevenue")) document.getElementById("totalRevenue").textContent = formatCurrency(data.totalRevenue);
        if (document.getElementById("totalExpenses")) document.getElementById("totalExpenses").textContent = formatCurrency(data.totalExpenses);
        if (document.getElementById("totalProfit")) document.getElementById("totalProfit").textContent = formatCurrency(data.totalProfit);
        if (document.getElementById("pendingPayments")) document.getElementById("pendingPayments").textContent = formatCurrency(data.pendingPayments);

    } catch (error) {
        console.error("Dashboard error:", error);
        const message = document.querySelector(".dashboard-panel p");
        if (message) message.textContent = error.message;
    }
}

// ===== Global Logout Handler =====
function initLogout() {
    const logoutButton = document.getElementById("logoutButton");
    if (logoutButton) {
        logoutButton.onclick = (e) => {
            e.preventDefault();
            sessionStorage.removeItem("fmsToken");
            sessionStorage.removeItem("fmsUser");
            window.location.replace("/login.html");
        };
    }
}

// ===== Safe App Initialization =====
function initApp() {
    // Priority 1: Navigation binding pehle ensure karein
    initMobileNavigation();
    initLogout();
    
    // Priority 2: Dashboard data loading
    loadDashboard();
}

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initApp);
} else {
    initApp();
}