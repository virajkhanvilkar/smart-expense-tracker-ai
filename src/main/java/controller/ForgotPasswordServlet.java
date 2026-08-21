package controller;

import dao.UserDAO;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;

@WebServlet("/ForgotPasswordServlet")
public class ForgotPasswordServlet extends HttpServlet {

    // =====================================================
    // GET
    // =====================================================

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("sendOtp".equals(action)) {
            sendOtp(request, response);
        } else {
            response.sendRedirect("login.jsp");
        }
    }


    // =====================================================
    // POST
    // =====================================================

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("verifyOtp".equals(action)) {
            verifyOtp(request, response);
            return;
        }

        if ("resetPassword".equals(action)) {
            resetPassword(request, response);
            return;
        }

        response.sendRedirect("login.jsp");
    }


    // =====================================================
    // STEP 1 : SEND OTP
    // =====================================================

    private void sendOtp(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");

        if (email == null || email.trim().isEmpty()) {

            response.getWriter().println(
                "<script>" +
                "alert('Please enter your registered email.');" +
                "location='login.jsp';" +
                "</script>"
            );

            return;
        }

        email = email.trim();

        UserDAO dao = new UserDAO();

        // Check registered email
        if (!dao.emailExists(email)) {

            response.getWriter().println(
                "<script>" +
                "alert('This email is not registered.');" +
                "location='login.jsp';" +
                "</script>"
            );

            return;
        }

        // Generate 6 digit OTP
        Random random = new Random();

        String otp = String.valueOf(
                100000 + random.nextInt(900000)
        );

        // Create session
        HttpSession session = request.getSession();

        session.setAttribute("resetEmail", email);

        session.setAttribute("resetOtp", otp);

        // OTP valid for 5 minutes
        session.setAttribute(
                "otpExpiry",
                System.currentTimeMillis() + (5 * 60 * 1000)
        );

        // Send OTP
        boolean sent = sendOTPEmail(email, otp);

        if (sent) {

            request.setAttribute(
                    "otpSent",
                    true
            );

            request.getRequestDispatcher(
                    "forgotPassword.jsp"
            ).forward(request, response);

        } else {

            response.getWriter().println(
                "<script>" +
                "alert('Unable to send OTP. Please check email configuration.');" +
                "location='forgotPassword.jsp';" +
                "</script>"
            );
        }
    }


    // =====================================================
    // STEP 2 : VERIFY OTP
    // =====================================================

    private void verifyOtp(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        String enteredOtp =
                request.getParameter("otp");

        HttpSession session =
                request.getSession();

        String savedOtp =
                (String) session.getAttribute("resetOtp");

        Long expiry =
                (Long) session.getAttribute("otpExpiry");


        if (savedOtp == null || expiry == null) {

            request.setAttribute(
                    "error",
                    "OTP expired. Please request a new OTP."
            );

            request.getRequestDispatcher(
                    "forgotPassword.jsp"
            ).forward(request, response);

            return;
        }


        // Check expiry

        if (System.currentTimeMillis() > expiry) {

            session.removeAttribute("resetOtp");
            session.removeAttribute("otpExpiry");

            request.setAttribute(
                    "error",
                    "OTP has expired. Please request a new OTP."
            );

            request.getRequestDispatcher(
                    "forgotPassword.jsp"
            ).forward(request, response);

            return;
        }


        // Verify OTP

        if (savedOtp.equals(enteredOtp)) {

            session.setAttribute(
                    "otpVerified",
                    true
            );

            request.setAttribute(
                    "verified",
                    true
            );

        } else {

            request.setAttribute(
                    "otpSent",
                    true
            );

            request.setAttribute(
                    "error",
                    "Invalid OTP. Please try again."
            );
        }


        request.getRequestDispatcher(
                "forgotPassword.jsp"
        ).forward(request, response);
    }


    // =====================================================
    // STEP 3 : RESET PASSWORD
    // =====================================================

    private void resetPassword(HttpServletRequest request,
                               HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession();

        Boolean verified =
                (Boolean) session.getAttribute(
                        "otpVerified"
                );


        if (verified == null || !verified) {

            response.sendRedirect("login.jsp");

            return;
        }


        String password =
                request.getParameter("password");

        String confirmPassword =
                request.getParameter("confirmPassword");


        // Check password

        if (password == null ||
            confirmPassword == null ||
            password.trim().isEmpty() ||
            confirmPassword.trim().isEmpty()) {

            request.setAttribute(
                    "verified",
                    true
            );

            request.setAttribute(
                    "error",
                    "Please enter both passwords."
            );

            request.getRequestDispatcher(
                    "forgotPassword.jsp"
            ).forward(request, response);

            return;
        }


        // Confirm password

        if (!password.equals(confirmPassword)) {

            request.setAttribute(
                    "verified",
                    true
            );

            request.setAttribute(
                    "error",
                    "Passwords do not match."
            );

            request.getRequestDispatcher(
                    "forgotPassword.jsp"
            ).forward(request, response);

            return;
        }


        // Get registered email

        String email =
                (String) session.getAttribute(
                        "resetEmail"
                );


        if (email == null) {

            response.sendRedirect("login.jsp");

            return;
        }


        // Update password

        UserDAO dao = new UserDAO();

        boolean updated =
                dao.updatePassword(
                        email,
                        password
                );


        if (updated) {

            session.invalidate();

            response.getWriter().println(
                "<script>" +
                "alert('Password reset successfully!');" +
                "location='login.jsp';" +
                "</script>"
            );

        } else {

            request.setAttribute(
                    "verified",
                    true
            );

            request.setAttribute(
                    "error",
                    "Failed to update password."
            );

            request.getRequestDispatcher(
                    "forgotPassword.jsp"
            ).forward(request, response);
        }
    }


    // =====================================================
    // SEND EMAIL
    // =====================================================

    private boolean sendOTPEmail(String recipientEmail,
                                 String otp) {

        try {

            // =================================================
            // SENDER GMAIL
            // =================================================

            final String senderEmail =
                    "smartexpensetracker22@gmail.com";


            // =================================================
            // GOOGLE APP PASSWORD
            // =================================================
            // DO NOT PUT Smart@2003 HERE.
            //
            // Generate a Google App Password and put it here.
            // =================================================

            final String appPassword =
                    "nyvk bxhh etcx fdvu";


            // =================================================
            // SMTP SETTINGS
            // =================================================

            Properties properties =
                    new Properties();

            properties.put(
                    "mail.smtp.host",
                    "smtp.gmail.com"
            );

            properties.put(
                    "mail.smtp.port",
                    "587"
            );

            properties.put(
                    "mail.smtp.auth",
                    "true"
            );

            properties.put(
                    "mail.smtp.starttls.enable",
                    "true"
            );


            // =================================================
            // MAIL SESSION
            // =================================================

            Session session =
                    Session.getInstance(
                            properties,
                            new Authenticator() {

                                @Override
                                protected PasswordAuthentication
                                getPasswordAuthentication() {

                                    return new PasswordAuthentication(
                                            senderEmail,
                                            appPassword
                                    );
                                }
                            }
                    );


            // =================================================
            // EMAIL
            // =================================================

            Message message =
                    new MimeMessage(session);

            message.setFrom(
                    new InternetAddress(
                            senderEmail
                    )
            );


            // Send to registered email

            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(
                            recipientEmail
                    )
            );


            message.setSubject(
                    "Smart Expense Tracker - Password Reset OTP"
            );


            message.setText(
                    "Hello,\n\n" +

                    "Your Smart Expense Tracker " +
                    "password reset OTP is:\n\n" +

                    otp + "\n\n" +

                    "This OTP is valid for 5 minutes.\n\n" +

                    "If you did not request a password reset, " +
                    "please ignore this email.\n\n" +

                    "Regards,\n" +
                    "Smart Expense Tracker"
            );


            // =================================================
            // SEND
            // =================================================

            Transport.send(message);

            System.out.println(
                    "OTP successfully sent to: "
                    + recipientEmail
            );

            return true;


        } catch (Exception e) {

            System.out.println(
                    "ERROR WHILE SENDING OTP:"
            );

            e.printStackTrace();

            return false;
        }
    }
}