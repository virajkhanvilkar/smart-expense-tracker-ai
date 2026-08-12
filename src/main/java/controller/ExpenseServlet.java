package controller;

import java.io.IOException;
import java.sql.Date;

import dao.ExpenseDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Expense;
import model.User;

@WebServlet("/expense")
public class ExpenseServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");

        String action = request.getParameter("action");

        ExpenseDAO dao = new ExpenseDAO();

        // ================= ADD =================

        if ("add".equals(action)) {

            Expense expense = new Expense();

            expense.setUserId(user.getUserId());
            expense.setCategoryId(Integer.parseInt(request.getParameter("categoryId")));
            expense.setAmount(Double.parseDouble(request.getParameter("amount")));
            expense.setDescription(request.getParameter("description"));
            expense.setExpenseDate(Date.valueOf(request.getParameter("expenseDate")));

            dao.addExpense(expense);

            response.sendRedirect("expenses.jsp");
        }

        // ================= UPDATE =================

        else if ("update".equals(action)) {

            Expense expense = new Expense();

            expense.setExpenseId(Integer.parseInt(request.getParameter("expenseId")));
            expense.setCategoryId(Integer.parseInt(request.getParameter("categoryId")));
            expense.setAmount(Double.parseDouble(request.getParameter("amount")));
            expense.setDescription(request.getParameter("description"));
            expense.setExpenseDate(Date.valueOf(request.getParameter("expenseDate")));

            dao.updateExpense(expense);

            response.sendRedirect("expenses.jsp");
        }

        // ================= DELETE =================

        else if ("delete".equals(action)) {

            int expenseId = Integer.parseInt(request.getParameter("expenseId"));

            dao.deleteExpense(expenseId);

            response.sendRedirect("expenses.jsp");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        doPost(request, response);

    }
}