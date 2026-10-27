
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Login - Smart Expense Tracker</title>

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
            width: 400px;
            max-width: calc(100% - 30px);
            margin: 80px auto;
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 0 10px #ccc;
        }

        h2 {
            text-align: center;
            margin-bottom: 25px;
            color: #2196F3;
        }

        label {
            display: block;
            margin-bottom: 6px;
        }

        input {
            width: 100%;
            padding: 12px;
            margin-top: 4px;
            margin-bottom: 18px;
            border: 1px solid #ccc;
            border-radius: 5px;
        }

        .forgot-password {
            text-align: right;
            margin-top: -8px;
            margin-bottom: 18px;
        }

        .forgot-password a,
        .register-link a {
            text-decoration: none;
            color: #2196F3;
            font-weight: bold;
        }

        .forgot-password a:hover,
        .register-link a:hover {
            text-decoration: underline;
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

        .register-link {
            text-align: center;
            margin-top: 20px;
        }
    </style>
</head>

<body>
    <div class="container">
        <h2>Smart Expense Tracker</h2>

        <form action="login" method="post">
            <label for="email">Email</label>
            <input
                type="email"
                name="email"
                id="email"
                placeholder="Enter email"
                autocomplete="username"
                required>

            <label for="password">Password</label>
            <input
                type="password"
                name="password"
                id="password"
                placeholder="Enter password"
                autocomplete="current-password"
                required>

            <div class="forgot-password">
                <a href="forgotPassword.jsp">Forgot Password?</a>
            </div>

            <button type="submit">Login</button>
        </form>

        <p class="register-link">
            Don't have an account?
            <a href="register.jsp">Register Here</a>
        </p>
    </div>
</body>
</html>
