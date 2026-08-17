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
     *
     * User question comes here.
     *
     * The AI decides which financial information is required.
     *
     */
    public String processQuestion(
            int userId,
            String question) throws Exception {

        String lower = question.toLowerCase();

        /*
         * --------------------------------------------------------
         * STEP 1
         * Ask Qwen3 what type of financial information is needed.
         * --------------------------------------------------------
         */

        String intent = detectIntent(question);

        System.out.println(
                "AI detected intent: " + intent
        );


        /*
         * --------------------------------------------------------
         * STEP 2
         * Execute the appropriate tool.
         * --------------------------------------------------------
         */

        String financialData;


        switch (intent) {

            case "TOTAL_INCOME":

                financialData =
                        "Total income: ₹"
                        + format(
                            transactionTool.getTotalIncome(userId)
                        );

                break;


            case "TOTAL_EXPENSE":

                financialData =
                        "Total expense: ₹"
                        + format(
                            transactionTool.getTotalExpense(userId)
                        );

                break;


            case "TRANSACTION_COUNT":

                financialData =
                        "Total transactions: "
                        + transactionTool.getTransactionCount(userId);

                break;


            case "CATEGORY_SPENDING":

                List<Map<String, Object>> categories =
                        transactionTool.getCategoryWiseExpense(
                                userId
                        );

                financialData =
                        categories.toString();

                break;


            case "HIGHEST_EXPENSES":

                List<Map<String, Object>> highest =
                        transactionTool.getHighestExpenses(
                                userId,
                                5
                        );

                financialData =
                        highest.toString();

                break;


            case "RECENT_TRANSACTIONS":

                List<Map<String, Object>> recent =
                        transactionTool.getRecentTransactions(
                                userId,
                                10
                        );

                financialData =
                        recent.toString();

                break;


            case "FINANCIAL_SUMMARY":

                Map<String, Object> summary =
                        transactionTool.getFinancialSummary(
                                userId
                        );

                financialData =
                        summary.toString();

                break;


            default:

                financialData =
                        "No financial database tool was required.";
        }


        /*
         * --------------------------------------------------------
         * STEP 3
         * Give the database information to Qwen3.
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
     *
     * Qwen3 decides which TransactionTool is required.
     *
     */
    private String detectIntent(
            String question) throws Exception {

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
                + "NONE\n\n"

                + "Examples:\n"
                + "How much did I earn -> TOTAL_INCOME\n"
                + "How much did I spend -> TOTAL_EXPENSE\n"
                + "Where do I spend most -> CATEGORY_SPENDING\n"
                + "What are my biggest expenses -> HIGHEST_EXPENSES\n"
                + "Show recent transactions -> RECENT_TRANSACTIONS\n"
                + "How many transactions -> TRANSACTION_COUNT\n"
                + "Give me my financial summary -> FINANCIAL_SUMMARY\n\n"

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
         * Make sure AI output is safe.
         * --------------------------------------------------------
         */

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
            String prompt) throws IOException, InterruptedException {


        String json =
                "{"
                + "\"model\":\"" + MODEL + "\","
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
                            URI.create(OLLAMA_URL)
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
                }

                else if (c == 't') {
                    result.append('\t');
                }

                else if (c == '"') {
                    result.append('"');
                }

                else if (c == '\\') {
                    result.append('\\');
                }

                else {
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
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
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