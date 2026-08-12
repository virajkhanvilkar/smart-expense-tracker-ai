package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import util.DBConnection;

public class ChartDAO {

    // ================= INCOME VS EXPENSE =================

    public double[] getIncomeExpenseData(int userId) {

        double[] data = new double[2];

        try {

            Connection con = DBConnection.getConnection();

            String incomeSql =
                    "SELECT IFNULL(SUM(amount),0) total FROM income WHERE user_id=?";

            PreparedStatement ps1 = con.prepareStatement(incomeSql);

            ps1.setInt(1, userId);

            ResultSet rs1 = ps1.executeQuery();

            if (rs1.next()) {

                data[0] = rs1.getDouble("total");

            }

            rs1.close();
            ps1.close();

            String expenseSql =
                    "SELECT IFNULL(SUM(amount),0) total FROM expenses WHERE user_id=?";

            PreparedStatement ps2 = con.prepareStatement(expenseSql);

            ps2.setInt(1, userId);

            ResultSet rs2 = ps2.executeQuery();

            if (rs2.next()) {

                data[1] = rs2.getDouble("total");

            }

            rs2.close();
            ps2.close();

            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return data;

    }

    // ================= EXPENSE CATEGORY CHART =================

    public ResultSet getExpenseCategoryData(int userId) {

        try {

            Connection con = DBConnection.getConnection();

            String sql =
                    "SELECT c.category_name, SUM(e.amount) total " +
                    "FROM expenses e " +
                    "INNER JOIN categories c " +
                    "ON e.category_id=c.category_id " +
                    "WHERE e.user_id=? " +
                    "GROUP BY c.category_name";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, userId);

            return ps.executeQuery();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return null;

    }

    // ================= MONTHLY EXPENSE CHART =================

    public ResultSet getMonthlyExpenseData(int userId) {

        try {

            Connection con = DBConnection.getConnection();

            String sql =
                    "SELECT MONTH(expense_date) month, " +
                    "SUM(amount) total " +
                    "FROM expenses " +
                    "WHERE user_id=? " +
                    "GROUP BY MONTH(expense_date) " +
                    "ORDER BY MONTH(expense_date)";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, userId);

            return ps.executeQuery();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return null;

    }

}