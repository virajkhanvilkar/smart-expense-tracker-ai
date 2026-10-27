
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - Smart Expense Tracker</title>
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
        }

        .login-card {
            background: white;
            width: 100%;
            max-width: 430px;
            padding: 30px 32px;
            border-radius: 12px;
            box-shadow: 0 2px 12px #0003;
        }

        h1 {
            color: #2196f3;
            text-align: center;
            font-size: 26px;
            margin: 0 0 22px;
        }

        label {
            display: block;
            margin: 16px 0 8px;
        }

        input {
            width: 100%;
            padding: 13px;
            border: 1px solid #ccc;
            border-radius: 5px;
            font-size: 15px;
        }

        .forgot {
            display: block;
            text-align: right;
            margin-top: 12px;
        }

        a {
            color: #1689e8;
            font-weight: bold;
            text-decoration: none;
        }

        a:hover { text-decoration: underline; }

        button {
            width: 100%;
            padding: 13px;
            margin-top: 20px;
            background: #2196f3;
            border: none;
            border-radius: 5px;
            color: white;
            font-size: 16px;
            cursor: pointer;
        }

        button:hover { background: #147dcc; }

        .register {
            text-align: center;
            margin-top: 22px;
        }
    </style>
</head>
<body>
    <main class="login-card">
        <h1>Smart Expense Tracker</h1>

        <form action="login" method="post">
            <label for="email">Email</label>
            <input type="email" id="email" name="email"
                   autocomplete="username" required>

            <label for="password">Password</label>
            <input type="password" id="password" name="password"
                   autocomplete="current-password" required>

            <div class="forgot">
                <a href="forgotPassword.jsp">Forgot Password?</a>
            </div>

            <button type="submit">Login</button>
        </form>

        <div class="register">
            Don't have an account?
            <a href="register.jsp">Register Here</a>
        </div>
    </main>
</body>
</html>
