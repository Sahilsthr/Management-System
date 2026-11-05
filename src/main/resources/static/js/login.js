// ===== Get HTML Elements =====
const loginForm = document.getElementById("loginForm");
const emailInput = document.getElementById("email");
const passwordInput = document.getElementById("password");
const togglePassword = document.getElementById("togglePassword");
const rememberMe = document.getElementById("rememberMe");
const loginMessage = document.getElementById("loginMessage");
const loginButton = document.querySelector(".login-button");


// ===== Remember Email =====
const savedEmail = localStorage.getItem("fmsRememberedEmail");

if (savedEmail) {
    emailInput.value = savedEmail;
    rememberMe.checked = true;
}


// ===== Show / Hide Password =====
togglePassword.addEventListener("click", () => {

    const isPassword = passwordInput.type === "password";

    passwordInput.type = isPassword ? "text" : "password";

    togglePassword.textContent = isPassword ? "Hide" : "Show";

});


// ===== Forgot Password =====
document.getElementById("forgotPassword").addEventListener("click", (event) => {

    event.preventDefault();

    loginMessage.textContent =
        "Password reset is not available yet. Please contact the administrator.";

    loginMessage.style.color = "#d97745";

});


// ===== Login API Integration =====
loginForm.addEventListener("submit", async (event) => {

    event.preventDefault();

    loginMessage.textContent = "";

    const email = emailInput.value.trim();
    const password = passwordInput.value;

    if (!email || !password) {

        loginMessage.textContent = "Please enter email and password.";
        loginMessage.style.color = "#d9534f";
        return;

    }

    loginButton.disabled = true;
    loginButton.textContent = "Signing in...";

    try {

        const response = await fetch("/api/auth/login", {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify({
                email: email,
                password: password
            })

        });

        const data = await response.json();

        if (!response.ok) {

            loginMessage.textContent =
                data.message || "Invalid email or password.";

            loginMessage.style.color = "#d9534f";
            return;

        }

        if (!data.token) {
            throw new Error("Login token was not received.");
        }

        // Store JWT for this browser tab/session only
        sessionStorage.setItem("fmsToken", data.token);

        // Remember email only, never store password
        if (rememberMe.checked) {
            localStorage.setItem("fmsRememberedEmail", email);
        } else {
            localStorage.removeItem("fmsRememberedEmail");
        }

        // Verify logged-in user and role
        const userResponse = await fetch("/api/auth/me", {

            headers: {
                "Authorization": `${data.tokenType || "Bearer"} ${data.token}`
            }

        });

        if (!userResponse.ok) {
            sessionStorage.removeItem("fmsToken");
            throw new Error("Unable to verify your account. Please login again.");
        }

        const user = await userResponse.json();

        sessionStorage.setItem("fmsUser", JSON.stringify({
            id: user.id,
            name: user.name,
            email: user.email,
            role: user.role
        }));

        // Redirect according to user role
        if (user.role === "ADMIN") {

            window.location.replace("/admin-dashboard.html");
            return;

        }

        // Employee dashboard is not ready yet
        sessionStorage.removeItem("fmsToken");
        sessionStorage.removeItem("fmsUser");

        loginMessage.textContent =
            "Employee dashboard is under development.";

        loginMessage.style.color = "#d97745";

    } catch (error) {

        loginMessage.textContent =
            error.message || "Unable to connect to the server.";

        loginMessage.style.color = "#d9534f";

    } finally {

        loginButton.disabled = false;
        loginButton.textContent = "Sign In";

    }

});