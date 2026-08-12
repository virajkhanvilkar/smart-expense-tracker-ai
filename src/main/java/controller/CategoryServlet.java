package controller;

import java.io.IOException;

import dao.CategoryDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Category;
import model.User;

@WebServlet("/category")
public class CategoryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // ========================= GET =========================

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {

            response.sendRedirect("login.jsp");
            return;
        }

        String action = request.getParameter("action");

        CategoryDAO dao = new CategoryDAO();

        // Delete Category
        if ("delete".equals(action)) {

            int id = Integer.parseInt(request.getParameter("id"));

            dao.deleteCategory(id);

            response.sendRedirect("categories.jsp");
            return;
        }

        // Edit Category
        if ("edit".equals(action)) {

            int id = Integer.parseInt(request.getParameter("id"));

            Category category = dao.getCategoryById(id);

            request.setAttribute("editCategory", category);

            request.getRequestDispatcher("categories.jsp")
                   .forward(request, response);

            return;
        }

        response.sendRedirect("categories.jsp");
    }

    // ========================= POST =========================

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

        CategoryDAO dao = new CategoryDAO();

        // ================= ADD =================

        if (action == null || action.equals("add")) {

            Category category = new Category();

            category.setUserId(user.getUserId());
            category.setCategoryName(request.getParameter("categoryName"));
            category.setType(request.getParameter("type"));

            dao.addCategory(category);

            response.sendRedirect("categories.jsp");
            return;
        }

        // ================= UPDATE =================

        if ("update".equals(action)) {

            Category category = new Category();

            category.setCategoryId(
                    Integer.parseInt(request.getParameter("categoryId")));

            category.setCategoryName(
                    request.getParameter("categoryName"));

            category.setType(
                    request.getParameter("type"));

            dao.updateCategory(category);

            response.sendRedirect("categories.jsp");
            return;
        }

    }

}