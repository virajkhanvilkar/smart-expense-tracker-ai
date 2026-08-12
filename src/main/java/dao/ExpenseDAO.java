package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import model.Expense;
import util.DBConnection;

public class ExpenseDAO {

    // ================= ADD EXPENSE =================

    public boolean addExpense(Expense expense) {

        boolean status = false;

        try {

            Connection con = DBConnection.getConnection();

            String sql = "INSERT INTO expenses(user_id,category_id,amount,description,expense_date) VALUES(?,?,?,?,?)";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, expense.getUserId());
            ps.setInt(2, expense.getCategoryId());
            ps.setDouble(3, expense.getAmount());
            ps.setString(4, expense.getDescription());
            ps.setDate(5, expense.getExpenseDate());

            status = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return status;
    }

    // ================= VIEW EXPENSE =================

    public ArrayList<Expense> getExpenses(int userId) {

        ArrayList<Expense> list = new ArrayList<>();

        try {

            Connection con = DBConnection.getConnection();

            String sql =
            "SELECT e.*,c.category_name FROM expenses e " +
            "INNER JOIN categories c ON e.category_id=c.category_id " +
            "WHERE e.user_id=? ORDER BY expense_date DESC";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {

                Expense expense = new Expense();

                expense.setExpenseId(rs.getInt("expense_id"));
                expense.setUserId(rs.getInt("user_id"));
                expense.setCategoryId(rs.getInt("category_id"));
                expense.setCategoryName(rs.getString("category_name"));
                expense.setAmount(rs.getDouble("amount"));
                expense.setDescription(rs.getString("description"));
                expense.setExpenseDate(rs.getDate("expense_date"));

                list.add(expense);

            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return list;
    }

    // ================= GET EXPENSE =================

    public Expense getExpenseById(int id) {

        Expense expense = null;

        try {

            Connection con = DBConnection.getConnection();

            String sql = "SELECT * FROM expenses WHERE expense_id=?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                expense = new Expense();

                expense.setExpenseId(rs.getInt("expense_id"));
                expense.setUserId(rs.getInt("user_id"));
                expense.setCategoryId(rs.getInt("category_id"));
                expense.setAmount(rs.getDouble("amount"));
                expense.setDescription(rs.getString("description"));
                expense.setExpenseDate(rs.getDate("expense_date"));

            }

            rs.close();
            ps.close();
            con.close();

        } catch(Exception e) {

            e.printStackTrace();

        }

        return expense;

    }

    // ================= UPDATE =================

    public boolean updateExpense(Expense expense) {

        boolean status = false;

        try {

            Connection con = DBConnection.getConnection();

            String sql =
            "UPDATE expenses SET category_id=?,amount=?,description=?,expense_date=? WHERE expense_id=?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, expense.getCategoryId());
            ps.setDouble(2, expense.getAmount());
            ps.setString(3, expense.getDescription());
            ps.setDate(4, expense.getExpenseDate());
            ps.setInt(5, expense.getExpenseId());

            status = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch(Exception e) {

            e.printStackTrace();

        }

        return status;

    }

    // ================= DELETE =================

    public boolean deleteExpense(int id) {

        boolean status = false;

        try {

            Connection con = DBConnection.getConnection();

            String sql =
            "DELETE FROM expenses WHERE expense_id=?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, id);

            status = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch(Exception e) {

            e.printStackTrace();

        }

        return status;

    }

}