<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="dao.BudgetDAO"%>
<%@ page import="dao.CategoryDAO"%>
<%@ page import="model.Budget"%>
<%@ page import="model.Category"%>
<%@ page import="model.User"%>

<%
User user=(User)session.getAttribute("user");
if(user==null){
    response.sendRedirect("login.jsp");
    return;
}

BudgetDAO budgetDAO=new BudgetDAO();
CategoryDAO categoryDAO=new CategoryDAO();

ArrayList<Budget> budgetList=budgetDAO.getBudgets(user.getUserId());
ArrayList<Category> categoryList=categoryDAO.getCategoriesByType(user.getUserId(),"Expense");

Budget editBudget=null;
String edit=request.getParameter("edit");

if(edit!=null){
    editBudget=budgetDAO.getBudgetById(Integer.parseInt(edit));
}

String[] months={"","January","February","March","April","May","June","July","August","September","October","November","December"};
%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Budget Management</title>

<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">

<style>
body{background:#f4f6f9;font-family:Arial;}
.container-box{
background:#fff;
padding:25px;
margin:30px auto;
border-radius:12px;
box-shadow:0 2px 12px rgba(0,0,0,.15);
}
h2{margin-bottom:20px;}
table th{
background:#0d6efd;
color:#fff;
}
.edit{
color:green;
font-weight:bold;
text-decoration:none;
}
.delete{
color:red;
font-weight:bold;
text-decoration:none;
}
</style>

</head>
<body>

<div class="container mt-4">

<div class="container-box">

<h2>Budget Management</h2>

<form action="<%=request.getContextPath()%>/budget" method="post" class="row g-3">

<% if(editBudget!=null){ %>

<input type="hidden" name="action" value="update">
<input type="hidden" name="budgetId" value="<%=editBudget.getBudgetId()%>">

<% }else{ %>

<input type="hidden" name="action" value="add">

<% } %>

<div class="col-md-3">
<label class="form-label">Category</label>
<select name="categoryId" class="form-select" required>

<option value="">Select Category</option>

<%
for(Category c:categoryList){
%>

<option value="<%=c.getCategoryId()%>"
<%=editBudget!=null && editBudget.getCategoryId()==c.getCategoryId()?"selected":""%>>

<%=c.getCategoryName()%>

</option>

<%
}
%>

</select>
</div>

<div class="col-md-2">
<label class="form-label">Budget</label>
<input type="number" step="0.01" class="form-control"
name="budgetAmount"
value="<%=editBudget!=null?editBudget.getBudgetAmount():""%>"
required>
</div>

<div class="col-md-2">
<label class="form-label">Month</label>

<select name="month" class="form-select" required>

<option value="">Month</option>

<%
for(int i=1;i<=12;i++){
%>

<option value="<%=i%>"
<%=editBudget!=null && editBudget.getMonth()==i?"selected":""%>>

<%=months[i]%>

</option>

<%
}
%>

</select>

</div>

<div class="col-md-2">
<label class="form-label">Year</label>

<input type="number"
class="form-control"
name="year"
value="<%=editBudget!=null?editBudget.getYear():java.time.Year.now().getValue()%>"
required>

</div>

<div class="col-md-3 d-flex align-items-end">

<button class="btn btn-primary" type="submit">

<%=editBudget!=null?"Update Budget":"Save Budget"%>

</button>

<%
if(editBudget!=null){
%>

<a href="budget.jsp" class="btn btn-secondary ms-2">Cancel</a>

<%
}
%>

</div>

</form>

<hr>

<table class="table table-bordered table-hover">

<thead>

<tr>

<th>ID</th>
<th>Category</th>
<th>Budget</th>
<th>Month</th>
<th>Year</th>
<th>Spent</th>
<th>Remaining</th>
<th>Action</th>

</tr>

</thead>

<tbody>

<%

for(Budget b:budgetList){

double spent=budgetDAO.getSpentAmount(user.getUserId(),b.getCategoryId(),b.getMonth(),b.getYear());

double remaining=budgetDAO.getRemainingBudget(user.getUserId(),b.getCategoryId(),b.getMonth(),b.getYear());

%>

<tr>

<td><%=b.getBudgetId()%></td>

<td><%=b.getCategoryName()%></td>

<td>₹ <%=String.format("%.2f",b.getBudgetAmount())%></td>

<td><%=months[b.getMonth()]%></td>

<td><%=b.getYear()%></td>

<td class="text-danger fw-bold">

₹ <%=String.format("%.2f",spent)%>

</td>

<td class="<%=remaining<0?"text-danger":"text-success"%> fw-bold">

₹ <%=String.format("%.2f",remaining)%>

</td>

<td>

<a class="edit"
href="budget.jsp?edit=<%=b.getBudgetId()%>">

Edit

</a>

|

<a class="delete"
href="<%=request.getContextPath()%>/budget?action=delete&budgetId=<%=b.getBudgetId()%>"
onclick="return confirm('Delete this budget?')">

Delete

</a>

</td>

</tr>

<%
}
%>

</tbody>

</table>

<a href="dashboard.jsp" class="btn btn-secondary">

← Back to Dashboard

</a>

</div>

</div>

</body>
</html>
