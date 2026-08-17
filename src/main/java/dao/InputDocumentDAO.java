package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.InputDocument;
import util.DBConnection;

public class InputDocumentDAO {

    // Save uploaded document information
    public boolean saveDocument(InputDocument document) {

        boolean status = false;

        String sql = "INSERT INTO input_documents "
                   + "(user_id, file_name, file_type, file_path, status) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try {

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, document.getUserId());
            ps.setString(2, document.getFileName());
            ps.setString(3, document.getFileType());
            ps.setString(4, document.getFilePath());
            ps.setString(5, document.getStatus());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                status = true;
            }

            ps.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return status;
    }


    // Get all documents uploaded by a user
    public List<InputDocument> getDocumentsByUser(int userId) {

        List<InputDocument> documents = new ArrayList<>();

        String sql = "SELECT * FROM input_documents "
                   + "WHERE user_id=? "
                   + "ORDER BY upload_date DESC";

        try {

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                InputDocument document = new InputDocument();

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

                documents.add(document);
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return documents;
    }


    // Get one document belonging to a specific user
    public InputDocument getDocumentById(int documentId, int userId) {

        InputDocument document = null;

        String sql = "SELECT * FROM input_documents "
                   + "WHERE document_id=? AND user_id=?";

        try {

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, documentId);
            ps.setInt(2, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                document = new InputDocument();

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


    // Update document status
    public boolean updateStatus(int documentId, String status) {

        boolean updated = false;

        String sql = "UPDATE input_documents "
                   + "SET status=? "
                   + "WHERE document_id=?";

        try {

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, status);
            ps.setInt(2, documentId);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                updated = true;
            }

            ps.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return updated;
    }
}