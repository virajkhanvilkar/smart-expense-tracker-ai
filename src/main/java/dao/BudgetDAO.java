package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import model.Budget;
import util.DBConnection;

public class BudgetDAO {

    // ================= ADD BUDGET =================

    public boolean addBudget(Budget budget) {

        boolean status = false;

        try {

            Connection con = DBConnection.getConnection();

            String sql = "INSERT INTO budgets(user_id,category_id,budget_amount,month,year) VALUES(?,?,?,?,?)";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, budget.getUserId());
            ps.setInt(2, budget.getCategoryId());
            ps.setDouble(3, budget.getBudgetAmount());
            ps.setInt(4, budget.getMonth());
            ps.setInt(5, budget.getYear());

            status = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return status;
    }

    // ================= VIEW BUDGET =================

    public ArrayList<Budget> getBudgets(int userId) {

        ArrayList<Budget> list = new ArrayList<>();

        try {

            Connection con = DBConnection.getConnection();

            String sql =
            "SELECT b.*,c.category_name " +
            "FROM budgets b " +
            "INNER JOIN categories c ON b.category_id=c.category_id " +
            "WHERE b.user_id=? " +
            "ORDER BY b.year DESC,b.month DESC";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {

                Budget budget = new Budget();

                budget.setBudgetId(rs.getInt("budget_id"));
                budget.setUserId(rs.getInt("user_id"));
                budget.setCategoryId(rs.getInt("category_id"));
                budget.setCategoryName(rs.getString("category_name"));
                budget.setBudgetAmount(rs.getDouble("budget_amount"));
                budget.setMonth(rs.getInt("month"));
                budget.setYear(rs.getInt("year"));

                list.add(budget);

            }

            rs.close();
            ps.close();
            con.close();

        } catch(Exception e) {

            e.printStackTrace();

        }

        return list;
    }

    // ================= GET BUDGET BY ID =================

    public Budget getBudgetById(int id) {

        Budget budget = null;

        try {

            Connection con = DBConnection.getConnection();

            String sql = "SELECT * FROM budgets WHERE budget_id=?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                budget = new Budget();

                budget.setBudgetId(rs.getInt("budget_id"));
                budget.setUserId(rs.getInt("user_id"));
                budget.setCategoryId(rs.getInt("category_id"));
                budget.setBudgetAmount(rs.getDouble("budget_amount"));
                budget.setMonth(rs.getInt("month"));
                budget.setYear(rs.getInt("year"));

            }

            rs.close();
            ps.close();
            con.close();

        } catch(Exception e) {

            e.printStackTrace();

        }

        return budget;
    }

    // ================= UPDATE BUDGET =================

    public boolean updateBudget(Budget budget) {

        boolean status = false;

        try {

            Connection con = DBConnection.getConnection();

            String sql =
            "UPDATE budgets SET category_id=?,budget_amount=?,month=?,year=? WHERE budget_id=?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, budget.getCategoryId());
            ps.setDouble(2, budget.getBudgetAmount());
            ps.setInt(3, budget.getMonth());
            ps.setInt(4, budget.getYear());
            ps.setInt(5, budget.getBudgetId());

            status = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch(Exception e) {

            e.printStackTrace();

        }

        return status;
    }

    // ================= DELETE BUDGET =================

    public boolean deleteBudget(int id) {

        boolean status = false;

        try {

            Connection con = DBConnection.getConnection();

            String sql =
            "DELETE FROM budgets WHERE budget_id=?";

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

    // ================= SPENT AMOUNT =================

    public double getSpentAmount(int userId,int categoryId,int month,int year) {

        double spent = 0;

        try {

            Connection con = DBConnection.getConnection();

            String sql =
            "SELECT IFNULL(SUM(amount),0) total " +
            "FROM expenses " +
            "WHERE user_id=? " +
            "AND category_id=? " +
            "AND MONTH(expense_date)=? " +
            "AND YEAR(expense_date)=?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, userId);
            ps.setInt(2, categoryId);
            ps.setInt(3, month);
            ps.setInt(4, year);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                spent = rs.getDouble("total");

            }

            rs.close();
            ps.close();
            con.close();

        } catch(Exception e) {

            e.printStackTrace();

        }

        return spent;
    }

    // ================= REMAINING BUDGET =================

    public double getRemainingBudget(int userId,int categoryId,int month,int year) {

        double budget = 0;

        try {

            Connection con = DBConnection.getConnection();

            String sql =
            "SELECT budget_amount FROM budgets " +
            "WHERE user_id=? " +
            "AND category_id=? " +
            "AND month=? " +
            "AND year=?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, userId);
            ps.setInt(2, categoryId);
            ps.setInt(3, month);
            ps.setInt(4, year);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                budget = rs.getDouble("budget_amount");

            }

            rs.close();
            ps.close();
            con.close();

        } catch(Exception e) {

            e.printStackTrace();

        }

        double spent = getSpentAmount(userId,categoryId,month,year);

        return budget - spent;

    }

}