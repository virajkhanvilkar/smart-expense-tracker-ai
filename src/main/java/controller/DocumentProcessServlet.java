package controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

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


    // ==========================================
    // INITIALIZE SERVICES
    // ==========================================

    @Override
    public void init() {

        documentDAO = new InputDocumentDAO();

        pdfProcessor = new PdfProcessor();

        documentProcessor = new DocumentProcessor();

        transactionDAO = new TransactionDAO();

        System.out.println(
                "DocumentProcessServlet initialized."
        );
    }


    // ==========================================
    // PROCESS DOCUMENT
    // ==========================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {


        // ==========================================
        // 1. CHECK LOGIN
        // ==========================================

        HttpSession session =
                request.getSession(false);

        if (session == null ||
            session.getAttribute("user") == null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/login.jsp"
            );

            return;
        }


        User user =
                (User) session.getAttribute("user");

        int userId =
                user.getUserId();


        // ==========================================
        // 2. GET DOCUMENT ID
        // ==========================================

        String documentIdParam =
                request.getParameter("documentId");


        if (documentIdParam == null ||
            documentIdParam.trim().isEmpty()) {

            showError(
                    request,
                    response,
                    "Document ID is missing."
            );

            return;
        }


        int documentId;


        try {

            documentId =
                    Integer.parseInt(
                            documentIdParam.trim()
                    );

        } catch (NumberFormatException e) {

            showError(
                    request,
                    response,
                    "Invalid document ID."
            );

            return;
        }


        // ==========================================
        // 3. GET DOCUMENT
        // ==========================================

        InputDocument document =
                documentDAO.getDocumentById(
                        documentId,
                        userId
                );


        if (document == null) {

            showError(
                    request,
                    response,
                    "Document not found."
            );

            return;
        }


        // ==========================================
        // 4. CHECK FILE TYPE
        // ==========================================

        if (!"pdf".equalsIgnoreCase(
                document.getFileType())) {

            showError(
                    request,
                    response,
                    "Currently only PDF files are supported."
            );

            return;
        }


        // ==========================================
        // 5. GET FILE PATH
        // ==========================================

        String filePath =
                document.getFilePath();


        if (filePath == null ||
            filePath.trim().isEmpty()) {

            showError(
                    request,
                    response,
                    "PDF file path is missing."
            );

            return;
        }


        File pdfFile =
                new File(filePath);


        System.out.println(
                "========================================"
        );

        System.out.println(
                "PROCESSING PDF"
        );

        System.out.println(
                "File Name : "
                + document.getFileName()
        );

        System.out.println(
                "File Path : "
                + pdfFile.getAbsolutePath()
        );

        System.out.println(
                "File Exists : "
                + pdfFile.exists()
        );

        System.out.println(
                "File Size : "
                + pdfFile.length()
                + " bytes"
        );


        if (!pdfFile.exists() ||
            !pdfFile.isFile()) {

            showError(
                    request,
                    response,
                    "Uploaded PDF file was not found."
            );

            return;
        }


        // ==========================================
        // 6. EXTRACT PDF TEXT
        // ==========================================

        String extractedText;


        try {

            extractedText =
                    pdfProcessor.extractText(
                            pdfFile
                    );

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    request,
                    response,
                    "Unable to read the PDF: "
                    + e.getMessage()
            );

            return;
        }


        // ==========================================
        // 7. CHECK EXTRACTED TEXT
        // ==========================================

        if (extractedText == null ||
            extractedText.trim().isEmpty()) {

            showError(
                    request,
                    response,
                    "PDF was opened successfully, "
                    + "but no readable text was extracted."
            );

            return;
        }


        System.out.println(
                "PDF TEXT EXTRACTED SUCCESSFULLY."
        );

        System.out.println(
                "Extracted text length: "
                + extractedText.length()
        );


        // ==========================================
        // 8. PRINT RAW PDF TEXT
        // ==========================================

        System.out.println(
                "========== RAW PDF TEXT =========="
        );

        System.out.println(
                extractedText
        );

        System.out.println(
                "=================================="
        );


        // ==========================================
        // 9. PROCESS TRANSACTIONS
        // ==========================================

        List<ExtractedTransaction> transactions;


        try {

            transactions =
                    documentProcessor.processText(
                            extractedText
                    );

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    request,
                    response,
                    "Unable to process transactions: "
                    + e.getMessage()
            );

            return;
        }


        // ==========================================
        // 10. CHECK TRANSACTIONS
        // ==========================================

        System.out.println(
                "========================================"
        );

        System.out.println(
                "TRANSACTION PROCESSING RESULT"
        );

        System.out.println(
                "PDF File: "
                + document.getFileName()
        );

        System.out.println(
                "Document ID: "
                + documentId
        );

        System.out.println(
                "User ID: "
                + userId
        );

        System.out.println(
                "Transactions Found: "
                + (transactions == null
                        ? 0
                        : transactions.size())
        );

        System.out.println(
                "========================================"
        );


        // ==========================================
        // CHECK NULL / EMPTY TRANSACTIONS
        // ==========================================

        if (transactions == null ||
            transactions.isEmpty()) {

            System.out.println(
                    "ERROR: No transactions extracted."
            );

            showError(
                    request,
                    response,
                    "No transactions were extracted from this PDF."
            );

            return;
        }


        // ==========================================
        // PRINT TRANSACTIONS
        // ==========================================

        for (ExtractedTransaction transaction
                : transactions) {

            System.out.println(
                    "Date        : "
                    + transaction.getDate()
            );

            System.out.println(
                    "Type        : "
                    + transaction.getType()
            );

            System.out.println(
                    "Category    : "
                    + transaction.getCategory()
            );

            System.out.println(
                    "Description : "
                    + transaction.getDescription()
            );

            System.out.println(
                    "Amount      : "
                    + transaction.getAmount()
            );

            System.out.println(
                    "----------------------------------------"
            );
        }


        System.out.println(
                "========================================"
        );


        // ==========================================
        // 11. SAVE TRANSACTIONS TO DATABASE
        // ==========================================

        System.out.println(
                "========================================"
        );

        System.out.println(
                "SAVING TRANSACTIONS TO DATABASE"
        );

        System.out.println(
                "Document ID : "
                + documentId
        );

        System.out.println(
                "User ID     : "
                + userId
        );

        System.out.println(
                "Transactions to save : "
                + transactions.size()
        );

        System.out.println(
                "========================================"
        );


        int insertedCount = 0;


        try {

            insertedCount =
                    transactionDAO.insertTransactions(
                            transactions,
                            documentId,
                            userId
                    );

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    request,
                    response,
                    "Transactions were extracted "
                    + "but could not be saved to database: "
                    + e.getMessage()
            );

            return;
        }


        // ==========================================
        // 12. DATABASE INSERT RESULT
        // ==========================================

        System.out.println(
                "========================================"
        );

        System.out.println(
                "DATABASE INSERT RESULT"
        );

        System.out.println(
                "Transactions Found : "
                + transactions.size()
        );

        System.out.println(
                "Transactions Inserted : "
                + insertedCount
        );

        System.out.println(
                "========================================"
        );


        // ==========================================
        // IMPORTANT:
        // IF NOTHING WAS INSERTED, STOP
        // ==========================================

        if (insertedCount == 0) {

            System.out.println(
                    "ERROR: ZERO TRANSACTIONS INSERTED."
            );

            showError(
                    request,
                    response,
                    "Transactions were extracted, "
                    + "but none were saved to the database. "
                    + "Please check the Tomcat console for "
                    + "the TransactionDAO error."
            );

            return;
        }


        // ==========================================
        // PARTIAL INSERT WARNING
        // ==========================================

        if (insertedCount < transactions.size()) {

            System.out.println(
                    "WARNING: Some transactions failed."
            );

            System.out.println(
                    "Expected : "
                    + transactions.size()
            );

            System.out.println(
                    "Inserted : "
                    + insertedCount
            );
        }


        // ==========================================
        // 13. SEND DATA TO JSP
        // ==========================================

        request.setAttribute(
                "document",
                document
        );


        request.setAttribute(
                "extractedText",
                extractedText
        );


        request.setAttribute(
                "transactions",
                transactions
        );


        request.setAttribute(
                "insertedCount",
                insertedCount
        );


        // ==========================================
        // 14. UPDATE DATABASE STATUS
        // ==========================================

        boolean statusUpdated =
                documentDAO.updateStatus(
                        documentId,
                        "ANALYZED"
                );


        System.out.println(
                "Document status updated: "
                + statusUpdated
        );


        // ==========================================
        // 15. SHOW RESULT PAGE
        // ==========================================

        request.getRequestDispatcher(
                "pdf-result.jsp"
        ).forward(
                request,
                response
        );
    }


    // ==========================================
    // ERROR HANDLER
    // ==========================================

    private void showError(
            HttpServletRequest request,
            HttpServletResponse response,
            String message)
            throws ServletException, IOException {


        request.setAttribute(
                "error",
                message
        );


        request.getRequestDispatcher(
                "input-document.jsp"
        ).forward(
                request,
                response
        );
    }
}