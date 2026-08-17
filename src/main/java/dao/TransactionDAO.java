package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.ExtractedTransaction;
import model.Transaction;
import util.DBConnection;

public class TransactionDAO {

    // ============================================================
    // GET RECENT TRANSACTIONS
    // ============================================================
    //
    // Gets transactions from:
    //
    // 1. income table
    // 2. expenses table
    // 3. transactions table
    //
    // This keeps the existing Dashboard working and also allows
    // imported PDF transactions to appear on Dashboard.
    // ============================================================

    public ArrayList<Transaction> getRecentTransactions(int userId) {

        ArrayList<Transaction> list =
                new ArrayList<>();

        String sql =
                "SELECT transaction_date, type, category_name, "
                + "description, amount "
                + "FROM ( "

                // ==================================================
                // NORMAL INCOME
                // ==================================================

                + "SELECT "
                + "i.income_date AS transaction_date, "
                + "'Income' AS type, "
                + "c.category_name AS category_name, "
                + "i.description AS description, "
                + "i.amount AS amount "
                + "FROM income i "
                + "INNER JOIN categories c "
                + "ON i.category_id = c.category_id "
                + "WHERE i.user_id = ? "

                + "UNION ALL "

                // ==================================================
                // NORMAL EXPENSE
                // ==================================================

                + "SELECT "
                + "e.expense_date AS transaction_date, "
                + "'Expense' AS type, "
                + "c.category_name AS category_name, "
                + "e.description AS description, "
                + "e.amount AS amount "
                + "FROM expenses e "
                + "INNER JOIN categories c "
                + "ON e.category_id = c.category_id "
                + "WHERE e.user_id = ? "

                + "UNION ALL "

                // ==================================================
                // PDF / BANK TRANSACTIONS
                // ==================================================

                + "SELECT "
                + "t.transaction_date AS transaction_date, "

                + "CASE "
                + "WHEN UPPER(t.type) = 'INCOME' "
                + "THEN 'Income' "
                + "ELSE 'Expense' "
                + "END AS type, "

                + "COALESCE(t.category, 'Uncategorized') "
                + "AS category_name, "

                + "t.description AS description, "

                + "t.amount AS amount "

                + "FROM transactions t "
                + "WHERE t.user_id = ? "

                + ") AS all_transactions "

                + "ORDER BY transaction_date DESC "

                + "LIMIT 5";


        try (
                Connection con =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            // income user_id
            ps.setInt(1, userId);

            // expense user_id
            ps.setInt(2, userId);

            // PDF transaction user_id
            ps.setInt(3, userId);


            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                while (rs.next()) {

                    Transaction transaction =
                            new Transaction();


                    transaction.setTransactionDate(
                            rs.getDate(
                                    "transaction_date"
                            )
                    );


                    transaction.setType(
                            rs.getString(
                                    "type"
                            )
                    );


                    transaction.setCategoryName(
                            rs.getString(
                                    "category_name"
                            )
                    );


                    transaction.setDescription(
                            rs.getString(
                                    "description"
                            )
                    );


                    transaction.setAmount(
                            rs.getDouble(
                                    "amount"
                            )
                    );


                    list.add(
                            transaction
                    );
                }
            }


        } catch (Exception e) {

            System.out.println(
                    "Error getting recent transactions:"
            );

            e.printStackTrace();
        }


        return list;
    }


    // ============================================================
    // INSERT ONE PDF TRANSACTION
    // ============================================================

    public boolean insertTransaction(
            ExtractedTransaction transaction,
            int documentId,
            int userId) {


        String sql =
                "INSERT INTO transactions "
                + "(document_id, user_id, transaction_date, "
                + "type, category, description, amount, balance) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";


        try (
                Connection con =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {


            // Document ID
            ps.setInt(
                    1,
                    documentId
            );


            // User ID
            ps.setInt(
                    2,
                    userId
            );


            // Transaction date
            ps.setDate(
                    3,
                    java.sql.Date.valueOf(
                            convertDate(
                                    transaction.getDate()
                            )
                    )
            );


            // Transaction type
            ps.setString(
                    4,
                    transaction.getType()
            );


            // Category
            ps.setString(
                    5,
                    transaction.getCategory()
            );


            // Description
            ps.setString(
                    6,
                    transaction.getDescription()
            );


            // Amount
            ps.setDouble(
                    7,
                    transaction.getAmount()
            );


            // Balance
            //
            // Currently NULL because your
            // ExtractedTransaction does not appear
            // to have a balance getter.
            //
            ps.setNull(
                    8,
                    java.sql.Types.DECIMAL
            );


            int rows =
                    ps.executeUpdate();


            return rows > 0;


        } catch (Exception e) {

            System.out.println(
                    "Transaction insert error: "
                    + e.getMessage()
            );

            e.printStackTrace();

            return false;
        }
    }


    // ============================================================
    // INSERT ALL PDF TRANSACTIONS
    // ============================================================

    public int insertTransactions(
            List<ExtractedTransaction> transactions,
            int documentId,
            int userId) {


        int inserted = 0;


        if (
                transactions == null
                || transactions.isEmpty()
        ) {

            return 0;
        }


        for (
                ExtractedTransaction transaction
                : transactions
        ) {


            if (
                    insertTransaction(
                            transaction,
                            documentId,
                            userId
                    )
            ) {

                inserted++;
            }
        }


        System.out.println(
                "Transactions inserted: "
                + inserted
                + " / "
                + transactions.size()
        );


        return inserted;
    }


    // ============================================================
    // GET TRANSACTIONS FOR A PARTICULAR DOCUMENT
    // ============================================================

    public ArrayList<ExtractedTransaction>
    getTransactionsByDocument(
            int documentId) {


        ArrayList<ExtractedTransaction> list =
                new ArrayList<>();


        String sql =
                "SELECT transaction_date, type, category, "
                + "description, amount "
                + "FROM transactions "
                + "WHERE document_id = ? "
                + "ORDER BY transaction_date ASC";


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


            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {


                while (rs.next()) {

                    ExtractedTransaction transaction =
                            new ExtractedTransaction();


                    transaction.setDate(
                            rs.getString(
                                    "transaction_date"
                            )
                    );


                    transaction.setType(
                            rs.getString(
                                    "type"
                            )
                    );


                    transaction.setCategory(
                            rs.getString(
                                    "category"
                            )
                    );


                    transaction.setDescription(
                            rs.getString(
                                    "description"
                            )
                    );


                    transaction.setAmount(
                            rs.getDouble(
                                    "amount"
                            )
                    );


                    list.add(
                            transaction
                    );
                }
            }


        } catch (Exception e) {

            System.out.println(
                    "Error getting document transactions:"
            );

            e.printStackTrace();
        }


        return list;
    }


    // ============================================================
    // DELETE TRANSACTIONS OF A DOCUMENT
    // ============================================================

    public boolean deleteTransactionsByDocument(
            int documentId) {


        String sql =
                "DELETE FROM transactions "
                + "WHERE document_id = ?";


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


            int rows =
                    ps.executeUpdate();


            return rows > 0;


        } catch (Exception e) {

            System.out.println(
                    "Error deleting transactions:"
            );

            e.printStackTrace();

            return false;
        }
    }


    // ============================================================
    // DATE CONVERSION
    // ============================================================

    private String convertDate(
            String date) {


        if (
                date == null
                || date.trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Transaction date is empty"
            );
        }


        date =
                date.trim();


        // --------------------------------------------------------
        // yyyy-MM-dd
        // --------------------------------------------------------

        if (
                date.matches(
                        "\\d{4}-\\d{2}-\\d{2}"
                )
        ) {

            return date;
        }


        // --------------------------------------------------------
        // dd-MM-yyyy
        // --------------------------------------------------------

        if (
                date.matches(
                        "\\d{2}-\\d{2}-\\d{4}"
                )
        ) {

            String[] parts =
                    date.split("-");


            return parts[2]
                    + "-"
                    + parts[1]
                    + "-"
                    + parts[0];
        }


        // --------------------------------------------------------
        // dd/MM/yyyy
        // --------------------------------------------------------

        if (
                date.matches(
                        "\\d{2}/\\d{2}/\\d{4}"
                )
        ) {

            String[] parts =
                    date.split("/");


            return parts[2]
                    + "-"
                    + parts[1]
                    + "-"
                    + parts[0];
        }


        // --------------------------------------------------------
        // dd MMM yyyy
        // Example: 02 Jul 2026
        // --------------------------------------------------------

        try {

            java.text.SimpleDateFormat inputFormat =
                    new java.text.SimpleDateFormat(
                            "dd MMM yyyy",
                            java.util.Locale.ENGLISH
                    );


            java.text.SimpleDateFormat outputFormat =
                    new java.text.SimpleDateFormat(
                            "yyyy-MM-dd"
                    );


            return outputFormat.format(
                    inputFormat.parse(date)
            );


        } catch (Exception ignored) {
        }


        // --------------------------------------------------------
        // dd-MMM-yyyy
        // Example: 02-Jul-2026
        // --------------------------------------------------------

        try {

            java.text.SimpleDateFormat inputFormat =
                    new java.text.SimpleDateFormat(
                            "dd-MMM-yyyy",
                            java.util.Locale.ENGLISH
                    );


            java.text.SimpleDateFormat outputFormat =
                    new java.text.SimpleDateFormat(
                            "yyyy-MM-dd"
                    );


            return outputFormat.format(
                    inputFormat.parse(date)
            );


        } catch (Exception ignored) {
        }


        throw new IllegalArgumentException(
                "Unsupported transaction date: "
                + date
        );
    }
}