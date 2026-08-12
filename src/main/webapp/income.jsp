<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%>

<%@ page import="java.util.ArrayList"%>
<%@ page import="dao.IncomeDAO"%>
<%@ page import="dao.CategoryDAO"%>
<%@ page import="model.Income"%>
<%@ page import="model.Category"%>
<%@ page import="model.User"%>

<%
User user = (User) session.getAttribute("user");

if(user==null){
    response.sendRedirect("login.jsp");
    return;
}

IncomeDAO incomeDAO = new IncomeDAO();
CategoryDAO categoryDAO = new CategoryDAO();

ArrayList<Income> incomeList = incomeDAO.getIncome(user.getUserId());
ArrayList<Category> categoryList = categoryDAO.getCategoriesByType(user.getUserId(), "Income");
Income editIncome = null;

String edit = request.getParameter("edit");

if(edit!=null){
    editIncome = incomeDAO.getIncomeById(Integer.parseInt(edit));
}
%>

<!DOCTYPE html>
<html>

<head>

<meta charset="UTF-8">

<title>Income Management</title>

<style>

body{
font-family:Arial;
background:#f4f4f4;
}

.container{
width:90%;
margin:auto;
margin-top:30px;
}

h2{
margin-bottom:20px;
}

input,select{
padding:10px;
margin:8px;
}

button{
padding:10px 20px;
background:#2196F3;
color:white;
border:none;
cursor:pointer;
}

button:hover{
background:#1976D2;
}

table{
width:100%;
margin-top:20px;
border-collapse:collapse;
background:white;
}

table th,table td{
border:1px solid #ddd;
padding:12px;
text-align:center;
}

th{
background:#2196F3;
color:white;
}

a{
text-decoration:none;
}

</style>

</head>

<body>

<div class="container">

<h2>Income Management</h2>

<form action="income" method="post">

<%
if(editIncome!=null){
%>

<input type="hidden" name="action" value="update">

<input type="hidden"
name="incomeId"
value="<%=editIncome.getIncomeId()%>">

<%
}else{
%>

<input type="hidden" name="action" value="add">

<%
}
%>

<select name="categoryId" required>

<option value="">Select Category</option>

<%
for(Category c : categoryList){
%>

<option
value="<%=c.getCategoryId()%>"

<%
if(editIncome!=null && editIncome.getCategoryId()==c.getCategoryId()){
%>

selected

<%
}
%>

>

<%=c.getCategoryName()%>

</option>

<%
}
%>

</select>

<input
type="number"
step="0.01"
name="amount"
placeholder="Amount"
value="<%=editIncome!=null ? editIncome.getAmount() : ""%>"
required>

<input
type="text"
name="description"
placeholder="Description"
value="<%=editIncome!=null ? editIncome.getDescription() : ""%>"
required>

<input
type="date"
name="incomeDate"
value="<%=editIncome!=null ? editIncome.getIncomeDate() : ""%>"
required>

<button type="submit">

<%=editIncome!=null ? "Update Income" : "Add Income"%>

</button>

<%
if(editIncome!=null){
%>

<a href="income.jsp">Cancel</a>

<%
}
%>

</form>

<table>

<tr>

<th>ID</th>

<th>Category</th>

<th>Amount</th>

<th>Description</th>

<th>Date</th>

<th>Action</th>

</tr>

<%
for(Income i : incomeList){
%>

<tr>

<td><%=i.getIncomeId()%></td>

<td><%=i.getCategoryName()%></td>

<td>₹ <%=i.getAmount()%></td>

<td><%=i.getDescription()%></td>

<td><%=i.getIncomeDate()%></td>

<td>

<a
href="income.jsp?edit=<%=i.getIncomeId()%>"
style="color:green;font-weight:bold;">

Edit

</a>

|

<a
href="income?action=delete&incomeId=<%=i.getIncomeId()%>"
style="color:red;font-weight:bold;"
onclick="return confirm('Delete this income?');">

Delete

</a>

</td>

</tr>

<%
}
%>

</table>

<br>

<a href="dashboard.jsp">

← Back to Dashboard

</a>

</div>

</body>
</html>