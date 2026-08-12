<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>User Registration</title>

<style>

body{
    font-family: Arial;
    background:#f5f5f5;
}

.container{

    width:400px;
    margin:40px auto;
    background:white;
    padding:25px;
    border-radius:8px;
    box-shadow:0 0 10px gray;
}

input{

    width:100%;
    padding:10px;
    margin-top:8px;
    margin-bottom:15px;
}

button{

    width:100%;
    padding:10px;
    background:#2196F3;
    color:white;
    border:none;
    cursor:pointer;
}

a{

    text-decoration:none;
}

</style>

</head>
<body>

<div class="container">

<h2 align="center">User Registration</h2>

<form action="register" method="post">

<label>Full Name</label>

<input type="text"
name="fullName"
required>

<label>Email</label>

<input type="email"
name="email"
required>

<label>Password</label>

<input type="password"
name="password"
required>

<label>Phone</label>

<input type="text"
name="phone"
required>

<button type="submit">

Register

</button>

</form>

<br>

Already have account?

<a href="login.jsp">

Login

</a>

</div>

</body>
</html>