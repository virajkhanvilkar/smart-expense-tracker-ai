package service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.ExtractedTransaction;

/*
 * ============================================================
 * SMART EXPENSE TRACKER + MULTI-BANK STATEMENT PROCESSOR
 * ============================================================
 *
 * Main fixes:
 *
 * 1. Correctly extracts the FIRST money value as transaction
 *    amount and the LAST money value as running balance.
 *
 * 2. Removes both amount and balance from the description,
 *    even when PDFBox places them in the middle of the text.
 *
 * 3. Removes dates, times, cheque/reference numbers and long
 *    UPI references before extracting money values.
 *
 * 4. Detects Canara UPI/DR as EXPENSE and UPI/CR as INCOME.
 *
 * 5. Uses running balance only as a fallback for transaction type.
 *
 * 6. Keeps support for Smart Expense Tracker text format.
 */
public class DocumentProcessor {

    // ============================================================
    // MAIN PROCESS METHOD
    // ============================================================

    public List<ExtractedTransaction> processText(String text) {

        List<ExtractedTransaction> transactions = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            return transactions;
        }

        System.out.println("======================================");
        System.out.println("DocumentProcessor started");
        System.out.println("Original text length: " + text.length());
        System.out.println("======================================");

        text = normalizeText(text);

        // ========================================================
        // STEP 1: SMART EXPENSE TRACKER FORMAT
        // ========================================================

        transactions.addAll(processSmartExpenseFormat(text));

        if (!transactions.isEmpty()) {

            System.out.println(
                    "Smart Expense Tracker format detected."
            );

            System.out.println(
                    "Transactions found: " + transactions.size()
            );

            return transactions;
        }

        // ========================================================
        // STEP 2: BANK STATEMENT
        // ========================================================

        transactions = processBankStatementFormat(text);

        System.out.println(
                "Bank transactions found: " + transactions.size()
        );

        return transactions;
    }

    // ============================================================
    // NORMALIZE PDF TEXT
    // ============================================================

    private String normalizeText(String text) {

        text = text.replace("\r", "");

        // Keep newline and tab, remove other control characters.
        text = text.replaceAll(
                "[\\p{Cntrl}&&[^\r\n\t]]",
                ""
        );

        // Normalize spaces but keep line breaks.
        text = text.replaceAll(
                "[ \t]+",
                " "
        );

        return text;
    }

    // ============================================================
    // SMART EXPENSE TRACKER FORMAT
    // ============================================================

    private List<ExtractedTransaction>
    processSmartExpenseFormat(String text) {

        List<ExtractedTransaction> transactions =
                new ArrayList<>();

        String[] lines = text.split("\\r?\\n");

        /*
         * Example:
         *
         * 2026-08-12 Expense Food Lunch 350.00
         */

        Pattern pattern = Pattern.compile(
                "^(\\d{4}-\\d{2}-\\d{2})\\s+"
                + "(Income|Expense)\\s+"
                + "(\\S+)\\s+"
                + "(.+?)\\s+"
                + "([0-9,]+(?:\\.\\d{1,2})?)$",
                Pattern.CASE_INSENSITIVE
        );

        for (String line : lines) {

            line = line.trim();

            if (line.isEmpty()) {
                continue;
            }

            if (line.equalsIgnoreCase(
                    "Date Type Category Description Amount")) {
                continue;
            }

            if (line.startsWith("Total Income")) {
                continue;
            }

            if (line.startsWith("Total Expense")) {
                continue;
            }

            if (line.startsWith("Balance")) {
                continue;
            }

            Matcher matcher = pattern.matcher(line);

            if (!matcher.matches()) {
                continue;
            }

            try {

                String date = matcher.group(1);

                String type =
                        matcher.group(2).toUpperCase(Locale.ENGLISH);

                String category = matcher.group(3);

                String description = matcher.group(4);

                double amount =
                        parseAmount(matcher.group(5));

                ExtractedTransaction transaction =
                        new ExtractedTransaction();

                transaction.setDate(date);
                transaction.setType(type);
                transaction.setCategory(category);
                transaction.setDescription(description);
                transaction.setAmount(amount);

                transactions.add(transaction);

            } catch (Exception e) {

                System.out.println(
                        "Smart format error: "
                                + e.getMessage()
                );
            }
        }

        return transactions;
    }

    // ============================================================
    // BANK STATEMENT PROCESSOR
    // ============================================================

    private List<ExtractedTransaction>
    processBankStatementFormat(String text) {

        List<ExtractedTransaction> transactions =
                new ArrayList<>();

        double previousBalance =
                extractOpeningBalance(text);

        System.out.println(
                "Opening Balance = " + previousBalance
        );

        // ========================================================
        // SPLIT STATEMENT INTO TRANSACTION BLOCKS
        // ========================================================

        /*
         * Handles:
         *
         * 01-02-2026
         * 01/02/2026
         * 02 Jul 2026
         * 02-Jul-2026
         *
         * Optional leading serial number is also supported.
         */

        Pattern transactionStart = Pattern.compile(
                "(?m)(?=^\\s*(?:\\d+\\s+)?(?:"
                + "\\d{2}[-/]\\d{2}[-/]\\d{4}"
                + "|"
                + "\\d{2}\\s+[A-Za-z]{3}\\s+\\d{4}"
                + "|"
                + "\\d{2}-[A-Za-z]{3}-\\d{4}"
                + ")\\b)"
        );

        String[] blocks = transactionStart.split(text);

        System.out.println(
                "Possible transaction blocks = "
                        + blocks.length
        );

        for (String block : blocks) {

            block = block.trim();

            if (block.isEmpty()) {
                continue;
            }

            if (isBankHeader(block)) {
                continue;
            }

            try {

                ExtractedTransaction transaction =
                        parseBankTransaction(
                                block,
                                previousBalance
                        );

                if (transaction == null) {
                    continue;
                }

                transactions.add(transaction);

                // Update previous balance using the last money
                // value in this transaction.
                double newBalance =
                        extractLastBalance(block);

                if (!Double.isNaN(newBalance)) {
                    previousBalance = newBalance;
                }

                System.out.println(
                        "FOUND: "
                                + transaction.getDate()
                                + " | "
                                + transaction.getType()
                                + " | Amount = "
                                + transaction.getAmount()
                                + " | "
                                + transaction.getDescription()
                );

            } catch (Exception e) {

                System.out.println(
                        "Unable to parse block:"
                );

                System.out.println(block);

                System.out.println(
                        e.getMessage()
                );
            }
        }

        return transactions;
    }

    // ============================================================
    // PARSE ONE BANK TRANSACTION
    // ============================================================

    private ExtractedTransaction
    parseBankTransaction(
            String block,
            double previousBalance) {

        // --------------------------------------------------------
        // DATE
        // --------------------------------------------------------

        String date = extractDate(block);

        if (date == null) {
            return null;
        }

        // --------------------------------------------------------
        // MONEY VALUES
        // --------------------------------------------------------

        List<Double> numbers =
                extractMoneyValues(block);

        /*
         * Canara statement example:
         *
         * 50.00 9,407.00
         *
         * 50.00    = transaction amount
         * 9,407.00 = running balance
         *
         * We intentionally require at least two money values.
         */

        if (numbers.size() < 2) {

            System.out.println(
                    "No amount/balance found:"
            );

            System.out.println(block);

            return null;
        }

        /*
         * IMPORTANT:
         *
         * Last money value = balance
         * Previous money value = transaction amount
         *
         * This is safer than taking a random number from the
         * description/reference text.
         */

        double currentBalance =
                numbers.get(numbers.size() - 1);

        double amount =
                numbers.get(numbers.size() - 2);

        System.out.println(
                "Money values = " + numbers
        );

        System.out.println(
                "Transaction amount = " + amount
        );

        System.out.println(
                "Current balance = " + currentBalance
        );

        // --------------------------------------------------------
        // DESCRIPTION
        // --------------------------------------------------------

        String description =
                extractDescription(
                        block,
                        amount,
                        currentBalance
                );

        // --------------------------------------------------------
        // TYPE
        // --------------------------------------------------------

        String type =
                detectTransactionType(
                        block,
                        previousBalance,
                        amount,
                        currentBalance
                );

        // --------------------------------------------------------
        // CATEGORY
        // --------------------------------------------------------

        String category =
                detectCategory(description);

        // --------------------------------------------------------
        // CREATE OBJECT
        // --------------------------------------------------------

        ExtractedTransaction transaction =
                new ExtractedTransaction();

        transaction.setDate(date);
        transaction.setType(type);
        transaction.setCategory(category);
        transaction.setDescription(description);
        transaction.setAmount(amount);

        return transaction;
    }

    // ============================================================
    // EXTRACT DATE
    // ============================================================

    private String extractDate(String block) {

        Pattern pattern = Pattern.compile(
                "^\\s*(?:\\d+\\s+)?("
                + "\\d{2}[-/]\\d{2}[-/]\\d{4}"
                + "|"
                + "\\d{2}\\s+[A-Za-z]{3}\\s+\\d{4}"
                + "|"
                + "\\d{2}-[A-Za-z]{3}-\\d{4}"
                + ")"
        );

        Matcher matcher = pattern.matcher(block);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    // ============================================================
    // EXTRACT DESCRIPTION
    // ============================================================

    private String extractDescription(
            String block,
            double amount,
            double balance) {

        String description = block;

        // --------------------------------------------------------
        // Remove optional serial number + date from beginning.
        // --------------------------------------------------------

        description = description.replaceFirst(
                "^\\s*(?:\\d+\\s+)?(?:"
                + "\\d{2}[-/]\\d{2}[-/]\\d{4}"
                + "|"
                + "\\d{2}\\s+[A-Za-z]{3}\\s+\\d{4}"
                + "|"
                + "\\d{2}-[A-Za-z]{3}-\\d{4}"
                + ")",
                ""
        );

        /*
         * --------------------------------------------------------
         * Remove the transaction amount + balance ANYWHERE in
         * the block.
         *
         * Old code only removed them when they were at the END.
         * PDFBox can place Canara statement columns like this:
         *
         * description ... 50.00 9,407.00 20:24:23 ...
         *
         * Therefore we remove the exact pair wherever it occurs.
         * --------------------------------------------------------
         */

        String amountText =
                formatMoneyForRegex(amount);

        String balanceText =
                formatMoneyForRegex(balance);

        String moneyPairRegex =
                "(?<![A-Za-z0-9])"
                + Pattern.quote(amountText)
                + "\\s+"
                + Pattern.quote(balanceText)
                + "(?![A-Za-z0-9])";

        description = description.replaceFirst(
                moneyPairRegex,
                " "
        );

        /*
         * Sometimes the PDF contains the same values without
         * comma formatting. Try the plain numeric representation
         * as a second pass.
         */

        String amountPlain =
                String.format(Locale.ENGLISH, "%.2f", amount);

        String balancePlain =
                String.format(Locale.ENGLISH, "%.2f", balance);

        String plainPairRegex =
                "(?<![A-Za-z0-9])"
                + Pattern.quote(amountPlain)
                + "\\s+"
                + Pattern.quote(balancePlain)
                + "(?![A-Za-z0-9])";

        description = description.replaceFirst(
                plainPairRegex,
                " "
        );

        // --------------------------------------------------------
        // Remove time values.
        // --------------------------------------------------------

        description = description.replaceAll(
                "\\b\\d{1,2}:\\d{2}:\\d{2}\\b",
                " "
        );

        // --------------------------------------------------------
        // Remove cheque number.
        // --------------------------------------------------------

        description = description.replaceAll(
                "(?i)\\bChq:\\s*\\d+\\b",
                " "
        );

        // --------------------------------------------------------
        // Remove common statement header text.
        // --------------------------------------------------------

        description = description.replaceAll(
                "(?i)\\bDate\\s+Particulars\\s+Deposits\\s+"
                + "Withdrawals\\s+Balance\\b",
                " "
        );

        // --------------------------------------------------------
        // Remove page text.
        // --------------------------------------------------------

        description = description.replaceAll(
                "(?i)\\bpage\\s+\\d+\\b",
                " "
        );

        // --------------------------------------------------------
        // Remove remaining date/time variants.
        // --------------------------------------------------------

        description = description.replaceAll(
                "\\b\\d{2}[-/]\\d{2}[-/]\\d{4}\\b",
                " "
        );

        description = description.replaceAll(
                "\\b\\d{2}\\s+[A-Za-z]{3}\\s+\\d{4}\\b",
                " "
        );

        description = description.replaceAll(
                "\\b\\d{2}-[A-Za-z]{3}-\\d{4}\\b",
                " "
        );

        // --------------------------------------------------------
        // Convert line breaks into spaces.
        // --------------------------------------------------------

        description = description.replace(
                "\n",
                " "
        );

        // --------------------------------------------------------
        // Remove repeated spaces.
        // --------------------------------------------------------

        description = description.replaceAll(
                "\\s+",
                " "
        ).trim();

        // --------------------------------------------------------
        // Remove trailing UPI reference.
        // --------------------------------------------------------

        description = description.replaceAll(
                "(?i)\\s+UPI[-/]\\d+$",
                ""
        ).trim();

        /*
         * If only statement column headings remain, return a
         * generic description instead of storing the header.
         */

        if (description.equalsIgnoreCase("Date")
                || description.equalsIgnoreCase("Particulars")
                || description.equalsIgnoreCase(
                        "Date Particulars")
                || description.isEmpty()) {

            description = "Bank Transaction";
        }

        return description;
    }

    // ============================================================
    // TRANSACTION TYPE
    // ============================================================

    private String detectTransactionType(
            String block,
            double previousBalance,
            double amount,
            double currentBalance) {

        String text =
                block.toUpperCase(Locale.ENGLISH);

        // --------------------------------------------------------
        // UPI DEBIT
        // --------------------------------------------------------

        if (text.contains("UPI/DR/")
                || text.contains("UPI/DR ")
                || text.contains("UPI-DR")
                || text.contains("UPI / DR")) {

            return "EXPENSE";
        }

        // --------------------------------------------------------
        // UPI CREDIT
        // --------------------------------------------------------

        if (text.contains("UPI/CR/")
                || text.contains("UPI/CR ")
                || text.contains("UPI-CR")
                || text.contains("UPI / CR")) {

            return "INCOME";
        }

        // --------------------------------------------------------
        // ATM
        // --------------------------------------------------------

        if (text.contains("ATM CASH")
                || text.contains("ATM/")
                || text.contains("CASH WITHDRAWAL")
                || text.contains("ATM WITHDRAWAL")) {

            return "EXPENSE";
        }

        // --------------------------------------------------------
        // CASH DEPOSIT
        // --------------------------------------------------------

        if (text.contains("CASH-BNA")
                || text.contains("CASH DEPOSIT")
                || text.contains("CASH DEP")) {

            return "INCOME";
        }

        // --------------------------------------------------------
        // NEFT
        // --------------------------------------------------------

        if (text.contains("NEFT/CR")
                || text.contains("NEFT CR")
                || text.contains("NEFT / CR")) {

            return "INCOME";
        }

        if (text.contains("NEFT/DR")
                || text.contains("NEFT DR")
                || text.contains("NEFT / DR")) {

            return "EXPENSE";
        }

        // --------------------------------------------------------
        // IMPS
        // --------------------------------------------------------

        if (text.contains("IMPS/CR")
                || text.contains("IMPS CR")
                || text.contains("IMPS / CR")) {

            return "INCOME";
        }

        if (text.contains("IMPS/DR")
                || text.contains("IMPS DR")
                || text.contains("IMPS / DR")) {

            return "EXPENSE";
        }

        // --------------------------------------------------------
        // RTGS
        // --------------------------------------------------------

        if (text.contains("RTGS/CR")
                || text.contains("RTGS CR")
                || text.contains("RTGS / CR")) {

            return "INCOME";
        }

        if (text.contains("RTGS/DR")
                || text.contains("RTGS DR")
                || text.contains("RTGS / DR")) {

            return "EXPENSE";
        }

        // --------------------------------------------------------
        // SALARY
        // --------------------------------------------------------

        if (text.contains("SALARY")
                || text.contains("PAYROLL")) {

            return "INCOME";
        }

        // --------------------------------------------------------
        // REVERSAL
        // --------------------------------------------------------

        if (text.contains("REVERSAL")
                || text.contains("UPI/REV")
                || text.contains("REV/")) {

            if (currentBalance > previousBalance) {
                return "INCOME";
            }

            return "EXPENSE";
        }

        // --------------------------------------------------------
        // BALANCE BASED FALLBACK
        // --------------------------------------------------------

        if (previousBalance > 0) {

            // Expense:
            // Previous - amount = Current

            if (Math.abs(
                    (previousBalance - amount)
                            - currentBalance
            ) < 0.05) {

                return "EXPENSE";
            }

            // Income:
            // Previous + amount = Current

            if (Math.abs(
                    (previousBalance + amount)
                            - currentBalance
            ) < 0.05) {

                return "INCOME";
            }
        }

        // --------------------------------------------------------
        // GENERIC KEYWORD FALLBACK
        // --------------------------------------------------------

        if (text.contains("PAYMENT")
                || text.contains("PURCHASE")
                || text.contains("WITHDRAW")
                || text.contains("DEBIT")) {

            return "EXPENSE";
        }

        if (text.contains("DEPOSIT")
                || text.contains("CREDIT")
                || text.contains("RECEIVED")
                || text.contains("TRANSFER FROM")) {

            return "INCOME";
        }

        // Safe default.
        return "EXPENSE";
    }

    // ============================================================
    // MONEY EXTRACTION
    // ============================================================

    /*
     * Bank statement money usually appears as:
     *
     * 50.00
     * 500.00
     * 1,200.00
     * 9,407.00
     *
     * We intentionally require a decimal part.
     *
     * This prevents random values such as:
     *
     * 1
     * 24
     * 52
     * 141
     * 286114435989
     *
     * from being treated as money.
     */

    private List<Double>
    extractMoneyValues(String block) {

        List<Double> values =
                new ArrayList<>();

        if (block == null
                || block.trim().isEmpty()) {

            return values;
        }

        String cleaned = block;

        // --------------------------------------------------------
        // STEP 1: Remove dates
        // --------------------------------------------------------

        cleaned = cleaned.replaceAll(
                "\\b\\d{2}[-/]\\d{2}[-/]\\d{4}\\b",
                " "
        );

        cleaned = cleaned.replaceAll(
                "\\b\\d{2}\\s+[A-Za-z]{3}\\s+\\d{4}\\b",
                " "
        );

        cleaned = cleaned.replaceAll(
                "\\b\\d{2}-[A-Za-z]{3}-\\d{4}\\b",
                " "
        );

        // --------------------------------------------------------
        // STEP 2: Remove time values
        // --------------------------------------------------------

        cleaned = cleaned.replaceAll(
                "\\b\\d{1,2}:\\d{2}:\\d{2}\\b",
                " "
        );

        // --------------------------------------------------------
        // STEP 3: Remove cheque numbers
        // --------------------------------------------------------

        cleaned = cleaned.replaceAll(
                "(?i)\\bChq:\\s*\\d+\\b",
                " "
        );

        // --------------------------------------------------------
        // STEP 4: Remove long numeric references
        // --------------------------------------------------------

        cleaned = cleaned.replaceAll(
                "(?<![A-Za-z0-9])\\d{8,}(?![A-Za-z0-9])",
                " "
        );

        /*
         * --------------------------------------------------------
         * STEP 5: Extract decimal money values.
         *
         * Supports:
         *
         * 50.00
         * 500.00
         * 1,200.00
         * 9,407.00
         * -500.00
         *
         * Also supports 1,23,456.00 style grouping.
         * --------------------------------------------------------
         */

        Pattern decimalMoneyPattern =
                Pattern.compile(
                        "(?<![A-Za-z0-9])"
                        + "-?"
                        + "(?:"
                        + "\\d{1,3}(?:,\\d{2,3})+"
                        + "|"
                        + "\\d+"
                        + ")"
                        + "\\.\\d{1,2}"
                        + "(?![A-Za-z0-9])"
                );

        Matcher matcher =
                decimalMoneyPattern.matcher(cleaned);

        while (matcher.find()) {

            String value = matcher.group();

            try {

                double number =
                        Double.parseDouble(
                                value.replace(",", "").trim()
                        );

                values.add(number);

            } catch (Exception ignored) {
                // Ignore invalid values.
            }
        }

        System.out.println(
                "Money values found = " + values
        );

        return values;
    }

    // ============================================================
    // FORMAT MONEY FOR REGEX
    // ============================================================

    private String formatMoneyForRegex(double value) {

        /*
         * Use two decimal places because bank statements normally
         * contain values such as 50.00 and 9,407.00.
         */

        return String.format(
                Locale.ENGLISH,
                "%.2f",
                value
        );
    }

    // ============================================================
    // EXTRACT LAST BALANCE
    // ============================================================

    private double extractLastBalance(
            String block) {

        List<Double> values =
                extractMoneyValues(block);

        if (values.size() < 1) {
            return Double.NaN;
        }

        return values.get(
                values.size() - 1
        );
    }

    // ============================================================
    // OPENING BALANCE
    // ============================================================

    private double extractOpeningBalance(
            String text) {

        // Opening Balance 9,457.00

        Pattern pattern = Pattern.compile(
                "(?i)"
                + "Opening\\s+Balance"
                + "\\s*[:=-]?\\s*"
                + "([0-9,]+(?:\\.\\d{1,2})?)"
        );

        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {

            return parseAmount(
                    matcher.group(1)
            );
        }

        // Other possible format:
        // Opening Bal : 9,457.00

        pattern = Pattern.compile(
                "(?i)"
                + "Opening\\s+Bal"
                + "\\s*[:=-]?\\s*"
                + "([0-9,]+(?:\\.\\d{1,2})?)"
        );

        matcher = pattern.matcher(text);

        if (matcher.find()) {

            return parseAmount(
                    matcher.group(1)
            );
        }

        return 0;
    }

    // ============================================================
    // CATEGORY DETECTION
    // ============================================================

    private String detectCategory(
            String description) {

        String text =
                description.toLowerCase(Locale.ENGLISH);

        // FOOD

        if (containsAny(
                text,
                "food",
                "restaurant",
                "hotel",
                "cafe",
                "coffee",
                "pizza",
                "burger",
                "biryani",
                "swiggy",
                "zomato",
                "dominos",
                "mcdonald",
                "kfc",
                "roll",
                "dhaba",
                "mess",
                "canteen",
                "bakery"
        )) {

            return "Food";
        }

        // TRANSPORT

        if (containsAny(
                text,
                "petrol",
                "fuel",
                "hpcl",
                "bpcl",
                "indian oil",
                "iocl",
                "uber",
                "ola",
                "rapido",
                "metro",
                "bus",
                "irctc",
                "railway",
                "transport",
                "parking",
                "toll",
                "fastag"
        )) {

            return "Transport";
        }

        // MEDICAL

        if (containsAny(
                text,
                "medical",
                "hospital",
                "pharmacy",
                "chemist",
                "apollo",
                "medplus",
                "doctor",
                "clinic",
                "diagnostic",
                "medicine"
        )) {

            return "Medical";
        }

        // SHOPPING

        if (containsAny(
                text,
                "shopping",
                "mart",
                "store",
                "mall",
                "amazon",
                "flipkart",
                "myntra",
                "reliance",
                "dmart",
                "big bazaar",
                "supermarket"
        )) {

            return "Shopping";
        }

        // BILLS / RECHARGE

        if (containsAny(
                text,
                "recharge",
                "jio",
                "airtel",
                " vi ",
                "vodafone",
                "bsnl",
                "electricity",
                "light bill",
                "water bill",
                "gas bill",
                "mobile bill",
                "broadband",
                "internet"
        )) {

            return "Bills & Recharge";
        }

        // EDUCATION

        if (containsAny(
                text,
                "college",
                "university",
                "school",
                "education",
                "course",
                "udemy",
                "coursera",
                "exam",
                "fees",
                "fee"
        )) {

            return "Education";
        }

        // ENTERTAINMENT

        if (containsAny(
                text,
                "movie",
                "cinema",
                "bookmyshow",
                "netflix",
                "spotify",
                "youtube",
                "prime video",
                "game",
                "gaming"
        )) {

            return "Entertainment";
        }

        // BANK TRANSFER

        if (containsAny(
                text,
                "neft",
                "imps",
                "rtgs",
                "bank transfer"
        )) {

            return "Bank Transfer";
        }

        // ATM

        if (containsAny(
                text,
                "atm cash",
                "atm withdrawal",
                "cash withdrawal"
        )) {

            return "Cash Withdrawal";
        }

        // SALARY

        if (containsAny(
                text,
                "salary",
                "payroll"
        )) {

            return "Salary";
        }

        // CASH DEPOSIT

        if (containsAny(
                text,
                "cash deposit",
                "cash-bna",
                "cash dep"
        )) {

            return "Cash Deposit";
        }

        // UPI

        if (text.contains("upi")) {
            return "UPI";
        }

        return "Other";
    }

    // ============================================================
    // CHECK MULTIPLE KEYWORDS
    // ============================================================

    private boolean containsAny(
            String text,
            String... keywords) {

        for (String keyword : keywords) {

            if (text.contains(
                    keyword.toLowerCase(Locale.ENGLISH)
            )) {

                return true;
            }
        }

        return false;
    }

    // ============================================================
    // PARSE AMOUNT
    // ============================================================

    private double parseAmount(
            String text) {

        if (text == null
                || text.trim().isEmpty()) {

            return 0;
        }

        try {

            return Double.parseDouble(
                    text.replace(",", "").trim()
            );

        } catch (Exception e) {

            return 0;
        }
    }

    // ============================================================
    // BANK HEADER DETECTION
    // ============================================================

    private boolean isBankHeader(
            String block) {

        String lower =
                block.toLowerCase(Locale.ENGLISH);

        if (lower.contains("statement for a/c")) {
            return true;
        }

        if (lower.contains("customer id")) {
            return true;
        }

        if (lower.contains("branch code")) {
            return true;
        }

        if (lower.contains("ifsc code")) {
            return true;
        }

        /*
         * Prevent the column header from becoming a transaction
         * if it appears as a block.
         */

        if (lower.contains("date particulars deposits")
                && lower.contains("withdrawals balance")) {

            return true;
        }

        return false;
    }
}
