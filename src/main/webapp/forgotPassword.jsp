
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    Boolean otpSent = (Boolean) request.getAttribute("otpSent");
    Boolean verified = (Boolean) request.getAttribute("verified");
    String error = (String) request.getAttribute("error");
    String emailValue = request.getParameter("email");

    if (emailValue == null) {
        emailValue = "";
    }

    String safeEmail = emailValue
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");

    String safeError = "";
    if (error != null) {
        safeError = error
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Forgot Password - Smart Expense Tracker</title>
    <style>
        * { box-sizing: border-box; }

        body {
            font-family: Arial, sans-serif;
            background: #f2f2f2;
            margin: 0;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }

        .card {
            width: 100%;
            max-width: 430px;
            background: white;
            padding: 30px;
            border-radius: 12px;
            box-shadow: 0 2px 12px #0003;
        }

        h1 {
            text-align: center;
            color: #2196f3;
            font-size: 25px;
        }

        p {
            text-align: center;
            line-height: 1.5;
        }

        label {
            display: block;
            margin: 18px 0 8px;
        }

        input {
            width: 100%;
            padding: 12px;
            border: 1px solid #ccc;
            border-radius: 5px;
            font-size: 15px;
        }

        button {
            width: 100%;
            margin-top: 20px;
            padding: 13px;
            background: #2196f3;
            color: white;
            border: none;
            border-radius: 5px;
            font-size: 16px;
            cursor: pointer;
        }

        button:hover { background: #147dcc; }

        .message {
            padding: 10px;
            border-radius: 5px;
            margin: 15px 0;
            overflow-wrap: anywhere;
        }

        .error {
            background: #ffebee;
            color: #b71c1c;
        }

        .success {
            background: #e8f5e9;
            color: #1b5e20;
        }

        .back {
            text-align: center;
            margin-top: 22px;
        }

        a {
            color: #1689e8;
            text-decoration: none;
            font-weight: bold;
        }
    </style>
</head>
<body>
<main class="card">
    <h1>Reset Your Password</h1>

    <% if (!safeError.isEmpty()) { %>
        <div class="message error"><%= safeError %></div>
    <% } %>

    <% if (Boolean.TRUE.equals(verified)) { %>

        <p>OTP verified. Enter your new password.</p>

        <form action="ForgotPasswordServlet" method="post">
            <input type="hidden" name="action" value="resetPassword">

            <label for="newPassword">New Password</label>
            <input type="password" id="newPassword" name="newPassword"
                   minlength="8" autocomplete="new-password" required>

            <label for="confirmPassword">Confirm Password</label>
            <input type="password" id="confirmPassword"
                   name="confirmPassword" minlength="8"
                   autocomplete="new-password" required>

            <button type="submit">Reset Password</button>
        </form>

    <% } else if (Boolean.TRUE.equals(otpSent)) { %>

        <p>Enter the OTP sent to your registered email address.</p>

        <form action="ForgotPasswordServlet" method="post">
            <input type="hidden" name="action" value="verifyOtp">

            <label for="otp">6-digit OTP</label>
            <input type="text" id="otp" name="otp"
                   inputmode="numeric" pattern="[0-9]{6}"
                   maxlength="6" autocomplete="one-time-code" required>

            <button type="submit">Verify OTP</button>
        </form>

    <% } else { %>

        <p>Enter your registered email address to request a password-reset OTP.</p>

        <form action="ForgotPasswordServlet" method="get">
            <input type="hidden" name="action" value="sendOtp">

            <label for="email">Registered Email</label>
            <input type="email" id="email" name="email"
                   value="<%= safeEmail %>"
                   autocomplete="email" required>

            <button type="submit">Send OTP</button>
        </form>

    <% } %>

    <div class="back">
        <a href="login.jsp">Back to Login</a>
    </div>
</main>
</body>
</html>
