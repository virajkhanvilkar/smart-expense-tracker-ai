<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%>

<%@ page import="java.util.ArrayList"%>
<%@ page import="dao.CategoryDAO"%>
<%@ page import="model.Category"%>
<%@ page import="model.User"%>

<%
User user = (User)session.getAttribute("user");

if(user==null){
    response.sendRedirect("login.jsp");
    return;
}

CategoryDAO dao = new CategoryDAO();

ArrayList<Category> list = dao.getCategories(user.getUserId());

// Used for Edit
Category editCategory = (Category)request.getAttribute("editCategory");
%>

<!DOCTYPE html>
<html>
<head>

<meta charset="UTF-8">

<title>Category Management</title>

<style>

body{
    font-family:Arial;
    background:#f4f4f4;
}

.container{
    width:80%;
    margin:auto;
    margin-top:30px;
}

h2{
    color:#333;
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
    background:#0b7dda;
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

.edit{
    color:green;
    font-weight:bold;
}

.delete{
    color:red;
    font-weight:bold;
}

</style>

</head>

<body>

<div class="container">

<h2>Category Management</h2>

<form action="category" method="post">

<input type="hidden"
name="action"
value="<%= (editCategory==null) ? "add" : "update" %>">

<%
if(editCategory!=null){
%>

<input type="hidden"
name="categoryId"
value="<%= editCategory.getCategoryId() %>">

<%
}
%>

<input
type="text"
name="categoryName"
placeholder="Category Name"
required
value="<%= (editCategory==null) ? "" : editCategory.getCategoryName() %>">

<select name="type">

<option value="Income"
<%= (editCategory!=null && editCategory.getType().equals("Income")) ? "selected" : "" %>>
Income
</option>

<option value="Expense"
<%= (editCategory!=null && editCategory.getType().equals("Expense")) ? "selected" : "" %>>
Expense
</option>

</select>

<button type="submit">

<%= (editCategory==null) ? "Add Category" : "Update Category" %>

</button>

<%
if(editCategory!=null){
%>

<a href="categories.jsp">Cancel</a>

<%
}
%>

</form>

<table>

<tr>

<th>ID</th>

<th>Category</th>

<th>Type</th>

<th>Action</th>

</tr>

<%
for(Category c : list){
%>

<tr>

<td><%= c.getCategoryId() %></td>

<td><%= c.getCategoryName() %></td>

<td><%= c.getType() %></td>

<td>

<a class="edit"
href="category?action=edit&id=<%=c.getCategoryId()%>">

Edit

</a>

|

<a class="delete"
href="category?action=delete&id=<%=c.getCategoryId()%>"
onclick="return confirm('Delete this category?')">

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