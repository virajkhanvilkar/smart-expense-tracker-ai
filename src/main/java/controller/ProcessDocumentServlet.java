package controller;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import dao.TransactionDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import model.ExtractedTransaction;
import service.DocumentProcessor;
import util.DBConnection;


@WebServlet("/ProcessDocumentServlet")
public class ProcessDocumentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;


    public ProcessDocumentServlet() {
        super();
    }


    // ============================================================
    // GET
    // ============================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        processDocument(request, response);
    }


    // ============================================================
    // POST
    // ============================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        processDocument(request, response);
    }


    // ============================================================
    // MAIN DOCUMENT PROCESS
    // ============================================================

    private void processDocument(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {


        System.out.println(
                "======================================"
        );

        System.out.println(
                "ProcessDocumentServlet started"
        );

        System.out.println(
                "======================================"
        );


        // ========================================================
        // STEP 1: GET DOCUMENT ID
        // ========================================================

        String documentIdText =
                request.getParameter("documentId");


        if (documentIdText == null
                || documentIdText.trim().isEmpty()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Document ID is missing."
            );

            return;
        }


        int documentId;


        try {

            documentId =
                    Integer.parseInt(
                            documentIdText.trim()
                    );

        } catch (NumberFormatException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid document ID."
            );

            return;
        }


        System.out.println(
                "Document ID = "
                + documentId
        );


        // ========================================================
        // STEP 2: GET USER ID FROM SESSION
        // ========================================================

        HttpSession session =
                request.getSession(false);


        if (session == null) {

            response.sendRedirect(
                    "login.jsp"
            );

            return;
        }


        Object userIdObject =
                session.getAttribute("userId");


        if (userIdObject == null) {

            response.sendRedirect(
                    "login.jsp"
            );

            return;
        }


        int userId;


        try {

            userId =
                    Integer.parseInt(
                            userIdObject.toString()
                    );

        } catch (Exception e) {

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Invalid user ID in session."
            );

            return;
        }


        System.out.println(
                "User ID = "
                + userId
        );


        // ========================================================
        // STEP 3: GET PDF PATH FROM input_documents
        // ========================================================

        String filePath =
                getDocumentPath(
                        documentId,
                        userId
                );


        if (filePath == null
                || filePath.trim().isEmpty()) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Document not found."
            );

            return;
        }


        System.out.println(
                "PDF path = "
                + filePath
        );


        // ========================================================
        // STEP 4: CHECK PDF FILE
        // ========================================================

        File pdfFile =
                new File(filePath);


        if (!pdfFile.exists()) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "PDF file does not exist: "
                    + filePath
            );

            return;
        }


        if (!pdfFile.isFile()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Selected path is not a valid file."
            );

            return;
        }


        // ========================================================
        // STEP 5: EXTRACT TEXT USING PDFBOX 3.x
        // ========================================================

        String extractedText;


        try (
                PDDocument document =
                        Loader.loadPDF(pdfFile)
        ) {

            PDFTextStripper stripper =
                    new PDFTextStripper();


            extractedText =
                    stripper.getText(
                            document
                    );


        } catch (Exception e) {

            System.out.println(
                    "PDF extraction error:"
            );

            e.printStackTrace();


            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to read PDF."
            );

            return;
        }


        System.out.println(
                "PDF text length = "
                + extractedText.length()
        );


        // ========================================================
        // STEP 6: CHECK EXTRACTED TEXT
        // ========================================================

        if (extractedText.trim().isEmpty()) {

            updateDocumentStatus(
                    documentId,
                    "FAILED"
            );


            request.setAttribute(
                    "errorMessage",
                    "No readable text found in PDF. "
                    + "This may be a scanned PDF requiring OCR."
            );


            request.setAttribute(
                    "documentId",
                    documentId
            );


            request.getRequestDispatcher(
                    "pdf_analysis.jsp"
            ).forward(
                    request,
                    response
            );


            return;
        }


        // ========================================================
        // STEP 7: DOCUMENT PROCESSOR
        // ========================================================

        DocumentProcessor processor =
                new DocumentProcessor();


        List<ExtractedTransaction> transactions =
                processor.processText(
                        extractedText
                );


        System.out.println(
                "======================================"
        );


        System.out.println(
                "Transactions extracted = "
                + transactions.size()
        );


        System.out.println(
                "======================================"
        );


        // ========================================================
        // STEP 8: NO TRANSACTIONS
        // ========================================================

        if (transactions.isEmpty()) {

            updateDocumentStatus(
                    documentId,
                    "FAILED"
            );


            request.setAttribute(
                    "errorMessage",
                    "No transactions could be extracted "
                    + "from this PDF."
            );


            request.setAttribute(
                    "documentId",
                    documentId
            );


            request.getRequestDispatcher(
                    "pdf_analysis.jsp"
            ).forward(
                    request,
                    response
            );


            return;
        }


        // ========================================================
        // STEP 9: SAVE TRANSACTIONS TO MYSQL
        // ========================================================

        TransactionDAO transactionDAO =
                new TransactionDAO();


        int inserted =
                transactionDAO.insertTransactions(
                        transactions,
                        documentId,
                        userId
                );


        System.out.println(
                "Transactions inserted into MySQL = "
                + inserted
        );


        // ========================================================
        // STEP 10: UPDATE DOCUMENT STATUS
        // ========================================================

        if (inserted > 0) {

            updateDocumentStatus(
                    documentId,
                    "ANALYZED"
            );

        } else {

            updateDocumentStatus(
                    documentId,
                    "FAILED"
            );
        }


        // ========================================================
        // STEP 11: SEND DATA TO JSP
        // ========================================================

        request.setAttribute(
                "transactions",
                transactions
        );


        request.setAttribute(
                "transactionCount",
                transactions.size()
        );


        request.setAttribute(
                "insertedCount",
                inserted
        );


        request.setAttribute(
                "documentId",
                documentId
        );


        // ========================================================
        // STEP 12: FORWARD TO PDF ANALYSIS PAGE
        // ========================================================

        request.getRequestDispatcher(
                "pdf_analysis.jsp"
        ).forward(
                request,
                response
        );
    }


    // ============================================================
    // GET DOCUMENT PATH
    // ============================================================

    private String getDocumentPath(
            int documentId,
            int userId) {


        String sql =
                "SELECT file_path "
                + "FROM input_documents "
                + "WHERE document_id = ? "
                + "AND user_id = ?";


        try (
                Connection con =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {


            ps.setInt(
                    1,
                    documentId
            );


            ps.setInt(
                    2,
                    userId
            );


            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {


                if (rs.next()) {

                    return rs.getString(
                            "file_path"
                    );
                }
            }


        } catch (Exception e) {

            System.out.println(
                    "Error getting document path:"
            );

            e.printStackTrace();
        }


        return null;
    }


    // ============================================================
    // UPDATE DOCUMENT STATUS
    // ============================================================

    private void updateDocumentStatus(
            int documentId,
            String status) {


        String sql =
                "UPDATE input_documents "
                + "SET status = ? "
                + "WHERE document_id = ?";


        try (
                Connection con =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {


            ps.setString(
                    1,
                    status
            );


            ps.setInt(
                    2,
                    documentId
            );


            ps.executeUpdate();


        } catch (Exception e) {

            System.out.println(
                    "Unable to update document status:"
            );

            e.printStackTrace();
        }
    }
}