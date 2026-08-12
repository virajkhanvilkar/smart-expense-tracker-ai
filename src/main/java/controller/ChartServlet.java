package controller;

import java.io.IOException;

import dao.ChartDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

@WebServlet("/chart")
public class ChartServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {

            response.sendRedirect("login.jsp");
            return;

        }

        User user = (User) session.getAttribute("user");

        ChartDAO dao = new ChartDAO();

        request.setAttribute(
                "incomeExpense",
                dao.getIncomeExpenseData(user.getUserId()));

        request.setAttribute(
                "expenseCategory",
                dao.getExpenseCategoryData(user.getUserId()));

        request.setAttribute(
                "monthlyExpense",
                dao.getMonthlyExpenseData(user.getUserId()));

        request.getRequestDispatcher("dashboard.jsp")
                .forward(request, response);

    }

}