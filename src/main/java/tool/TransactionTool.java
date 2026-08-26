package tool;

import java.sql.Connection;

import java.sql.PreparedStatement;

import java.sql.ResultSet;

import java.sql.Date;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;

import java.util.Map;

import java.util.regex.Pattern;

import dao.ExpenseDAO;

import dao.IncomeDAO;

import model.Expense;

import model.Income;

import util.DBConnection;



public class TransactionTool {



    // ============================================================

    // GET TOTAL INCOME

    // ============================================================

    public double getTotalIncome(int userId) {

        String sql =

                "SELECT COALESCE(SUM(amount), 0) AS total_income " +

                "FROM transactions " +

                "WHERE user_id = ? " +

                "AND type = 'INCOME'";

        try (

                Connection con = DBConnection.getConnection();

                PreparedStatement ps = con.prepareStatement(sql)

        ) {

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



    // ============================================================

    // GET TOTAL EXPENSE

    // ============================================================

    public double getTotalExpense(int userId) {

        String sql =

                "SELECT COALESCE(SUM(amount), 0) AS total_expense " +

                "FROM transactions " +

                "WHERE user_id = ? " +

                "AND type = 'EXPENSE'";

        try (

                Connection con = DBConnection.getConnection();

                PreparedStatement ps = con.prepareStatement(sql)

        ) {

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



    // ============================================================

    // GET TOTAL TRANSACTION COUNT

    // ============================================================

    public int getTransactionCount(int userId) {

        String sql =

                "SELECT COUNT(*) AS total_transactions " +

                "FROM transactions " +

                "WHERE user_id = ?";

        try (

                Connection con = DBConnection.getConnection();

                PreparedStatement ps = con.prepareStatement(sql)

        ) {

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



    // ============================================================

    // GET CATEGORY SPENDING

    // ============================================================

    public double getCategorySpending(

            int userId,

            String category) {

        String sql =

                "SELECT COALESCE(SUM(amount), 0) AS total " +

                "FROM transactions " +

                "WHERE user_id = ? " +

                "AND type = 'EXPENSE' " +

                "AND LOWER(category) = LOWER(?)";

        try (

                Connection con = DBConnection.getConnection();

                PreparedStatement ps = con.prepareStatement(sql)

        ) {

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



    // ============================================================

    // GET CATEGORY-WISE EXPENSE

    // ============================================================

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

        try (

                Connection con = DBConnection.getConnection();

                PreparedStatement ps = con.prepareStatement(sql)

        ) {

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



    // ============================================================

    // GET MONTHLY SUMMARY

    // ============================================================

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

        try (

                Connection con = DBConnection.getConnection();

                PreparedStatement ps = con.prepareStatement(sql)

        ) {

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



    // ============================================================

    // GET RECENT TRANSACTIONS

    // ============================================================

    public List<Map<String, Object>> getRecentTransactions(

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

                "type, " +

                "category, " +

                "description, " +

                "amount " +

                "FROM transactions " +

                "WHERE user_id = ? " +

                "ORDER BY transaction_date DESC, id DESC " +

                "LIMIT ?";

        try (

                Connection con = DBConnection.getConnection();

                PreparedStatement ps = con.prepareStatement(sql)

        ) {

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



    // ============================================================

    // GET HIGHEST EXPENSES

    // ============================================================

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

        try (

                Connection con = DBConnection.getConnection();

                PreparedStatement ps = con.prepareStatement(sql)

        ) {

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



    // ============================================================

    // GET FINANCIAL SUMMARY

    // ============================================================

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



    // ============================================================

    // FIND CATEGORY FROM USER QUESTION

    // ============================================================

    public String findCategoryFromQuestion(

            int userId,

            String question) {

        // --------------------------------------------------------

        // VALIDATE QUESTION

        // --------------------------------------------------------

        if (question == null ||

                question.trim().isEmpty()) {

            System.out.println(

                    "CATEGORY SEARCH: Question is empty"

            );

            return null;

        }



        // --------------------------------------------------------

        // GET USER'S CATEGORIES

        // --------------------------------------------------------

        String sql =

                "SELECT category_name " +

                "FROM categories " +

                "WHERE user_id = ? " +

                "ORDER BY LENGTH(category_name) DESC";

        try (

                Connection con = DBConnection.getConnection();

                PreparedStatement ps = con.prepareStatement(sql)

        ) {

            ps.setInt(

                    1,

                    userId

            );

            try (ResultSet rs = ps.executeQuery()) {

                String lowerQuestion =

                        question.trim().toLowerCase();



                // ------------------------------------------------

                // CHECK EACH DATABASE CATEGORY

                // ------------------------------------------------

                while (rs.next()) {

                    String category =

                            rs.getString(

                                    "category_name"

                            );



                    if (category == null ||

                            category.trim().isEmpty()) {

                        continue;

                    }



                    category =

                            category.trim();



                    String lowerCategory =

                            category.toLowerCase();



                    // ------------------------------------------------

                    // SAFE WORD / PHRASE MATCH

                    // ------------------------------------------------

                    String pattern =

                            "(^|[^a-z0-9])"

                                    + Pattern.quote(

                                            lowerCategory

                                    )

                                    + "([^a-z0-9]|$)";



                    if (

                            Pattern

                                    .compile(pattern)

                                    .matcher(lowerQuestion)

                                    .find()

                    ) {

                        System.out.println(

                                "CATEGORY FOUND FROM QUESTION: "

                                        + category

                        );

                        return category;

                    }

                }

            }

        } catch (Exception e) {

            System.out.println(

                    "TransactionTool - "

                            + "findCategoryFromQuestion error: "

                            + e.getMessage()

            );

            e.printStackTrace();

        }



        // --------------------------------------------------------

        // CATEGORY NOT FOUND

        // --------------------------------------------------------

        System.out.println(

                "CATEGORY NOT FOUND IN USER QUESTION"

        );

        return null;

    }



    // ============================================================

    // ADD EXPENSE

    // ============================================================

    public boolean addExpense(

            int userId,

            String category,

            String description,

            double amount) {

        try {

            // ----------------------------------------------------

            // FIND CATEGORY ID

            // ----------------------------------------------------

            String categorySql =

                    "SELECT category_id " +

                    "FROM categories " +

                    "WHERE user_id = ? " +

                    "AND LOWER(category_name) = LOWER(?) " +

                    "LIMIT 1";

            try (

                    Connection con =

                            DBConnection.getConnection();

                    PreparedStatement ps =

                            con.prepareStatement(

                                    categorySql

                            )

            ) {

                ps.setInt(

                        1,

                        userId

                );

                ps.setString(

                        2,

                        category

                );



                try (

                        ResultSet rs =

                                ps.executeQuery()

                ) {

                    if (!rs.next()) {

                        System.out.println(

                                "Category not found: "

                                        + category

                        );

                        return false;

                    }



                    int categoryId =

                            rs.getInt(

                                    "category_id"

                            );



                    // ------------------------------------------------

                    // CREATE EXPENSE OBJECT

                    // ------------------------------------------------

                    Expense expense =

                            new Expense();



                    expense.setUserId(

                            userId

                    );



                    expense.setCategoryId(

                            categoryId

                    );



                    expense.setAmount(

                            amount

                    );



                    expense.setDescription(

                            description

                    );



                    expense.setExpenseDate(

                            new Date(

                                    System.currentTimeMillis()

                            )

                    );



                    // ------------------------------------------------

                    // USE EXPENSE DAO

                    // ------------------------------------------------

                    ExpenseDAO expenseDAO =

                            new ExpenseDAO();



                    return expenseDAO.addExpense(

                            expense

                    );

                }

            }

        } catch (Exception e) {

            System.out.println(

                    "TransactionTool - addExpense error: "

                            + e.getMessage()

            );

            e.printStackTrace();

            return false;

        }

    }

    // ============================================================
    // FIND EXPENSE BY CATEGORY + OLD AMOUNT
    // ============================================================
    // IMPORTANT: return expenses.expense_id, NOT transactions.id.
    public int findExpenseIdByCategoryAndAmount(
            int userId,
            String category,
            double oldAmount) {

        try {
            ExpenseDAO expenseDAO = new ExpenseDAO();

            Expense expense =
                    expenseDAO.findExpenseByCategoryAndAmount(
                            userId, category, oldAmount
                    );

            if (expense == null) {
                System.out.println(
                        "No expense found: user=" + userId
                                + ", category=" + category
                                + ", amount=" + oldAmount
                );
                return -1;
            }

            System.out.println("DIRECT EXPENSE LOOKUP SUCCESS");
            System.out.println("Expense ID : " + expense.getExpenseId());
            System.out.println("User ID    : " + expense.getUserId());
            System.out.println("Category   : " + expense.getCategoryName());
            System.out.println("Old Amount : " + expense.getAmount());

            return expense.getExpenseId();

        } catch (Exception e) {
            System.out.println(
                    "TransactionTool - findExpenseIdByCategoryAndAmount error: "
                            + e.getMessage()
            );
            e.printStackTrace();
            return -1;
        }
    }




    // ============================================================

    // UPDATE EXPENSE

    // ============================================================

    public boolean updateExpense(

            int userId,

            int expenseId,

            String category,

            String description,

            double amount) {

        try {

            // ----------------------------------------------------

            // FIND CATEGORY ID

            // ----------------------------------------------------

            String categorySql =

                    "SELECT category_id " +

                    "FROM categories " +

                    "WHERE user_id = ? " +

                    "AND LOWER(category_name) = LOWER(?) " +

                    "LIMIT 1";

            try (

                    Connection con =

                            DBConnection.getConnection();

                    PreparedStatement ps =

                            con.prepareStatement(

                                    categorySql

                            )

            ) {

                ps.setInt(

                        1,

                        userId

                );

                ps.setString(

                        2,

                        category

                );



                try (

                        ResultSet rs =

                                ps.executeQuery()

                ) {

                    if (!rs.next()) {

                        System.out.println(

                                "Category not found: "

                                        + category

                        );

                        return false;

                    }



                    int categoryId =

                            rs.getInt(

                                    "category_id"

                            );



                    // ------------------------------------------------

                    // CREATE UPDATED EXPENSE OBJECT

                    // ------------------------------------------------

                    Expense expense =

                            new Expense();



                    expense.setExpenseId(

                            expenseId

                    );



                    expense.setUserId(

                            userId

                    );



                    expense.setCategoryId(

                            categoryId

                    );



                    expense.setAmount(

                            amount

                    );



                    expense.setDescription(

                            description

                    );



                    expense.setExpenseDate(

                            new Date(

                                    System.currentTimeMillis()

                            )

                    );



                    // ------------------------------------------------

                    // UPDATE USING EXPENSE DAO

                    // ------------------------------------------------

                    ExpenseDAO expenseDAO =

                            new ExpenseDAO();



                    return expenseDAO.updateExpense(

                            expense

                    );

                }

            }

        } catch (Exception e) {

            System.out.println(

                    "TransactionTool - updateExpense error: "

                            + e.getMessage()

            );

            e.printStackTrace();

            return false;

        }

    }



    // ============================================================

    // DELETE EXPENSE

    // ============================================================

    public boolean deleteExpense(

            int userId,

            int expenseId) {

        try {

            ExpenseDAO expenseDAO =

                    new ExpenseDAO();



            // ----------------------------------------------------

            // FIRST VERIFY EXPENSE

            // BELONGS TO LOGGED-IN USER

            // ----------------------------------------------------

            Expense expense =

                    expenseDAO.getExpenseById(

                            expenseId

                    );



            if (expense == null) {

                return false;

            }



            if (expense.getUserId() != userId) {

                return false;

            }



            // ----------------------------------------------------

            // DELETE EXPENSE

            // ----------------------------------------------------

            return expenseDAO.deleteExpense(

                    expenseId

            );

        } catch (Exception e) {

            System.out.println(

                    "TransactionTool - deleteExpense error: "

                            + e.getMessage()

            );

            e.printStackTrace();

            return false;

        }

    }



    // ============================================================

    // ADD INCOME

    // ============================================================

    public boolean addIncome(

            int userId,

            String category,

            String description,

            double amount) {

        try {

            // ----------------------------------------------------

            // FIND CATEGORY ID

            // ----------------------------------------------------

            String categorySql =

                    "SELECT category_id " +

                    "FROM categories " +

                    "WHERE user_id = ? " +

                    "AND LOWER(category_name) = LOWER(?) " +

                    "LIMIT 1";



            try (

                    Connection con =

                            DBConnection.getConnection();

                    PreparedStatement ps =

                            con.prepareStatement(

                                    categorySql

                            )

            ) {

                ps.setInt(

                        1,

                        userId

                );

                ps.setString(

                        2,

                        category

                );



                try (

                        ResultSet rs =

                                ps.executeQuery()

                ) {

                    if (!rs.next()) {

                        System.out.println(

                                "Income category not found: "

                                        + category

                        );

                        return false;

                    }



                    int categoryId =

                            rs.getInt(

                                    "category_id"

                            );



                    // ------------------------------------------------

                    // CREATE INCOME OBJECT

                    // ------------------------------------------------

                    Income income =

                            new Income();



                    income.setUserId(

                            userId

                    );



                    income.setCategoryId(

                            categoryId

                    );



                    income.setAmount(

                            amount

                    );



                    income.setDescription(

                            description

                    );



                    income.setIncomeDate(

                            new Date(

                                    System.currentTimeMillis()

                            )

                    );



                    // ------------------------------------------------

                    // USE INCOME DAO

                    // ------------------------------------------------

                    IncomeDAO incomeDAO =

                            new IncomeDAO();



                    return incomeDAO.addIncome(

                            income

                    );

                }

            }

        } catch (Exception e) {

            System.out.println(

                    "TransactionTool - addIncome error: "

                            + e.getMessage()

            );

            e.printStackTrace();

            return false;

        }

    }




    // ============================================================
    // FIND INCOME BY OLD AMOUNT
    // ============================================================

    public Income findIncomeByAmount(
            int userId,
            double amount) {

        String sql =
                "SELECT i.*, c.category_name " +
                "FROM income i " +
                "INNER JOIN categories c " +
                "ON i.category_id = c.category_id " +
                "WHERE i.user_id = ? " +
                "AND i.amount = ? " +
                "ORDER BY i.income_id DESC " +
                "LIMIT 1";

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, userId);
            ps.setDouble(2, amount);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    Income income = new Income();

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

                    return income;
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "TransactionTool - findIncomeByAmount error: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return null;
    }

    // ============================================================

    // UPDATE INCOME

    // ============================================================

    public boolean updateIncome(
            int userId,
            int incomeId,
            String category,
            String description,
            double amount) {

        try {

            IncomeDAO incomeDAO =
                    new IncomeDAO();

            Income oldIncome =
                    incomeDAO.getIncomeById(
                            incomeId
                    );

            if (oldIncome == null) {

                System.out.println(
                        "Income not found: "
                                + incomeId
                );

                return false;
            }

            if (oldIncome.getUserId() != userId) {

                System.out.println(
                        "Income does not belong to logged-in user."
                );

                return false;
            }

            // Preserve existing category if the user did not
            // request a category change.
            if (category == null ||
                    category.trim().isEmpty()) {

                category =
                        oldIncome.getCategoryName();
            }

            // Preserve existing description if the user did not
            // request a description change.
            if (description == null ||
                    description.trim().isEmpty()) {

                description =
                        oldIncome.getDescription();
            }

            if (amount <= 0) {

                System.out.println(
                        "Invalid income amount: "
                                + amount
                );

                return false;
            }

            String categorySql =
                    "SELECT category_id " +
                    "FROM categories " +
                    "WHERE user_id = ? " +
                    "AND LOWER(category_name) = LOWER(?) " +
                    "LIMIT 1";

            try (
                    Connection con =
                            DBConnection.getConnection();

                    PreparedStatement ps =
                            con.prepareStatement(
                                    categorySql
                            )
            ) {

                ps.setInt(1, userId);
                ps.setString(2, category);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {

                        System.out.println(
                                "Income category not found: "
                                        + category
                        );

                        return false;
                    }

                    int categoryId =
                            rs.getInt("category_id");

                    Income income =
                            new Income();

                    income.setIncomeId(
                            incomeId
                    );

                    income.setUserId(
                            userId
                    );

                    income.setCategoryId(
                            categoryId
                    );

                    income.setAmount(
                            amount
                    );

                    income.setDescription(
                            description
                    );

                    income.setIncomeDate(
                            new Date(
                                    System.currentTimeMillis()
                            )
                    );

                    return incomeDAO.updateIncome(
                            income
                    );
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "TransactionTool - updateIncome error: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return false;
        }
    }

}