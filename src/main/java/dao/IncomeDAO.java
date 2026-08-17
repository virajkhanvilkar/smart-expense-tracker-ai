//package dao;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.util.ArrayList;
//
//import model.Income;
//import util.DBConnection;
//
//public class IncomeDAO {
//
//    // ================= ADD INCOME =================
//
//    public boolean addIncome(Income income) {
//
//        boolean status = false;
//
//        try {
//
//            Connection con = DBConnection.getConnection();
//
//            String sql = "INSERT INTO income(user_id,category_id,amount,description,income_date) VALUES(?,?,?,?,?)";
//
//            PreparedStatement ps = con.prepareStatement(sql);
//
//            ps.setInt(1, income.getUserId());
//            ps.setInt(2, income.getCategoryId());
//            ps.setDouble(3, income.getAmount());
//            ps.setString(4, income.getDescription());
//            ps.setDate(5, income.getIncomeDate());
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
//    // ================= VIEW INCOME =================
//
//    public ArrayList<Income> getIncome(int userId) {
//
//        ArrayList<Income> list = new ArrayList<>();
//
//        try {
//
//            Connection con = DBConnection.getConnection();
//
//            String sql =
//            "SELECT i.*, c.category_name FROM income i " +
//            "INNER JOIN categories c ON i.category_id = c.category_id " +
//            "WHERE i.user_id=? ORDER BY income_date DESC";
//
//            PreparedStatement ps = con.prepareStatement(sql);
//
//            ps.setInt(1, userId);
//
//            ResultSet rs = ps.executeQuery();
//
//            while (rs.next()) {
//
//                Income income = new Income();
//
//                income.setIncomeId(rs.getInt("income_id"));
//                income.setUserId(rs.getInt("user_id"));
//                income.setCategoryId(rs.getInt("category_id"));
//                income.setCategoryName(rs.getString("category_name"));
//                income.setAmount(rs.getDouble("amount"));
//                income.setDescription(rs.getString("description"));
//                income.setIncomeDate(rs.getDate("income_date"));
//
//                list.add(income);
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
//    // ================= GET INCOME BY ID =================
//
//    public Income getIncomeById(int id) {
//
//        Income income = null;
//
//        try {
//
//            Connection con = DBConnection.getConnection();
//
//            String sql = "SELECT * FROM income WHERE income_id=?";
//
//            PreparedStatement ps = con.prepareStatement(sql);
//
//            ps.setInt(1, id);
//
//            ResultSet rs = ps.executeQuery();
//
//            if (rs.next()) {
//
//                income = new Income();
//
//                income.setIncomeId(rs.getInt("income_id"));
//                income.setUserId(rs.getInt("user_id"));
//                income.setCategoryId(rs.getInt("category_id"));
//                income.setAmount(rs.getDouble("amount"));
//                income.setDescription(rs.getString("description"));
//                income.setIncomeDate(rs.getDate("income_date"));
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
//        return income;
//    }
//
//    // ================= UPDATE INCOME =================
//
//    public boolean updateIncome(Income income) {
//
//        boolean status = false;
//
//        try {
//
//            Connection con = DBConnection.getConnection();
//
//            String sql =
//            "UPDATE income SET category_id=?, amount=?, description=?, income_date=? WHERE income_id=?";
//
//            PreparedStatement ps = con.prepareStatement(sql);
//
//            ps.setInt(1, income.getCategoryId());
//            ps.setDouble(2, income.getAmount());
//            ps.setString(3, income.getDescription());
//            ps.setDate(4, income.getIncomeDate());
//            ps.setInt(5, income.getIncomeId());
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
//    // ================= DELETE INCOME =================
//
//    public boolean deleteIncome(int id) {
//
//        boolean status = false;
//
//        try {
//
//            Connection con = DBConnection.getConnection();
//
//            String sql = "DELETE FROM income WHERE income_id=?";
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
//        } catch (Exception e) {
//
//            e.printStackTrace();
//
//        }
//
//        return status;
//    }
//
//}

package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import model.Income;
import util.DBConnection;

public class IncomeDAO {

    // ============================================================
    // ADD INCOME
    // ============================================================

    public boolean addIncome(Income income) {

        Connection con = null;

        try {

            con =
                    DBConnection.getConnection();

            con.setAutoCommit(false);

            // ----------------------------------------------------
            // 1. INSERT INTO income
            // ----------------------------------------------------

            String incomeSql =
                    "INSERT INTO income " +
                    "(user_id, category_id, amount, description, income_date) " +
                    "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement incomePs =
                    con.prepareStatement(
                            incomeSql,
                            Statement.RETURN_GENERATED_KEYS
                    );

            incomePs.setInt(
                    1,
                    income.getUserId()
            );

            incomePs.setInt(
                    2,
                    income.getCategoryId()
            );

            incomePs.setDouble(
                    3,
                    income.getAmount()
            );

            incomePs.setString(
                    4,
                    income.getDescription()
            );

            incomePs.setDate(
                    5,
                    income.getIncomeDate()
            );

            int incomeResult =
                    incomePs.executeUpdate();

            if (incomeResult == 0) {

                con.rollback();

                return false;
            }

            // ----------------------------------------------------
            // 2. GET GENERATED INCOME ID
            // ----------------------------------------------------

            int incomeId = 0;

            ResultSet keys =
                    incomePs.getGeneratedKeys();

            if (keys.next()) {

                incomeId =
                        keys.getInt(1);
            }

            keys.close();
            incomePs.close();

            if (incomeId == 0) {

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
                    "SELECT NULL, ?, ?, 'INCOME', c.category_name, ?, ?, NULL, ?, 'INCOME' " +
                    "FROM categories c " +
                    "WHERE c.category_id = ?";

            PreparedStatement transactionPs =
                    con.prepareStatement(transactionSql);

            transactionPs.setInt(
                    1,
                    income.getUserId()
            );

            transactionPs.setDate(
                    2,
                    income.getIncomeDate()
            );

            transactionPs.setString(
                    3,
                    income.getDescription()
            );

            transactionPs.setDouble(
                    4,
                    income.getAmount()
            );

            transactionPs.setInt(
                    5,
                    incomeId
            );

            transactionPs.setInt(
                    6,
                    income.getCategoryId()
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
    // VIEW INCOME
    // ============================================================

    public ArrayList<Income> getIncome(int userId) {

        ArrayList<Income> list =
                new ArrayList<>();

        try {

            Connection con =
                    DBConnection.getConnection();

            String sql =
                    "SELECT i.*, c.category_name " +
                    "FROM income i " +
                    "INNER JOIN categories c " +
                    "ON i.category_id = c.category_id " +
                    "WHERE i.user_id=? " +
                    "ORDER BY i.income_date DESC";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setInt(
                    1,
                    userId
            );

            ResultSet rs =
                    ps.executeQuery();

            while (rs.next()) {

                Income income =
                        new Income();

                income.setIncomeId(
                        rs.getInt("income_id")
                );

                income.setUserId(
                        rs.getInt("user_id")
                );

                income.setCategoryId(
                        rs.getInt("category_id")
                );

                income.setCategoryName(
                        rs.getString("category_name")
                );

                income.setAmount(
                        rs.getDouble("amount")
                );

                income.setDescription(
                        rs.getString("description")
                );

                income.setIncomeDate(
                        rs.getDate("income_date")
                );

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


    // ============================================================
    // GET INCOME BY ID
    // ============================================================

    public Income getIncomeById(int id) {

        Income income = null;

        try {

            Connection con =
                    DBConnection.getConnection();

            String sql =
                    "SELECT i.*, c.category_name " +
                    "FROM income i " +
                    "INNER JOIN categories c " +
                    "ON i.category_id = c.category_id " +
                    "WHERE i.income_id=?";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setInt(
                    1,
                    id
            );

            ResultSet rs =
                    ps.executeQuery();

            if (rs.next()) {

                income =
                        new Income();

                income.setIncomeId(
                        rs.getInt("income_id")
                );

                income.setUserId(
                        rs.getInt("user_id")
                );

                income.setCategoryId(
                        rs.getInt("category_id")
                );

                income.setCategoryName(
                        rs.getString("category_name")
                );

                income.setAmount(
                        rs.getDouble("amount")
                );

                income.setDescription(
                        rs.getString("description")
                );

                income.setIncomeDate(
                        rs.getDate("income_date")
                );
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();
        }

        return income;
    }


    // ============================================================
    // UPDATE INCOME
    // ============================================================

    public boolean updateIncome(Income income) {

        Connection con = null;

        try {

            con =
                    DBConnection.getConnection();

            con.setAutoCommit(false);

            // ----------------------------------------------------
            // Get existing income
            // ----------------------------------------------------

            Income oldIncome =
                    getIncomeByIdUsingConnection(
                            con,
                            income.getIncomeId()
                    );

            if (oldIncome == null) {

                con.rollback();

                return false;
            }

            // ----------------------------------------------------
            // Update income table
            // ----------------------------------------------------

            String incomeSql =
                    "UPDATE income " +
                    "SET category_id=?, " +
                    "amount=?, " +
                    "description=?, " +
                    "income_date=? " +
                    "WHERE income_id=? " +
                    "AND user_id=?";

            PreparedStatement incomePs =
                    con.prepareStatement(incomeSql);

            incomePs.setInt(
                    1,
                    income.getCategoryId()
            );

            incomePs.setDouble(
                    2,
                    income.getAmount()
            );

            incomePs.setString(
                    3,
                    income.getDescription()
            );

            incomePs.setDate(
                    4,
                    income.getIncomeDate()
            );

            incomePs.setInt(
                    5,
                    income.getIncomeId()
            );

            incomePs.setInt(
                    6,
                    oldIncome.getUserId()
            );

            int incomeResult =
                    incomePs.executeUpdate();

            incomePs.close();

            if (incomeResult == 0) {

                con.rollback();

                return false;
            }

            // ----------------------------------------------------
            // Update transactions table
            // ----------------------------------------------------

            String transactionSql =
                    "UPDATE transactions t " +
                    "INNER JOIN categories c " +
                    "ON c.category_id=? " +
                    "SET t.transaction_date=?, " +
                    "t.category=c.category_name, " +
                    "t.description=?, " +
                    "t.amount=? " +
                    "WHERE t.source_id=? " +
                    "AND t.source_type='INCOME' " +
                    "AND t.user_id=?";

            PreparedStatement transactionPs =
                    con.prepareStatement(transactionSql);

            transactionPs.setInt(
                    1,
                    income.getCategoryId()
            );

            transactionPs.setDate(
                    2,
                    income.getIncomeDate()
            );

            transactionPs.setString(
                    3,
                    income.getDescription()
            );

            transactionPs.setDouble(
                    4,
                    income.getAmount()
            );

            transactionPs.setInt(
                    5,
                    income.getIncomeId()
            );

            transactionPs.setInt(
                    6,
                    oldIncome.getUserId()
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
    // DELETE INCOME
    // ============================================================

    public boolean deleteIncome(int id) {

        Connection con = null;

        try {

            con =
                    DBConnection.getConnection();

            con.setAutoCommit(false);

            // ----------------------------------------------------
            // Get existing income
            // ----------------------------------------------------

            Income income =
                    getIncomeByIdUsingConnection(
                            con,
                            id
                    );

            if (income == null) {

                con.rollback();

                return false;
            }

            // ----------------------------------------------------
            // Delete transaction
            // ----------------------------------------------------

            String transactionSql =
                    "DELETE FROM transactions " +
                    "WHERE source_id=? " +
                    "AND source_type='INCOME' " +
                    "AND user_id=?";

            PreparedStatement transactionPs =
                    con.prepareStatement(transactionSql);

            transactionPs.setInt(
                    1,
                    id
            );

            transactionPs.setInt(
                    2,
                    income.getUserId()
            );

            transactionPs.executeUpdate();

            transactionPs.close();

            // ----------------------------------------------------
            // Delete income
            // ----------------------------------------------------

            String incomeSql =
                    "DELETE FROM income " +
                    "WHERE income_id=? " +
                    "AND user_id=?";

            PreparedStatement incomePs =
                    con.prepareStatement(incomeSql);

            incomePs.setInt(
                    1,
                    id
            );

            incomePs.setInt(
                    2,
                    income.getUserId()
            );

            int result =
                    incomePs.executeUpdate();

            incomePs.close();

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
    // INTERNAL GET INCOME USING EXISTING CONNECTION
    // ============================================================

    private Income getIncomeByIdUsingConnection(
            Connection con,
            int id) throws Exception {

        String sql =
                "SELECT * FROM income " +
                "WHERE income_id=?";

        PreparedStatement ps =
                con.prepareStatement(sql);

        ps.setInt(
                1,
                id
        );

        ResultSet rs =
                ps.executeQuery();

        Income income = null;

        if (rs.next()) {

            income =
                    new Income();

            income.setIncomeId(
                    rs.getInt("income_id")
            );

            income.setUserId(
                    rs.getInt("user_id")
            );

            income.setCategoryId(
                    rs.getInt("category_id")
            );

            income.setAmount(
                    rs.getDouble("amount")
            );

            income.setDescription(
                    rs.getString("description")
            );

            income.setIncomeDate(
                    rs.getDate("income_date")
            );
        }

        rs.close();
        ps.close();

        return income;
    }
}