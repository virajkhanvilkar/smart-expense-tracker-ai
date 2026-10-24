
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="model.InputDocument" %>
<%@ page import="model.ExtractedTransaction" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>PDF Analysis - Expense Tracker</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css"
          rel="stylesheet">

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            background: #f4f6f9;
            color: #212529;
            font-family: Arial, sans-serif;
            overflow-x: hidden;
        }

        /* Sidebar */
        .sidebar {
            position: fixed;
            top: 0;
            left: 0;
            width: 260px;
            height: 100vh;
            overflow-y: auto;
            background: #212529;
            padding-top: 25px;
            z-index: 1000;
        }

        .sidebar-title {
            color: #fff;
            text-align: center;
            margin-bottom: 35px;
            font-size: 27px;
        }

        .sidebar a {
            display: block;
            padding: 14px 25px;
            color: #fff;
            text-decoration: none;
            font-size: 16px;
        }

        .sidebar a:hover {
            background: #343a40;
        }

        .sidebar a.active {
            background: #0d6efd;
        }

        .sidebar a i {
            margin-right: 8px;
        }

        /* Main content */
        .main-container {
            margin-left: 260px;
            padding: 30px;
            min-width: 0;
        }

        .result-card {
            background: #fff;
            border-radius: 15px;
            padding: 28px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
            min-width: 0;
        }

        .page-title {
            color: #212529;
            font-size: 32px;
            font-weight: 700;
        }

        .file-info {
            background: #f8f9fa;
            padding: 18px;
            border-radius: 10px;
            overflow-wrap: anywhere;
        }

        /* Summary cards */
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
            font-weight: 700;
        }

        /* Opening and closing balance */
        .balance-summary {
            display: flex;
            justify-content: space-between;
            flex-wrap: wrap;
            gap: 15px;
            background: #eef3f8;
            padding: 18px;
            border-radius: 10px;
            margin-bottom: 20px;
        }

        .balance-summary strong {
            color: #212529;
        }

        /* Transactions */
        .transaction-table {
            margin-top: 15px;
            width: 100%;
        }

        .transaction-table th {
            white-space: nowrap;
            vertical-align: middle;
        }

        .transaction-table td {
            vertical-align: middle;
        }

        .income {
            color: #198754;
            font-weight: 700;
        }

        .expense {
            color: #dc3545;
            font-weight: 700;
        }

        .amount {
            font-weight: 700;
            white-space: nowrap;
        }

        .description-cell {
            min-width: 220px;
            max-width: 500px;
            overflow-wrap: anywhere;
        }

        .back-button {
            margin-top: 25px;
            max-width: 300px;
        }

        .back-button a {
            padding: 12px 18px;
        }

        @media (max-width: 768px) {
            .sidebar {
                width: 200px;
            }

            .sidebar-title {
                font-size: 21px;
            }

            .sidebar a {
                padding: 12px 14px;
                font-size: 14px;
            }

            .main-container {
                margin-left: 200px;
                padding: 14px;
            }

            .result-card {
                padding: 16px;
            }

            .page-title {
                font-size: 25px;
            }

            .summary-value {
                font-size: 20px;
            }
        }

        @media (max-width: 520px) {
            .sidebar {
                position: relative;
                width: 100%;
                height: auto;
            }

            .sidebar-title {
                padding-top: 10px;
                margin-bottom: 15px;
            }

            .sidebar a {
                display: inline-block;
                padding: 10px;
            }

            .main-container {
                margin-left: 0;
                padding: 12px;
            }
        }
    </style>
</head>

<body>

<!-- Sidebar -->
<div class="sidebar">
    <h3 class="sidebar-title">Expense Tracker</h3>

    <a href="<%= request.getContextPath() %>/dashboard.jsp">
        <i class="bi bi-speedometer2"></i>Dashboard
    </a>

    <a href="<%= request.getContextPath() %>/income.jsp">
        <i class="bi bi-cash-stack"></i>Income
    </a>

    <a href="<%= request.getContextPath() %>/expenses.jsp">
        <i class="bi bi-wallet2"></i>Expenses
    </a>

    <a href="<%= request.getContextPath() %>/categories.jsp">
        <i class="bi bi-tags"></i>Categories
    </a>

    <a href="<%= request.getContextPath() %>/budget.jsp">
        <i class="bi bi-piggy-bank"></i>Budget
    </a>

    <a href="<%= request.getContextPath() %>/report">
        <i class="bi bi-bar-chart"></i>Reports
    </a>

    <a class="active"
       href="<%= request.getContextPath() %>/inputDocument">
        <i class="bi bi-file-earmark-arrow-up"></i>Bank Statement
    </a>

    <a href="<%= request.getContextPath() %>/profile">
        <i class="bi bi-person-circle"></i>Profile
    </a>

    <a href="<%= request.getContextPath() %>/logout">
        <i class="bi bi-box-arrow-right"></i>Logout
    </a>
</div>

<!-- Main content -->
<main class="main-container">
    <div class="result-card">

        <h2 class="page-title mb-4">
            <i class="bi bi-file-earmark-pdf text-danger"></i>
            PDF Analysis
        </h2>

        <hr>

        <%
            InputDocument document =
                (InputDocument) request.getAttribute("document");

            List<ExtractedTransaction> transactions =
                (List<ExtractedTransaction>)
                    request.getAttribute("transactions");

            double totalIncome = 0.0;
            double totalExpense = 0.0;
            int incomeCount = 0;
            int expenseCount = 0;

            if (transactions != null) {
                for (ExtractedTransaction item : transactions) {
                    if ("INCOME".equalsIgnoreCase(item.getType())) {
                        totalIncome += item.getAmount();
                        incomeCount++;
                    } else {
                        totalExpense += item.getAmount();
                        expenseCount++;
                    }
                }
            }

            /*
             * ProcessDocumentServlet should provide the opening balance.
             * The fallback of 941.56 is only for the current Kotak statement.
             */
            Object openingBalanceObject =
                request.getAttribute("openingBalance");

            double openingBalance;

            if (openingBalanceObject instanceof Number) {
                openingBalance =
                    ((Number) openingBalanceObject).doubleValue();
            } else {
                openingBalance = 0.0;
            }

            double calculatedClosingBalance =
                openingBalance + totalIncome - totalExpense;

            Object closingBalanceObject =
                request.getAttribute("closingBalance");

            double closingBalance;

            if (closingBalanceObject instanceof Number) {
                closingBalance =
                    ((Number) closingBalanceObject).doubleValue();
            } else {
                closingBalance = calculatedClosingBalance;
            }
        %>

        <!-- Document information -->
        <% if (document != null) { %>
            <div class="file-info mb-4">
                <p class="mb-2">
                    <strong>File:</strong>
                    <%= document.getFileName() %>
                </p>

                <p class="mb-0">
                    <strong>Status:</strong>
                    <span class="badge bg-success">ANALYZED</span>
                </p>
            </div>
        <% } %>

        <h3 class="mb-3">
            <i class="bi bi-table"></i>
            Extracted Transactions
        </h3>

        <% if (transactions != null && !transactions.isEmpty()) { %>

            <!-- Summary cards -->
            <div class="row g-3 mb-4">

                <div class="col-md-4">
                    <div class="summary-card summary-income">
                        <div class="summary-title">Total Income</div>
                        <div class="summary-value text-success">
                            ₹ <%= String.format(
                                Locale.US, "%.2f", totalIncome) %>
                        </div>
                    </div>
                </div>

                <div class="col-md-4">
                    <div class="summary-card summary-expense">
                        <div class="summary-title">Total Expense</div>
                        <div class="summary-value text-danger">
                            ₹ <%= String.format(
                                Locale.US, "%.2f", totalExpense) %>
                        </div>
                    </div>
                </div>

                <div class="col-md-4">
                    <div class="summary-card summary-count">
                        <div class="summary-title">Transactions</div>
                        <div class="summary-value text-primary">
                            <%= transactions.size() %>
                        </div>
                        <small class="text-muted">
                            Income: <%= incomeCount %>
                            |
                            Expense: <%= expenseCount %>
                        </small>
                    </div>
                </div>

            </div>

            <!-- Opening and closing balance -->
            <div class="balance-summary">
                <div>
                    <strong>Opening Balance:</strong>
                    <span class="text-primary">
                        ₹ <%= String.format(
                            Locale.US, "%.2f", openingBalance) %>
                    </span>
                </div>

                <div>
                    <strong>Closing Balance:</strong>
                    <span class="<%= closingBalance >= 0
                        ? "text-success" : "text-danger" %>">
                        ₹ <%= String.format(
                            Locale.US, "%.2f", closingBalance) %>
                    </span>
                </div>
            </div>

            <!-- Transaction table -->
            <div class="table-responsive">
                <table class="table table-bordered table-hover transaction-table">

                    <thead class="table-dark">
                        <tr>
                            <th>#</th>
                            <th>Date</th>
                            <th>Type</th>
                            <th>Category</th>
                            <th>Description</th>
                            <th class="text-end">Amount</th>
                            <th class="text-end">Balance</th>
                        </tr>
                    </thead>

                    <tbody>
                    <%
                        int transactionNumber = 1;
                        double runningBalance = openingBalance;

                        for (ExtractedTransaction transaction : transactions) {

                            if ("INCOME".equalsIgnoreCase(
                                    transaction.getType())) {
                                runningBalance += transaction.getAmount();
                            } else {
                                runningBalance -= transaction.getAmount();
                            }
                    %>

                        <tr>
                            <td><%= transactionNumber++ %></td>

                            <td><%= transaction.getDate() %></td>

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

                            <td>
                                <span class="badge text-bg-light border">
                                    <%= transaction.getCategory() %>
                                </span>
                            </td>

                            <td class="description-cell">
                                <%= transaction.getDescription() %>
                            </td>

                            <td class="text-end amount">
                                ₹ <%= String.format(
                                    Locale.US,
                                    "%.2f",
                                    transaction.getAmount()) %>
                            </td>

                            <td class="text-end amount <%= runningBalance >= 0
                                    ? "text-success" : "text-danger" %>">
                                ₹ <%= String.format(
                                    Locale.US,
                                    "%.2f",
                                    runningBalance) %>
                            </td>
                        </tr>

                    <% } %>
                    </tbody>

                    <tfoot>
                        <tr class="table-light">
                            <td colspan="6" class="text-end">
                                <strong>Closing Balance</strong>
                            </td>

                            <td class="text-end">
                                <strong class="<%= closingBalance >= 0
                                    ? "text-success" : "text-danger" %>">
                                    ₹ <%= String.format(
                                        Locale.US,
                                        "%.2f",
                                        closingBalance) %>
                                </strong>
                            </td>
                        </tr>
                    </tfoot>

                </table>
            </div>

        <% } else { %>

            <div class="alert alert-warning">
                <i class="bi bi-exclamation-triangle"></i>
                <strong>
                    No transactions could be extracted from this PDF.
                </strong>
                <br>
                <small>
                    The PDF was opened successfully, but its transaction
                    format could not be recognized.
                </small>
            </div>

        <% } %>

        <!-- Back button -->
        <div class="back-button">
            <a href="<%= request.getContextPath() %>/inputDocument"
               class="btn btn-secondary w-100">
                <i class="bi bi-arrow-left"></i>
                Back to Bank Statements
            </a>
        </div>

    </div>
</main>

</body>
</html>
