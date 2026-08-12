package controller;

import java.io.IOException;
import java.sql.Date;

import dao.IncomeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Income;
import model.User;

@WebServlet("/income")
public class IncomeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        IncomeDAO dao = new IncomeDAO();

        if ("delete".equals(action)) {

            int incomeId = Integer.parseInt(request.getParameter("incomeId"));

            dao.deleteIncome(incomeId);

            response.sendRedirect("income.jsp");
        }
    }

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

        IncomeDAO dao = new IncomeDAO();

        // ================= ADD =================

        if ("add".equals(action)) {

            Income income = new Income();

            income.setUserId(user.getUserId());
            income.setCategoryId(
                    Integer.parseInt(request.getParameter("categoryId")));
            income.setAmount(
                    Double.parseDouble(request.getParameter("amount")));
            income.setDescription(
                    request.getParameter("description"));
            income.setIncomeDate(
                    Date.valueOf(request.getParameter("incomeDate")));

            dao.addIncome(income);

            response.sendRedirect("income.jsp");
        }

        // ================= UPDATE =================

        else if ("update".equals(action)) {

            Income income = new Income();

            income.setIncomeId(
                    Integer.parseInt(request.getParameter("incomeId")));
            income.setCategoryId(
                    Integer.parseInt(request.getParameter("categoryId")));
            income.setAmount(
                    Double.parseDouble(request.getParameter("amount")));
            income.setDescription(
                    request.getParameter("description"));
            income.setIncomeDate(
                    Date.valueOf(request.getParameter("incomeDate")));

            dao.updateIncome(income);

            response.sendRedirect("income.jsp");
        }

    }

}