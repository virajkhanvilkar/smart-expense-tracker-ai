package controller;

import dao.UserDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.regex.Pattern;

@WebServlet("/ForgotPasswordServlet")
public class ForgotPasswordServlet extends HttpServlet {

    private static final long OTP_VALIDITY_MILLIS = 5 * 60 * 1000L;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

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

    // STEP 1: Generate and send OTP.
    private void sendOtp(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");

        if (email == null || email.trim().isEmpty()
                || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            showAlert(response, "Please enter a valid registered email.",
                    "forgotPassword.jsp");
            return;
        }

        email = email.trim();

        UserDAO dao = new UserDAO();

        if (!dao.emailExists(email)) {
            showAlert(response, "This email is not registered.",
                    "forgotPassword.jsp");
            return;
        }

        // Generate a cryptographically secure six-digit OTP.
        String otp = String.valueOf(100000 + SECURE_RANDOM.nextInt(900000));

        boolean sent = sendOTPEmail(email, otp);

        if (sent) {
            HttpSession session = request.getSession();
            session.setAttribute("resetEmail", email);
            session.setAttribute("resetOtp", otp);
            session.setAttribute("otpExpiry",
                    System.currentTimeMillis() + OTP_VALIDITY_MILLIS);
            session.removeAttribute("otpVerified");
            session.removeAttribute("otpAttempts");

            request.setAttribute("otpSent", true);
            request.getRequestDispatcher("forgotPassword.jsp")
                    .forward(request, response);
        } else {
            // Do not keep a usable OTP in the session if email delivery failed.
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute("resetOtp");
                session.removeAttribute("otpExpiry");
                session.removeAttribute("otpVerified");
            }

            showAlert(response,
                    "Unable to send OTP. Please check the email configuration and try again.",
                    "forgotPassword.jsp");
        }
    }

    // STEP 2: Verify the OTP.
    private void verifyOtp(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            request.setAttribute("error",
                    "OTP session expired. Please request a new OTP.");
            request.getRequestDispatcher("forgotPassword.jsp")
                    .forward(request, response);
            return;
        }

        String enteredOtp = request.getParameter("otp");
        String savedOtp = (String) session.getAttribute("resetOtp");
        Long expiry = (Long) session.getAttribute("otpExpiry");

        if (savedOtp == null || expiry == null) {
            request.setAttribute("error",
                    "OTP expired. Please request a new OTP.");
            request.getRequestDispatcher("forgotPassword.jsp")
                    .forward(request, response);
            return;
        }

        if (System.currentTimeMillis() > expiry) {
            clearOtpState(session);
            request.setAttribute("error",
                    "OTP has expired. Please request a new OTP.");
            request.getRequestDispatcher("forgotPassword.jsp")
                    .forward(request, response);
            return;
        }

        Integer attempts = (Integer) session.getAttribute("otpAttempts");
        if (attempts == null) {
            attempts = 0;
        }

        if (attempts >= 5) {
            clearOtpState(session);
            request.setAttribute("error",
                    "Too many incorrect attempts. Please request a new OTP.");
            request.getRequestDispatcher("forgotPassword.jsp")
                    .forward(request, response);
            return;
        }

        if (enteredOtp != null && savedOtp.equals(enteredOtp.trim())) {
            session.setAttribute("otpVerified", true);
            session.removeAttribute("resetOtp");
            session.removeAttribute("otpAttempts");

            request.setAttribute("verified", true);
        } else {
            session.setAttribute("otpAttempts", attempts + 1);
            request.setAttribute("otpSent", true);
            request.setAttribute("error", "Invalid OTP. Please try again.");
        }

        request.getRequestDispatcher("forgotPassword.jsp")
                .forward(request, response);
    }

    // STEP 3: Reset the password after successful OTP verification.
    private void resetPassword(HttpServletRequest request,
                               HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null
                || !Boolean.TRUE.equals(session.getAttribute("otpVerified"))) {
            response.sendRedirect("login.jsp");
            return;
        }

        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        if (password == null || confirmPassword == null
                || password.trim().isEmpty()
                || confirmPassword.trim().isEmpty()) {
            request.setAttribute("verified", true);
            request.setAttribute("error", "Please enter both passwords.");
            request.getRequestDispatcher("forgotPassword.jsp")
                    .forward(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("verified", true);
            request.setAttribute("error", "Passwords do not match.");
            request.getRequestDispatcher("forgotPassword.jsp")
                    .forward(request, response);
            return;
        }

        String email = (String) session.getAttribute("resetEmail");
        if (email == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        UserDAO dao = new UserDAO();
        boolean updated = dao.updatePassword(email, password);

        if (updated) {
            session.invalidate();
            showAlert(response, "Password reset successfully!", "login.jsp");
        } else {
            request.setAttribute("verified", true);
            request.setAttribute("error", "Failed to update password.");
            request.getRequestDispatcher("forgotPassword.jsp")
                    .forward(request, response);
        }
    }

    // Send OTP through Resend's HTTPS API (not SMTP).
    private boolean sendOTPEmail(String recipientEmail, String otp) {
        String apiKey = System.getenv("RESEND_API_KEY");
        String fromEmail = System.getenv("RESEND_FROM_EMAIL");

        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("RESEND_API_KEY is not configured.");
            return false;
        }

        if (fromEmail == null || fromEmail.isBlank()) {
            System.err.println("RESEND_FROM_EMAIL is not configured.");
            return false;
        }

        try {
            String subject =
                    "Smart Expense Tracker - Password Reset OTP";
            String body =
                    "Hello,\n\n"
                    + "Your Smart Expense Tracker password reset OTP is:\n\n"
                    + otp + "\n\n"
                    + "This OTP is valid for 5 minutes.\n\n"
                    + "If you did not request a password reset, please ignore this email.\n\n"
                    + "Regards,\nSmart Expense Tracker";

            String json = "{"
                    + "\"from\":\"" + escapeJson(fromEmail) + "\","
                    + "\"to\":[\"" + escapeJson(recipientEmail) + "\"],"
                    + "\"subject\":\"" + escapeJson(subject) + "\","
                    + "\"text\":\"" + escapeJson(body) + "\""
                    + "}";

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> result = HTTP_CLIENT.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            int status = result.statusCode();

            if (status >= 200 && status < 300) {
                System.out.println("OTP email accepted by Resend.");
                return true;
            }

            // The response can contain useful provider error details.
            // Never print the API key.
            System.err.println("Resend API HTTP status: " + status);
            System.err.println("Resend API response: " + result.body());
            return false;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Resend OTP request was interrupted.");
            return false;
        } catch (Exception e) {
            System.err.println("Failed to send OTP through Resend: "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
            return false;
        }
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private void clearOtpState(HttpSession session) {
        session.removeAttribute("resetOtp");
        session.removeAttribute("otpExpiry");
        session.removeAttribute("otpVerified");
        session.removeAttribute("otpAttempts");
    }

    private void showAlert(HttpServletResponse response,
                           String message,
                           String redirectPath) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        String safeMessage = message.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\r", "")
                .replace("\n", "\\n");
        String safePath = redirectPath.replace("'", "");
        response.getWriter().println(
                "<script>alert('" + safeMessage + "');"
                + "location='" + safePath + "';</script>");
    }
}
