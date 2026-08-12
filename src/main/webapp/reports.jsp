<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page import="java.util.ArrayList" %>
<%@ page import="model.Report" %>
<%@ page import="model.User" %>

<%
    // ================= LOGIN CHECK =================

    User user =
            (User) session.getAttribute("user");

    if (user == null) {

        response.sendRedirect("login.jsp");

        return;
    }


    // ================= GET REPORT DATA =================

    ArrayList<Report> reportList =
            (ArrayList<Report>)
            request.getAttribute("reportList");


    Double totalIncome =
            (Double)
            request.getAttribute("totalIncome");


    Double totalExpense =
            (Double)
            request.getAttribute("totalExpense");


    Double balance =
            (Double)
            request.getAttribute("balance");


    // ================= GET CATEGORIES =================

    ArrayList<String> categories =
            (ArrayList<String>)
            request.getAttribute("categories");


    // ================= GET SELECTED FILTERS =================

    String fromDate =
            (String)
            request.getAttribute("fromDate");


    String toDate =
            (String)
            request.getAttribute("toDate");


    String selectedCategory =
            (String)
            request.getAttribute("selectedCategory");


    String selectedType =
            (String)
            request.getAttribute("selectedType");


    // ================= SAFETY CHECK =================

    if (reportList == null) {

        response.sendRedirect(
                request.getContextPath() + "/report"
        );

        return;
    }

%>

<!DOCTYPE html>

<html>

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Financial Reports</title>


    <style>

        * {
            box-sizing: border-box;
        }


        body {

            margin: 0;

            padding: 0;

            font-family: Arial, sans-serif;

            background: #f4f6f9;

            color: #212529;
        }


        .container {

            width: 95%;

            max-width: 1400px;

            margin: 30px auto;
        }


        /* ================= HEADER ================= */

        .page-header {

            display: flex;

            justify-content: space-between;

            align-items: center;

            margin-bottom: 25px;

            flex-wrap: wrap;

            gap: 10px;
        }


        .page-header h2 {

            margin: 0;

            font-size: 28px;
        }


        .welcome {

            color: #666;

            font-size: 14px;
        }


        /* ================= FILTER BOX ================= */

        .filter-box {

            background: white;

            padding: 25px;

            border-radius: 12px;

            box-shadow:
                0 2px 10px rgba(0,0,0,.12);

            margin-bottom: 25px;
        }


        .filter-box h3 {

            margin-top: 0;

            margin-bottom: 20px;

            font-size: 20px;
        }


        .filter-row {

            display: flex;

            gap: 15px;

            align-items: flex-end;

            flex-wrap: wrap;
        }


        .filter-group {

            display: flex;

            flex-direction: column;

            min-width: 170px;
        }


        .filter-group label {

            font-weight: bold;

            margin-bottom: 7px;

            font-size: 14px;
        }


        input,
        select {

            padding: 11px;

            min-width: 170px;

            border: 1px solid #ccc;

            border-radius: 6px;

            font-size: 14px;

            background: white;
        }


        input:focus,
        select:focus {

            outline: none;

            border-color: #2196F3;

            box-shadow:
                0 0 0 2px rgba(33,150,243,.15);
        }


        button {

            padding: 11px 20px;

            border: none;

            border-radius: 6px;

            cursor: pointer;

            font-size: 14px;
        }


        .filter-btn {

            background: #2196F3;

            color: white;
        }


        .filter-btn:hover {

            background: #0b7dda;
        }


        .clear-btn {

            background: #6c757d;

            color: white;

            text-decoration: none;

            display: inline-block;

            padding: 11px 20px;

            border-radius: 6px;
        }


        .clear-btn:hover {

            background: #5a6268;
        }


        /* ================= SUMMARY CARDS ================= */

        .card-container {

            display: grid;

            grid-template-columns:
                repeat(3, 1fr);

            gap: 20px;

            margin-bottom: 25px;
        }


        .card {

            background: white;

            padding: 25px;

            border-radius: 12px;

            box-shadow:
                0 2px 10px rgba(0,0,0,.12);

            text-align: center;
        }


        .card h3 {

            margin-top: 0;

            margin-bottom: 10px;

            font-size: 18px;
        }


        .card h1 {

            margin: 0;

            font-size: 30px;
        }


        .income {

            color: #198754;

            font-weight: bold;
        }


        .expense {

            color: #dc3545;

            font-weight: bold;
        }


        .balance {

            font-weight: bold;
        }


        /* ================= ACTIONS ================= */

        .actions {

            display: flex;

            gap: 10px;

            flex-wrap: wrap;

            margin-bottom: 25px;
        }


        .action-btn {

            display: inline-block;

            padding: 11px 20px;

            color: white;

            text-decoration: none;

            border-radius: 6px;

            font-size: 14px;
        }


        .action-btn:hover {

            opacity: .9;
        }


        .pdf {

            background: #dc3545;
        }


        .excel {

            background: #198754;
        }


        .back {

            background: #6c757d;
        }


        /* ================= TABLE ================= */

        .table-container {

            background: white;

            border-radius: 12px;

            box-shadow:
                0 2px 10px rgba(0,0,0,.12);

            overflow-x: auto;
        }


        table {

            width: 100%;

            border-collapse: collapse;
        }


        table th,
        table td {

            padding: 13px;

            border-bottom: 1px solid #ddd;

            text-align: center;
        }


        table th {

            background: #212529;

            color: white;

            font-size: 14px;
        }


        table tr:hover {

            background: #f8f9fa;
        }


        .badge {

            display: inline-block;

            padding: 6px 10px;

            border-radius: 5px;

            color: white;

            font-size: 12px;

            font-weight: bold;
        }


        .badge-income {

            background: #198754;
        }


        .badge-expense {

            background: #dc3545;
        }


        .no-data {

            padding: 30px !important;

            color: #777;

            font-weight: bold;
        }


        /* ================= RESPONSIVE ================= */

        @media (max-width: 900px) {

            .card-container {

                grid-template-columns:
                    1fr;
            }

        }


        @media (max-width: 600px) {

            .container {

                width: 92%;

                margin-top: 20px;
            }


            .filter-group {

                width: 100%;
            }


            input,
            select {

                width: 100%;
            }


            .filter-btn,
            .clear-btn {

                width: 100%;

                text-align: center;
            }


            .page-header h2 {

                font-size: 23px;
            }

        }

    </style>

</head>


<body>


<div class="container">


    <!-- ================= HEADER ================= -->

    <div class="page-header">

        <div>

            <h2>📊 Financial Reports</h2>

            <div class="welcome">

                Welcome,
                <strong>
                    <%=user.getFullName()%>
                </strong>

            </div>

        </div>

    </div>


    <!-- ================================================= -->
    <!-- FILTER TRANSACTIONS -->
    <!-- ================================================= -->

    <div class="filter-box">

        <h3>
            🔍 Filter Transactions
        </h3>


        <form
            action="<%=request.getContextPath()%>/report"
            method="get">


            <div class="filter-row">


                <!-- ================= FROM DATE ================= -->

                <div class="filter-group">

                    <label>
                        From Date
                    </label>

                    <input
                        type="date"
                        name="fromDate"
                        value="<%=fromDate != null ? fromDate : ""%>">

                </div>


                <!-- ================= TO DATE ================= -->

                <div class="filter-group">

                    <label>
                        To Date
                    </label>

                    <input
                        type="date"
                        name="toDate"
                        value="<%=toDate != null ? toDate : ""%>">

                </div>


                <!-- ================= CATEGORY ================= -->

                <div class="filter-group">

                    <label>
                        Category
                    </label>

                    <select name="category">

                        <option value="">
                            All Categories
                        </option>


                        <%

                        if (categories != null) {

                            for (String cat : categories) {

                        %>

                            <option
                                value="<%=cat%>"
                                <%=cat.equals(selectedCategory)
                                    ? "selected"
                                    : ""%>>

                                <%=cat%>

                            </option>

                        <%

                            }

                        }

                        %>

                    </select>

                </div>


                <!-- ================= TYPE ================= -->

                <div class="filter-group">

                    <label>
                        Type
                    </label>

                    <select name="type">

                        <option value="">
                            All Transactions
                        </option>


                        <option
                            value="Income"
                            <%="Income".equals(selectedType)
                                ? "selected"
                                : ""%>>

                            Income

                        </option>


                        <option
                            value="Expense"
                            <%="Expense".equals(selectedType)
                                ? "selected"
                                : ""%>>

                            Expense

                        </option>

                    </select>

                </div>


                <!-- ================= FILTER ================= -->

                <div class="filter-group">

                    <button
                        type="submit"
                        class="filter-btn">

                        🔍 Filter

                    </button>

                </div>


                <!-- ================= CLEAR ================= -->

                <div class="filter-group">

                    <a
                        href="<%=request.getContextPath()%>/report"
                        class="clear-btn">

                        🔄 Clear

                    </a>

                </div>


            </div>

        </form>

    </div>


    <!-- ================================================= -->
    <!-- SUMMARY CARDS -->
    <!-- ================================================= -->

    <div class="card-container">


        <!-- TOTAL INCOME -->

        <div class="card">

            <h3>
                💰 Total Income
            </h3>

            <h1 class="income">

                ₹
                <%=String.format("%.2f", totalIncome)%>

            </h1>

        </div>


        <!-- TOTAL EXPENSE -->

        <div class="card">

            <h3>
                💸 Total Expense
            </h3>

            <h1 class="expense">

                ₹
                <%=String.format("%.2f", totalExpense)%>

            </h1>

        </div>


        <!-- BALANCE -->

        <div class="card">

            <h3>
                💵 Balance
            </h3>

            <h1
                class="balance"
                style="color:<%=balance >= 0 ? "#0d6efd" : "#dc3545"%>;">

                ₹
                <%=String.format("%.2f", balance)%>

            </h1>

        </div>


    </div>


    <!-- ================================================= -->
    <!-- EXPORT / NAVIGATION -->
    <!-- ================================================= -->

    <div class="actions">


        <!-- PDF -->

<!-- ================= EXPORT PDF ================= -->

<form action="<%=request.getContextPath()%>/exportPDF"
      method="get"
      style="display:inline;">

    <!-- Keep current filters -->
    <input type="hidden"
           name="fromDate"
           value="<%= request.getAttribute("fromDate") != null
                   ? request.getAttribute("fromDate") : "" %>">

    <input type="hidden"
           name="toDate"
           value="<%= request.getAttribute("toDate") != null
                   ? request.getAttribute("toDate") : "" %>">

    <input type="hidden"
           name="category"
           value="<%= request.getAttribute("selectedCategory") != null
                   ? request.getAttribute("selectedCategory") : "" %>">

    <input type="hidden"
           name="type"
           value="<%= request.getAttribute("selectedType") != null
                   ? request.getAttribute("selectedType") : "" %>">

    <button type="submit"
            class="btn btn-danger">
        📄 Export PDF
    </button>

</form>
        <!-- EXCEL -->

       <!-- ================= EXPORT EXCEL ================= -->

<form action="<%=request.getContextPath()%>/exportExcel"
      method="get"
      style="display:inline;">

    <!-- Keep current filters -->
    <input type="hidden"
           name="fromDate"
           value="<%= request.getAttribute("fromDate") != null
                   ? request.getAttribute("fromDate") : "" %>">

    <input type="hidden"
           name="toDate"
           value="<%= request.getAttribute("toDate") != null
                   ? request.getAttribute("toDate") : "" %>">

    <input type="hidden"
           name="category"
           value="<%= request.getAttribute("selectedCategory") != null
                   ? request.getAttribute("selectedCategory") : "" %>">

    <input type="hidden"
           name="type"
           value="<%= request.getAttribute("selectedType") != null
                   ? request.getAttribute("selectedType") : "" %>">

    <button type="submit"
            class="btn btn-success">
        📊 Export Excel
    </button>

</form>
        <!-- DASHBOARD -->

        <a
            href="<%=request.getContextPath()%>/dashboard.jsp"
            class="action-btn back">

            ← Back to Dashboard

        </a>


    </div>


    <!-- ================================================= -->
    <!-- TRANSACTION TABLE -->
    <!-- ================================================= -->

    <div class="table-container">

        <table>

            <thead>

                <tr>

                    <th>
                        Date
                    </th>

                    <th>
                        Type
                    </th>

                    <th>
                        Category
                    </th>

                    <th>
                        Description
                    </th>

                    <th>
                        Amount
                    </th>

                </tr>

            </thead>


            <tbody>


            <%

            if (reportList.isEmpty()) {

            %>


                <tr>

                    <td
                        colspan="5"
                        class="no-data">

                        ❌ No transactions found
                        for the selected filters.

                    </td>

                </tr>


            <%

            } else {

                for (Report r : reportList) {

            %>


                <tr>


                    <!-- DATE -->

                    <td>

                        <%=r.getDate()%>

                    </td>


                    <!-- TYPE -->

                    <td>

                        <%

                        if ("Income".equals(r.getType())) {

                        %>

                            <span
                                class="badge badge-income">

                                Income

                            </span>

                        <%

                        } else {

                        %>

                            <span
                                class="badge badge-expense">

                                Expense

                            </span>

                        <%

                        }

                        %>

                    </td>


                    <!-- CATEGORY -->

                    <td>

                        <%=r.getCategory()%>

                    </td>


                    <!-- DESCRIPTION -->

                    <td>

                        <%=r.getDescription() != null
                            ? r.getDescription()
                            : "-"%>

                    </td>


                    <!-- AMOUNT -->

                    <td>

                        <%

                        if ("Income".equals(r.getType())) {

                        %>

                            <span class="income">

                                + ₹
                                <%=String.format(
                                    "%.2f",
                                    r.getAmount()
                                )%>

                            </span>

                        <%

                        } else {

                        %>

                            <span class="expense">

                                - ₹
                                <%=String.format(
                                    "%.2f",
                                    r.getAmount()
                                )%>

                            </span>

                        <%

                        }

                        %>

                    </td>


                </tr>


            <%

                }

            }

            %>


            </tbody>

        </table>

    </div>


</div>


</body>

</html>