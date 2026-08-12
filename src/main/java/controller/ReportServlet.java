package controller;

import dao.ReportDAO;
import model.Report;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet("/report")
public class ReportServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        User user =
                (User) session.getAttribute("user");

        // ================= LOGIN CHECK =================

        if (user == null) {

            response.sendRedirect("login.jsp");

            return;
        }


        int userId = user.getUserId();


        ReportDAO reportDAO = new ReportDAO();


        // ================= GET FILTER VALUES =================

        String fromDate =
                request.getParameter("fromDate");

        String toDate =
                request.getParameter("toDate");

        String category =
                request.getParameter("category");

        String type =
                request.getParameter("type");


        // ================= GET FILTERED REPORT =================

        ArrayList<Report> reportList =
                reportDAO.getFilteredReport(
                        userId,
                        fromDate,
                        toDate,
                        category,
                        type
                );


        // ================= CALCULATE FILTERED TOTALS =================

        double totalIncome = 0;

        double totalExpense = 0;


        for (Report r : reportList) {

            if ("Income".equals(r.getType())) {

                totalIncome += r.getAmount();

            } else if ("Expense".equals(r.getType())) {

                totalExpense += r.getAmount();

            }

        }


        double balance =
                totalIncome - totalExpense;


        // ================= GET CATEGORIES =================

        ArrayList<String> categories =
                reportDAO.getCategories(userId);


        // ================= SEND DATA TO JSP =================

        request.setAttribute(
                "reportList",
                reportList
        );

        request.setAttribute(
                "totalIncome",
                totalIncome
        );

        request.setAttribute(
                "totalExpense",
                totalExpense
        );

        request.setAttribute(
                "balance",
                balance
        );

        request.setAttribute(
                "categories",
                categories
        );


        // Keep selected filters

        request.setAttribute(
                "fromDate",
                fromDate
        );

        request.setAttribute(
                "toDate",
                toDate
        );

        request.setAttribute(
                "selectedCategory",
                category
        );

        request.setAttribute(
                "selectedType",
                type
        );


        // ================= FORWARD =================

        request.getRequestDispatcher(
                "/reports.jsp"
        ).forward(request, response);
    }
}