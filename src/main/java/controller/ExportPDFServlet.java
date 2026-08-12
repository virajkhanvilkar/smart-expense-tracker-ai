package controller;

import java.io.IOException;
import java.util.ArrayList;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import dao.ReportDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import model.Report;
import model.User;

@WebServlet("/exportPDF")
public class ExportPDFServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // ================= LOGIN CHECK =================

        HttpSession session =
                request.getSession(false);

        if (session == null ||
            session.getAttribute("user") == null) {

            response.sendRedirect("login.jsp");
            return;
        }

        User user =
                (User) session.getAttribute("user");

        int userId =
                user.getUserId();


        // ================= GET FILTER VALUES =================

        String fromDate =
                request.getParameter("fromDate");

        String toDate =
                request.getParameter("toDate");

        String category =
                request.getParameter("category");

        String type =
                request.getParameter("type");


        // ================= GET FILTERED DATA =================

        ReportDAO dao =
                new ReportDAO();

        ArrayList<Report> reportList =
                dao.getFilteredReport(
                        userId,
                        fromDate,
                        toDate,
                        category,
                        type
                );


        // ================= CALCULATE TOTALS =================

        double totalIncome = 0;

        double totalExpense = 0;

        for (Report r : reportList) {

            if ("Income".equals(r.getType())) {

                totalIncome += r.getAmount();

            } else if ("Expense".equals(r.getType())) {

                totalExpense += r.getAmount();
            }
        }

        double balance =
                totalIncome - totalExpense;


        // ================= PDF RESPONSE =================

        response.setContentType(
                "application/pdf");

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=ExpenseReport.pdf");


        try {

            Document document =
                    new Document();

            PdfWriter.getInstance(
                    document,
                    response.getOutputStream()
            );

            document.open();


            // ================= TITLE =================

            Font title =
                    new Font(
                            Font.FontFamily.HELVETICA,
                            18,
                            Font.BOLD
                    );

            Paragraph heading =
                    new Paragraph(
                            "Smart Expense Tracker",
                            title
                    );

            heading.setAlignment(
                    Paragraph.ALIGN_CENTER
            );

            document.add(heading);

            document.add(
                    new Paragraph(" ")
            );


            // ================= USER =================

            document.add(
                    new Paragraph(
                            "User : "
                            + user.getFullName()
                    )
            );


            // ================= FILTER INFORMATION =================

            document.add(
                    new Paragraph(" ")
            );

            document.add(
                    new Paragraph(
                            "Transaction Report"
                    )
            );

            if (fromDate != null &&
                !fromDate.isEmpty()) {

                document.add(
                        new Paragraph(
                                "From Date : "
                                + fromDate
                        )
                );
            }

            if (toDate != null &&
                !toDate.isEmpty()) {

                document.add(
                        new Paragraph(
                                "To Date : "
                                + toDate
                        )
                );
            }

            if (category != null &&
                !category.isEmpty()) {

                document.add(
                        new Paragraph(
                                "Category : "
                                + category
                        )
                );
            }

            if (type != null &&
                !type.isEmpty()) {

                document.add(
                        new Paragraph(
                                "Type : "
                                + type
                        )
                );
            }

            document.add(
                    new Paragraph(" ")
            );


            // ================= TABLE =================

            PdfPTable table =
                    new PdfPTable(5);

            table.setWidthPercentage(100);


            table.addCell(
                    new PdfPCell(
                            new Phrase("Date")
                    )
            );

            table.addCell(
                    new PdfPCell(
                            new Phrase("Type")
                    )
            );

            table.addCell(
                    new PdfPCell(
                            new Phrase("Category")
                    )
            );

            table.addCell(
                    new PdfPCell(
                            new Phrase("Description")
                    )
            );

            table.addCell(
                    new PdfPCell(
                            new Phrase("Amount")
                    )
            );


            // ================= TRANSACTIONS =================

            for (Report r : reportList) {

                table.addCell(
                        r.getDate().toString()
                );

                table.addCell(
                        r.getType()
                );

                table.addCell(
                        r.getCategory()
                );

                table.addCell(
                        r.getDescription()
                );

                table.addCell(
                        String.valueOf(
                                r.getAmount()
                        )
                );
            }


            document.add(table);

            document.add(
                    new Paragraph(" ")
            );


            // ================= TOTALS =================

            document.add(
                    new Paragraph(
                            "Total Income : ₹ "
                            + String.format(
                                    "%.2f",
                                    totalIncome
                            )
                    )
            );

            document.add(
                    new Paragraph(
                            "Total Expense : ₹ "
                            + String.format(
                                    "%.2f",
                                    totalExpense
                            )
                    )
            );

            document.add(
                    new Paragraph(
                            "Balance : ₹ "
                            + String.format(
                                    "%.2f",
                                    balance
                            )
                    )
            );


            document.close();


        } catch (Exception e) {

            e.printStackTrace();

        }
    }
}