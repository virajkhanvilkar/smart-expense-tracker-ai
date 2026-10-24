
package controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import dao.InputDocumentDAO;
import dao.TransactionDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import model.ExtractedTransaction;
import model.InputDocument;
import model.User;

import service.DocumentProcessor;
import service.PdfProcessor;

@WebServlet("/processDocument")
public class DocumentProcessServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private InputDocumentDAO documentDAO;
    private PdfProcessor pdfProcessor;
    private DocumentProcessor documentProcessor;
    private TransactionDAO transactionDAO;

    @Override
    public void init() {
        documentDAO = new InputDocumentDAO();
        pdfProcessor = new PdfProcessor();
        documentProcessor = new DocumentProcessor();
        transactionDAO = new TransactionDAO();

        System.out.println("DocumentProcessServlet initialized.");
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        processDocument(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        processDocument(request, response);
    }

    private void processDocument(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Check login
        HttpSession session = request.getSession(false);

        if (session == null ||
                session.getAttribute("user") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/login.jsp");
            return;
        }

        Object sessionUser = session.getAttribute("user");

        if (!(sessionUser instanceof User)) {
            response.sendRedirect(
                    request.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) sessionUser;
        int userId = user.getUserId();

        // 2. Validate document ID
        String documentIdParam = request.getParameter("documentId");

        if (documentIdParam == null ||
                documentIdParam.trim().isEmpty()) {

            showError(request, response, "Document ID is missing.");
            return;
        }

        final int documentId;

        try {
            documentId = Integer.parseInt(documentIdParam.trim());
        } catch (NumberFormatException ex) {
            showError(request, response, "Invalid document ID.");
            return;
        }

        // 3. Load document belonging to the logged-in user
        InputDocument document =
                documentDAO.getDocumentById(documentId, userId);

        if (document == null) {
            showError(request, response, "Document not found.");
            return;
        }

        // 4. Validate PDF
        if (!"pdf".equalsIgnoreCase(document.getFileType())) {
            showError(
                    request,
                    response,
                    "Currently only PDF files are supported.");
            return;
        }

        String filePath = document.getFilePath();

        if (filePath == null || filePath.trim().isEmpty()) {
            showError(request, response, "PDF file path is missing.");
            return;
        }

        File pdfFile = new File(filePath);

        if (!pdfFile.isFile()) {
            showError(
                    request,
                    response,
                    "Uploaded PDF file was not found: "
                            + pdfFile.getAbsolutePath());
            return;
        }

        // 5. Extract PDF text
        final String extractedText;

        try {
            extractedText = pdfProcessor.extractText(pdfFile);
        } catch (Exception ex) {
            ex.printStackTrace();

            showError(
                    request,
                    response,
                    "Unable to read PDF: " + ex.getMessage());
            return;
        }

        if (extractedText == null || extractedText.trim().isEmpty()) {
            showError(
                    request,
                    response,
                    "No readable text was found. A scanned PDF may require OCR.");
            return;
        }

        System.out.println("========== PDF TEXT ==========");
        System.out.println(
                extractedText.substring(
                        0, Math.min(extractedText.length(), 4000)));
        System.out.println("==============================");

        // 6. Extract opening balance dynamically
        double openingBalance =
                documentProcessor.getOpeningBalance(extractedText);

        System.out.println(
                "Servlet opening balance = "
                        + (Double.isFinite(openingBalance)
                        ? String.format(Locale.ROOT, "%.2f", openingBalance)
                        : "NOT FOUND"));

        // 7. Parse transactions
        final List<ExtractedTransaction> transactions;

        try {
            transactions = documentProcessor.processText(extractedText);
        } catch (Exception ex) {
            ex.printStackTrace();

            showError(
                    request,
                    response,
                    "Unable to process transactions: " + ex.getMessage());
            return;
        }

        if (transactions == null || transactions.isEmpty()) {
            request.setAttribute(
                    "error",
                    "No transactions could be extracted from this PDF.");
            request.setAttribute("document", document);
            request.setAttribute("documentId", documentId);
            request.getRequestDispatcher("pdf-result.jsp")
                    .forward(request, response);
            return;
        }

        // 8. Calculate income, expenses and closing balance
        double totalIncome = 0.0;
        double totalExpense = 0.0;

        for (ExtractedTransaction tx : transactions) {
            if (tx == null) {
                continue;
            }

            if ("INCOME".equalsIgnoreCase(tx.getType())) {
                totalIncome += tx.getAmount();
            } else if ("EXPENSE".equalsIgnoreCase(tx.getType())) {
                totalExpense += tx.getAmount();
            }
        }

        totalIncome = Math.round(totalIncome * 100.0) / 100.0;
        totalExpense = Math.round(totalExpense * 100.0) / 100.0;

        Double closingBalance = null;

        if (Double.isFinite(openingBalance)) {
            closingBalance = Math.round(
                    (openingBalance + totalIncome - totalExpense) * 100.0
            ) / 100.0;
        }

        // 9. Save transactions
        int insertedCount;

        try {
            insertedCount = transactionDAO.insertTransactions(
                    transactions,
                    documentId,
                    userId);
        } catch (Exception ex) {
            ex.printStackTrace();

            showError(
                    request,
                    response,
                    "Transactions were extracted but could not be saved: "
                            + ex.getMessage());
            return;
        }

        // 10. Update document status
        boolean statusUpdated =
                documentDAO.updateStatus(documentId, "ANALYZED");

        System.out.println("Transactions extracted: " + transactions.size());
        System.out.println("Transactions inserted: " + insertedCount);
        System.out.println("Total income: " + totalIncome);
        System.out.println("Total expenses: " + totalExpense);
        System.out.println("Document status updated: " + statusUpdated);

        // 11. Pass all data to the result JSP
        request.setAttribute("document", document);
        request.setAttribute("documentId", documentId);
        request.setAttribute("extractedText", extractedText);
        request.setAttribute("transactions", transactions);
        request.setAttribute("transactionCount", transactions.size());
        request.setAttribute("insertedCount", insertedCount);

        request.setAttribute("openingBalance",
                Double.isFinite(openingBalance)
                        ? openingBalance
                        : null);

        request.setAttribute("closingBalance", closingBalance);
        request.setAttribute("totalIncome", totalIncome);
        request.setAttribute("totalExpense", totalExpense);

        // 12. Forward to the existing result page
        request.getRequestDispatcher("pdf-result.jsp")
                .forward(request, response);
    }

    private void showError(
            HttpServletRequest request,
            HttpServletResponse response,
            String message)
            throws ServletException, IOException {

        request.setAttribute("error", message);

        request.getRequestDispatcher("input-document.jsp")
                .forward(request, response);
    }
}
