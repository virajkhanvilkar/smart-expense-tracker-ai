
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Forgot Password - Smart Expense Tracker</title>

    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: Arial, Helvetica, sans-serif;
        }

        body {
            background: #f2f2f2;
        }

        .container {
            width: 450px;
            max-width: calc(100% - 30px);
            margin: 60px auto;
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 0 10px #ccc;
        }

        h2 {
            text-align: center;
            margin-bottom: 15px;
            color: #2196F3;
        }

        .description {
            text-align: center;
            color: #666;
            margin-bottom: 22px;
            font-size: 14px;
            line-height: 1.5;
        }

        label {
            display: block;
            margin-bottom: 8px;
            font-weight: bold;
        }

        input {
            width: 100%;
            padding: 12px;
            margin-bottom: 18px;
            border: 1px solid #ccc;
            border-radius: 5px;
            font-size: 14px;
        }

        button {
            width: 100%;
            padding: 12px;
            background: #2196F3;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 16px;
        }

        button:hover {
            background: #1976D2;
        }

        .message {
            text-align: center;
            margin-bottom: 20px;
            color: green;
            font-weight: bold;
        }

        .error {
            text-align: center;
            margin-bottom: 20px;
            color: #c62828;
            font-weight: bold;
            overflow-wrap: anywhere;
        }

        .back-login {
            text-align: center;
            margin-top: 20px;
        }

        a {
            text-decoration: none;
            color: #2196F3;
            font-weight: bold;
        }
    </style>
</head>

<body>
<div class="container">

    <h2>Reset Password</h2>

    <p class="description">
        Enter your registered email address to reset your password.
    </p>

    <% if (request.getAttribute("error") != null) {
        String errorMessage =
            String.valueOf(request.getAttribute("error"));
        errorMessage = errorMessage
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    %>
        <div class="error"><%= errorMessage %></div>
    <% } %>

    <%-- STEP 1: Enter email and request OTP --%>
    <% if (Boolean.TRUE.equals(request.getAttribute("verified"))) { %>

        <div class="message">
            OTP verified successfully. Enter your new password.
        </div>

        <form action="ForgotPasswordServlet" method="post">
            <input type="hidden" name="action" value="resetPassword">

            <label for="password">New Password</label>
            <input
                type="password"
                id="password"
                name="password"
                placeholder="Enter new password"
                autocomplete="new-password"
                required>

            <label for="confirmPassword">Confirm Password</label>
            <input
                type="password"
                id="confirmPassword"
                name="confirmPassword"
                placeholder="Confirm new password"
                autocomplete="new-password"
                required>

            <button type="submit">Reset Password</button>
        </form>

    <%-- STEP 2: OTP has been accepted for sending; show verification --%>
    <% } else if (Boolean.TRUE.equals(request.getAttribute("otpSent"))) { %>

        <div class="message">
            OTP request accepted. Check your email and spam folder.
        </div>

        <form action="ForgotPasswordServlet" method="post">
            <input type="hidden" name="action" value="verifyOtp">

            <label for="otp">Enter 6-digit OTP</label>
            <input
                type="text"
                id="otp"
                name="otp"
                placeholder="Enter 6-digit OTP"
                inputmode="numeric"
                pattern="[0-9]{6}"
                minlength="6"
                maxlength="6"
                autocomplete="one-time-code"
                required>

            <button type="submit">Verify OTP</button>
        </form>

    <%-- STEP 3: Email form, including when sending fails --%>
    <% } else { %>

        <form action="ForgotPasswordServlet" method="get">
            <input type="hidden" name="action" value="sendOtp">

            <label for="email">Registered Email</label>
            <input
                type="email"
                id="email"
                name="email"
                placeholder="Enter your registered email"
                autocomplete="email"
                required>

            <button type="submit">Send OTP</button>
        </form>

    <% } %>

    <div class="back-login">
        <a href="login.jsp">Back to Login</a>
    </div>

</div>
</body>
</html>
