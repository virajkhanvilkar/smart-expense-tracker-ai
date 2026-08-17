package controller;

import java.io.IOException;

import ai_servic.AgentService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import model.User;

@WebServlet("/aiChat")
public class AIChatServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private AgentService agentService;


    /*
     * ============================================================
     * INITIALIZE
     * ============================================================
     */

    @Override
    public void init()
            throws ServletException {

        agentService =
                new AgentService();

        System.out.println(
                "AIChatServlet initialized successfully."
        );
    }


    /*
     * ============================================================
     * GET
     * ============================================================
     */

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);


        /*
         * Check login
         */

        if (session == null
                || session.getAttribute("user") == null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/login.jsp"
            );

            return;
        }


        /*
         * Open AI chat
         */

        request.getRequestDispatcher(
                "/ai-chat.jsp"
        ).forward(
                request,
                response
        );
    }


    /*
     * ============================================================
     * POST
     * ============================================================
     */

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {


        /*
         * --------------------------------------------------------
         * CHECK LOGIN
         * --------------------------------------------------------
         */

        HttpSession session =
                request.getSession(false);


        if (session == null
                || session.getAttribute("user") == null) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.setContentType(
                    "text/plain;charset=UTF-8"
            );

            response.getWriter().write(
                    "Please login first."
            );

            return;
        }


        /*
         * --------------------------------------------------------
         * GET USER
         * --------------------------------------------------------
         */

        User user =
                (User) session.getAttribute("user");


        int userId =
                user.getUserId();


        /*
         * --------------------------------------------------------
         * GET QUESTION
         * --------------------------------------------------------
         */

        String message =
                request.getParameter("message");


        /*
         * --------------------------------------------------------
         * VALIDATE QUESTION
         * --------------------------------------------------------
         */

        if (message == null
                || message.trim().isEmpty()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.setContentType(
                    "text/plain;charset=UTF-8"
            );

            response.getWriter().write(
                    "Please enter a question."
            );

            return;
        }


        message =
                message.trim();


        /*
         * --------------------------------------------------------
         * LOG QUESTION
         * --------------------------------------------------------
         */

        System.out.println(
                "========================================"
        );

        System.out.println(
                "AI Question: "
                + message
        );

        System.out.println(
                "User ID: "
                + userId
        );

        System.out.println(
                "========================================"
        );


        /*
         * --------------------------------------------------------
         * ASK REAL AI AGENT
         * --------------------------------------------------------
         */

        String answer;

        try {

            answer =
                    agentService.processQuestion(
                            userId,
                            message
                    );

        }

        catch (Exception e) {

            e.printStackTrace();

            answer =
                    "Sorry, I could not process your request.";
        }


        /*
         * --------------------------------------------------------
         * RETURN AI RESPONSE
         * --------------------------------------------------------
         */

        response.setContentType(
                "text/plain;charset=UTF-8"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );


        response.getWriter().write(
                answer
        );
    }
}