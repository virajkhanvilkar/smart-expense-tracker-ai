package controller;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import dao.InputDocumentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import model.InputDocument;
import model.User;
import util.DBConnection;

@WebServlet("/inputDocument")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 10 * 1024 * 1024,
    maxRequestSize = 15 * 1024 * 1024
)
public class InputDocumentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private InputDocumentDAO documentDAO;

    @Override
    public void init() {
        documentDAO = new InputDocumentDAO();
    }


    // Show document page
    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("user") == null) {

            response.sendRedirect("login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");

        int userId = user.getUserId();

        // Get documents belonging to logged-in user
        List<InputDocument> documents =
                documentDAO.getDocumentsByUser(userId);

        request.setAttribute(
            "documents",
            documents
        );

        request.getRequestDispatcher(
            "input-document.jsp"
        ).forward(request, response);
    }

    public InputDocument getDocumentById(
            int documentId,
            int userId) {

        InputDocument document = null;

        String sql =
            "SELECT * FROM input_documents " +
            "WHERE document_id=? AND user_id=?";

        try {

            Connection con =
                    DBConnection.getConnection();

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setInt(1, documentId);
            ps.setInt(2, userId);

            ResultSet rs =
                    ps.executeQuery();


            if (rs.next()) {

                document =
                        new InputDocument();

                document.setDocumentId(
                        rs.getInt("document_id")
                );

                document.setUserId(
                        rs.getInt("user_id")
                );

                document.setFileName(
                        rs.getString("file_name")
                );

                document.setFileType(
                        rs.getString("file_type")
                );

                document.setFilePath(
                        rs.getString("file_path")
                );

                document.setUploadDate(
                        rs.getString("upload_date")
                );

                document.setStatus(
                        rs.getString("status")
                );
            }


            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();
        }


        return document;
    }

    // Upload document
    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("user") == null) {

            response.sendRedirect("login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");

        int userId = user.getUserId();

        Part filePart = request.getPart("document");

        if (filePart == null ||
            filePart.getSize() == 0) {

            request.setAttribute(
                "error",
                "Please select a document."
            );

            doGet(request, response);
            return;
        }


        String originalFileName =
                getFileName(filePart);

        String extension =
                getFileExtension(originalFileName);


        // Only PDF for Phase 2
        if (!extension.equals("pdf")) {

            request.setAttribute(
                "error",
                "Currently only PDF files are supported."
            );

            doGet(request, response);
            return;
        }


        // Upload directory
        String uploadPath =
                getServletContext()
                .getRealPath("")
                + File.separator
                + "uploads"
                + File.separator
                + "documents";


        File uploadDir = new File(uploadPath);

        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }


        // Create unique file name
        String fileName =
                System.currentTimeMillis()
                + "_" + originalFileName;


        String filePath =
                uploadPath
                + File.separator
                + fileName;


        // Save physical file
        filePart.write(filePath);


        // Save document information
        InputDocument document =
                new InputDocument();

        document.setUserId(userId);
        document.setFileName(originalFileName);
        document.setFileType(extension);
        document.setFilePath(filePath);
        document.setStatus("UPLOADED");


        boolean saved =
                documentDAO.saveDocument(document);


        if (saved) {

            response.sendRedirect(
                request.getContextPath()
                + "/inputDocument?success=1"
            );

        } else {

            request.setAttribute(
                "error",
                "File uploaded but database save failed."
            );

            doGet(request, response);
        }
    }


    private String getFileName(Part part) {

        String content =
                part.getHeader("content-disposition");

        for (String token : content.split(";")) {

            if (token.trim().startsWith("filename")) {

                return token.substring(
                        token.indexOf("=") + 1
                ).trim().replace("\"", "");
            }
        }

        return "unknown";
    }


    private String getFileExtension(String fileName) {

        int dot =
                fileName.lastIndexOf(".");

        if (dot == -1) {
            return "";
        }

        return fileName
                .substring(dot + 1)
                .toLowerCase();
    }
}