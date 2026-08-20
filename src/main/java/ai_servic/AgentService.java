package ai_servic;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import tool.TransactionTool;

public class AgentService {

    private final TransactionTool transactionTool;

    private static final String OLLAMA_URL =
            "http://localhost:11434/api/chat";

    private static final String MODEL =
            "qwen3:latest";

    private final HttpClient httpClient;


    /*
     * ============================================================
     * CONSTRUCTOR
     * ============================================================
     */

    public AgentService() {

        transactionTool = new TransactionTool();

        httpClient = HttpClient.newHttpClient();
    }


    /*
     * ============================================================
     * MAIN AGENT METHOD
     * ============================================================
     */

    public String processQuestion(
            int userId,
            String question) throws Exception {

        if (question == null ||
                question.trim().isEmpty()) {

            return "Please enter a financial question.";
        }


        /*
         * --------------------------------------------------------
         * STEP 1
         * Detect intent
         * --------------------------------------------------------
         */

        String intent = detectIntent(question);

        System.out.println(
                "AI detected intent: " + intent
        );


        /*
         * --------------------------------------------------------
         * STEP 2
         * Execute appropriate action
         * --------------------------------------------------------
         */

        String financialData;


        switch (intent) {


            /*
             * ====================================================
             * TOTAL INCOME
             * ====================================================
             */

            case "TOTAL_INCOME":

                financialData =
                        "Total income: ₹"
                                + format(
                                        transactionTool
                                                .getTotalIncome(userId)
                                );

                break;


            /*
             * ====================================================
             * TOTAL EXPENSE
             * ====================================================
             */

            case "TOTAL_EXPENSE":

                financialData =
                        "Total expense: ₹"
                                + format(
                                        transactionTool
                                                .getTotalExpense(userId)
                                );

                break;


            /*
             * ====================================================
             * TRANSACTION COUNT
             * ====================================================
             */

            case "TRANSACTION_COUNT":

                financialData =
                        "Total transactions: "
                                + transactionTool
                                        .getTransactionCount(userId);

                break;


            /*
             * ====================================================
             * CATEGORY SPENDING
             * ====================================================
             */

            case "CATEGORY_SPENDING":

                List<Map<String, Object>> categories =
                        transactionTool
                                .getCategoryWiseExpense(userId);

                financialData =
                        categories.toString();

                break;


            /*
             * ====================================================
             * HIGHEST EXPENSES
             * ====================================================
             */

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


            /*
             * ====================================================
             * RECENT TRANSACTIONS
             * ====================================================
             */

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


            /*
             * ====================================================
             * FINANCIAL SUMMARY
             * ====================================================
             */

            case "FINANCIAL_SUMMARY":

                Map<String, Object> summary =
                        transactionTool
                                .getFinancialSummary(userId);

                financialData =
                        summary.toString();

                break;


            /*
             * ====================================================
             * ADD EXPENSE
             * ====================================================
             */

            case "ADD_EXPENSE":

                /*
                 * Extract:
                 *
                 * CATEGORY
                 * AMOUNT
                 * DESCRIPTION / PARTICULAR
                 */

                String[] expenseDetails =
                        extractExpenseDetails(question);


                String category =
                        expenseDetails[0];

                double amount =
                        Double.parseDouble(
                                expenseDetails[1]
                        );

                String description =
                        expenseDetails[2];


                /*
                 * Validate amount
                 */

                if (amount <= 0) {

                    return "Please provide a valid expense amount.";
                }


                /*
                 * If category is empty, use Other.
                 */

                if (category == null ||
                        category.trim().isEmpty()) {

                    category = "Other";
                }


                /*
                 * If description is empty,
                 * create a default description.
                 */

                if (description == null ||
                        description.trim().isEmpty()) {

                    description =
                            category + " expense";
                }


                System.out.println(
                        "ADD EXPENSE"
                );

                System.out.println(
                        "Category: " + category
                );

                System.out.println(
                        "Amount: " + amount
                );

                System.out.println(
                        "Description: " + description
                );


                /*
                 * Insert into database
                 */

                boolean added =
                        transactionTool.addExpense(
                                userId,
                                category,
                                description,
                                amount
                        );


                if (added) {

                    return "✅ Added ₹"
                            + format(amount)
                            + " expense in "
                            + category
                            + " ("
                            + description
                            + ").";

                } else {

                    return "❌ I could not add the expense.";
                }


            /*
             * ====================================================
             * DELETE TRANSACTION
             * ====================================================
             */

            case "DELETE_TRANSACTION":

                int transactionId =
                        extractTransactionId(question);


                if (transactionId <= 0) {

                    return "Please provide a valid transaction ID.";
                }


                boolean deleted =
                        transactionTool.deleteTransaction(
                                userId,
                                transactionId
                        );


                if (deleted) {

                    return "✅ Transaction "
                            + transactionId
                            + " deleted successfully.";

                } else {

                    return "❌ Transaction "
                            + transactionId
                            + " was not found or could not be deleted.";
                }


            /*
             * ====================================================
             * DEFAULT
             * ====================================================
             */

            default:

                financialData =
                        "No financial database tool was required.";
        }


        /*
         * --------------------------------------------------------
         * STEP 3
         * Generate final AI answer
         * --------------------------------------------------------
         */

        return generateFinalAnswer(
                question,
                financialData
        );
    }


    /*
     * ============================================================
     * INTENT DETECTION
     * ============================================================
     */

    private String detectIntent(
            String question) throws Exception {


        String lower =
                question.toLowerCase().trim();


        /*
         * --------------------------------------------------------
         * Direct ADD detection
         * --------------------------------------------------------
         */

        if (looksLikeAddExpense(lower)) {

            return "ADD_EXPENSE";
        }


        /*
         * --------------------------------------------------------
         * Direct DELETE detection
         * --------------------------------------------------------
         */

        if (looksLikeDeleteTransaction(lower)) {

            return "DELETE_TRANSACTION";
        }


        /*
         * --------------------------------------------------------
         * Ask Qwen for normal questions
         * --------------------------------------------------------
         */

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

                + "ADD_EXPENSE\n"

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

                + "Add food expense 500 -> ADD_EXPENSE\n"

                + "Add food exp 500 -> ADD_EXPENSE\n"

                + "I spent 500 on food -> ADD_EXPENSE\n"

                + "Add 300 travel expense -> ADD_EXPENSE\n"

                + "Add 500 exp and particular is Sarthak -> ADD_EXPENSE\n"

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
                response.trim()
                        .toUpperCase();


        /*
         * --------------------------------------------------------
         * Validate AI response
         * --------------------------------------------------------
         */

        if (response.contains("ADD_EXPENSE")) {

            return "ADD_EXPENSE";
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


    /*
     * ============================================================
     * CHECK ADD EXPENSE COMMAND
     * ============================================================
     */

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


    /*
     * ============================================================
     * CHECK DELETE COMMAND
     * ============================================================
     */

    private boolean looksLikeDeleteTransaction(
            String question) {


        boolean hasDelete =
                question.contains("delete")
                        || question.contains("remove");


        boolean hasTransaction =
                question.contains("transaction")
                        || question.contains("expense");


        boolean hasNumber =
                question.matches(".*\\d+.*");


        return hasDelete
                && hasTransaction
                && hasNumber;
    }


    /*
     * ============================================================
     * EXTRACT EXPENSE DETAILS
     * ============================================================
     *
     * Return format:
     *
     * CATEGORY|AMOUNT|DESCRIPTION
     *
     * Example:
     *
     * add food expense 500
     *
     * Food|500|Food expense
     *
     *
     * Example:
     *
     * add 500 food and particular is Sarthak
     *
     * Food|500|Sarthak
     *
     *
     * Example:
     *
     * add 500 exp and particular is Sarthak
     *
     * Other|500|Sarthak
     *
     * ============================================================
     */

    private String[] extractExpenseDetails(
            String question) throws Exception {


        String prompt =

                "Extract expense information from the user's message.\n\n"

                + "Return ONLY this exact format:\n"

                + "CATEGORY|AMOUNT|DESCRIPTION\n\n"


                + "IMPORTANT RULES:\n"

                + "1. Extract the numeric amount.\n"

                + "2. Extract the real expense category if the user gives one.\n"

                + "3. Words such as 'expense', 'exp', 'spent', 'add' are NOT categories.\n"

                + "4. If the user only says 'exp' or 'expense' without a real category, use Other as CATEGORY.\n"

                + "5. If the user says 'particular', use its value as DESCRIPTION.\n"

                + "6. If the user says 'description', use its value as DESCRIPTION.\n"

                + "7. Do not use 'EXPENSE' as the category.\n"

                + "8. Return only CATEGORY|AMOUNT|DESCRIPTION.\n\n"


                + "Examples:\n"

                + "add food expense 500 -> Food|500|Food expense\n"

                + "add food exp 500 -> Food|500|Food expense\n"

                + "add 500 food -> Food|500|Food expense\n"

                + "add 500 food and particular is Sarthak -> Food|500|Sarthak\n"

                + "add 500 food particular Sarthak -> Food|500|Sarthak\n"

                + "add 500 exp and particular is Sarthak -> Other|500|Sarthak\n"

                + "add 500 expense particular Sarthak -> Other|500|Sarthak\n"

                + "I spent 250 on shopping -> Shopping|250|Shopping expense\n"

                + "spent 700 on travel -> Travel|700|Travel expense\n"

                + "add 1000 petrol description Bike fuel -> Petrol|1000|Bike fuel\n\n"


                + "User message:\n"

                + question;


        String response =
                callOllama(prompt);


        response =
                response.trim();


        /*
         * Remove accidental markdown.
         */

        response =
                response.replace(
                        "```",
                        ""
                ).trim();


        /*
         * Sometimes Qwen may add:
         *
         * CATEGORY|AMOUNT|DESCRIPTION
         * Food|500|Sarthak
         *
         * We only want the actual data line.
         */

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


        /*
         * Prevent EXPENSE from becoming category.
         */

        if (category.equalsIgnoreCase("expense") ||
                category.equalsIgnoreCase("exp") ||
                category.equalsIgnoreCase("spent") ||
                category.equalsIgnoreCase("add")) {

            category = "Other";
        }


        /*
         * Empty category
         */

        if (category.isEmpty()) {

            category = "Other";
        }


        /*
         * Empty description
         */

        if (description.isEmpty()) {

            description =
                    category + " expense";
        }


        /*
         * Validate amount
         */

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


    /*
     * ============================================================
     * EXTRACT TRANSACTION ID
     * ============================================================
     */

    private int extractTransactionId(
            String question) throws Exception {


        String prompt =

                "Extract the transaction ID from the user's message.\n\n"

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


        return Integer.parseInt(response);
    }


    /*
     * ============================================================
     * FINAL AI RESPONSE
     * ============================================================
     */

    private String generateFinalAnswer(
            String question,
            String financialData) throws Exception {


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


        return callOllama(prompt);
    }


    /*
     * ============================================================
     * OLLAMA API CALL
     * ============================================================
     */

    private String callOllama(
            String prompt)
            throws IOException, InterruptedException {


        String json =

                "{"

                + "\"model\":\""
                + MODEL
                + "\","

                + "\"messages\":["

                + "{"

                + "\"role\":\"user\","

                + "\"content\":\""
                + escapeJson(prompt)
                + "\""

                + "}"

                + "],"

                + "\"stream\":false,"

                + "\"think\":false,"

                + "\"keep_alive\":\"5m\""

                + "}";


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
                                HttpRequest.BodyPublishers.ofString(
                                        json
                                )
                        )

                        .build();


        HttpResponse<String> response =

                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
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


    /*
     * ============================================================
     * EXTRACT AI CONTENT
     * ============================================================
     */

    private String extractContent(
            String json) {


        String marker =
                "\"content\":\"";


        int start =
                json.indexOf(marker);


        if (start == -1) {

            return "Sorry, I could not understand the AI response.";
        }


        start += marker.length();


        StringBuilder result =
                new StringBuilder();


        boolean escaped = false;


        for (int i = start;
             i < json.length();
             i++) {


            char c =
                    json.charAt(i);


            if (escaped) {

                if (c == 'n') {

                    result.append('\n');

                } else if (c == 't') {

                    result.append('\t');

                } else if (c == '"') {

                    result.append('"');

                } else if (c == '\\') {

                    result.append('\\');

                } else {

                    result.append(c);
                }


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


    /*
     * ============================================================
     * JSON ESCAPE
     * ============================================================
     */

    private String escapeJson(
            String text) {

        return text

                .replace(
                        "\\",
                        "\\\\"
                )

                .replace(
                        "\"",
                        "\\\""
                )

                .replace(
                        "\r",
                        "\\r"
                )

                .replace(
                        "\n",
                        "\\n"
                )

                .replace(
                        "\t",
                        "\\t"
                );
    }


    /*
     * ============================================================
     * NUMBER FORMAT
     * ============================================================
     */

    private String format(
            double value) {

        return String.format(
                "%.2f",
                value
        );
    }
}