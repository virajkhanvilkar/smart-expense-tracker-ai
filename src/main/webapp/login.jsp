<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

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
            margin: 80px auto;
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0px 0px 10px gray;
        }

        h2 {
            text-align: center;
            margin-bottom: 20px;
            color: #2196F3;
        }

        input {
            width: 100%;
            padding: 12px;
            margin-top: 8px;
            margin-bottom: 18px;
            border: 1px solid #ccc;
            border-radius: 5px;
        }

        /* Forgot Password */

        .forgot-password {
            text-align: right;
            margin-top: -8px;
            margin-bottom: 18px;
        }

        .forgot-password a {
            text-decoration: none;
            color: #2196F3;
            font-size: 14px;
            font-weight: bold;
            cursor: pointer;
        }

        .forgot-password a:hover {
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

        p {
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

        <h2>Smart Expense Tracker</h2>

        <form action="login" method="post">

            <label>Email</label>

            <input
                type="email"
                name="email"
                id="email"
                placeholder="Enter Email"
                required>


            <label>Password</label>

            <input
                type="password"
                name="password"
                placeholder="Enter Password"
                required>


            <!-- Forgot Password -->

            <div class="forgot-password">

                <a href="#"
                   onclick="forgotPassword(); return false;">

                    Forgot Password?

                </a>

            </div>


            <button type="submit">
                Login
            </button>

        </form>


        <p>

            Don't have an account?

            <a href="register.jsp">
                Register Here
            </a>

        </p>

    </div>


    <!-- ==========================================
         FORGOT PASSWORD JAVASCRIPT
         ========================================== -->

    <script>

        function forgotPassword() {

            // Get email entered by user

            var email =
                document.getElementById("email").value.trim();


            // Check email entered

            if (email === "") {

                alert(
                    "Please enter your registered email first."
                );

                document.getElementById("email").focus();

                return;
            }


            // Send email to ForgotPasswordServlet

            window.location.href =
                "ForgotPasswordServlet?action=sendOtp&email="
                + encodeURIComponent(email);
        }

    </script>

</body>

</html>