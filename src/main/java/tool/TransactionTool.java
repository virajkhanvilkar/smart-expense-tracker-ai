package tool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.DBConnection;

public class TransactionTool {

    /*
     * ============================================================
     * GET TOTAL INCOME
     * ============================================================
     */

    public double getTotalIncome(int userId) {

        String sql =
                "SELECT COALESCE(SUM(amount), 0) AS total_income " +
                "FROM transactions " +
                "WHERE user_id = ? " +
                "AND type = 'INCOME'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getDouble("total_income");
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "TransactionTool - getTotalIncome error: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return 0.0;
    }


    /*
     * ============================================================
     * GET TOTAL EXPENSE
     * ============================================================
     */

    public double getTotalExpense(int userId) {

        String sql =
                "SELECT COALESCE(SUM(amount), 0) AS total_expense " +
                "FROM transactions " +
                "WHERE user_id = ? " +
                "AND type = 'EXPENSE'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getDouble("total_expense");
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "TransactionTool - getTotalExpense error: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return 0.0;
    }


    /*
     * ============================================================
     * GET TOTAL TRANSACTION COUNT
     * ============================================================
     */

    public int getTransactionCount(int userId) {

        String sql =
                "SELECT COUNT(*) AS total_transactions " +
                "FROM transactions " +
                "WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt("total_transactions");
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "TransactionTool - getTransactionCount error: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return 0;
    }


    /*
     * ============================================================
     * GET CATEGORY SPENDING
     * ============================================================
     */

    public double getCategorySpending(
            int userId,
            String category) {

        String sql =
                "SELECT COALESCE(SUM(amount), 0) AS total " +
                "FROM transactions " +
                "WHERE user_id = ? " +
                "AND type = 'EXPENSE' " +
                "AND LOWER(category) = LOWER(?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, category);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "TransactionTool - getCategorySpending error: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return 0.0;
    }


    /*
     * ============================================================
     * GET CATEGORY-WISE EXPENSE
     * ============================================================
     */

    public List<Map<String, Object>> getCategoryWiseExpense(
            int userId) {

        List<Map<String, Object>> result =
                new ArrayList<>();

        String sql =
                "SELECT category, " +
                "COUNT(*) AS transaction_count, " +
                "COALESCE(SUM(amount), 0) AS total_amount " +
                "FROM transactions " +
                "WHERE user_id = ? " +
                "AND type = 'EXPENSE' " +
                "GROUP BY category " +
                "ORDER BY total_amount DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Map<String, Object> row =
                            new HashMap<>();

                    row.put(
                            "category",
                            rs.getString("category")
                    );

                    row.put(
                            "transaction_count",
                            rs.getInt("transaction_count")
                    );

                    row.put(
                            "total_amount",
                            rs.getDouble("total_amount")
                    );

                    result.add(row);
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "TransactionTool - getCategoryWiseExpense error: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return result;
    }


    /*
     * ============================================================
     * GET MONTHLY SUMMARY
     * ============================================================
     */

    public Map<String, Object> getMonthlySummary(
            int userId,
            int month,
            int year) {

        Map<String, Object> result =
                new HashMap<>();

        String sql =
                "SELECT " +
                "COUNT(*) AS transaction_count, " +
                "COALESCE(SUM(CASE " +
                "WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) " +
                "AS total_income, " +
                "COALESCE(SUM(CASE " +
                "WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) " +
                "AS total_expense " +
                "FROM transactions " +
                "WHERE user_id = ? " +
                "AND MONTH(transaction_date) = ? " +
                "AND YEAR(transaction_date) = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    result.put(
                            "transaction_count",
                            rs.getInt("transaction_count")
                    );

                    result.put(
                            "total_income",
                            rs.getDouble("total_income")
                    );

                    result.put(
                            "total_expense",
                            rs.getDouble("total_expense")
                    );

                    double income =
                            rs.getDouble("total_income");

                    double expense =
                            rs.getDouble("total_expense");

                    result.put(
                            "balance",
                            income - expense
                    );
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "TransactionTool - getMonthlySummary error: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return result;
    }


    /*
     * ============================================================
     * GET RECENT TRANSACTIONS
     * ============================================================
     */

    public List<Map<String, Object>> getRecentTransactions(
            int userId,
            int limit) {

        List<Map<String, Object>> result =
                new ArrayList<>();

        /*
         * Prevent invalid LIMIT values.
         */

        if (limit <= 0) {
            limit = 5;
        }

        if (limit > 50) {
            limit = 50;
        }

        String sql =
                "SELECT " +
                "id, " +
                "transaction_date, " +
                "type, " +
                "category, " +
                "description, " +
                "amount " +
                "FROM transactions " +
                "WHERE user_id = ? " +
                "ORDER BY transaction_date DESC, id DESC " +
                "LIMIT ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Map<String, Object> row =
                            new HashMap<>();

                    row.put(
                            "id",
                            rs.getInt("id")
                    );

                    row.put(
                            "transaction_date",
                            rs.getDate("transaction_date")
                    );

                    row.put(
                            "type",
                            rs.getString("type")
                    );

                    row.put(
                            "category",
                            rs.getString("category")
                    );

                    row.put(
                            "description",
                            rs.getString("description")
                    );

                    row.put(
                            "amount",
                            rs.getDouble("amount")
                    );

                    result.add(row);
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "TransactionTool - getRecentTransactions error: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return result;
    }


    /*
     * ============================================================
     * GET HIGHEST EXPENSES
     * ============================================================
     */

    public List<Map<String, Object>> getHighestExpenses(
            int userId,
            int limit) {

        List<Map<String, Object>> result =
                new ArrayList<>();

        if (limit <= 0) {
            limit = 5;
        }

        if (limit > 50) {
            limit = 50;
        }

        String sql =
                "SELECT " +
                "id, " +
                "transaction_date, " +
                "category, " +
                "description, " +
                "amount " +
                "FROM transactions " +
                "WHERE user_id = ? " +
                "AND type = 'EXPENSE' " +
                "ORDER BY amount DESC " +
                "LIMIT ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Map<String, Object> row =
                            new HashMap<>();

                    row.put(
                            "id",
                            rs.getInt("id")
                    );

                    row.put(
                            "transaction_date",
                            rs.getDate("transaction_date")
                    );

                    row.put(
                            "category",
                            rs.getString("category")
                    );

                    row.put(
                            "description",
                            rs.getString("description")
                    );

                    row.put(
                            "amount",
                            rs.getDouble("amount")
                    );

                    result.add(row);
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "TransactionTool - getHighestExpenses error: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return result;
    }


    /*
     * ============================================================
     * GET FINANCIAL SUMMARY
     * ============================================================
     */

    public Map<String, Object> getFinancialSummary(
            int userId) {

        Map<String, Object> result =
                new HashMap<>();

        double income =
                getTotalIncome(userId);

        double expense =
                getTotalExpense(userId);

        int transactionCount =
                getTransactionCount(userId);

        result.put(
                "total_income",
                income
        );

        result.put(
                "total_expense",
                expense
        );

        result.put(
                "balance",
                income - expense
        );

        result.put(
                "transaction_count",
                transactionCount
        );

        return result;
    }


    /*
     * ============================================================
     * ADD EXPENSE
     * ============================================================
     */

    public boolean addExpense(
            int userId,
            String category,
            String description,
            double amount) {

        String sql =
                "INSERT INTO transactions " +
                "(user_id, transaction_date, type, category, description, amount) " +
                "VALUES (?, CURDATE(), 'EXPENSE', ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, category);
            ps.setString(3, description);
            ps.setDouble(4, amount);

            int rows =
                    ps.executeUpdate();

            return rows > 0;

        } catch (Exception e) {

            System.out.println(
                    "TransactionTool - addExpense error: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return false;
        }
    }


    /*
     * ============================================================
     * DELETE TRANSACTION
     * ============================================================
     */

    public boolean deleteTransaction(
            int userId,
            int transactionId) {

        String sql =
                "DELETE FROM transactions " +
                "WHERE id = ? AND user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, transactionId);
            ps.setInt(2, userId);

            int rows =
                    ps.executeUpdate();

            return rows > 0;

        } catch (Exception e) {

            System.out.println(
                    "TransactionTool - deleteTransaction error: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return false;
        }
    }
}