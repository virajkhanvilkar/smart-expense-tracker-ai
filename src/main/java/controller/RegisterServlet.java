package controller;

import java.io.IOException;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String phone = request.getParameter("phone");

        UserDAO dao = new UserDAO();

        if (dao.emailExists(email)) {

            response.getWriter().println(
                    "<script>alert('Email already exists');location='register.jsp';</script>");

            return;
        }

        User user = new User();

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(password);
        user.setPhone(phone);

        boolean status = dao.registerUser(user);

        if (status) {

            response.sendRedirect("login.jsp");

        } else {

            response.getWriter().println(
                    "<script>alert('Registration Failed');location='register.jsp';</script>");
        }

    }

}