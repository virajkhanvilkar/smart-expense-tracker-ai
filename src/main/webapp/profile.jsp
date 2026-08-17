<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page import="model.User"%>

<%
    User sessionUser = (User) session.getAttribute("user");

    if (sessionUser == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    User user = (User) request.getAttribute("profileUser");

    if (user == null) {
        user = sessionUser;
    }

    String success = request.getParameter("success");
    String error = (String) request.getAttribute("error");
%>

<!DOCTYPE html>

<html>

<head>

<meta charset="UTF-8">

<title>My Profile - Expense Tracker</title>

<link
    href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
    rel="stylesheet">

<link
    href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css"
    rel="stylesheet">


<style>

body {
    margin: 0;
    background: #f4f6f9;
    font-family: Arial, sans-serif;
}


/* Sidebar */

.sidebar {

    position: fixed;

    left: 0;
    top: 0;

    width: 260px;
    height: 100vh;

    background: #212529;

    padding-top: 25px;
}


.sidebar h3 {

    color: white;

    text-align: center;

    margin-bottom: 35px;

    font-weight: bold;
}


.sidebar a {

    display: block;

    padding: 14px 30px;

    color: white;

    text-decoration: none;

    font-size: 17px;
}


.sidebar a:hover {

    background: #343a40;

}


.sidebar a.active {

    background: #0d6efd;

}


/* Content */

.content {

    margin-left: 260px;

    padding: 35px;
}


/* Profile */

.profile-card {

    background: white;

    border-radius: 15px;

    padding: 30px;

    box-shadow: 0 4px 15px rgba(0,0,0,0.08);

}


.profile-icon {

    width: 110px;

    height: 110px;

    border-radius: 50%;

    background: #0d6efd;

    color: white;

    display: flex;

    align-items: center;

    justify-content: center;

    font-size: 50px;

    margin: auto;

}


.profile-name {

    text-align: center;

    margin-top: 20px;

    font-weight: bold;

}


.profile-email {

    text-align: center;

    color: #6c757d;

}


/* Form */

.form-label {

    font-weight: 600;

}


</style>

</head>


<body>


<!-- SIDEBAR -->

<div class="sidebar">

    <h3>Expense Tracker</h3>


    <a href="dashboard.jsp">

        <i class="bi bi-speedometer2"></i>

        Dashboard

    </a>


    <a href="income.jsp">

        <i class="bi bi-cash-stack"></i>

        Income

    </a>


    <a href="expenses.jsp">

        <i class="bi bi-wallet2"></i>

        Expenses

    </a>


    <a href="categories.jsp">

        <i class="bi bi-tags"></i>

        Categories

    </a>


    <a href="budget.jsp">

        <i class="bi bi-piggy-bank"></i>

        Budget

    </a>


    <a href="report">

        <i class="bi bi-bar-chart"></i>

        Reports

    </a>


    <a href="profile" class="active">

        <i class="bi bi-person-circle"></i>

        Profile

    </a>


    <a href="logout">

        <i class="bi bi-box-arrow-right"></i>

        Logout

    </a>

</div>



<!-- MAIN CONTENT -->

<div class="content">


    <h2 class="mb-4">

        <i class="bi bi-person-circle"></i>

        My Profile

    </h2>


    <!-- SUCCESS -->

    <% if ("1".equals(success)) { %>

        <div class="alert alert-success">

            <i class="bi bi-check-circle"></i>

            Profile updated successfully!

        </div>

    <% } %>


    <!-- ERROR -->

    <% if (error != null) { %>

        <div class="alert alert-danger">

            <%= error %>

        </div>

    <% } %>



    <div class="row g-4">


        <!-- PROFILE SUMMARY -->

        <div class="col-md-4">

            <div class="profile-card text-center">


                <div class="profile-icon">

                    <i class="bi bi-person"></i>

                </div>


                <h4 class="profile-name">

                    <%= user.getFullName() %>

                </h4>


                <p class="profile-email">

                    <%= user.getEmail() %>

                </p>


                <hr>


                <p>

                    <strong>Phone:</strong><br>

                    <%= user.getPhone() != null
                        ? user.getPhone()
                        : "Not provided" %>

                </p>


            </div>

        </div>



        <!-- EDIT PROFILE -->

        <div class="col-md-8">

            <div class="profile-card">


                <h4 class="mb-4">

                    <i class="bi bi-person-gear"></i>

                    Profile Information

                </h4>


                <form action="<%=request.getContextPath()%>/profile"
                      method="post">


                    <!-- FULL NAME -->

                    <div class="mb-3">

                        <label class="form-label">

                            Full Name

                        </label>

                        <input
                            type="text"
                            name="fullName"
                            class="form-control"
                            value="<%=user.getFullName()%>"
                            required>

                    </div>


                    <!-- EMAIL -->

                    <div class="mb-3">

                        <label class="form-label">

                            Email

                        </label>

                        <input
                            type="email"
                            name="email"
                            class="form-control"
                            value="<%=user.getEmail()%>"
                            required>

                    </div>


                    <!-- PHONE -->

                    <div class="mb-3">

                        <label class="form-label">

                            Phone Number

                        </label>

                        <input
                            type="tel"
                            name="phone"
                            class="form-control"
                            value="<%=user.getPhone() != null
                                ? user.getPhone()
                                : ""%>"
                            maxlength="15">

                    </div>


                    <!-- BUTTONS -->

                    <div class="mt-4">

                        <button
                            type="submit"
                            class="btn btn-primary">

                            <i class="bi bi-save"></i>

                            Save Changes

                        </button>


                        <a
                            href="dashboard.jsp"
                            class="btn btn-secondary">

                            Cancel

                        </a>

                    </div>


                </form>

            </div>

        </div>


    </div>

</div>


</body>

</html>