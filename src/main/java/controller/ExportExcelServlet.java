package controller;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import dao.ReportDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Report;
import model.User;

@WebServlet("/exportExcel")
public class ExportExcelServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // ================= LOGIN CHECK =================

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {

            response.sendRedirect("login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");

        int userId = user.getUserId();

        ReportDAO dao = new ReportDAO();


        // ================= GET FILTER VALUES =================

        String fromDate =
                request.getParameter("fromDate");

        String toDate =
                request.getParameter("toDate");

        String category =
                request.getParameter("category");

        String type =
                request.getParameter("type");


        // ================= GET FILTERED REPORT =================

        ArrayList<Report> reportList =
                dao.getFilteredReport(
                        userId,
                        fromDate,
                        toDate,
                        category,
                        type
                );


        // ================= EXCEL RESPONSE =================

        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=ExpenseReport.xlsx"
        );


        // ================= CREATE WORKBOOK =================

        Workbook workbook = new XSSFWorkbook();

        Sheet sheet =
                workbook.createSheet("Expense Report");


        // ================= HEADER STYLE =================

        Font headerFont =
                workbook.createFont();

        headerFont.setBold(true);

        CellStyle headerStyle =
                workbook.createCellStyle();

        headerStyle.setFont(headerFont);


        // ================= TITLE =================

        int rowNum = 0;

        Row titleRow =
                sheet.createRow(rowNum++);

        Cell title =
                titleRow.createCell(0);

        title.setCellValue(
                "Smart Expense Tracker Report"
        );


        // ================= USER =================

        Row userRow =
                sheet.createRow(rowNum++);

        Cell userCell =
                userRow.createCell(0);

        userCell.setCellValue(
                "User : " + user.getFullName()
        );


        // ================= FILTER INFORMATION =================

        Row filterRow =
                sheet.createRow(rowNum++);

        Cell filterCell =
                filterRow.createCell(0);

        String filterText =
                "Filters - From: " +
                (fromDate == null || fromDate.isEmpty()
                        ? "All"
                        : fromDate)

                + " | To: " +
                (toDate == null || toDate.isEmpty()
                        ? "All"
                        : toDate)

                + " | Category: " +
                (category == null || category.isEmpty()
                        ? "All"
                        : category)

                + " | Type: " +
                (type == null || type.isEmpty()
                        ? "All Transactions"
                        : type);

        filterCell.setCellValue(filterText);


        // ================= BLANK ROW =================

        rowNum++;


        // ================= TABLE HEADER =================

        Row header =
                sheet.createRow(rowNum++);

        String[] columns = {
                "Date",
                "Type",
                "Category",
                "Description",
                "Amount"
        };


        for (int i = 0;
             i < columns.length;
             i++) {

            Cell cell =
                    header.createCell(i);

            cell.setCellValue(
                    columns[i]
            );

            cell.setCellStyle(
                    headerStyle
            );
        }


        // ================= TOTALS =================

        double totalIncome = 0;

        double totalExpense = 0;


        // ================= TRANSACTION DATA =================

        for (Report report : reportList) {

            Row row =
                    sheet.createRow(rowNum++);


            // Date

            row.createCell(0)
                    .setCellValue(
                            report.getDate().toString()
                    );


            // Type

            row.createCell(1)
                    .setCellValue(
                            report.getType()
                    );


            // Category

            row.createCell(2)
                    .setCellValue(
                            report.getCategory()
                    );


            // Description

            row.createCell(3)
                    .setCellValue(
                            report.getDescription()
                    );


            // Amount

            row.createCell(4)
                    .setCellValue(
                            report.getAmount()
                    );


            // Calculate totals

            if ("Income".equalsIgnoreCase(
                    report.getType())) {

                totalIncome +=
                        report.getAmount();

            } else if ("Expense".equalsIgnoreCase(
                    report.getType())) {

                totalExpense +=
                        report.getAmount();
            }
        }


        // ================= SUMMARY =================

        rowNum++;


        Row incomeRow =
                sheet.createRow(rowNum++);

        incomeRow
                .createCell(3)
                .setCellValue(
                        "Total Income"
                );

        incomeRow
                .createCell(4)
                .setCellValue(
                        totalIncome
                );


        Row expenseRow =
                sheet.createRow(rowNum++);

        expenseRow
                .createCell(3)
                .setCellValue(
                        "Total Expense"
                );

        expenseRow
                .createCell(4)
                .setCellValue(
                        totalExpense
                );


        Row balanceRow =
                sheet.createRow(rowNum++);

        balanceRow
                .createCell(3)
                .setCellValue(
                        "Balance"
                );

        balanceRow
                .createCell(4)
                .setCellValue(
                        totalIncome -
                        totalExpense
                );


        // ================= AUTO SIZE =================

        for (int i = 0; i < 5; i++) {

            sheet.autoSizeColumn(i);
        }


        // ================= WRITE EXCEL =================

        workbook.write(
                response.getOutputStream()
        );

        workbook.close();
    }
}