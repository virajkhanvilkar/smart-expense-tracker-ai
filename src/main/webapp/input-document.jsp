<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page import="model.User" %>
<%@ page import="java.util.List" %>
<%@ page import="model.InputDocument" %>

<%
    User user = (User) session.getAttribute("user");

    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    String success = request.getParameter("success");
    String error = (String) request.getAttribute("error");

    List<InputDocument> documents =
        (List<InputDocument>) request.getAttribute("documents");
%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <title>Bank Statement - Expense Tracker</title>

    <!-- Bootstrap -->
    <link
        href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
        rel="stylesheet">

    <!-- Bootstrap Icons -->
    <link
        href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css"
        rel="stylesheet">

    <style>

        body {
            margin: 0;
            background: #f4f6f9;
            font-family: Arial, sans-serif;
        }

        /* ================= SIDEBAR ================= */

        .sidebar {
            position: fixed;
            left: 0;
            top: 0;

            width: 280px;
            height: 100vh;

            background: #212529;

            padding-top: 25px;

            z-index: 1000;
        }

        .sidebar h3 {
            color: white;

            text-align: center;

            margin-bottom: 30px;
        }

        .sidebar a {
            display: block;

            padding: 14px 30px;

            color: white;

            text-decoration: none;

            font-size: 17px;
        }

        .sidebar a:hover {
            background: #343a40;
        }

        .sidebar a.active {
            background: #0d6efd;
        }

        .sidebar i {
            margin-right: 8px;
        }


        /* ================= CONTENT ================= */

        .content {
            margin-left: 280px;

            padding: 35px 45px;

            min-height: 100vh;
        }


        /* ================= PAGE HEADER ================= */

        .page-header {
            margin-bottom: 25px;
        }

        .page-header h2 {
            font-weight: 600;

            margin-bottom: 5px;
        }

        .page-header p {
            color: #6c757d;

            margin: 0;
        }


        /* ================= ALERT ================= */

        .alert {
            max-width: 1200px;

            margin: 0 auto 20px auto;
        }


        /* ================= UPLOAD CARD ================= */

        .upload-card {
            max-width: 850px;

            margin: 0 auto 45px auto;

            background: white;

            padding: 35px 45px;

            border-radius: 16px;

            box-shadow: 0 4px 18px rgba(0,0,0,0.08);

            text-align: center;
        }

        .upload-icon {
            font-size: 55px;

            color: #0d6efd;
        }

        .upload-card h3 {
            margin-top: 12px;
        }

        .upload-card p {
            color: #6c757d;
        }


        /* ================= DOCUMENTS ================= */

        .documents-section {
            max-width: 1200px;

            margin: 0 auto;
        }

        .documents-header {
            display: flex;

            justify-content: space-between;

            align-items: center;

            margin-bottom: 20px;
        }

        .documents-header h3 {
            margin: 0;

            font-weight: 600;
        }


        /* ================= DOCUMENT CARD ================= */

        .document-card {
            background: white;

            border: none;

            border-radius: 14px;

            padding: 22px;

            height: 100%;

            box-shadow: 0 3px 12px rgba(0,0,0,0.07);
        }

        .document-title {
            display: flex;

            align-items: flex-start;

            gap: 10px;

            margin-bottom: 15px;
        }

        .document-title i {
            color: #dc3545;

            font-size: 25px;
        }

        .document-name {
            font-size: 17px;

            font-weight: 600;

            word-break: break-word;

            line-height: 1.4;
        }

        .document-info {
            color: #6c757d;

            font-size: 14px;

            margin-bottom: 7px;
        }

        .document-footer {
            display: flex;

            justify-content: space-between;

            align-items: center;

            margin-top: 20px;
        }


        /* ================= EMPTY DOCUMENTS ================= */

        .empty-documents {
            background: white;

            padding: 40px;

            text-align: center;

            border-radius: 14px;

            color: #6c757d;

            box-shadow: 0 3px 12px rgba(0,0,0,0.05);
        }


        /* ================= RESPONSIVE ================= */

        @media (max-width: 768px) {

            .sidebar {
                width: 220px;
            }

            .content {
                margin-left: 220px;

                padding: 25px 20px;
            }

            .upload-card {
                padding: 30px 20px;
            }

        }

    </style>

</head>


<body>


<!-- ===================================================== -->
<!-- SIDEBAR -->
<!-- ===================================================== -->

<div class="sidebar">

    <h3>
        Expense Tracker
    </h3>


    <!-- Dashboard -->

    <a href="<%=request.getContextPath()%>/dashboard.jsp">

        <i class="bi bi-speedometer2"></i>

        Dashboard

    </a>


    <!-- Income -->

    <a href="<%=request.getContextPath()%>/income.jsp">

        <i class="bi bi-cash-stack"></i>

        Income

    </a>


    <!-- Expenses -->

    <a href="<%=request.getContextPath()%>/expenses.jsp">

        <i class="bi bi-wallet2"></i>

        Expenses

    </a>


    <!-- Categories -->

    <a href="<%=request.getContextPath()%>/categories.jsp">

        <i class="bi bi-tags"></i>

        Categories

    </a>


    <!-- Budget -->

    <a href="<%=request.getContextPath()%>/budget.jsp">

        <i class="bi bi-piggy-bank"></i>

        Budget

    </a>


    <!-- Reports -->

    <a href="<%=request.getContextPath()%>/report">

        <i class="bi bi-bar-chart"></i>

        Reports

    </a>


    <!-- Bank Statement -->

    <a href="<%=request.getContextPath()%>/inputDocument"
       class="active">

        <i class="bi bi-file-earmark-arrow-up"></i>

        Bank Statement

    </a>


    <!-- Profile -->

    <a href="<%=request.getContextPath()%>/profile">

        <i class="bi bi-person-circle"></i>

        Profile

    </a>


    <!-- Logout -->

    <a href="<%=request.getContextPath()%>/logout">

        <i class="bi bi-box-arrow-right"></i>

        Logout

    </a>

</div>



<!-- ===================================================== -->
<!-- MAIN CONTENT -->
<!-- ===================================================== -->

<div class="content">


    <!-- ================= PAGE HEADER ================= -->

    <div class="page-header">

        <h2>

            <i class="bi bi-file-earmark-text"></i>

            Bank Statements

        </h2>


        <p>

            Upload and analyze your bank statements
            for intelligent expense management.

        </p>

    </div>



    <!-- ================= SUCCESS MESSAGE ================= -->

    <% if ("1".equals(success)) { %>

        <div class="alert alert-success">

            <i class="bi bi-check-circle"></i>

            Bank statement uploaded successfully.

        </div>

    <% } %>



    <!-- ================= ERROR MESSAGE ================= -->

    <% if (error != null) { %>

        <div class="alert alert-danger">

            <i class="bi bi-exclamation-circle"></i>

            <%= error %>

        </div>

    <% } %>



    <!-- ================================================= -->
    <!-- UPLOAD SECTION -->
    <!-- ================================================= -->

    <div class="upload-card">


        <div class="upload-icon">

            <i class="bi bi-cloud-arrow-up"></i>

        </div>


        <h3>

            Upload Bank Statement

        </h3>


        <p>

            Upload your PDF bank statement
            for automatic transaction extraction.

        </p>


        <p class="small text-muted">

            Supported format:

            <strong>PDF</strong>

            <br>

            Maximum file size:

            <strong>10 MB</strong>

        </p>



        <!-- UPLOAD FORM -->

        <form
            action="<%=request.getContextPath()%>/inputDocument"
            method="post"
            enctype="multipart/form-data">


            <div class="mb-3">

                <input
                    type="file"
                    name="document"
                    class="form-control"
                    accept=".pdf,application/pdf"
                    required>

            </div>


            <button
                type="submit"
                class="btn btn-primary px-4">

                <i class="bi bi-upload"></i>

                Upload & Save

            </button>


        </form>

    </div>



    <!-- ================================================= -->
    <!-- DOCUMENTS SECTION -->
    <!-- ================================================= -->

    <div class="documents-section">


        <!-- DOCUMENT HEADER -->

        <div class="documents-header">


            <h3>

                <i class="bi bi-files"></i>

                Uploaded Documents

            </h3>



            <% if (documents != null) { %>

                <span class="badge bg-secondary">

                    <%= documents.size() %>

                    document(s)

                </span>

            <% } %>


        </div>



        <!-- ================================================= -->
        <!-- DOCUMENT LIST -->
        <!-- ================================================= -->

        <% if (documents != null && !documents.isEmpty()) { %>


            <div class="row g-4">


                <% for (InputDocument document : documents) { %>


                    <div class="col-lg-6">


                        <div class="document-card">


                            <!-- DOCUMENT TITLE -->

                            <div class="document-title">


                                <i class="bi bi-file-earmark-pdf"></i>


                                <div class="document-name">

                                    <%= document.getFileName() %>

                                </div>


                            </div>



                            <!-- FILE TYPE -->

                            <div class="document-info">

                                <i class="bi bi-filetype-pdf"></i>

                                Type:

                                <strong>

                                    <%= document.getFileType().toUpperCase() %>

                                </strong>

                            </div>



                            <!-- UPLOAD DATE -->

                            <div class="document-info">

                                <i class="bi bi-calendar3"></i>

                                Uploaded:

                                <%= document.getUploadDate() %>

                            </div>



                            <!-- DOCUMENT FOOTER -->

                            <div class="document-footer">


                                <!-- STATUS -->

                                <% if ("ANALYZED".equalsIgnoreCase(
                                        document.getStatus())) { %>


                                    <span class="badge bg-success">

                                        <i class="bi bi-check-circle"></i>

                                        ANALYZED

                                    </span>


                                <% } else { %>


                                    <span class="badge bg-secondary">

                                        <i class="bi bi-clock"></i>

                                        UPLOADED

                                    </span>


                                <% } %>



                                <!-- ANALYZE PDF BUTTON -->

                                <a
                                    href="<%=request.getContextPath()%>/processDocument?documentId=<%=document.getDocumentId()%>"
                                    class="btn btn-primary btn-sm">

                                    <i class="bi bi-search"></i>

                                    Analyze PDF

                                </a>


                            </div>


                        </div>


                    </div>


                <% } %>


            </div>


        <% } else { %>


            <!-- ================================================= -->
            <!-- NO DOCUMENTS -->
            <!-- ================================================= -->

            <div class="empty-documents">


                <i
                    class="bi bi-file-earmark-x"
                    style="font-size:45px;">
                </i>


                <h5 class="mt-3">

                    No bank statements uploaded

                </h5>


                <p>

                    Upload your first PDF bank statement
                    to begin analysis.

                </p>


            </div>


        <% } %>


    </div>


</div>


</body>

</html>