package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import model.Income;
import util.DBConnection;

public class IncomeDAO {

    // ================= ADD INCOME =================

    public boolean addIncome(Income income) {

        boolean status = false;

        try {

            Connection con = DBConnection.getConnection();

            String sql = "INSERT INTO income(user_id,category_id,amount,description,income_date) VALUES(?,?,?,?,?)";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, income.getUserId());
            ps.setInt(2, income.getCategoryId());
            ps.setDouble(3, income.getAmount());
            ps.setString(4, income.getDescription());
            ps.setDate(5, income.getIncomeDate());

            status = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return status;
    }

    // ================= VIEW INCOME =================

    public ArrayList<Income> getIncome(int userId) {

        ArrayList<Income> list = new ArrayList<>();

        try {

            Connection con = DBConnection.getConnection();

            String sql =
            "SELECT i.*, c.category_name FROM income i " +
            "INNER JOIN categories c ON i.category_id = c.category_id " +
            "WHERE i.user_id=? ORDER BY income_date DESC";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Income income = new Income();

                income.setIncomeId(rs.getInt("income_id"));
                income.setUserId(rs.getInt("user_id"));
                income.setCategoryId(rs.getInt("category_id"));
                income.setCategoryName(rs.getString("category_name"));
                income.setAmount(rs.getDouble("amount"));
                income.setDescription(rs.getString("description"));
                income.setIncomeDate(rs.getDate("income_date"));

                list.add(income);

            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return list;
    }

    // ================= GET INCOME BY ID =================

    public Income getIncomeById(int id) {

        Income income = null;

        try {

            Connection con = DBConnection.getConnection();

            String sql = "SELECT * FROM income WHERE income_id=?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                income = new Income();

                income.setIncomeId(rs.getInt("income_id"));
                income.setUserId(rs.getInt("user_id"));
                income.setCategoryId(rs.getInt("category_id"));
                income.setAmount(rs.getDouble("amount"));
                income.setDescription(rs.getString("description"));
                income.setIncomeDate(rs.getDate("income_date"));

            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return income;
    }

    // ================= UPDATE INCOME =================

    public boolean updateIncome(Income income) {

        boolean status = false;

        try {

            Connection con = DBConnection.getConnection();

            String sql =
            "UPDATE income SET category_id=?, amount=?, description=?, income_date=? WHERE income_id=?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, income.getCategoryId());
            ps.setDouble(2, income.getAmount());
            ps.setString(3, income.getDescription());
            ps.setDate(4, income.getIncomeDate());
            ps.setInt(5, income.getIncomeId());

            status = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return status;
    }

    // ================= DELETE INCOME =================

    public boolean deleteIncome(int id) {

        boolean status = false;

        try {

            Connection con = DBConnection.getConnection();

            String sql = "DELETE FROM income WHERE income_id=?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, id);

            status = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return status;
    }

}