package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import model.Report;
import util.DBConnection;

public class ReportDAO {

	public ArrayList<Report> getFilteredReport(
	        int userId,
	        String fromDate,
	        String toDate,
	        String category,
	        String type) {

	    ArrayList<Report> list = new ArrayList<>();

	    try {

	        Connection con = DBConnection.getConnection();

	        StringBuilder sql = new StringBuilder();

	        sql.append(
	            "SELECT * FROM (" +

	            "SELECT income_date AS trans_date, " +
	            "'Income' AS trans_type, " +
	            "c.category_name, " +
	            "i.description, " +
	            "i.amount, " +
	            "i.user_id " +

	            "FROM income i " +

	            "INNER JOIN categories c " +
	            "ON i.category_id = c.category_id " +

	            "UNION ALL " +

	            "SELECT expense_date AS trans_date, " +
	            "'Expense' AS trans_type, " +
	            "c.category_name, " +
	            "e.description, " +
	            "e.amount, " +
	            "e.user_id " +

	            "FROM expenses e " +

	            "INNER JOIN categories c " +
	            "ON e.category_id = c.category_id " +

	            ") report " +

	            "WHERE user_id = ? "
	        );

	        // ================= DATE FILTER =================

	        if (fromDate != null && !fromDate.isEmpty()) {

	            sql.append(
	                "AND trans_date >= ? "
	            );
	        }

	        if (toDate != null && !toDate.isEmpty()) {

	            sql.append(
	                "AND trans_date <= ? "
	            );
	        }

	        // ================= CATEGORY FILTER =================

	        if (category != null && !category.isEmpty()) {

	            sql.append(
	                "AND category_name = ? "
	            );
	        }

	        // ================= TYPE FILTER =================

	        if (type != null && !type.isEmpty()) {

	            sql.append(
	                "AND trans_type = ? "
	            );
	        }

	        sql.append(
	            "ORDER BY trans_date DESC"
	        );


	        PreparedStatement ps =
	                con.prepareStatement(sql.toString());

	        int index = 1;

	        // User

	        ps.setInt(index++, userId);


	        // From Date

	        if (fromDate != null && !fromDate.isEmpty()) {

	            ps.setDate(
	                index++,
	                Date.valueOf(fromDate)
	            );
	        }


	        // To Date

	        if (toDate != null && !toDate.isEmpty()) {

	            ps.setDate(
	                index++,
	                Date.valueOf(toDate)
	            );
	        }


	        // Category

	        if (category != null && !category.isEmpty()) {

	            ps.setString(
	                index++,
	                category
	            );
	        }


	        // Type

	        if (type != null && !type.isEmpty()) {

	            ps.setString(
	                index++,
	                type
	            );
	        }


	        ResultSet rs = ps.executeQuery();


	        while (rs.next()) {

	            Report report = new Report();

	            report.setDate(
	                rs.getDate("trans_date")
	            );

	            report.setType(
	                rs.getString("trans_type")
	            );

	            report.setCategory(
	                rs.getString("category_name")
	            );

	            report.setDescription(
	                rs.getString("description")
	            );

	            report.setAmount(
	                rs.getDouble("amount")
	            );

	            list.add(report);
	        }


	        rs.close();
	        ps.close();
	        con.close();

	    } catch (Exception e) {

	        e.printStackTrace();

	    }

	    return list;
		}
	public ArrayList<Report> getRecentTransactions(int userId) {

	    ArrayList<Report> list = new ArrayList<>();

	    try {

	        Connection con = DBConnection.getConnection();

	        String sql =
	        "SELECT transaction_date, type, category_name, description, amount " +
	        "FROM report_view " +
	        "WHERE user_id=? " +
	        "ORDER BY transaction_date DESC " +
	        "LIMIT 5";

	        PreparedStatement ps = con.prepareStatement(sql);

	        ps.setInt(1, userId);

	        ResultSet rs = ps.executeQuery();

	        while(rs.next()){

	            Report r = new Report();

	            r.setDate(rs.getDate("transaction_date"));
	            r.setType(rs.getString("type"));
	            r.setCategory(rs.getString("category_name"));
	            r.setDescription(rs.getString("description"));
	            r.setAmount(rs.getDouble("amount"));

	            list.add(r);
	        }

	    }catch(Exception e){

	        e.printStackTrace();

	    }

	    return list;
	}
	
	
	
    // ================= ALL TRANSACTIONS =================

    public ArrayList<Report> getReport(int userId) {

        ArrayList<Report> list = new ArrayList<>();

        try {

            Connection con = DBConnection.getConnection();

            String sql =

            "SELECT income_date AS trans_date,'Income' AS trans_type," +
            "c.category_name,description,amount " +
            "FROM income i " +
            "INNER JOIN categories c ON i.category_id=c.category_id " +
            "WHERE i.user_id=? " +

            "UNION ALL " +

            "SELECT expense_date AS trans_date,'Expense' AS trans_type," +
            "c.category_name,description,amount " +
            "FROM expenses e " +
            "INNER JOIN categories c ON e.category_id=c.category_id " +
            "WHERE e.user_id=? " +

            "ORDER BY trans_date DESC";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, userId);
            ps.setInt(2, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Report r = new Report();

                r.setDate(rs.getDate("trans_date"));
                r.setType(rs.getString("trans_type"));
                r.setCategory(rs.getString("category_name"));
                r.setDescription(rs.getString("description"));
                r.setAmount(rs.getDouble("amount"));

                list.add(r);

            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return list;
    }
    
    
    
    
 // ================= GET USER CATEGORIES =================

    public ArrayList<String> getCategories(int userId) {

        ArrayList<String> categories = new ArrayList<>();

        try {

            Connection con = DBConnection.getConnection();

            String sql =
                    "SELECT category_name " +
                    "FROM categories " +
                    "WHERE user_id = ? " +
                    "ORDER BY category_name";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                categories.add(
                        rs.getString("category_name")
                );

            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return categories;
    }

    // ================= DATE WISE REPORT =================

    public ArrayList<Report> getReportByDate(int userId, Date fromDate, Date toDate) {

        ArrayList<Report> list = new ArrayList<>();

        try {

            Connection con = DBConnection.getConnection();

            String sql =

            "SELECT income_date AS trans_date,'Income' AS trans_type," +
            "c.category_name,description,amount " +
            "FROM income i " +
            "INNER JOIN categories c ON i.category_id=c.category_id " +
            "WHERE i.user_id=? AND income_date BETWEEN ? AND ? " +

            "UNION ALL " +

            "SELECT expense_date AS trans_date,'Expense' AS trans_type," +
            "c.category_name,description,amount " +
            "FROM expenses e " +
            "INNER JOIN categories c ON e.category_id=c.category_id " +
            "WHERE e.user_id=? AND expense_date BETWEEN ? AND ? " +

            "ORDER BY trans_date DESC";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, userId);
            ps.setDate(2, fromDate);
            ps.setDate(3, toDate);

            ps.setInt(4, userId);
            ps.setDate(5, fromDate);
            ps.setDate(6, toDate);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Report r = new Report();

                r.setDate(rs.getDate("trans_date"));
                r.setType(rs.getString("trans_type"));
                r.setCategory(rs.getString("category_name"));
                r.setDescription(rs.getString("description"));
                r.setAmount(rs.getDouble("amount"));

                list.add(r);

            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return list;
    }

    // ================= TOTAL INCOME =================

    public double getTotalIncome(int userId) {

        double total = 0;

        try {

            Connection con = DBConnection.getConnection();

            String sql =
                    "SELECT IFNULL(SUM(amount),0) total FROM income WHERE user_id=?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                total = rs.getDouble("total");

            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return total;
    }

    // ================= TOTAL EXPENSE =================

    public double getTotalExpense(int userId) {

        double total = 0;

        try {

            Connection con = DBConnection.getConnection();

            String sql =
                    "SELECT IFNULL(SUM(amount),0) total FROM expenses WHERE user_id=?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                total = rs.getDouble("total");

            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return total;
    }

    // ================= BALANCE =================

    public double getBalance(int userId) {

        return getTotalIncome(userId) - getTotalExpense(userId);

    }

}