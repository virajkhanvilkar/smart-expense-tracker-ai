
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

    // STEP 1: Validate the registered email and send an OTP.
    private void sendOtp(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println(
                "DEBUG: ForgotPasswordServlet sendOtp() called");

        String email = request.getParameter("email");

        if (email == null || email.trim().isEmpty()
                || !EMAIL_PATTERN.matcher(email.trim()).matches()) {

            showError(request, response,
                    "Please enter a valid email on the login page.");
            return;
        }

        email = email.trim();

        UserDAO dao = new UserDAO();

        // Check that the email belongs to a registered account.
        if (!dao.emailExists(email)) {
            showError(request, response,
                    "No registered account was found for that email.");
            return;
        }

        // Generate a secure six-digit OTP.
        String otp = String.valueOf(
                100000 + SECURE_RANDOM.nextInt(900000));

        // Send only to the email that passed the database check.
        boolean sent = sendOTPEmail(email, otp);

        if (!sent) {
            showError(request, response,
                    "Unable to send OTP. Please try again later.");
            return;
        }

        HttpSession session = request.getSession();

        // Store the account and OTP server-side.
        session.setAttribute("resetEmail", email);
        session.setAttribute("resetOtp", otp);
        session.setAttribute(
                "otpExpiry",
                System.currentTimeMillis() + OTP_VALIDITY_MILLIS);

        session.removeAttribute("otpVerified");
        session.removeAttribute("otpAttempts");

        request.setAttribute("otpSent", true);

        request.getRequestDispatcher("forgotPassword.jsp")
                .forward(request, response);
    }

    // STEP 2: Verify the OTP.
    private void verifyOtp(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null) {
            showError(request, response,
                    "Your session expired. Please request a new OTP.");
            return;
        }

        String enteredOtp = request.getParameter("otp");
        String savedOtp = (String) session.getAttribute("resetOtp");
        Long expiry = (Long) session.getAttribute("otpExpiry");

        if (savedOtp == null || expiry == null) {
            clearOtpState(session);

            showError(request, response,
                    "OTP expired. Please request a new OTP.");
            return;
        }

        if (System.currentTimeMillis() > expiry) {
            clearOtpState(session);

            showError(request, response,
                    "OTP expired. Please request a new OTP.");
            return;
        }

        Integer attempts = (Integer) session.getAttribute("otpAttempts");

        if (attempts == null) {
            attempts = 0;
        }

        if (attempts >= 5) {
            clearOtpState(session);

            showError(request, response,
                    "Too many incorrect attempts. Please request a new OTP.");
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
            request.setAttribute("error",
                    "Invalid OTP. Please try again.");
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
                || !Boolean.TRUE.equals(
                        session.getAttribute("otpVerified"))) {

            showError(request, response,
                    "Please verify your OTP before resetting your password.");
            return;
        }

        // Matches name="newPassword" in forgotPassword.jsp.
        String password = request.getParameter("newPassword");
        String confirmPassword =
                request.getParameter("confirmPassword");

        if (password == null || confirmPassword == null
                || password.length() < 8
                || !password.equals(confirmPassword)) {

            request.setAttribute("verified", true);
            request.setAttribute("error",
                    "Enter matching passwords with at least 8 characters.");

            request.getRequestDispatcher("forgotPassword.jsp")
                    .forward(request, response);
            return;
        }

        String email = (String) session.getAttribute("resetEmail");

        if (email == null) {
            session.invalidate();
            showError(request, response,
                    "Your reset session expired. Please start again.");
            return;
        }

        UserDAO dao = new UserDAO();

        boolean updated = dao.updatePassword(email, password);

        if (updated) {
            session.invalidate();

            response.sendRedirect("login.jsp?reset=success");
        } else {
            request.setAttribute("verified", true);
            request.setAttribute("error",
                    "Password update failed. Please try again.");

            request.getRequestDispatcher("forgotPassword.jsp")
                    .forward(request, response);
        }
    }

    // STEP 4: Send OTP through the Resend API.
    private boolean sendOTPEmail(String recipientEmail, String otp) {

        String apiKey = System.getenv("RESEND_API_KEY");
        String fromEmail = System.getenv("RESEND_FROM_EMAIL");

        if (apiKey == null || apiKey.isBlank()) {
            System.err.println(
                    "ERROR: RESEND_API_KEY is not configured.");
            return false;
        }

        if (fromEmail == null || fromEmail.isBlank()) {
            System.err.println(
                    "ERROR: RESEND_FROM_EMAIL is not configured.");
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
                    + "If you did not request this reset, ignore this email.\n\n"
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

            System.out.println(
                    "DEBUG: Sending OTP request to Resend");

            HttpResponse<String> result = HTTP_CLIENT.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofString(
                            StandardCharsets.UTF_8));

            int status = result.statusCode();

            System.out.println(
                    "DEBUG: Resend HTTP status: " + status);

            if (status >= 200 && status < 300) {
                System.out.println(
                        "OTP email accepted by Resend.");
                return true;
            }

            // Never log the API key or OTP.
            System.err.println(
                    "ERROR: Resend API response: " + result.body());

            return false;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            System.err.println(
                    "ERROR: Resend request interrupted.");
            return false;

        } catch (Exception e) {
            System.err.println(
                    "ERROR: Resend request failed: "
                    + e.getClass().getSimpleName()
                    + ": " + e.getMessage());

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

    private void showError(HttpServletRequest request,
                           HttpServletResponse response,
                           String message)
            throws ServletException, IOException {

        request.setAttribute("error", message);

        request.getRequestDispatcher("forgotPassword.jsp")
                .forward(request, response);
    }
}
