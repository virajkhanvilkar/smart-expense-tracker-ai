//package dao;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//
//import model.Dashboard;
//import util.DBConnection;
//
//public class DashboardDAO {
//
//    public Dashboard getDashboardData(int userId) {
//
//        Dashboard dashboard = new Dashboard();
//
//        try {
//
//            Connection con = DBConnection.getConnection();
//
//            // ================= Total Income - Current Month =================
//
//            String incomeSql =
//                    "SELECT IFNULL(SUM(amount), 0) AS totalIncome " +
//                    "FROM income " +
//                    "WHERE user_id = ? " +
//                    "AND MONTH(income_date) = MONTH(CURDATE()) " +
//                    "AND YEAR(income_date) = YEAR(CURDATE())";
//
//            PreparedStatement psIncome =
//                    con.prepareStatement(incomeSql);
//
//            psIncome.setInt(1, userId);
//
//            ResultSet rsIncome =
//                    psIncome.executeQuery();
//
//            if (rsIncome.next()) {
//
//                dashboard.setTotalIncome(
//                        rsIncome.getDouble("totalIncome")
//                );
//            }
//
//            rsIncome.close();
//            psIncome.close();
//
//
//            // ================= Total Expense - Current Month =================
//
//            String expenseSql =
//                    "SELECT IFNULL(SUM(amount), 0) AS totalExpense " +
//                    "FROM expenses " +
//                    "WHERE user_id = ? " +
//                    "AND MONTH(expense_date) = MONTH(CURDATE()) " +
//                    "AND YEAR(expense_date) = YEAR(CURDATE())";
//
//            PreparedStatement psExpense =
//                    con.prepareStatement(expenseSql);
//
//            psExpense.setInt(1, userId);
//
//            ResultSet rsExpense =
//                    psExpense.executeQuery();
//
//            if (rsExpense.next()) {
//
//                dashboard.setTotalExpense(
//                        rsExpense.getDouble("totalExpense")
//                );
//            }
//
//            rsExpense.close();
//            psExpense.close();
//
//
//            // ================= Total Categories =================
//
//            String categorySql =
//                    "SELECT COUNT(*) AS totalCategories " +
//                    "FROM categories " +
//                    "WHERE user_id = ?";
//
//            PreparedStatement psCategory =
//                    con.prepareStatement(categorySql);
//
//            psCategory.setInt(1, userId);
//
//            ResultSet rsCategory =
//                    psCategory.executeQuery();
//
//            if (rsCategory.next()) {
//
//                dashboard.setTotalCategories(
//                        rsCategory.getInt("totalCategories")
//                );
//            }
//
//            rsCategory.close();
//            psCategory.close();
//
//
//            // ================= Balance =================
//
//            dashboard.setBalance(
//                    dashboard.getTotalIncome()
//                    - dashboard.getTotalExpense()
//            );
//
//
//            con.close();
//
//        } catch (Exception e) {
//
//            e.printStackTrace();
//
//        }
//
//        return dashboard;
//    }
//}


package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.Dashboard;
import model.BudgetStatus;
import util.DBConnection;

public class DashboardDAO {

    public Dashboard getDashboardData(int userId) {

        Dashboard dashboard = new Dashboard();

        try {

            Connection con = DBConnection.getConnection();

            // ================= Total Income - Current Month =================

            String incomeSql =
                    "SELECT IFNULL(SUM(amount), 0) AS totalIncome " +
                    "FROM income " +
                    "WHERE user_id = ? " +
                    "AND MONTH(income_date) = MONTH(CURDATE()) " +
                    "AND YEAR(income_date) = YEAR(CURDATE())";

            PreparedStatement psIncome =
                    con.prepareStatement(incomeSql);

            psIncome.setInt(1, userId);

            ResultSet rsIncome =
                    psIncome.executeQuery();

            if (rsIncome.next()) {

                dashboard.setTotalIncome(
                        rsIncome.getDouble("totalIncome")
                );
            }

            rsIncome.close();
            psIncome.close();


            // ================= Total Expense - Current Month =================

            String expenseSql =
                    "SELECT IFNULL(SUM(amount), 0) AS totalExpense " +
                    "FROM expenses " +
                    "WHERE user_id = ? " +
                    "AND MONTH(expense_date) = MONTH(CURDATE()) " +
                    "AND YEAR(expense_date) = YEAR(CURDATE())";

            PreparedStatement psExpense =
                    con.prepareStatement(expenseSql);

            psExpense.setInt(1, userId);

            ResultSet rsExpense =
                    psExpense.executeQuery();

            if (rsExpense.next()) {

                dashboard.setTotalExpense(
                        rsExpense.getDouble("totalExpense")
                );
            }

            rsExpense.close();
            psExpense.close();


            // ================= Total Categories =================

            String categorySql =
                    "SELECT COUNT(*) AS totalCategories " +
                    "FROM categories " +
                    "WHERE user_id = ?";

            PreparedStatement psCategory =
                    con.prepareStatement(categorySql);

            psCategory.setInt(1, userId);

            ResultSet rsCategory =
                    psCategory.executeQuery();

            if (rsCategory.next()) {

                dashboard.setTotalCategories(
                        rsCategory.getInt("totalCategories")
                );
            }

            rsCategory.close();
            psCategory.close();


            // ================= Balance =================

            dashboard.setBalance(
                    dashboard.getTotalIncome()
                    - dashboard.getTotalExpense()
            );


            // ================= Budget Status =================

            List<BudgetStatus> budgetStatuses = new ArrayList<>();

            String budgetSql =
                    "SELECT " +
                    "b.budget_id, " +
                    "c.category_name, " +
                    "b.budget_amount, " +
                    "IFNULL(SUM(e.amount), 0) AS spent " +
                    "FROM budgets b " +
                    "JOIN categories c " +
                    "ON b.category_id = c.category_id " +
                    "LEFT JOIN expenses e " +
                    "ON e.category_id = b.category_id " +
                    "AND e.user_id = b.user_id " +
                    "AND MONTH(e.expense_date) = b.month " +
                    "AND YEAR(e.expense_date) = b.year " +
                    "WHERE b.user_id = ? " +
                    "AND b.month = MONTH(CURDATE()) " +
                    "AND b.year = YEAR(CURDATE()) " +
                    "GROUP BY b.budget_id, c.category_name, b.budget_amount";

            PreparedStatement psBudget =
                    con.prepareStatement(budgetSql);

            psBudget.setInt(1, userId);

            ResultSet rsBudget =
                    psBudget.executeQuery();

            while (rsBudget.next()) {

                String categoryName =
                        rsBudget.getString("category_name");

                double budgetAmount =
                        rsBudget.getDouble("budget_amount");

                double spent =
                        rsBudget.getDouble("spent");

                double remaining =
                        budgetAmount - spent;

                BudgetStatus status =
                        new BudgetStatus(
                                categoryName,
                                budgetAmount,
                                spent,
                                remaining
                        );

                budgetStatuses.add(status);
            }

            rsBudget.close();
            psBudget.close();

            dashboard.setBudgetStatuses(budgetStatuses);


            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return dashboard;
    }
}