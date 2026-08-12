package controller;

import java.io.IOException;

import dao.BudgetDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Budget;
import model.User;

@WebServlet("/budget")
public class BudgetServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public BudgetServlet() {
        super();
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");

        String action = request.getParameter("action");

        BudgetDAO dao = new BudgetDAO();

        try {

            Budget budget = new Budget();

            budget.setUserId(user.getUserId());
            budget.setCategoryId(Integer.parseInt(request.getParameter("categoryId")));
            budget.setBudgetAmount(Double.parseDouble(request.getParameter("budgetAmount")));
            budget.setMonth(Integer.parseInt(request.getParameter("month")));
            budget.setYear(Integer.parseInt(request.getParameter("year")));

            if ("update".equalsIgnoreCase(action)) {

                budget.setBudgetId(Integer.parseInt(request.getParameter("budgetId")));

                dao.updateBudget(budget);

            } else {

                dao.addBudget(budget);

            }

            response.sendRedirect(request.getContextPath() + "/budget.jsp");

        } catch (Exception e) {

            e.printStackTrace();

            response.getWriter().println(
                "<script>alert('Unable to Save Budget');location='budget.jsp';</script>");

        }

    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {

            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;

        }

        String action = request.getParameter("action");

        BudgetDAO dao = new BudgetDAO();

        if ("delete".equalsIgnoreCase(action)) {

            try {

                int budgetId = Integer.parseInt(request.getParameter("budgetId"));

                dao.deleteBudget(budgetId);

            } catch (Exception e) {

                e.printStackTrace();

            }

        }

        response.sendRedirect(request.getContextPath() + "/budget.jsp");

    }

}