package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import model.Transaction;
import util.DBConnection;

public class TransactionDAO {

    // ================= RECENT TRANSACTIONS =================

    public ArrayList<Transaction> getRecentTransactions(int userId) {

        ArrayList<Transaction> list = new ArrayList<>();

        try {

            Connection con = DBConnection.getConnection();

            String sql =

            "SELECT income_date AS transaction_date, " +
            "'Income' AS type, " +
            "c.category_name, " +
            "i.description, " +
            "i.amount " +
            "FROM income i " +
            "INNER JOIN categories c ON i.category_id = c.category_id " +
            "WHERE i.user_id=? " +

            "UNION ALL " +

            "SELECT expense_date AS transaction_date, " +
            "'Expense' AS type, " +
            "c.category_name, " +
            "e.description, " +
            "e.amount " +
            "FROM expenses e " +
            "INNER JOIN categories c ON e.category_id = c.category_id " +
            "WHERE e.user_id=? " +

            "ORDER BY transaction_date DESC " +
            "LIMIT 5";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, userId);
            ps.setInt(2, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Transaction transaction = new Transaction();

                transaction.setTransactionDate(
                        rs.getDate("transaction_date"));

                transaction.setType(
                        rs.getString("type"));

                transaction.setCategoryName(
                        rs.getString("category_name"));

                transaction.setDescription(
                        rs.getString("description"));

                transaction.setAmount(
                        rs.getDouble("amount"));

                list.add(transaction);

            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return list;

    }

}