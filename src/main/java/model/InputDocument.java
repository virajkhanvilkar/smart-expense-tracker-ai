package model;

public class InputDocument {

    private int documentId;
    private int userId;
    private String fileName;
    private String fileType;
    private String filePath;
    private String uploadDate;
    private String status;

    public InputDocument() {
    }

    public InputDocument(int documentId,
                         int userId,
                         String fileName,
                         String fileType,
                         String filePath,
                         String uploadDate,
                         String status) {

        this.documentId = documentId;
        this.userId = userId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.filePath = filePath;
        this.uploadDate = uploadDate;
        this.status = status;
    }

    public int getDocumentId() {
        return documentId;
    }

    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}