<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%>

<%@ page import="java.util.ArrayList"%>
<%@ page import="dao.ExpenseDAO"%>
<%@ page import="dao.CategoryDAO"%>
<%@ page import="model.Expense"%>
<%@ page import="model.Category"%>
<%@ page import="model.User"%>

<%
User user=(User)session.getAttribute("user");

if(user==null){
    response.sendRedirect("login.jsp");
    return;
}

ExpenseDAO expenseDAO=new ExpenseDAO();
CategoryDAO categoryDAO=new CategoryDAO();

ArrayList<Expense> expenseList = expenseDAO.getExpenses(user.getUserId());
ArrayList<Category> categoryList = categoryDAO.getCategoriesByType(user.getUserId(), "Expense");
Expense editExpense = null;


String edit=request.getParameter("edit");

if(edit!=null){
    editExpense=expenseDAO.getExpenseById(Integer.parseInt(edit));
}
%>

<!DOCTYPE html>

<html>

<head>

<meta charset="UTF-8">

<title>Expense Management</title>

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

<h2>Expense Management</h2>

<form action="expense" method="post">

<%
if(editExpense!=null){
%>

<input type="hidden" name="action" value="update">

<input type="hidden"
name="expenseId"
value="<%=editExpense.getExpenseId()%>">

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
if(editExpense!=null && editExpense.getCategoryId()==c.getCategoryId()){
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

value="<%=editExpense!=null?editExpense.getAmount():""%>"

required>

<input
type="text"
name="description"
placeholder="Description"

value="<%=editExpense!=null?editExpense.getDescription():""%>"

required>

<input
type="date"
name="expenseDate"

value="<%=editExpense!=null?editExpense.getExpenseDate():""%>"

required>

<button type="submit">

<%=editExpense!=null?"Update Expense":"Add Expense"%>

</button>

<%
if(editExpense!=null){
%>

<a href="expenses.jsp">Cancel</a>

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

for(Expense e : expenseList){

%>

<tr>

<td><%=e.getExpenseId()%></td>

<td><%=e.getCategoryName()%></td>

<td>₹ <%=e.getAmount()%></td>

<td><%=e.getDescription()%></td>

<td><%=e.getExpenseDate()%></td>

<td>

<a
href="expenses.jsp?edit=<%=e.getExpenseId()%>"
style="color:green;font-weight:bold;">

Edit

</a>

|

<a
href="expense?action=delete&expenseId=<%=e.getExpenseId()%>"
style="color:red;font-weight:bold;"
onclick="return confirm('Delete this expense?');">

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