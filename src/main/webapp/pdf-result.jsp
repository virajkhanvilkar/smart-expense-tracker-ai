<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page import="java.util.List" %>
<%@ page import="model.InputDocument" %>
<%@ page import="model.ExtractedTransaction" %>

<!DOCTYPE html>

<html>

<head>

    <meta charset="UTF-8">

    <title>PDF Analysis - Expense Tracker</title>

    <!-- Bootstrap -->
    <link
        href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
        rel="stylesheet">

    <!-- Bootstrap Icons -->
    <link
        href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css"
        rel="stylesheet">


    <style>

        body {
            background: #f4f6f9;
            font-family: Arial, sans-serif;
        }

        /* =========================
           SIDEBAR
           ========================= */

        .sidebar {
            position: fixed;
            left: 0;
            top: 0;
            width: 260px;
            height: 100vh;
            background: #212529;
            padding-top: 25px;
            z-index: 1000;
        }

        .sidebar-title {
            color: white;
            text-align: center;
            margin-bottom: 35px;
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

        .sidebar i {
            margin-right: 8px;
        }


        /* =========================
           MAIN CONTENT
           ========================= */

        .main-container {
            margin-left: 260px;
            padding: 35px;
        }


        /* =========================
           RESULT CARD
           ========================= */

        .result-card {
            background: white;
            border-radius: 15px;
            padding: 30px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.08);
        }


        .page-title {
            font-size: 32px;
            font-weight: 600;
        }


        /* =========================
           FILE INFORMATION
           ========================= */

        .file-info {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 10px;
        }


        /* =========================
           TRANSACTION TABLE
           ========================= */

        .transaction-table {
            margin-top: 20px;
        }

        .transaction-table th {
            white-space: nowrap;
        }

        .transaction-table td {
            vertical-align: middle;
        }


        .income {
            color: #198754;
            font-weight: bold;
        }


        .expense {
            color: #dc3545;
            font-weight: bold;
        }


        .amount {
            font-weight: bold;
            white-space: nowrap;
        }


        /* =========================
           DESCRIPTION
           ========================= */

        .description-cell {
            max-width: 500px;
            word-break: break-word;
        }


        /* =========================
           BACK BUTTON
           ========================= */

        .back-button {
            margin-top: 30px;
        }


        /* =========================
           SUMMARY CARDS
           ========================= */

        .summary-card {
            border-radius: 12px;
            padding: 18px;
            height: 100%;
        }

        .summary-income {
            background: #e8f7ee;
            border-left: 5px solid #198754;
        }

        .summary-expense {
            background: #fdebec;
            border-left: 5px solid #dc3545;
        }

        .summary-count {
            background: #eef3f8;
            border-left: 5px solid #0d6efd;
        }

        .summary-title {
            font-size: 14px;
            color: #6c757d;
        }

        .summary-value {
            font-size: 24px;
            font-weight: bold;
        }


        /* =========================
           MOBILE
           ========================= */

        @media (max-width: 768px) {

            .sidebar {
                width: 200px;
            }

            .main-container {
                margin-left: 200px;
                padding: 20px;
            }

            .page-title {
                font-size: 26px;
            }

        }

    </style>

</head>


<body>


<!-- =========================================================
     SIDEBAR
     ========================================================= -->

<div class="sidebar">

    <h3 class="sidebar-title">
        Expense Tracker
    </h3>


    <a href="<%=request.getContextPath()%>/dashboard.jsp">

        <i class="bi bi-speedometer2"></i>
        Dashboard

    </a>


    <a href="<%=request.getContextPath()%>/income.jsp">

        <i class="bi bi-cash-stack"></i>
        Income

    </a>


    <a href="<%=request.getContextPath()%>/expenses.jsp">

        <i class="bi bi-wallet2"></i>
        Expenses

    </a>


    <a href="<%=request.getContextPath()%>/categories.jsp">

        <i class="bi bi-tags"></i>
        Categories

    </a>


    <a href="<%=request.getContextPath()%>/budget.jsp">

        <i class="bi bi-piggy-bank"></i>
        Budget

    </a>


    <a href="<%=request.getContextPath()%>/report">

        <i class="bi bi-bar-chart"></i>
        Reports

    </a>


    <a
        href="<%=request.getContextPath()%>/inputDocument"
        class="active">

        <i class="bi bi-file-earmark-arrow-up"></i>
        Bank Statement

    </a>


    <a href="<%=request.getContextPath()%>/profile">

        <i class="bi bi-person-circle"></i>
        Profile

    </a>


    <a href="<%=request.getContextPath()%>/logout">

        <i class="bi bi-box-arrow-right"></i>
        Logout

    </a>

</div>


<!-- =========================================================
     MAIN CONTENT
     ========================================================= -->

<div class="main-container">

    <div class="result-card">


        <!-- =================================================
             PAGE TITLE
             ================================================= -->

        <h2 class="page-title mb-4">

            <i class="bi bi-file-earmark-pdf text-danger"></i>

            PDF Analysis

        </h2>


        <hr>


        <!-- =================================================
             DOCUMENT INFORMATION
             ================================================= -->

        <%
            InputDocument document =
                (InputDocument) request.getAttribute("document");
        %>


        <% if (document != null) { %>

            <div class="file-info mb-4">

                <p class="mb-2">

                    <strong>File:</strong>

                    <%= document.getFileName() %>

                </p>


                <p class="mb-0">

                    <strong>Status:</strong>

                    <span class="badge bg-success">

                        ANALYZED

                    </span>

                </p>

            </div>

        <% } %>


        <!-- =================================================
             TRANSACTIONS
             ================================================= -->

        <h3 class="mb-3">

            <i class="bi bi-table"></i>

            Extracted Transactions

        </h3>


        <%
            List<ExtractedTransaction> transactions =
                (List<ExtractedTransaction>)
                    request.getAttribute("transactions");
        %>


        <!-- =================================================
             SUMMARY
             ================================================= -->

        <%
            double totalIncome = 0;
            double totalExpense = 0;

            int incomeCount = 0;
            int expenseCount = 0;

            if (transactions != null) {

                for (ExtractedTransaction transaction
                        : transactions) {

                    if ("INCOME".equalsIgnoreCase(
                            transaction.getType())) {

                        totalIncome +=
                            transaction.getAmount();

                        incomeCount++;

                    } else {

                        totalExpense +=
                            transaction.getAmount();

                        expenseCount++;
                    }
                }
            }

            double balance =
                totalIncome - totalExpense;
        %>


        <% if (transactions != null &&
               !transactions.isEmpty()) { %>


            <div class="row g-3 mb-4">


                <!-- TOTAL INCOME -->

                <div class="col-md-4">

                    <div class="summary-card summary-income">

                        <div class="summary-title">
                            Total Income
                        </div>

                        <div class="summary-value text-success">

                            ₹ <%= String.format(
                                    "%.2f",
                                    totalIncome
                                ) %>

                        </div>

                    </div>

                </div>


                <!-- TOTAL EXPENSE -->

                <div class="col-md-4">

                    <div class="summary-card summary-expense">

                        <div class="summary-title">
                            Total Expense
                        </div>

                        <div class="summary-value text-danger">

                            ₹ <%= String.format(
                                    "%.2f",
                                    totalExpense
                                ) %>

                        </div>

                    </div>

                </div>


                <!-- TRANSACTION COUNT -->

                <div class="col-md-4">

                    <div class="summary-card summary-count">

                        <div class="summary-title">
                            Transactions
                        </div>

                        <div class="summary-value text-primary">

                            <%= transactions.size() %>

                        </div>

                        <small class="text-muted">

                            Income:
                            <%= incomeCount %>
                            |
                            Expense:
                            <%= expenseCount %>

                        </small>

                    </div>

                </div>

            </div>


            <!-- =================================================
                 TRANSACTION TABLE
                 ================================================= -->

            <div class="table-responsive">

                <table
                    class="table table-bordered table-hover transaction-table">


                    <thead class="table-dark">

                        <tr>

                            <th>
                                #
                            </th>

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

                            <th class="text-end">
                                Amount
                            </th>

                        </tr>

                    </thead>


                    <tbody>


                    <%

                        int transactionNumber = 1;

                        for (ExtractedTransaction transaction
                                : transactions) {

                    %>


                        <tr>


                            <!-- NUMBER -->

                            <td>

                                <%= transactionNumber++ %>

                            </td>


                            <!-- DATE -->

                            <td>

                                <%= transaction.getDate() %>

                            </td>


                            <!-- TYPE -->

                            <td>

                                <% if ("INCOME".equalsIgnoreCase(
                                        transaction.getType())) { %>


                                    <span class="income">

                                        <i class="bi bi-arrow-down-circle"></i>

                                        INCOME

                                    </span>


                                <% } else { %>


                                    <span class="expense">

                                        <i class="bi bi-arrow-up-circle"></i>

                                        EXPENSE

                                    </span>


                                <% } %>

                            </td>


                            <!-- CATEGORY -->

                            <td>

                                <span class="badge text-bg-light border">

                                    <%= transaction.getCategory() %>

                                </span>

                            </td>


                            <!-- DESCRIPTION -->

                            <td class="description-cell">

                                <%= transaction.getDescription() %>

                            </td>


                            <!-- AMOUNT -->

                            <td class="text-end amount">

                                ₹ <%= String.format(
                                        "%.2f",
                                        transaction.getAmount()
                                    ) %>

                            </td>


                        </tr>


                    <%

                        }

                    %>


                    </tbody>


                    <!-- =================================================
                         TABLE TOTAL
                         ================================================= -->

                    <tfoot>

                        <tr class="table-light">

                            <td colspan="5"
                                class="text-end">

                                <strong>
                                    Net Balance
                                </strong>

                            </td>

                            <td class="text-end">

                                <strong
                                    class="<%= balance >= 0
                                        ? "text-success"
                                        : "text-danger" %>">

                                    ₹ <%= String.format(
                                            "%.2f",
                                            balance
                                        ) %>

                                </strong>

                            </td>

                        </tr>

                    </tfoot>


                </table>

            </div>


        <% } else { %>


            <!-- =================================================
                 NO TRANSACTIONS
                 ================================================= -->

            <div class="alert alert-warning">

                <i class="bi bi-exclamation-triangle"></i>

                <strong>
                    No transactions could be extracted from this PDF.
                </strong>

                <br>

                <small>

                    The PDF was opened successfully, but the
                    transaction format could not be recognized.

                </small>

            </div>


        <% } %>


        <!-- =================================================
             BACK BUTTON
             ================================================= -->

        <div class="back-button">

            <a
                href="<%=request.getContextPath()%>/inputDocument"
                class="btn btn-secondary w-100">

                <i class="bi bi-arrow-left"></i>

                Back to Bank Statements

            </a>

        </div>


    </div>

</div>


</body>

</html>