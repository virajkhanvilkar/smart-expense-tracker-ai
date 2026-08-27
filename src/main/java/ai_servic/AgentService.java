package ai_servic;

import java.io.IOException;

import java.net.URI;

import java.net.http.HttpClient;

import java.net.http.HttpRequest;

import java.net.http.HttpResponse;

import java.util.List;

import java.util.Map;



import model.Income;

import tool.TransactionTool;

public class AgentService {

    private final TransactionTool transactionTool;

    private static final String OLLAMA_URL =

            "http://localhost:11434/api/chat";

    private static final String MODEL =

            "qwen3:latest";

    private final HttpClient httpClient;









    // ============================================================

    // CONSTRUCTOR

    // ============================================================

    public AgentService() {

        transactionTool =

                new TransactionTool();

        httpClient =

                HttpClient.newHttpClient();

    }









    // ============================================================

    // MAIN AGENT METHOD

    // ============================================================

    public String processQuestion(

            int userId,

            String question)

            throws Exception {

        if (question == null ||

                question.trim().isEmpty()) {

            return "Please enter a financial question.";

        }

        question =

                question.trim();









        // ========================================================

        // STEP 1 - DETECT INTENT

        // ========================================================

        String intent =

                detectIntent(question);









        System.out.println(

                "========================================"

        );

        System.out.println(

                "AI Question: " + question

        );

        System.out.println(

                "Logged-in User ID: " + userId

        );

        System.out.println(

                "AI detected intent: " + intent

        );

        System.out.println(

                "========================================"

        );









        // ========================================================

        // STEP 2 - EXECUTE ACTION

        // ========================================================

        String financialData;









        switch (intent) {









            // ====================================================

            // TOTAL INCOME

            // ====================================================

            case "TOTAL_INCOME":

                financialData =

                        "Total income: ₹"

                                + format(

                                        transactionTool

                                                .getTotalIncome(

                                                        userId

                                                )

                                );

                break;









            // ====================================================

            // TOTAL EXPENSE

            // ====================================================

            case "TOTAL_EXPENSE":

                financialData =

                        "Total expense: ₹"

                                + format(

                                        transactionTool

                                                .getTotalExpense(

                                                        userId

                                                )

                                );

                break;









            // ====================================================

            // TRANSACTION COUNT

            // ====================================================

            case "TRANSACTION_COUNT":

                financialData =

                        "Total transactions: "

                                + transactionTool

                                        .getTransactionCount(

                                                userId

                                        );

                break;









            // ====================================================

            // CATEGORY SPENDING

            // ====================================================

            case "CATEGORY_SPENDING":

                List<Map<String, Object>> categories =

                        transactionTool

                                .getCategoryWiseExpense(

                                        userId

                                );

                financialData =

                        categories.toString();

                break;









            // ====================================================

            // HIGHEST EXPENSES

            // ====================================================

            case "HIGHEST_EXPENSES":

                List<Map<String, Object>> highest =

                        transactionTool

                                .getHighestExpenses(

                                        userId,

                                        5

                                );

                financialData =

                        highest.toString();

                break;









            // ====================================================

            // RECENT TRANSACTIONS

            // ====================================================

            case "RECENT_TRANSACTIONS":

                List<Map<String, Object>> recent =

                        transactionTool

                                .getRecentTransactions(

                                        userId,

                                        10

                                );

                financialData =

                        recent.toString();

                break;









            // ====================================================

            // FINANCIAL SUMMARY

            // ====================================================

            case "FINANCIAL_SUMMARY":

                Map<String, Object> summary =

                        transactionTool

                                .getFinancialSummary(

                                        userId

                                );

                financialData =

                        summary.toString();

                break;









            // ====================================================

            // ADD INCOME

            // ====================================================

            case "ADD_INCOME":

                String[] incomeDetails =

                        extractIncomeDetails(

                                question

                        );









                String incomeCategory =

                        incomeDetails[0];









                double incomeAmount =

                        Double.parseDouble(

                                incomeDetails[1]

                        );









                String incomeDescription =

                        incomeDetails[2];









                // ------------------------------------------------

                // VALIDATE AMOUNT

                // ------------------------------------------------

                if (incomeAmount <= 0) {

                    return

                            "Please provide a valid income amount.";

                }









                // ------------------------------------------------

                // FIND REAL DATABASE CATEGORY

                // ------------------------------------------------

                String incomeDatabaseCategory =

                        transactionTool

                                .findCategoryFromQuestion(

                                        userId,

                                        question

                                );









                if (incomeDatabaseCategory != null &&

                        !incomeDatabaseCategory

                                .trim()

                                .isEmpty()) {

                    incomeCategory =

                            incomeDatabaseCategory;

                    System.out.println(

                            "INCOME CATEGORY CORRECTED: "

                                    + incomeCategory

                    );

                }









                // ------------------------------------------------

                // DEFAULT CATEGORY

                // ------------------------------------------------

                if (incomeCategory == null ||

                        incomeCategory

                                .trim()

                                .isEmpty()) {

                    incomeCategory =

                            "Other";

                }









                // ------------------------------------------------

                // DEFAULT DESCRIPTION

                // ------------------------------------------------

                if (incomeDescription == null ||

                        incomeDescription

                                .trim()

                                .isEmpty()) {

                    incomeDescription =

                            incomeCategory

                                    + " income";

                }









                System.out.println(

                        "========================================"

                );

                System.out.println(

                        "AI ADD INCOME"

                );

                System.out.println(

                        "User ID      : "

                                + userId

                );

                System.out.println(

                        "Category     : ["

                                + incomeCategory

                                + "]"

                );

                System.out.println(

                        "Description  : ["

                                + incomeDescription

                                + "]"

                );

                System.out.println(

                        "Amount       : "

                                + incomeAmount

                );

                System.out.println(

                        "========================================"

                );









                // ------------------------------------------------

                // INSERT INCOME

                // ------------------------------------------------

                boolean incomeAdded =

                        transactionTool

                                .addIncome(

                                        userId,

                                        incomeCategory,

                                        incomeDescription,

                                        incomeAmount

                                );









                if (incomeAdded) {

                    return

                            "✅ Added ₹"

                                    + format(

                                            incomeAmount

                                    )

                                    + " income in "

                                    + incomeCategory

                                    + " ("

                                    + incomeDescription

                                    + ").";

                } else {

                    return

                            "❌ I could not add the income.";

                }









            // ====================================================

            // UPDATE INCOME

            // ====================================================

            case "UPDATE_INCOME":

                String[] updateIncomeDetails =

                        extractUpdateIncomeDetails(

                                question

                        );

                String updateIncomeIdText =

                        updateIncomeDetails[0];

                String oldIncomeAmountText =

                        updateIncomeDetails[1];

                String updateIncomeCategory =

                        updateIncomeDetails[2];

                double updateIncomeAmount =

                        Double.parseDouble(

                                updateIncomeDetails[3]

                        );

                String updateIncomeDescription =

                        updateIncomeDetails[4];

                // ------------------------------------------------

                // FIND INCOME ID

                // ------------------------------------------------

                int updateIncomeId = -1;

                if (updateIncomeIdText != null &&

                        !updateIncomeIdText.trim().isEmpty()) {

                    updateIncomeId =

                            Integer.parseInt(

                                    updateIncomeIdText

                            );

                }

                // ------------------------------------------------

                // NO ID:

                // FIND THE INCOME USING THE OLD AMOUNT

                // ------------------------------------------------

                if (updateIncomeId <= 0) {

                    if (oldIncomeAmountText == null ||

                            oldIncomeAmountText.trim().isEmpty()) {

                        return

                                "Please provide an income ID or the old income amount.";

                    }

                    double oldIncomeAmount =

                            Double.parseDouble(

                                    oldIncomeAmountText

                            );

                    Income existingIncome =

                            transactionTool.findIncomeByAmount(

                                    userId,

                                    oldIncomeAmount

                            );

                    if (existingIncome == null) {

                        return

                                "❌ No income found with amount ₹"

                                        + format(oldIncomeAmount)

                                        + ".";

                    }

                    updateIncomeId =

                            existingIncome.getIncomeId();

                    // Preserve existing values when the command

                    // only changes the amount.

                    if (updateIncomeCategory == null ||

                            updateIncomeCategory.trim().isEmpty()) {

                        updateIncomeCategory =

                                existingIncome.getCategoryName();

                    }

                    if (updateIncomeDescription == null ||

                            updateIncomeDescription.trim().isEmpty()) {

                        updateIncomeDescription =

                                existingIncome.getDescription();

                    }

                    System.out.println(

                            "INCOME FOUND BY OLD AMOUNT: "

                                    + oldIncomeAmount

                    );

                    System.out.println(

                            "MATCHED INCOME ID: "

                                    + updateIncomeId

                    );

                }

                // ------------------------------------------------

                // VALIDATE ID

                // ------------------------------------------------

                if (updateIncomeId <= 0) {

                    return

                            "Please provide a valid income ID.";

                }

                // ------------------------------------------------

                // VALIDATE NEW AMOUNT

                // ------------------------------------------------

                if (updateIncomeAmount <= 0) {

                    return

                            "Please provide a valid income amount.";

                }

                // ------------------------------------------------

                // FIND DATABASE CATEGORY ONLY WHEN PROVIDED

                // ------------------------------------------------

                String updateIncomeDatabaseCategory =

                        transactionTool

                                .findCategoryFromQuestion(

                                        userId,

                                        question

                                );

                if (updateIncomeDatabaseCategory != null &&

                        !updateIncomeDatabaseCategory

                                .trim()

                                .isEmpty()) {

                    updateIncomeCategory =

                            updateIncomeDatabaseCategory;

                    System.out.println(

                            "UPDATE INCOME CATEGORY CORRECTED: "

                                    + updateIncomeCategory

                    );

                }

                // Empty values are intentionally passed through.

                // TransactionTool will preserve the old values.

                if (updateIncomeCategory != null &&

                        updateIncomeCategory.trim().isEmpty()) {

                    updateIncomeCategory = null;

                }

                if (updateIncomeDescription != null &&

                        updateIncomeDescription.trim().isEmpty()) {

                    updateIncomeDescription = null;

                }

                System.out.println(

                        "========================================"

                );

                System.out.println(

                        "AI UPDATE INCOME"

                );

                System.out.println(

                        "User ID      : "

                                + userId

                );

                System.out.println(

                        "Income ID    : "

                                + updateIncomeId

                );

                System.out.println(

                        "Old Amount   : "

                                + (

                                    oldIncomeAmountText == null ||

                                    oldIncomeAmountText.trim().isEmpty()

                                    ? "Not provided"

                                    : oldIncomeAmountText

                                )

                );

                System.out.println(

                        "Category     : ["

                                + updateIncomeCategory

                                + "]"

                );

                System.out.println(

                        "Description  : ["

                                + updateIncomeDescription

                                + "]"

                );

                System.out.println(

                        "New Amount   : "

                                + updateIncomeAmount

                );

                System.out.println(

                        "========================================"

                );

                boolean incomeUpdated =

                        transactionTool

                                .updateIncome(

                                        userId,

                                        updateIncomeId,

                                        updateIncomeCategory,

                                        updateIncomeDescription,

                                        updateIncomeAmount

                                );

                if (incomeUpdated) {

                    return

                            "✅ Income "

                                    + updateIncomeId

                                    + " updated successfully to ₹"

                                    + format(updateIncomeAmount)

                                    + ".";

                } else {

                    return

                            "❌ Income "

                                    + updateIncomeId

                                    + " was not found or could not be updated.";

                }

            // ====================================================

            // ADD EXPENSE

            // ====================================================

            case "ADD_EXPENSE":

                String[] expenseDetails =

                        extractExpenseDetails(

                                question

                        );









                String category =

                        expenseDetails[0];









                double amount =

                        Double.parseDouble(

                                expenseDetails[1]

                        );









                String description =

                        expenseDetails[2];









                // ------------------------------------------------

                // VALIDATE AMOUNT

                // ------------------------------------------------

                if (amount <= 0) {

                    return

                            "Please provide a valid expense amount.";

                }









                // ------------------------------------------------

                // IMPORTANT CATEGORY FIX

                // ------------------------------------------------

                String databaseCategory =

                        transactionTool

                                .findCategoryFromQuestion(

                                        userId,

                                        question

                                );









                if (databaseCategory != null &&

                        !databaseCategory

                                .trim()

                                .isEmpty()) {

                    category =

                            databaseCategory;

                    System.out.println(

                            "CATEGORY CORRECTED FROM DATABASE"

                    );

                    System.out.println(

                            "Category: "

                                    + category

                    );

                }









                // ------------------------------------------------

                // DEFAULT CATEGORY

                // ------------------------------------------------

                if (category == null ||

                        category.trim().isEmpty()) {

                    category =

                            "Other";

                }









                // ------------------------------------------------

                // DEFAULT DESCRIPTION

                // ------------------------------------------------

                if (description == null ||

                        description.trim().isEmpty()) {

                    description =

                            category

                                    + " expense";

                }









                System.out.println(

                        "========================================"

                );

                System.out.println(

                        "AI ADD EXPENSE"

                );

                System.out.println(

                        "User ID      : "

                                + userId

                );

                System.out.println(

                        "Category     : ["

                                + category

                                + "]"

                );

                System.out.println(

                        "Description  : ["

                                + description

                                + "]"

                );

                System.out.println(

                        "Amount       : "

                                + amount

                );

                System.out.println(

                        "========================================"

                );









                // ------------------------------------------------

                // INSERT

                // ------------------------------------------------

                boolean added =

                        transactionTool

                                .addExpense(

                                        userId,

                                        category,

                                        description,

                                        amount

                                );









                if (added) {

                    return

                            "✅ Added ₹"

                                    + format(amount)

                                    + " expense in "

                                    + category

                                    + " ("

                                    + description

                                    + ").";

                } else {

                    return

                            "❌ I could not add the expense.";

                }









            // ====================================================

            // UPDATE EXPENSE

            // ====================================================

            case "UPDATE_EXPENSE":

                String[] updateDetails = extractUpdateExpenseDetails(question);

                String updateExpenseIdText = updateDetails[0];
                String updateCategory = updateDetails[1];
                String oldExpenseAmountText = updateDetails[2];
                String newExpenseAmountText = updateDetails[3];
                String updateDescription = updateDetails[4];

                int updateExpenseId = -1;

                if (updateExpenseIdText != null && !updateExpenseIdText.trim().isEmpty()) {
                    try {
                        updateExpenseId = Integer.parseInt(updateExpenseIdText.trim());
                    } catch (NumberFormatException e) {
                        return "Please provide a valid expense ID.";
                    }
                }

                if (newExpenseAmountText == null || newExpenseAmountText.trim().isEmpty()) {
                    return "Please provide the new expense amount.";
                }

                double updateAmount;
                try {
                    updateAmount = Double.parseDouble(newExpenseAmountText.trim());
                } catch (NumberFormatException e) {
                    return "Please provide a valid new expense amount.";
                }

                if (updateAmount <= 0) {
                    return "Please provide a valid expense amount.";
                }

                String updateDatabaseCategory =
                        transactionTool.findCategoryFromQuestion(userId, question);

                if (updateDatabaseCategory != null && !updateDatabaseCategory.trim().isEmpty()) {
                    updateCategory = updateDatabaseCategory.trim();
                    System.out.println("UPDATE CATEGORY CORRECTED: " + updateCategory);
                }

                // If the user gave an explicit expense ID but did not provide a
                // category, preserve the existing category instead of changing it
                // to Other.
                if (updateExpenseId > 0 &&
                        (updateDatabaseCategory == null || updateDatabaseCategory.trim().isEmpty())) {
                    String existingCategory = findExpenseCategoryById(userId, updateExpenseId);
                    if (existingCategory != null && !existingCategory.trim().isEmpty()) {
                        updateCategory = existingCategory.trim();
                        System.out.println("EXISTING CATEGORY PRESERVED: " + updateCategory);
                    }
                }

                // No explicit ID: find the actual expense using category + old amount.
                if (updateExpenseId <= 0) {
                    if (oldExpenseAmountText == null || oldExpenseAmountText.trim().isEmpty()) {
                        return "Please provide an expense ID or the old expense amount. Example: update bike expense 500 to 200";
                    }

                    double oldExpenseAmount;
                    try {
                        oldExpenseAmount = Double.parseDouble(oldExpenseAmountText.trim());
                    } catch (NumberFormatException e) {
                        return "Please provide a valid old expense amount.";
                    }

                    updateExpenseId = findExpenseIdByCategoryAndAmount(
                            userId, updateCategory, oldExpenseAmount);

                    if (updateExpenseId <= 0) {
                        return "❌ No expense found in category " + updateCategory
                                + " with amount ₹" + format(oldExpenseAmount) + ".";
                    }

                    System.out.println("EXPENSE FOUND BY CATEGORY + OLD AMOUNT");
                    System.out.println("Category    : " + updateCategory);
                    System.out.println("Old Amount  : " + oldExpenseAmount);
                    System.out.println("Matched ID  : " + updateExpenseId);
                }

                if (updateCategory == null || updateCategory.trim().isEmpty()) {
                    updateCategory = "Other";
                }

                if (updateDescription == null || updateDescription.trim().isEmpty()) {
                    updateDescription = updateCategory + " expense";
                }

                System.out.println("========================================");
                System.out.println("AI UPDATE EXPENSE");
                System.out.println("User ID      : " + userId);
                System.out.println("Expense ID   : " + updateExpenseId);
                System.out.println("Old Amount   : " +
                        (oldExpenseAmountText == null || oldExpenseAmountText.trim().isEmpty()
                                ? "Not provided" : oldExpenseAmountText));
                System.out.println("Category     : [" + updateCategory + "]");
                System.out.println("Description  : [" + updateDescription + "]");
                System.out.println("New Amount   : " + updateAmount);
                System.out.println("========================================");

                boolean updated = transactionTool.updateExpense(
                        userId, updateExpenseId, updateCategory,
                        updateDescription, updateAmount);

                if (updated) {
                    if (oldExpenseAmountText != null && !oldExpenseAmountText.trim().isEmpty()) {
                        return "✅ Expense " + updateExpenseId + " (" + updateCategory
                                + ") updated from ₹"
                                + format(Double.parseDouble(oldExpenseAmountText))
                                + " to ₹" + format(updateAmount) + ".";
                    }
                    return "✅ Expense " + updateExpenseId
                            + " updated successfully to ₹" + format(updateAmount) + ".";
                } else {
                    return "❌ Expense " + updateExpenseId
                            + " was not found or could not be updated.";
                }

            // DELETE EXPENSE

            // ====================================================

            case "DELETE_TRANSACTION":

                int expenseId =

                        extractTransactionId(

                                question

                        );









                if (expenseId <= 0) {

                    return

                            "Please provide a valid expense ID.";

                }









                System.out.println(

                        "========================================"

                );

                System.out.println(

                        "AI DELETE EXPENSE"

                );

                System.out.println(

                        "User ID    : "

                                + userId

                );

                System.out.println(

                        "Expense ID : "

                                + expenseId

                );

                System.out.println(

                        "========================================"

                );









                // ------------------------------------------------

                // DELETE

                // ------------------------------------------------

                boolean deleted =

                        transactionTool

                                .deleteExpense(

                                        userId,

                                        expenseId

                                );









                if (deleted) {

                    return

                            "✅ Expense "

                                    + expenseId

                                    + " deleted successfully.";

                } else {

                    return

                            "❌ Expense "

                                    + expenseId

                                    + " was not found or could not be deleted.";

                }









            // ====================================================

            // DEFAULT

            // ====================================================

            default:

                financialData =

                        "No financial database tool was required.";

                break;

        }









        // ========================================================

        // STEP 3 - GENERATE FINAL AI RESPONSE

        // ========================================================

        return generateFinalAnswer(

                question,

                financialData

        );

    }









    // ============================================================

    // INTENT DETECTION

    // ============================================================

    private String detectIntent(

            String question)

            throws Exception {

        String lower =

                question

                        .toLowerCase()

                        .trim();









        // ========================================================

        // DIRECT ADD INCOME DETECTION

        // ========================================================

        if (looksLikeAddIncome(lower)) {

            return "ADD_INCOME";

        }









        // ========================================================

        // DIRECT UPDATE INCOME DETECTION

        // ========================================================

        if (looksLikeUpdateIncome(lower)) {

            return "UPDATE_INCOME";

        }









        // ========================================================

        // DIRECT ADD EXPENSE DETECTION

        // ========================================================

        if (looksLikeAddExpense(lower)) {

            return "ADD_EXPENSE";

        }









        // ========================================================

        // DIRECT UPDATE EXPENSE DETECTION

        // ========================================================

        if (looksLikeUpdateExpense(lower)) {

            return "UPDATE_EXPENSE";

        }









        // ========================================================

        // DIRECT DELETE DETECTION

        // ========================================================

        if (looksLikeDeleteTransaction(lower)) {

            return "DELETE_TRANSACTION";

        }









        // ========================================================

        // ASK QWEN

        // ========================================================

        String systemPrompt =

                "You are the tool-selection AI for a financial assistant.\n"

                + "Choose exactly ONE intent.\n\n"

                + "Available intents:\n"

                + "TOTAL_INCOME\n"

                + "TOTAL_EXPENSE\n"

                + "TRANSACTION_COUNT\n"

                + "CATEGORY_SPENDING\n"

                + "HIGHEST_EXPENSES\n"

                + "RECENT_TRANSACTIONS\n"

                + "FINANCIAL_SUMMARY\n"

                + "ADD_INCOME\n"

                + "UPDATE_INCOME\n"

                + "ADD_EXPENSE\n"

                + "UPDATE_EXPENSE\n"

                + "DELETE_TRANSACTION\n"

                + "NONE\n\n"









                + "Examples:\n"

                + "How much did I earn -> TOTAL_INCOME\n"

                + "How much did I spend -> TOTAL_EXPENSE\n"

                + "Where do I spend most -> CATEGORY_SPENDING\n"

                + "What are my biggest expenses -> HIGHEST_EXPENSES\n"

                + "Show recent transactions -> RECENT_TRANSACTIONS\n"

                + "How many transactions -> TRANSACTION_COUNT\n"

                + "Give me my financial summary -> FINANCIAL_SUMMARY\n"









                // INCOME

                + "Add income 10000 salary -> ADD_INCOME\n"

                + "Add salary income 25000 -> ADD_INCOME\n"

                + "I received 12000 salary -> ADD_INCOME\n"

                + "I earned 15000 from salary -> ADD_INCOME\n"

                + "Add 20000 income and particular Salary -> ADD_INCOME\n"

                + "Update income 5 amount to 15000 -> UPDATE_INCOME\n"

                + "Edit income 5 amount to 20000 -> UPDATE_INCOME\n"

                + "Change income 7 to salary 25000 -> UPDATE_INCOME\n"









                // EXPENSE

                + "Add food expense 500 -> ADD_EXPENSE\n"

                + "Add food exp 500 -> ADD_EXPENSE\n"

                + "I spent 500 on food -> ADD_EXPENSE\n"

                + "Add 300 travel expense -> ADD_EXPENSE\n"

                + "Add 500 exp and particular is Sarthak -> ADD_EXPENSE\n"

                + "Update expense 5 amount to 700 -> UPDATE_EXPENSE\n"

                + "Edit expense 10 amount to 500 -> UPDATE_EXPENSE\n"

                + "Change expense 7 to 1000 -> UPDATE_EXPENSE\n"









                // DELETE

                + "Delete transaction 25 -> DELETE_TRANSACTION\n"

                + "Delete expense 25 -> DELETE_TRANSACTION\n"

                + "Remove transaction 25 -> DELETE_TRANSACTION\n\n"









                + "Return ONLY the intent name.\n"

                + "Do not explain anything.";









        String prompt =

                systemPrompt

                        + "\n\nUser question:\n"

                        + question;









        String response =

                callOllama(prompt);









        response =

                response

                        .trim()

                        .toUpperCase();









        // ========================================================

        // VALIDATE AI RESPONSE

        // ========================================================

        if (response.contains("ADD_INCOME")) {

            return "ADD_INCOME";

        }









        if (response.contains("UPDATE_INCOME")) {

            return "UPDATE_INCOME";

        }









        if (response.contains("ADD_EXPENSE")) {

            return "ADD_EXPENSE";

        }









        if (response.contains("UPDATE_EXPENSE")) {

            return "UPDATE_EXPENSE";

        }









        if (response.contains("DELETE_TRANSACTION")) {

            return "DELETE_TRANSACTION";

        }









        if (response.contains("TOTAL_INCOME")) {

            return "TOTAL_INCOME";

        }









        if (response.contains("TOTAL_EXPENSE")) {

            return "TOTAL_EXPENSE";

        }









        if (response.contains("TRANSACTION_COUNT")) {

            return "TRANSACTION_COUNT";

        }









        if (response.contains("CATEGORY_SPENDING")) {

            return "CATEGORY_SPENDING";

        }









        if (response.contains("HIGHEST_EXPENSES")) {

            return "HIGHEST_EXPENSES";

        }









        if (response.contains("RECENT_TRANSACTIONS")) {

            return "RECENT_TRANSACTIONS";

        }









        if (response.contains("FINANCIAL_SUMMARY")) {

            return "FINANCIAL_SUMMARY";

        }









        return "NONE";

    }









    // ============================================================

    // CHECK ADD INCOME COMMAND

    // ============================================================

    private boolean looksLikeAddIncome(

            String question) {

        boolean hasAddOrReceive =

                question.contains("add")

                        || question.contains("received")

                        || question.contains("receive")

                        || question.contains("earned")

                        || question.contains("earn")

                        || question.contains("got");









        boolean hasIncome =

                question.contains("income")

                        || question.contains("salary")

                        || question.contains("earning")

                        || question.contains("payment");









        boolean hasMoney =

                question.matches(

                        ".*\\d+(\\.\\d+)?.*"

                );









        return hasAddOrReceive

                && hasIncome

                && hasMoney;

    }









    // ============================================================

    // CHECK UPDATE INCOME COMMAND

    // ============================================================

    private boolean looksLikeUpdateIncome(

            String question) {

        boolean hasUpdate =

                question.contains("update")

                        || question.contains("edit")

                        || question.contains("change")

                        || question.contains("modify");









        boolean hasIncome =

                question.contains("income")

                        || question.contains("salary")

                        || question.contains("earning");









        boolean hasNumber =

                question.matches(

                        ".*\\d+.*"

                );









        return hasUpdate

                && hasIncome

                && hasNumber;

    }









    // ============================================================

    // CHECK ADD EXPENSE COMMAND

    // ============================================================

    private boolean looksLikeAddExpense(

            String question) {

        boolean hasAddOrSpend =

                question.contains("add")

                        || question.contains("spent")

                        || question.contains("spend");









        boolean hasExpenseWord =

                question.contains("expense")

                        || question.contains("exp");









        boolean hasMoney =

                question.matches(

                        ".*\\d+(\\.\\d+)?.*"

                );









        return hasAddOrSpend

                && hasExpenseWord

                && hasMoney;

    }









    // ============================================================

    // CHECK UPDATE EXPENSE COMMAND

    // ============================================================

    private boolean looksLikeUpdateExpense(

            String question) {

        boolean hasUpdate =

                question.contains("update")

                        || question.contains("edit")

                        || question.contains("change")

                        || question.contains("modify");









        boolean hasExpense =

                question.contains("expense")

                        || question.contains("transaction");









        boolean hasNumber =

                question.matches(

                        ".*\\d+.*"

                );









        return hasUpdate

                && hasExpense

                && hasNumber;

    }









    // ============================================================

    // CHECK DELETE COMMAND

    // ============================================================

    private boolean looksLikeDeleteTransaction(

            String question) {

        boolean hasDelete =

                question.contains("delete")

                        || question.contains("remove");









        boolean hasTransaction =

                question.contains("transaction")

                        || question.contains("expense");









        boolean hasNumber =

                question.matches(

                        ".*\\d+.*"

                );









        return hasDelete

                && hasTransaction

                && hasNumber;

    }









    // ============================================================

    // EXTRACT INCOME DETAILS

    // ============================================================

    private String[] extractIncomeDetails(

            String question)

            throws Exception {

        String prompt =

                "Extract income information from the user's message.\n\n"

                + "Return ONLY this exact format:\n"

                + "CATEGORY|AMOUNT|DESCRIPTION\n\n"

                + "IMPORTANT RULES:\n"

                + "1. Extract the numeric income amount.\n"

                + "2. Extract the real income category.\n"

                + "3. Words such as income, earn, received, add are NOT categories.\n"

                + "4. If the user gives a category such as Salary, Business, Freelance, use it.\n"

                + "5. If the user says 'particular X', treat X as a possible category.\n"

                + "6. Do not use INCOME as category.\n"

                + "7. Return only CATEGORY|AMOUNT|DESCRIPTION.\n\n"









                + "Examples:\n"

                + "add income 10000 salary -> Salary|10000|Salary income\n"

                + "add salary income 25000 -> Salary|25000|Salary income\n"

                + "I received 15000 from salary -> Salary|15000|Salary income\n"

                + "add income 20000 and particular Salary -> Salary|20000|Salary income\n"

                + "add 50000 business income -> Business|50000|Business income\n"

                + "received 12000 from company -> Other|12000|company income\n\n"









                + "User message:\n"

                + question;









        String response =

                callOllama(prompt);









        response =

                response

                        .trim()

                        

                        .trim();









        String[] lines =

                response.split("\\r?\\n");









        String dataLine = null;









        for (String line : lines) {

            line = line.trim();









            if (line.contains("|") &&

                    line.split("\\|").length >= 3) {

                dataLine = line;

                break;

            }

        }









        if (dataLine == null) {

            throw new Exception(

                    "Could not extract income details from AI response: "

                            + response

            );

        }









        String[] parts =

                dataLine.split("\\|", 3);









        if (parts.length != 3) {

            throw new Exception(

                    "Invalid income format from AI: "

                            + response

            );

        }









        String category =

                parts[0].trim();









        String amountText =

                parts[1]

                        .trim()

                        .replaceAll(

                                "[^0-9.]",

                                ""

                        );









        String description =

                parts[2].trim();









        // --------------------------------------------------------

        // PREVENT INVALID CATEGORY

        // --------------------------------------------------------

        if (category.equalsIgnoreCase("income") ||

                category.equalsIgnoreCase("earn") ||

                category.equalsIgnoreCase("earned") ||

                category.equalsIgnoreCase("received") ||

                category.equalsIgnoreCase("receive") ||

                category.equalsIgnoreCase("add")) {

            category = "Other";

        }









        if (category.isEmpty()) {

            category = "Other";

        }









        if (description.isEmpty()) {

            description =

                    category + " income";

        }









        if (amountText.isEmpty()) {

            throw new Exception(

                    "Could not find income amount."

            );

        }









        double amount;









        try {

            amount =

                    Double.parseDouble(

                            amountText

                    );

        } catch (NumberFormatException e) {

            throw new Exception(

                    "Invalid income amount: "

                            + amountText

            );

        }









        return new String[]{

                category,

                String.valueOf(amount),

                description

        };

    }









    // ============================================================

    // EXTRACT UPDATE INCOME DETAILS

    // ============================================================

    private String[] extractUpdateIncomeDetails(

            String question)

            throws Exception {

        /*

         * Return:

         *

         * [0] INCOME_ID

         * [1] OLD_AMOUNT

         * [2] CATEGORY

         * [3] NEW_AMOUNT

         * [4] DESCRIPTION

         *

         * Examples:

         *

         * update income 5 amount to 15000

         * -> 5|| |15000|

         *

         * update income 12000 instead of 10000

         * -> |||12000|

         *

         * change income from 10000 to 12000

         * -> |||12000|

         */

        String normalizedQuestion =

                question

                        .trim()

                        .replaceAll(

                                "\\s+",

                                " "

                        );

        String incomeId = "";

        String oldAmount = "";

        String category = "";

        String newAmount = "";

        String description = "";

        // --------------------------------------------------------

        // 1. EXPLICIT INCOME ID

        // --------------------------------------------------------

        java.util.regex.Matcher idMatcher =

                java.util.regex.Pattern

                        .compile(

                                "(?i)\\bincome\\s+(?:id\\s*)?#?(\\d+)\\s+(?=amount|category|to|as)"

                        )

                        .matcher(

                                normalizedQuestion

                        );

        if (idMatcher.find()) {

            incomeId =

                    idMatcher.group(1);

        } else {

            java.util.regex.Matcher idMatcher2 =

                    java.util.regex.Pattern

                            .compile(

                                    "(?i)\\bincome\\s+id\\s*#?(\\d+)\\b"

                            )

                            .matcher(

                                    normalizedQuestion

                            );

            if (idMatcher2.find()) {

                incomeId =

                        idMatcher2.group(1);

            } else {

                java.util.regex.Matcher idMatcher3 =

                        java.util.regex.Pattern

                                .compile(

                                        "(?i)\\bid\\s*#?(\\d+)\\b"

                                )

                                .matcher(

                                        normalizedQuestion

                                );

                if (idMatcher3.find()) {

                    incomeId =

                            idMatcher3.group(1);

                }

            }

        }

        // --------------------------------------------------------

        // 2. "NEW INSTEAD OF OLD"

        // --------------------------------------------------------

        java.util.regex.Matcher insteadMatcher =

                java.util.regex.Pattern

                        .compile(

                                "(?i)\\b(\\d+(?:\\.\\d+)?)\\s+(?:instead|insted)\\s+of\\s+(\\d+(?:\\.\\d+)?)\\b"

                        )

                        .matcher(

                                normalizedQuestion

                        );

        if (insteadMatcher.find()) {

            newAmount =

                    insteadMatcher.group(1);

            oldAmount =

                    insteadMatcher.group(2);

        }

        // --------------------------------------------------------

        // 3. "FROM OLD TO NEW"

        // --------------------------------------------------------

        if (newAmount.isEmpty()) {

            java.util.regex.Matcher fromToMatcher =

                    java.util.regex.Pattern

                            .compile(

                                    "(?i)\\bfrom\\s+(\\d+(?:\\.\\d+)?)\\s+to\\s+(\\d+(?:\\.\\d+)?)\\b"

                            )

                            .matcher(

                                    normalizedQuestion

                            );

            if (fromToMatcher.find()) {

                oldAmount =

                        fromToMatcher.group(1);

                newAmount =

                        fromToMatcher.group(2);

            }

        }

        // --------------------------------------------------------

        // 4. "OLD TO NEW" WHEN THERE IS NO EXPLICIT ID

        // --------------------------------------------------------

        if (incomeId.isEmpty() &&

                newAmount.isEmpty()) {

            java.util.regex.Matcher toMatcher =

                    java.util.regex.Pattern

                            .compile(

                                    "(?i)\\b(\\d+(?:\\.\\d+)?)\\s+to\\s+(\\d+(?:\\.\\d+)?)\\b"

                            )

                            .matcher(

                                    normalizedQuestion

                            );

            if (toMatcher.find()) {

                oldAmount =

                        toMatcher.group(1);

                newAmount =

                        toMatcher.group(2);

            }

        }

        // --------------------------------------------------------

        // 5. ASK OLLAMA FOR CATEGORY / DESCRIPTION / FALLBACK

        // --------------------------------------------------------

        String prompt =

                "Extract income update information from the user's message.\n\n"

                + "Return ONLY this exact format:\n"

                + "INCOME_ID|OLD_AMOUNT|CATEGORY|NEW_AMOUNT|DESCRIPTION\n\n"

                + "Rules:\n"

                + "1. Income ID is optional. Leave blank if no ID is given.\n"

                + "2. OLD_AMOUNT is the amount being replaced.\n"

                + "3. NEW_AMOUNT is the new amount.\n"

                + "4. For '12000 instead of 10000', NEW_AMOUNT=12000 and OLD_AMOUNT=10000.\n"

                + "5. For 'from 10000 to 12000', OLD_AMOUNT=10000 and NEW_AMOUNT=12000.\n"

                + "6. Do not treat a new amount as an income ID.\n"

                + "7. Category is blank when no category change is requested.\n"

                + "8. Description is blank when no description change is requested.\n"

                + "9. Return only five pipe-separated values.\n\n"

                + "Examples:\n"

                + "update income 5 amount to 15000 -> 5|||15000|\n"

                + "update income 5 to salary 20000 -> 5||Salary|20000|\n"

                + "edit income 7 amount 25000 category salary -> 7||Salary|25000|\n"

                + "change income 10 to Business 50000 -> 10||Business|50000|\n"

                + "update income 12000 instead of 10000 -> |||12000|\n"

                + "change income from 10000 to 12000 -> |||12000|\n\n"

                + "User message:\n"

                + question;

        String response =

                callOllama(prompt);

        response =

                response

                        .trim()

                        .replace(

                                "```",

                                ""

                        )

                        .trim();

        String[] lines =

                response.split(

                        "\\r?\\n"

                );

        String dataLine = null;

        for (String line : lines) {

            line = line.trim();

            if (line.contains("|") &&

                    line.split(

                            "\\\\|",

                            -1

                    ).length >= 5) {

                dataLine = line;

                break;

            }

        }

        if (dataLine != null) {

            String[] parts =

                    dataLine.split(

                            "\\\\|",

                            5

                    );

            if (parts.length == 5) {

                if (incomeId.isEmpty()) {

                    incomeId =

                            parts[0].trim();

                }

                if (oldAmount.isEmpty()) {

                    oldAmount =

                            parts[1]

                                    .trim()

                                    .replaceAll(

                                            "[^0-9.]",

                                            ""

                                    );

                }

                category =

                        parts[2].trim();

                if (newAmount.isEmpty()) {

                    newAmount =

                            parts[3]

                                    .trim()

                                    .replaceAll(

                                            "[^0-9.]",

                                            ""

                                    );

                }

                description =

                        parts[4].trim();

            }

        }

        // --------------------------------------------------------

        // 6. DETERMINISTIC OVERRIDE FOR "INSTEAD OF"

        // --------------------------------------------------------

        insteadMatcher =

                java.util.regex.Pattern

                        .compile(

                                "(?i)\\b(\\d+(?:\\.\\d+)?)\\s+(?:instead|insted)\\s+of\\s+(\\d+(?:\\.\\d+)?)\\b"

                        )

                        .matcher(

                                normalizedQuestion

                        );

        if (insteadMatcher.find()) {

            newAmount =

                    insteadMatcher.group(1);

            oldAmount =

                    insteadMatcher.group(2);

        }

        // --------------------------------------------------------

        // 7. DETERMINISTIC OVERRIDE FOR "FROM OLD TO NEW"

        // --------------------------------------------------------

        java.util.regex.Matcher fromToMatcher =

                java.util.regex.Pattern

                        .compile(

                                "(?i)\\bfrom\\s+(\\d+(?:\\.\\d+)?)\\s+to\\s+(\\d+(?:\\.\\d+)?)\\b"

                        )

                        .matcher(

                                normalizedQuestion

                        );

        if (fromToMatcher.find()) {

            oldAmount =

                    fromToMatcher.group(1);

            newAmount =

                    fromToMatcher.group(2);

        }

        // --------------------------------------------------------

        // 8. EXPLICIT ID + "AMOUNT TO NEW_AMOUNT"

        // --------------------------------------------------------

        if (!incomeId.isEmpty()) {

            java.util.regex.Matcher amountMatcher =

                    java.util.regex.Pattern

                            .compile(

                                    "(?i)\\bamount\\s*(?:to|=)\\s*(\\d+(?:\\.\\d+)?)\\b"

                            )

                            .matcher(

                                    normalizedQuestion

                            );

            if (amountMatcher.find()) {

                newAmount =

                        amountMatcher.group(1);

            }

        }

        // --------------------------------------------------------

        // 9. VALIDATE NEW AMOUNT

        // --------------------------------------------------------

        if (newAmount.isEmpty()) {

            throw new Exception(

                    "Could not find the new income amount."

            );

        }

        try {

            Double.parseDouble(

                    newAmount

            );

            if (!oldAmount.isEmpty()) {

                Double.parseDouble(

                        oldAmount

                );

            }

        } catch (NumberFormatException e) {

            throw new Exception(

                    "Invalid update income amount."

            );

        }

        return new String[]{

                incomeId,

                oldAmount,

                category,

                newAmount,

                description

        };

    }

















    // ============================================================

    // EXTRACT EXPENSE DETAILS

    // ============================================================

    private String[] extractExpenseDetails(

            String question)

            throws Exception {

        String prompt =

                "Extract expense information from the user's message.\n\n"

                + "Return ONLY this exact format:\n"

                + "CATEGORY|AMOUNT|DESCRIPTION\n\n"

                + "IMPORTANT RULES:\n"

                + "1. Extract the numeric amount.\n"

                + "2. Extract the real expense category if the user gives one.\n"

                + "3. Words such as 'expense', 'exp', 'spent', 'add' are NOT categories.\n"

                + "4. If the user only says expense without a real category, use Other as CATEGORY.\n"

                + "5. If the user explicitly gives a category, use that category.\n"

                + "6. If the user says 'particular X', first treat X as a possible category.\n"

                + "7. If 'particular X' matches a real category, use X as CATEGORY.\n"

                + "8. If 'particular X' does not look like a category, X may be DESCRIPTION.\n"

                + "9. Do not use EXPENSE as the category.\n"

                + "10. Return only CATEGORY|AMOUNT|DESCRIPTION.\n\n"









                + "Examples:\n"

                + "add food expense 500 -> Food|500|Food expense\n"

                + "add food exp 500 -> Food|500|Food expense\n"

                + "add 500 food -> Food|500|Food expense\n"

                + "add 500 food and particular is Sarthak -> Food|500|Sarthak\n"

                + "add 500 petrol description Bike fuel -> Petrol|500|Bike fuel\n"

                + "add 200 and particular Rent -> Rent|200|Rent expense\n"

                + "add expense 200 particular Rent -> Rent|200|Rent expense\n"

                + "add 500 exp and particular Sarthak -> Other|500|Sarthak\n"

                + "I spent 250 on shopping -> Shopping|250|Shopping expense\n"

                + "spent 700 on travel -> Travel|700|Travel expense\n\n"









                + "User message:\n"

                + question;









        String response =

                callOllama(prompt);









        response =

                response

                        .trim()

                        

                        .trim();









        String[] lines =

                response.split("\\r?\\n");









        String dataLine = null;









        for (String line : lines) {

            line = line.trim();









            if (line.contains("|") &&

                    line.split("\\|").length >= 3) {

                dataLine = line;

                break;

            }

        }









        if (dataLine == null) {

            throw new Exception(

                    "Could not extract expense details from AI response: "

                            + response

            );

        }









        String[] parts =

                dataLine.split("\\|", 3);









        if (parts.length != 3) {

            throw new Exception(

                    "Invalid expense format from AI: "

                            + response

            );

        }









        String category =

                parts[0].trim();









        String amountText =

                parts[1]

                        .trim()

                        .replaceAll(

                                "[^0-9.]",

                                ""

                        );









        String description =

                parts[2].trim();









        // --------------------------------------------------------

        // PREVENT INVALID CATEGORY

        // --------------------------------------------------------

        if (category.equalsIgnoreCase("expense") ||

                category.equalsIgnoreCase("exp") ||

                category.equalsIgnoreCase("spent") ||

                category.equalsIgnoreCase("add")) {

            category =

                    "Other";

        }









        if (category.isEmpty()) {

            category =

                    "Other";

        }









        if (description.isEmpty()) {

            description =

                    category + " expense";

        }









        if (amountText.isEmpty()) {

            throw new Exception(

                    "Could not find expense amount."

            );

        }









        double amount;









        try {

            amount =

                    Double.parseDouble(

                            amountText

                    );

        } catch (NumberFormatException e) {

            throw new Exception(

                    "Invalid expense amount: "

                            + amountText

            );

        }









        return new String[]{

                category,

                String.valueOf(amount),

                description

        };

    }









    // ============================================================

    // EXTRACT UPDATE EXPENSE DETAILS

    // ============================================================

    private String[] extractUpdateExpenseDetails(String question) throws Exception {

        String q = question == null ? "" : question.trim().replaceAll("\\s+", " ");

        String expenseId = "";
        String category = "";
        String oldAmount = "";
        String newAmount = "";
        String description = "";

        // Explicit ID only. This prevents 500 from becoming the ID in
        // commands such as: update bike expense 500 to 200
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                "(?i)\\b(?:expense|transaction)\\s+id\\s*#?(\\d+)\\b")
                .matcher(q);

        if (m.find()) {
            expenseId = m.group(1);
        } else {
            m = java.util.regex.Pattern.compile("(?i)\\bid\\s*#?(\\d+)\\b").matcher(q);
            if (m.find()) {
                expenseId = m.group(1);
            } else {
                m = java.util.regex.Pattern.compile(
                        "(?i)\\bexpense\\s+#?(\\d+)\\s+(?=(?:amount|category|description|to|as)\\b)")
                        .matcher(q);
                if (m.find()) {
                    expenseId = m.group(1);
                }
            }
        }

        // from OLD to NEW
        m = java.util.regex.Pattern.compile(
                "(?i)\\bfrom\\s+(\\d+(?:\\.\\d+)?)\\s+to\\s+(\\d+(?:\\.\\d+)?)\\b")
                .matcher(q);
        if (m.find()) {
            oldAmount = m.group(1);
            newAmount = m.group(2);
        }

        // OLD to NEW when there is no explicit ID.
        if (newAmount.isEmpty() && expenseId.isEmpty()) {
            m = java.util.regex.Pattern.compile(
                    "(?i)\\b(\\d+(?:\\.\\d+)?)\\s+to\\s+(\\d+(?:\\.\\d+)?)\\b")
                    .matcher(q);
            if (m.find()) {
                oldAmount = m.group(1);
                newAmount = m.group(2);
            }
        }

        // Explicit ID + new amount.
        if (newAmount.isEmpty() && !expenseId.isEmpty()) {
            m = java.util.regex.Pattern.compile(
                    "(?i)\\b(?:amount\\s*)?(?:to|=)\\s*(\\d+(?:\\.\\d+)?)\\b")
                    .matcher(q);
            if (m.find()) {
                newAmount = m.group(1);
            }
        }

        if (!expenseId.isEmpty() && newAmount.isEmpty()) {
            m = java.util.regex.Pattern.compile(
                    "(?i)\\bamount\\s*(?:to|=)?\\s*(\\d+(?:\\.\\d+)?)\\b")
                    .matcher(q);
            if (m.find()) {
                newAmount = m.group(1);
            }
        }

        // Ask Ollama only for category/description. Amounts are parsed
        // deterministically so the model cannot confuse ID and amount.
        String prompt =
                "Extract expense update information from the user's message.\\n\\n"
                + "Return ONLY: CATEGORY|NEW_AMOUNT|DESCRIPTION\\n"
                + "Use the real category. Do not treat numbers as categories.\\n"
                + "Examples:\n"
                + "update bike expense 500 to 200 -> Bike|200|\\n"
                + "update food expense 500 to 700 -> Food|700|\\n"
                + "update expense 18 to travel 900 -> Travel|900|\\n\\n"
                + "User message:\n" + q;

        try {
            String response = callOllama(prompt).trim().replace("```", "");
            for (String line : response.split("\\r?\\n")) {
                line = line.trim();
                if (!line.contains("|")) continue;
                String[] parts = line.split("\\|", 3);
                if (parts.length == 3) {
                    if (category.isEmpty()) category = parts[0].trim();
                    if (newAmount.isEmpty()) {
                        newAmount = parts[1].trim().replaceAll("[^0-9.]", "");
                    }
                    description = parts[2].trim();
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Update expense AI extraction skipped: " + e.getMessage());
        }

        if (category.equalsIgnoreCase("expense") ||
                category.equalsIgnoreCase("exp") ||
                category.equalsIgnoreCase("update") ||
                category.equalsIgnoreCase("change") ||
                category.equalsIgnoreCase("amount") ||
                category.equalsIgnoreCase("to") ||
                category.equalsIgnoreCase("from") ||
                category.matches("\\d+(?:\\.\\d+)?")) {
            category = "";
        }

        if (newAmount.isEmpty()) {
            throw new Exception("Could not find the new expense amount.");
        }

        try {
            Double.parseDouble(newAmount);
            if (!oldAmount.isEmpty()) Double.parseDouble(oldAmount);
        } catch (NumberFormatException e) {
            throw new Exception("Invalid expense amount in update command.");
        }

        if (category.isEmpty()) category = "Other";
        if (description.isEmpty()) description = category + " expense";

        return new String[]{expenseId, category, oldAmount, newAmount, description};
    }


    // ============================================================
    // FIND EXISTING EXPENSE CATEGORY BY ID
    // ============================================================
    private String findExpenseCategoryById(int userId, int expenseId) {
        try {
            List<Map<String, Object>> transactions =
                    transactionTool.getRecentTransactions(userId, 10000);

            if (transactions == null) return null;

            for (Map<String, Object> row : transactions) {
                if (row == null) continue;

                Object idObj = getMapValue(
                        row,
                        "id",
                        "expense_id",
                        "expenseId",
                        "transaction_id",
                        "transactionId"
                );

                if (idObj == null) continue;

                int id;
                try {
                    id = Integer.parseInt(String.valueOf(idObj));
                } catch (NumberFormatException e) {
                    continue;
                }

                if (id != expenseId) continue;

                Object typeObj = getMapValue(
                        row,
                        "type",
                        "transaction_type",
                        "transactionType"
                );

                if (typeObj != null &&
                        String.valueOf(typeObj).toLowerCase().contains("income")) {
                    return null;
                }

                Object categoryObj = getMapValue(
                        row,
                        "category",
                        "category_name",
                        "categoryName"
                );

                return categoryObj == null ? null : String.valueOf(categoryObj);
            }
        } catch (Exception e) {
            System.out.println(
                    "Could not find expense category by ID: " + e.getMessage()
            );
        }

        return null;
    }

    // ============================================================
    // FIND EXPENSE ID USING CATEGORY + OLD AMOUNT
    // ============================================================
    private int findExpenseIdByCategoryAndAmount(int userId, String category, double oldAmount) {
        try {
            List<Map<String, Object>> transactions =
                    transactionTool.getRecentTransactions(userId, 10000);

            if (transactions == null) return -1;

            for (Map<String, Object> row : transactions) {
                if (row == null) continue;

                Object typeObj = getMapValue(row, "type", "transaction_type", "transactionType");
                if (typeObj != null && String.valueOf(typeObj).toLowerCase().contains("income")) {
                    continue;
                }

                Object categoryObj = getMapValue(row, "category", "category_name", "categoryName");
                Object amountObj = getMapValue(row, "amount", "expense_amount", "expenseAmount");
                Object idObj = getMapValue(row, "id", "expense_id", "expenseId", "transaction_id", "transactionId");

                if (categoryObj == null || amountObj == null || idObj == null) continue;
                if (!String.valueOf(categoryObj).trim().equalsIgnoreCase(category == null ? "" : category.trim())) continue;

                double dbAmount;
                try { dbAmount = Double.parseDouble(String.valueOf(amountObj)); }
                catch (NumberFormatException e) { continue; }

                if (Math.abs(dbAmount - oldAmount) > 0.0001) continue;

                try { return Integer.parseInt(String.valueOf(idObj)); }
                catch (NumberFormatException ignored) { }
            }
        } catch (Exception e) {
            System.out.println("Could not find expense by category + amount: " + e.getMessage());
        }
        return -1;
    }

    private Object getMapValue(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            if (row.containsKey(key)) return row.get(key);
        }
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (entry.getKey() == null) continue;
            for (String key : keys) {
                if (entry.getKey().equalsIgnoreCase(key)) return entry.getValue();
            }
        }
        return null;
    }

    
// EXTRACT TRANSACTION / EXPENSE ID

    // ============================================================

    private int extractTransactionId(

            String question)

            throws Exception {

        String prompt =

                "Extract the expense ID from the user's message.\n\n"

                + "Return ONLY the number.\n\n"

                + "Examples:\n"

                + "delete transaction 25 -> 25\n"

                + "delete expense 10 -> 10\n"

                + "remove transaction 7 -> 7\n"

                + "delete transaction id 15 -> 15\n\n"

                + "User message:\n"

                + question;









        String response =

                callOllama(prompt);









        response =

                response.trim();









        response =

                response.replaceAll(

                        "[^0-9]",

                        ""

                );









        if (response.isEmpty()) {

            return -1;

        }









        return Integer.parseInt(

                response

        );

    }









    // ============================================================

    // FINAL AI RESPONSE

    // ============================================================

    private String generateFinalAnswer(

            String question,

            String financialData)

            throws Exception {

        String prompt =

                "You are a helpful AI financial assistant.\n\n"

                + "User question:\n"

                + question

                + "\n\n"

                + "Verified financial data from the user's database:\n"

                + financialData

                + "\n\n"

                + "Answer the user's question using ONLY the "

                + "verified financial data above.\n"

                + "Do not invent numbers.\n"

                + "Do not mention tools, databases, SQL, or internal systems.\n"

                + "Keep the answer concise and easy to understand.";









        return callOllama(

                prompt

        );

    }









    // ============================================================

    // OLLAMA API CALL

    // ============================================================

    private String callOllama(

            String prompt)

            throws IOException, InterruptedException {

        String json = """

                {"model":"%s","messages":[{"role":"user","content":"%s"}],"stream":false,"think":false,"keep_alive":"5m"}

                """.formatted(

                        MODEL,

                        escapeJson(prompt)

                );

        HttpRequest request =

                HttpRequest.newBuilder()

                        .uri(

                                URI.create(

                                        OLLAMA_URL

                                )

                        )

                        .header(

                                "Content-Type",

                                "application/json"

                        )

                        .POST(

                                HttpRequest.BodyPublishers

                                        .ofString(

                                                json

                                        )

                        )

                        .build();

        HttpResponse<String> response =

                httpClient.send(

                        request,

                        HttpResponse.BodyHandlers

                                .ofString()

                );

        System.out.println(

                "Ollama HTTP status: "

                        + response.statusCode()

        );

        if (response.statusCode() != 200) {

            throw new IOException(

                    "Ollama error: "

                            + response.body()

            );

        }

        return extractContent(

                response.body()

        );

    }









    private String extractContent(String json) {
        String marker = "\"content\":\"";
        int start = json.indexOf(marker);

        if (start == -1) {
            return "Sorry, I could not understand the AI response.";
        }

        start += marker.length();
        StringBuilder result = new StringBuilder();
        boolean escaped = false;

        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escaped) {
                if (c == 'n') result.append('\n');
                else if (c == 't') result.append('\t');
                else if (c == 'r') result.append('\r');
                else if (c == '"') result.append('"');
                else if (c == '\\') result.append('\\');
                else result.append(c);

                escaped = false;
                continue;
            }

            if (c == '\\') {
                escaped = true;
                continue;
            }

            if (c == '"') {
                break;
            }

            result.append(c);
        }

        return result.toString().trim();
    }


    // JSON ESCAPE

    // ============================================================

    private String escapeJson(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }


    // NUMBER FORMAT

    // ============================================================

    private String format(

            double value) {

        return String.format(

                "%.2f",

                value

        );

    }

}