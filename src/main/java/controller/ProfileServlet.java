package controller;

import java.io.IOException;

import dao.ProfileDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private ProfileDAO profileDAO;

    @Override
    public void init() {
        profileDAO = new ProfileDAO();
    }


    // Open Profile Page
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // Check Login
        if (session == null || session.getAttribute("user") == null) {

            response.sendRedirect("login.jsp");
            return;
        }

        User sessionUser = (User) session.getAttribute("user");

        int userId = sessionUser.getUserId();

        User user = profileDAO.getUserById(userId);

        if (user != null) {

            request.setAttribute("profileUser", user);

        } else {

            request.setAttribute("error",
                    "Unable to load profile.");
        }

        request.getRequestDispatcher("profile.jsp")
               .forward(request, response);
    }


    // Update Profile
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // Check Login
        if (session == null || session.getAttribute("user") == null) {

            response.sendRedirect("login.jsp");
            return;
        }

        User sessionUser = (User) session.getAttribute("user");

        int userId = sessionUser.getUserId();

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");


        // Validation
        if (fullName == null || fullName.trim().isEmpty()
                || email == null || email.trim().isEmpty()) {

            request.setAttribute("error",
                    "Full Name and Email are required.");

            doGet(request, response);
            return;
        }


        boolean updated = profileDAO.updateProfile(
                userId,
                fullName.trim(),
                email.trim(),
                phone
        );


        if (updated) {

            // Update session data
            sessionUser.setFullName(fullName.trim());
            sessionUser.setEmail(email.trim());
            sessionUser.setPhone(phone);

            session.setAttribute("user", sessionUser);

            response.sendRedirect(
                    request.getContextPath()
                    + "/profile?success=1"
            );

        } else {

            request.setAttribute("error",
                    "Profile update failed.");

            doGet(request, response);
        }
    }
}