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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processDocument(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processDocument(request, response);
    }

    private void processDocument(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String documentIdText = request.getParameter("documentId");
        if (documentIdText == null || documentIdText.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Document ID is missing.");
            return;
        }

        final int documentId;
        try {
            documentId = Integer.parseInt(documentIdText.trim());
        } catch (NumberFormatException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid document ID.");
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        final int userId;
        try {
            userId = Integer.parseInt(session.getAttribute("userId").toString());
        } catch (Exception ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Invalid user ID in session.");
            return;
        }

        String filePath = getDocumentPath(documentId, userId);
        if (filePath == null || filePath.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Document not found.");
            return;
        }

        File pdfFile = new File(filePath);
        if (!pdfFile.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "PDF file does not exist or is not a file: " + filePath);
            return;
        }

        final String extractedText;
        try (PDDocument pdf = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            extractedText = stripper.getText(pdf);
            System.out.println("===== PDF TEXT (first 2500 chars) =====");
            System.out.println(extractedText.substring(0, Math.min(extractedText.length(), 2500)));
            System.out.println("=======================================");
        } catch (Exception ex) {
            ex.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to read PDF.");
            return;
        }

        if (extractedText == null || extractedText.trim().isEmpty()) {
            updateDocumentStatus(documentId, "FAILED");
            request.setAttribute("errorMessage",
                    "No readable text found in PDF. A scanned PDF may require OCR.");
            request.setAttribute("documentId", documentId);
            request.getRequestDispatcher("pdf_analysis.jsp").forward(request, response);
            return;
        }

        DocumentProcessor processor = new DocumentProcessor();
        // Extract opening balance dynamically from the same PDF text used for parsing.
        double openingBalance = processor.getOpeningBalance(extractedText);
        System.out.println("Servlet opening balance = " +
                (Double.isFinite(openingBalance) ? String.format(java.util.Locale.ROOT, "%.2f", openingBalance) : "NOT FOUND"));

        List<ExtractedTransaction> transactions = processor.processText(extractedText);

        request.setAttribute("openingBalance", Double.isFinite(openingBalance) ? openingBalance : null);
        request.setAttribute("documentId", documentId);

        if (transactions.isEmpty()) {
            updateDocumentStatus(documentId, "FAILED");
            request.setAttribute("errorMessage", "No transactions could be extracted from this PDF.");
            request.setAttribute("transactions", transactions);
            request.getRequestDispatcher("pdf_analysis.jsp").forward(request, response);
            return;
        }

        TransactionDAO transactionDAO = new TransactionDAO();
        int inserted = transactionDAO.insertTransactions(transactions, documentId, userId);
        updateDocumentStatus(documentId, inserted > 0 ? "ANALYZED" : "FAILED");

        double totalIncome = 0.0;
        double totalExpense = 0.0;
        for (ExtractedTransaction tx : transactions) {
            if ("INCOME".equalsIgnoreCase(tx.getType())) totalIncome += tx.getAmount();
            else totalExpense += tx.getAmount();
        }

        if (Double.isFinite(openingBalance)) {
            request.setAttribute("closingBalance", openingBalance + totalIncome - totalExpense);
        } else {
            request.setAttribute("closingBalance", null);
        }

        request.setAttribute("transactions", transactions);
        request.setAttribute("transactionCount", transactions.size());
        request.setAttribute("insertedCount", inserted);
        request.getRequestDispatcher("pdf_analysis.jsp").forward(request, response);
    }

    private String getDocumentPath(int documentId, int userId) {
        String sql = "SELECT file_path FROM input_documents WHERE document_id = ? AND user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, documentId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("file_path");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void updateDocumentStatus(int documentId, String status) {
        String sql = "UPDATE input_documents SET status = ? WHERE document_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, documentId);
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
