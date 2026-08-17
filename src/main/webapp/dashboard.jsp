<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%>

<%@ page import="model.User"%>
<%@ page import="model.Dashboard"%>
<%@ page import="dao.DashboardDAO"%>
<%@ page import="dao.TransactionDAO"%>
<%@ page import="model.Transaction"%>
<%@ page import="java.util.ArrayList"%>

<%
User user=(User)session.getAttribute("user");

if(user==null){
    response.sendRedirect("login.jsp");
    return;
}

DashboardDAO dashboardDAO=new DashboardDAO();
Dashboard dashboard=dashboardDAO.getDashboardData(user.getUserId());

TransactionDAO transactionDAO=new TransactionDAO();
ArrayList<Transaction> recentTransactions=
transactionDAO.getRecentTransactions(user.getUserId());
%>

<!DOCTYPE html>
<html>
<head>

<meta charset="UTF-8">

<title>Dashboard</title>

<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">

<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

<style>

body{

background:#f4f6f9;

}

.sidebar{

position:fixed;

left:0;

top:0;

height:100vh;

width:240px;

background:#212529;

padding-top:20px;

}

.sidebar h3{

color:white;

text-align:center;

margin-bottom:30px;

}

.sidebar a{

display:block;

padding:14px 25px;

color:white;

text-decoration:none;

}

.sidebar a:hover{

background:#0d6efd;

}

.content{

margin-left:250px;

padding:25px;

}

.card{

border:none;

border-radius:15px;

box-shadow:0 2px 12px rgba(0,0,0,.15);

transition:.3s;

}

.card:hover{

transform:translateY(-5px);

}

</style>

</head>

<body>

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

<a href="reports.jsp">

<i class="bi bi-bar-chart"></i>

Reports

</a>

<a href="<%= request.getContextPath() %>/ai-chat.jsp">
    🤖 AI Assistant
</a>

<a href="inputDocument">
    <i class="bi bi-file-earmark-arrow-up"></i>
    Bank Statement
</a>

</a>

<a href="profile">
    <i class="bi bi-person-circle"></i>
    Profile
</a>

<a href="logout">

<i class="bi bi-box-arrow-right"></i>

Logout

</a>

</div>

<div class="content">

<h2>Dashboard</h2>

<p>

Welcome

<b><%=user.getFullName()%></b>

</p>

<div class="row g-4">

<div class="col-md-3">

<div class="card text-center p-4">

<h5 class="text-success">

💰 Total Income

</h5>

<h2>

₹ <%=dashboard.getTotalIncome()%>

</h2>

</div>

</div>

<div class="col-md-3">

<div class="card text-center p-4">

<h5 class="text-danger">

💸 Total Expense

</h5>

<h2>

₹ <%=dashboard.getTotalExpense()%>

</h2>

</div>

</div>

<div class="col-md-3">

<div class="card text-center p-4">

<h5 class="text-primary">

💵 Balance

</h5>

<h2>

₹ <%=dashboard.getBalance()%>

</h2>

</div>

</div>

<div class="col-md-3">

<div class="card text-center p-4">

<h5 class="text-warning">

📂 Categories

</h5>

<h2>

<%=dashboard.getTotalCategories()%>

</h2>

</div>

</div>

</div>



<div class="row mt-4">

    <div class="col-md-6">

        <div class="card shadow-sm">

            <div class="card-body">

                <h5 class="card-title">
                    <i class="bi bi-file-earmark-pdf text-danger"></i>
                    Bank Statement Analysis
                </h5>

                <p class="card-text text-muted">
                    Upload and analyze your bank statement
                    using PDF extraction and AI-powered analysis.
                </p>

                <a href="<%=request.getContextPath()%>/inputDocument"
                   class="btn btn-primary">

                    <i class="bi bi-upload"></i>
                    Manage Bank Statements

                </a>

            </div>

        </div>

    </div>

</div>


<!-- ================= Charts ================= -->

<div class="row mt-5">

    <!-- Income vs Expense Chart -->
    <div class="col-lg-6">

        <div class="card p-3">

            <h5>Income vs Expense</h5>

            <canvas id="incomeExpenseChart"></canvas>

        </div>

    </div>


    <!-- Expense Distribution Chart -->
    <div class="col-lg-6">

        <div class="card p-3">

            <h5>Expense Distribution</h5>

            <canvas id="expenseChart"></canvas>

        </div>

    </div>

</div>


<!-- ================= Budget Status ================= -->

<div class="row mt-4">

    <div class="col-12">

        <div class="card p-4">

            <div class="d-flex justify-content-between align-items-center">

                <h4 class="mb-0">

                    <i class="bi bi-piggy-bank"></i>

                    Budget Status

                </h4>


                <a href="budget.jsp"
                   class="btn btn-outline-primary btn-sm">

                    Manage Budget

                </a>

            </div>


            <div class="table-responsive mt-3">

                <table class="table table-hover table-bordered">

                    <thead class="table-dark">

                        <tr>

                            <th>Category</th>

                            <th>Budget</th>

                            <th>Spent</th>

                            <th>Remaining</th>

                            <th>Status</th>

                        </tr>

                    </thead>


                    <tbody>

                    <%

                    if (dashboard.getBudgetStatuses() == null ||
                        dashboard.getBudgetStatuses().isEmpty()) {

                    %>

                        <tr>

                            <td colspan="5"
                                class="text-center">

                                No Budget Set For This Month

                            </td>

                        </tr>

                    <%

                    } else {

                        for (model.BudgetStatus budget :
                             dashboard.getBudgetStatuses()) {

                    %>

                        <tr>

                            <!-- Category -->

                            <td class="fw-bold">

                                <%=budget.getCategoryName()%>

                            </td>


                            <!-- Budget -->

                            <td>

                                ₹ <%=String.format("%.2f",
                                    budget.getBudgetAmount())%>

                            </td>


                            <!-- Spent -->

                            <td>

                                <%

                                if (budget.getSpent() >
                                    budget.getBudgetAmount()) {

                                %>

                                    <span class="text-danger fw-bold">

                                        ₹ <%=String.format("%.2f",
                                            budget.getSpent())%>

                                    </span>

                                <%

                                } else {

                                %>

                                    <span>

                                        ₹ <%=String.format("%.2f",
                                            budget.getSpent())%>

                                    </span>

                                <%

                                }

                                %>

                            </td>


                            <!-- Remaining -->

                            <td>

                                <%

                                if (budget.getRemaining() < 0) {

                                %>

                                    <span class="text-danger fw-bold">

                                        - ₹ <%=String.format("%.2f",
                                            Math.abs(
                                                budget.getRemaining()
                                            ))%>

                                    </span>

                                <%

                                } else {

                                %>

                                    <span class="text-success fw-bold">

                                        ₹ <%=String.format("%.2f",
                                            budget.getRemaining())%>

                                    </span>

                                <%

                                }

                                %>

                            </td>


                            <!-- Status -->

                            <td>

                                <%

                                if (budget.getRemaining() < 0) {

                                %>

                                    <span class="badge bg-danger">

                                        ⚠️ Budget Exceeded

                                    </span>

                                <%

                                } else if (budget.getRemaining() == 0) {

                                %>

                                    <span class="badge bg-warning text-dark">

                                        ⚠️ Budget Fully Used

                                    </span>

                                <%

                                } else {

                                %>

                                    <span class="badge bg-success">

                                        ✓ Within Budget

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

    </div>

</div>


<!-- ================= Recent Transactions ================= -->

<div class="row mt-5">

    <!-- Recent Transactions -->

    <div class="col-lg-8">

        <div class="card p-3">

            <div class="d-flex justify-content-between align-items-center">

                <h5>

                    <i class="bi bi-clock-history"></i>

                    Recent Transactions

                </h5>


                <a href="report"
                   class="btn btn-outline-primary btn-sm">

                    View All

                </a>

            </div>


            <table class="table table-hover table-bordered mt-3">

                <thead class="table-dark">

                    <tr>

                        <th>Date</th>

                        <th>Type</th>

                        <th>Category</th>

                        <th>Description</th>

                        <th class="text-end">Amount</th>

                    </tr>

                </thead>


                <tbody>

                <%

                if (recentTransactions == null ||
                    recentTransactions.isEmpty()) {

                %>

                    <tr>

                        <td colspan="5"
                            class="text-center">

                            No Transactions Available

                        </td>

                    </tr>

                <%

                } else {

                    java.text.SimpleDateFormat sdf =
                        new java.text.SimpleDateFormat("dd MMM yyyy");


                    for (Transaction t : recentTransactions) {

                %>

                    <tr>

                        <!-- Date -->

                        <td>

                            <%=sdf.format(
                                t.getTransactionDate()
                            )%>

                        </td>


                        <!-- Type -->

                        <td>

                            <%

                            if ("Income".equals(t.getType())) {

                            %>

                                <span class="badge bg-success">

                                    Income

                                </span>

                            <%

                            } else {

                            %>

                                <span class="badge bg-danger">

                                    Expense

                                </span>

                            <%

                            }

                            %>

                        </td>


                        <!-- Category -->

                        <td>

                            <%=t.getCategoryName()%>

                        </td>


                        <!-- Description -->

                        <td>

                            <%=t.getDescription()%>

                        </td>


                        <!-- Amount -->

                        <td class="text-end">

                            <%

                            if ("Income".equals(t.getType())) {

                            %>

                                <span class="text-success fw-bold">

                                    + ₹ <%=t.getAmount()%>

                                </span>

                            <%

                            } else {

                            %>

                                <span class="text-danger fw-bold">

                                    - ₹ <%=t.getAmount()%>

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


    <!-- ================= Quick Actions ================= -->

    <div class="col-lg-4">

        <div class="card p-3">

            <h5>

                Quick Actions

            </h5>


            <a href="income.jsp"
               class="btn btn-success mb-2">

                + Add Income

            </a>


            <a href="expenses.jsp"
               class="btn btn-danger mb-2">

                + Add Expense

            </a>


            <a href="categories.jsp"
               class="btn btn-primary mb-2">

                + Add Category

            </a>


            <a href="budget.jsp"
               class="btn btn-warning">

                Budget

            </a>

        </div>

    </div>

</div>


</div>
<!-- End Content -->


<!-- ================= Chart JavaScript ================= -->

<script>

// =====================================================
// Income vs Expense Chart
// =====================================================

new Chart(
    document.getElementById("incomeExpenseChart"),
    {

        type: 'bar',

        data: {

            labels: [
                'Income',
                'Expense'
            ],

            datasets: [{

                label: 'Amount',

                data: [

                    <%=dashboard.getTotalIncome()%>,

                    <%=dashboard.getTotalExpense()%>

                ],

                backgroundColor: [

                    '#28a745',

                    '#dc3545'

                ]

            }]

        },

        options: {

            responsive: true,

            plugins: {

                legend: {

                    display: false

                }

            },

            scales: {

                y: {

                    beginAtZero: true

                }

            }

        }

    }
);


// =====================================================
// Expense Distribution Chart
// =====================================================

new Chart(
    document.getElementById("expenseChart"),
    {

        type: 'pie',

        data: {

            labels: [

                'Expense',

                'Remaining Balance'

            ],

            datasets: [{

                data: [

                    <%=dashboard.getTotalExpense()%>,

                    <%=dashboard.getBalance() > 0
                        ? dashboard.getBalance()
                        : 0%>

                ],

                backgroundColor: [

                    '#dc3545',

                    '#0d6efd'

                ]

            }]

        },

        options: {

            responsive: true,

            plugins: {

                legend: {

                    position: 'top'

                }

            }

        }

    }
);

</script>

</body>

</html>
