<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <title>Verify OTP - Smart Expense Tracker</title>

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
            margin: 80px auto;
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0px 0px 10px gray;
        }

        h2 {
            text-align: center;
            margin-bottom: 15px;
            color: #2196F3;
        }

        .description {
            text-align: center;
            color: #666;
            margin-bottom: 25px;
            font-size: 14px;
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
            margin-bottom: 15px;
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
            color: red;
            font-weight: bold;
        }

        .back-login {
            text-align: center;
            margin-top: 15px;
        }

        .back-login a {
            text-decoration: none;
            color: #2196F3;
            font-weight: bold;
        }

    </style>

</head>

<body>

<div class="container">

    <h2>Verify OTP</h2>

    <p class="description">
        We have sent a 6-digit OTP to your registered email address.
    </p>


    <!-- ERROR -->

    <% if (request.getAttribute("error") != null) { %>

        <div class="error">
            <%= request.getAttribute("error") %>
        </div>

    <% } %>


    <!-- ==========================================
         STEP 2 : ENTER OTP
         ========================================== -->

    <% if (request.getAttribute("verified") == null) { %>

        <div class="message">
            OTP has been sent to your registered email.
        </div>

        <form action="ForgotPasswordServlet" method="post">

            <input type="hidden"
                   name="action"
                   value="verifyOtp">

            <label>Enter OTP</label>

            <input
                type="text"
                name="otp"
                placeholder="Enter 6-digit OTP"
                maxlength="6"
                pattern="[0-9]{6}"
                inputmode="numeric"
                required>

            <button type="submit">
                Verify OTP
            </button>

        </form>

    <% } %>


    <!-- ==========================================
         STEP 3 : NEW PASSWORD
         ========================================== -->

    <% if (request.getAttribute("verified") != null) { %>

        <div class="message">
            OTP Verified Successfully!
        </div>

        <form action="ForgotPasswordServlet" method="post">

            <input type="hidden"
                   name="action"
                   value="resetPassword">

            <label>New Password</label>

            <input
                type="password"
                name="password"
                placeholder="Enter new password"
                required>

            <label>Confirm Password</label>

            <input
                type="password"
                name="confirmPassword"
                placeholder="Confirm new password"
                required>

            <button type="submit">
                Reset Password
            </button>

        </form>

    <% } %>


    <div class="back-login">

        <a href="login.jsp">
            ← Back to Login
        </a>

    </div>

</div>

</body>

</html>