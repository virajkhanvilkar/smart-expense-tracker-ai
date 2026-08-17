//package dao;
//
//import java.sql.Connection;
//import java.sql.Date;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.util.ArrayList;
//
//import model.Expense;
//import util.DBConnection;
//
//public class ExpenseDAO {
//
//    // ================= ADD EXPENSE =================
//
//    public boolean addExpense(Expense expense) {
//
//        boolean status = false;
//
//        try {
//
//            Connection con = DBConnection.getConnection();
//
//            String sql = "INSERT INTO expenses(user_id,category_id,amount,description,expense_date) VALUES(?,?,?,?,?)";
//
//            PreparedStatement ps = con.prepareStatement(sql);
//
//            ps.setInt(1, expense.getUserId());
//            ps.setInt(2, expense.getCategoryId());
//            ps.setDouble(3, expense.getAmount());
//            ps.setString(4, expense.getDescription());
//            ps.setDate(5, expense.getExpenseDate());
//
//            status = ps.executeUpdate() > 0;
//
//            ps.close();
//            con.close();
//
//        } catch (Exception e) {
//
//            e.printStackTrace();
//
//        }
//
//        return status;
//    }
//
//    // ================= VIEW EXPENSE =================
//
//    public ArrayList<Expense> getExpenses(int userId) {
//
//        ArrayList<Expense> list = new ArrayList<>();
//
//        try {
//
//            Connection con = DBConnection.getConnection();
//
//            String sql =
//            "SELECT e.*,c.category_name FROM expenses e " +
//            "INNER JOIN categories c ON e.category_id=c.category_id " +
//            "WHERE e.user_id=? ORDER BY expense_date DESC";
//
//            PreparedStatement ps = con.prepareStatement(sql);
//
//            ps.setInt(1, userId);
//
//            ResultSet rs = ps.executeQuery();
//
//            while(rs.next()) {
//
//                Expense expense = new Expense();
//
//                expense.setExpenseId(rs.getInt("expense_id"));
//                expense.setUserId(rs.getInt("user_id"));
//                expense.setCategoryId(rs.getInt("category_id"));
//                expense.setCategoryName(rs.getString("category_name"));
//                expense.setAmount(rs.getDouble("amount"));
//                expense.setDescription(rs.getString("description"));
//                expense.setExpenseDate(rs.getDate("expense_date"));
//
//                list.add(expense);
//
//            }
//
//            rs.close();
//            ps.close();
//            con.close();
//
//        } catch (Exception e) {
//
//            e.printStackTrace();
//
//        }
//
//        return list;
//    }
//
//    // ================= GET EXPENSE =================
//
//    public Expense getExpenseById(int id) {
//
//        Expense expense = null;
//
//        try {
//
//            Connection con = DBConnection.getConnection();
//
//            String sql = "SELECT * FROM expenses WHERE expense_id=?";
//
//            PreparedStatement ps = con.prepareStatement(sql);
//
//            ps.setInt(1, id);
//
//            ResultSet rs = ps.executeQuery();
//
//            if(rs.next()) {
//
//                expense = new Expense();
//
//                expense.setExpenseId(rs.getInt("expense_id"));
//                expense.setUserId(rs.getInt("user_id"));
//                expense.setCategoryId(rs.getInt("category_id"));
//                expense.setAmount(rs.getDouble("amount"));
//                expense.setDescription(rs.getString("description"));
//                expense.setExpenseDate(rs.getDate("expense_date"));
//
//            }
//
//            rs.close();
//            ps.close();
//            con.close();
//
//        } catch(Exception e) {
//
//            e.printStackTrace();
//
//        }
//
//        return expense;
//
//    }
//
//    // ================= UPDATE =================
//
//    public boolean updateExpense(Expense expense) {
//
//        boolean status = false;
//
//        try {
//
//            Connection con = DBConnection.getConnection();
//
//            String sql =
//            "UPDATE expenses SET category_id=?,amount=?,description=?,expense_date=? WHERE expense_id=?";
//
//            PreparedStatement ps = con.prepareStatement(sql);
//
//            ps.setInt(1, expense.getCategoryId());
//            ps.setDouble(2, expense.getAmount());
//            ps.setString(3, expense.getDescription());
//            ps.setDate(4, expense.getExpenseDate());
//            ps.setInt(5, expense.getExpenseId());
//
//            status = ps.executeUpdate() > 0;
//
//            ps.close();
//            con.close();
//
//        } catch(Exception e) {
//
//            e.printStackTrace();
//
//        }
//
//        return status;
//
//    }
//
//    // ================= DELETE =================
//
//    public boolean deleteExpense(int id) {
//
//        boolean status = false;
//
//        try {
//
//            Connection con = DBConnection.getConnection();
//
//            String sql =
//            "DELETE FROM expenses WHERE expense_id=?";
//
//            PreparedStatement ps = con.prepareStatement(sql);
//
//            ps.setInt(1, id);
//
//            status = ps.executeUpdate() > 0;
//
//            ps.close();
//            con.close();
//
//        } catch(Exception e) {
//
//            e.printStackTrace();
//
//        }
//
//        return status;
//
//    }
//
//}





package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import model.Expense;
import util.DBConnection;

public class ExpenseDAO {

    // ============================================================
    // ADD EXPENSE
    // ============================================================

    public boolean addExpense(Expense expense) {

        Connection con = null;

        try {

            con = DBConnection.getConnection();

            con.setAutoCommit(false);

            // ----------------------------------------------------
            // 1. INSERT INTO expenses
            // ----------------------------------------------------

            String expenseSql =
                    "INSERT INTO expenses " +
                    "(user_id, category_id, amount, description, expense_date) " +
                    "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement expensePs =
                    con.prepareStatement(
                            expenseSql,
                            Statement.RETURN_GENERATED_KEYS
                    );

            expensePs.setInt(
                    1,
                    expense.getUserId()
            );

            expensePs.setInt(
                    2,
                    expense.getCategoryId()
            );

            expensePs.setDouble(
                    3,
                    expense.getAmount()
            );

            expensePs.setString(
                    4,
                    expense.getDescription()
            );

            expensePs.setDate(
                    5,
                    expense.getExpenseDate()
            );

            int expenseResult =
                    expensePs.executeUpdate();

            if (expenseResult == 0) {

                con.rollback();

                return false;
            }

            // ----------------------------------------------------
            // 2. GET GENERATED EXPENSE ID
            // ----------------------------------------------------

            int expenseId = 0;

            ResultSet keys =
                    expensePs.getGeneratedKeys();

            if (keys.next()) {

                expenseId =
                        keys.getInt(1);
            }

            keys.close();
            expensePs.close();

            if (expenseId == 0) {

                con.rollback();

                return false;
            }

            // ----------------------------------------------------
            // 3. INSERT INTO TRANSACTIONS
            // ----------------------------------------------------

            String transactionSql =
                    "INSERT INTO transactions " +
                    "(document_id, user_id, transaction_date, type, " +
                    "category, description, amount, balance, " +
                    "source_id, source_type) " +
                    "SELECT NULL, ?, ?, 'EXPENSE', c.category_name, ?, ?, NULL, ?, 'EXPENSE' " +
                    "FROM categories c " +
                    "WHERE c.category_id = ?";

            PreparedStatement transactionPs =
                    con.prepareStatement(transactionSql);

            transactionPs.setInt(
                    1,
                    expense.getUserId()
            );

            transactionPs.setDate(
                    2,
                    expense.getExpenseDate()
            );

            transactionPs.setString(
                    3,
                    expense.getDescription()
            );

            transactionPs.setDouble(
                    4,
                    expense.getAmount()
            );

            transactionPs.setInt(
                    5,
                    expenseId
            );

            transactionPs.setInt(
                    6,
                    expense.getCategoryId()
            );

            int transactionResult =
                    transactionPs.executeUpdate();

            transactionPs.close();

            if (transactionResult == 0) {

                con.rollback();

                return false;
            }

            // ----------------------------------------------------
            // 4. COMMIT
            // ----------------------------------------------------

            con.commit();

            return true;

        } catch (Exception e) {

            e.printStackTrace();

            try {

                if (con != null) {

                    con.rollback();
                }

            } catch (Exception rollbackError) {

                rollbackError.printStackTrace();
            }

            return false;

        } finally {

            try {

                if (con != null) {

                    con.setAutoCommit(true);
                    con.close();
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }


    // ============================================================
    // VIEW EXPENSE
    // ============================================================

    public ArrayList<Expense> getExpenses(int userId) {

        ArrayList<Expense> list =
                new ArrayList<>();

        try {

            Connection con =
                    DBConnection.getConnection();

            String sql =
                    "SELECT e.*, c.category_name " +
                    "FROM expenses e " +
                    "INNER JOIN categories c " +
                    "ON e.category_id = c.category_id " +
                    "WHERE e.user_id = ? " +
                    "ORDER BY e.expense_date DESC";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setInt(
                    1,
                    userId
            );

            ResultSet rs =
                    ps.executeQuery();

            while (rs.next()) {

                Expense expense =
                        new Expense();

                expense.setExpenseId(
                        rs.getInt("expense_id")
                );

                expense.setUserId(
                        rs.getInt("user_id")
                );

                expense.setCategoryId(
                        rs.getInt("category_id")
                );

                expense.setCategoryName(
                        rs.getString("category_name")
                );

                expense.setAmount(
                        rs.getDouble("amount")
                );

                expense.setDescription(
                        rs.getString("description")
                );

                expense.setExpenseDate(
                        rs.getDate("expense_date")
                );

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


    // ============================================================
    // GET EXPENSE BY ID
    // ============================================================

    public Expense getExpenseById(int id) {

        Expense expense = null;

        try {

            Connection con =
                    DBConnection.getConnection();

            String sql =
                    "SELECT e.*, c.category_name " +
                    "FROM expenses e " +
                    "INNER JOIN categories c " +
                    "ON e.category_id = c.category_id " +
                    "WHERE e.expense_id = ?";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setInt(
                    1,
                    id
            );

            ResultSet rs =
                    ps.executeQuery();

            if (rs.next()) {

                expense =
                        new Expense();

                expense.setExpenseId(
                        rs.getInt("expense_id")
                );

                expense.setUserId(
                        rs.getInt("user_id")
                );

                expense.setCategoryId(
                        rs.getInt("category_id")
                );

                expense.setCategoryName(
                        rs.getString("category_name")
                );

                expense.setAmount(
                        rs.getDouble("amount")
                );

                expense.setDescription(
                        rs.getString("description")
                );

                expense.setExpenseDate(
                        rs.getDate("expense_date")
                );
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();
        }

        return expense;
    }


    // ============================================================
    // UPDATE EXPENSE
    // ============================================================

    public boolean updateExpense(Expense expense) {

        Connection con = null;

        try {

            con =
                    DBConnection.getConnection();

            con.setAutoCommit(false);

            // ----------------------------------------------------
            // Get existing expense
            // ----------------------------------------------------

            Expense oldExpense =
                    getExpenseByIdUsingConnection(
                            con,
                            expense.getExpenseId()
                    );

            if (oldExpense == null) {

                con.rollback();

                return false;
            }

            // ----------------------------------------------------
            // Update expenses table
            // ----------------------------------------------------

            String expenseSql =
                    "UPDATE expenses " +
                    "SET category_id=?, " +
                    "amount=?, " +
                    "description=?, " +
                    "expense_date=? " +
                    "WHERE expense_id=? " +
                    "AND user_id=?";

            PreparedStatement expensePs =
                    con.prepareStatement(expenseSql);

            expensePs.setInt(
                    1,
                    expense.getCategoryId()
            );

            expensePs.setDouble(
                    2,
                    expense.getAmount()
            );

            expensePs.setString(
                    3,
                    expense.getDescription()
            );

            expensePs.setDate(
                    4,
                    expense.getExpenseDate()
            );

            expensePs.setInt(
                    5,
                    expense.getExpenseId()
            );

            expensePs.setInt(
                    6,
                    oldExpense.getUserId()
            );

            int expenseResult =
                    expensePs.executeUpdate();

            expensePs.close();

            if (expenseResult == 0) {

                con.rollback();

                return false;
            }

            // ----------------------------------------------------
            // Update transactions table
            // ----------------------------------------------------

            String transactionSql =
                    "UPDATE transactions t " +
                    "INNER JOIN categories c " +
                    "ON c.category_id = ? " +
                    "SET t.transaction_date=?, " +
                    "t.category=c.category_name, " +
                    "t.description=?, " +
                    "t.amount=? " +
                    "WHERE t.source_id=? " +
                    "AND t.source_type='EXPENSE' " +
                    "AND t.user_id=?";

            PreparedStatement transactionPs =
                    con.prepareStatement(transactionSql);

            transactionPs.setInt(
                    1,
                    expense.getCategoryId()
            );

            transactionPs.setDate(
                    2,
                    expense.getExpenseDate()
            );

            transactionPs.setString(
                    3,
                    expense.getDescription()
            );

            transactionPs.setDouble(
                    4,
                    expense.getAmount()
            );

            transactionPs.setInt(
                    5,
                    expense.getExpenseId()
            );

            transactionPs.setInt(
                    6,
                    oldExpense.getUserId()
            );

            transactionPs.executeUpdate();

            transactionPs.close();

            // ----------------------------------------------------
            // COMMIT
            // ----------------------------------------------------

            con.commit();

            return true;

        } catch (Exception e) {

            e.printStackTrace();

            try {

                if (con != null) {

                    con.rollback();
                }

            } catch (Exception rollbackError) {

                rollbackError.printStackTrace();
            }

            return false;

        } finally {

            try {

                if (con != null) {

                    con.setAutoCommit(true);
                    con.close();
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }


    // ============================================================
    // DELETE EXPENSE
    // ============================================================

    public boolean deleteExpense(int id) {

        Connection con = null;

        try {

            con =
                    DBConnection.getConnection();

            con.setAutoCommit(false);

            // ----------------------------------------------------
            // Get expense first
            // ----------------------------------------------------

            Expense expense =
                    getExpenseByIdUsingConnection(
                            con,
                            id
                    );

            if (expense == null) {

                con.rollback();

                return false;
            }

            // ----------------------------------------------------
            // Delete transaction
            // ----------------------------------------------------

            String transactionSql =
                    "DELETE FROM transactions " +
                    "WHERE source_id=? " +
                    "AND source_type='EXPENSE' " +
                    "AND user_id=?";

            PreparedStatement transactionPs =
                    con.prepareStatement(transactionSql);

            transactionPs.setInt(
                    1,
                    id
            );

            transactionPs.setInt(
                    2,
                    expense.getUserId()
            );

            transactionPs.executeUpdate();

            transactionPs.close();

            // ----------------------------------------------------
            // Delete expense
            // ----------------------------------------------------

            String expenseSql =
                    "DELETE FROM expenses " +
                    "WHERE expense_id=? " +
                    "AND user_id=?";

            PreparedStatement expensePs =
                    con.prepareStatement(expenseSql);

            expensePs.setInt(
                    1,
                    id
            );

            expensePs.setInt(
                    2,
                    expense.getUserId()
            );

            int result =
                    expensePs.executeUpdate();

            expensePs.close();

            if (result == 0) {

                con.rollback();

                return false;
            }

            // ----------------------------------------------------
            // COMMIT
            // ----------------------------------------------------

            con.commit();

            return true;

        } catch (Exception e) {

            e.printStackTrace();

            try {

                if (con != null) {

                    con.rollback();
                }

            } catch (Exception rollbackError) {

                rollbackError.printStackTrace();
            }

            return false;

        } finally {

            try {

                if (con != null) {

                    con.setAutoCommit(true);
                    con.close();
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }


    // ============================================================
    // INTERNAL GET EXPENSE USING EXISTING CONNECTION
    // ============================================================

    private Expense getExpenseByIdUsingConnection(
            Connection con,
            int id) throws Exception {

        String sql =
                "SELECT * FROM expenses " +
                "WHERE expense_id=?";

        PreparedStatement ps =
                con.prepareStatement(sql);

        ps.setInt(
                1,
                id
        );

        ResultSet rs =
                ps.executeQuery();

        Expense expense = null;

        if (rs.next()) {

            expense =
                    new Expense();

            expense.setExpenseId(
                    rs.getInt("expense_id")
            );

            expense.setUserId(
                    rs.getInt("user_id")
            );

            expense.setCategoryId(
                    rs.getInt("category_id")
            );

            expense.setAmount(
                    rs.getDouble("amount")
            );

            expense.setDescription(
                    rs.getString("description")
            );

            expense.setExpenseDate(
                    rs.getDate("expense_date")
            );
        }

        rs.close();
        ps.close();

        return expense;
    }
}