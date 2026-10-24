package service;

import model.ExtractedTransaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts transactions from Smart Expense Tracker exports and text-based bank
 * statements. Opening balance is detected dynamically from the statement text.
 * Scanned/image-only PDFs require OCR before this class can parse them.
 */
public class DocumentProcessor {

    /** Extracts the opening balance dynamically for the servlet/JSP. */
    public double getOpeningBalance(String text) {
        return extractOpeningBalance(text);
    }

    private static final Pattern SMART_FORMAT = Pattern.compile(
        "^(\\d{4}-\\d{2}-\\d{2})\\s+(Income|Expense)\\s+(\\S+)\\s+(.+?)\\s+([0-9,]+(?:\\.\\d{1,2})?)$",
        Pattern.CASE_INSENSITIVE
    );

    private static final String DATE_PATTERN =
        "(?:\\d{2}[-/]\\d{2}[-/]\\d{4}|\\d{2}\\s+[A-Za-z]{3}\\s+\\d{4}|\\d{2}-[A-Za-z]{3}-\\d{4})";

    private static final Pattern DATE_AT_LINE_START = Pattern.compile(
        "^\\s*(?:(\\d{1,4})\\s+)?(" + DATE_PATTERN + ")\\b"
    );

    private static final Pattern MONEY = Pattern.compile(
        "(?<![A-Za-z0-9])(-?(?:\\d{1,3}(?:,\\d{2,3})+|\\d+))\\.(\\d{1,2})(?![A-Za-z0-9])"
    );

    public List<ExtractedTransaction> processText(String text) {
        List<ExtractedTransaction> transactions = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) return transactions;

        String normalized = normalizeText(text);
        transactions.addAll(processSmartExpenseFormat(normalized));
        if (!transactions.isEmpty()) return transactions;

        return processBankStatementFormat(normalized);
    }

    private String normalizeText(String text) {
        return text.replace('\u00A0', ' ')
                   .replace("\r", "")
                   .replaceAll("[\\p{Cntrl}&&[^\\n\\t]]", "")
                   .replaceAll("[\\t ]+", " ");
    }

    private List<ExtractedTransaction> processSmartExpenseFormat(String text) {
        List<ExtractedTransaction> result = new ArrayList<>();
        for (String rawLine : text.split("\\n")) {
            Matcher m = SMART_FORMAT.matcher(rawLine.trim());
            if (!m.matches()) continue;

            ExtractedTransaction tx = new ExtractedTransaction();
            tx.setDate(m.group(1));
            tx.setType(m.group(2).equalsIgnoreCase("income") ? "INCOME" : "EXPENSE");
            tx.setCategory(m.group(3));
            tx.setDescription(m.group(4).trim());
            tx.setAmount(parseAmount(m.group(5)));
            result.add(tx);
        }
        return result;
    }

    private List<ExtractedTransaction> processBankStatementFormat(String text) {
        List<ExtractedTransaction> result = new ArrayList<>();
        String[] lines = text.split("\\n");
        double previousBalance = extractOpeningBalance(text);
        System.out.println("Detected statement opening balance: " +
                (Double.isFinite(previousBalance) ? String.format(Locale.ROOT, "%.2f", previousBalance) : "NOT FOUND"));

        StringBuilder block = new StringBuilder();
        boolean inTransactionSection = false;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;
            String lower = line.toLowerCase(Locale.ROOT);

            if (lower.contains("savings account transactions") || lower.contains("account transactions")) {
                inTransactionSection = true;
                continue;
            }

            if (inTransactionSection && (lower.contains("account summary")
                    || lower.contains("end of statement")
                    || lower.contains("statement generated on")
                    || lower.contains("important information")
                    || lower.contains("commonly used narrations"))) {
                if (block.length() > 0) {
                    double newBalance = appendParsedBlock(result, block.toString(), previousBalance);
                    if (Double.isFinite(newBalance)) previousBalance = newBalance;
                    block.setLength(0);
                }
                inTransactionSection = false;
                continue;
            }

            if (!inTransactionSection || isTableHeader(line) || isBoilerplateLine(line)) continue;

            Matcher start = DATE_AT_LINE_START.matcher(line);
            if (start.find()) {
                if (block.length() > 0) {
                    double newBalance = appendParsedBlock(result, block.toString(), previousBalance);
                    if (Double.isFinite(newBalance)) previousBalance = newBalance;
                }
                block.setLength(0);
                block.append(line);
            } else if (block.length() > 0 && !isBoilerplateLine(line)) {
                block.append(' ').append(line);
            }
        }

        if (block.length() > 0) {
            appendParsedBlock(result, block.toString(), previousBalance);
        }
        return result;
    }

    private double appendParsedBlock(List<ExtractedTransaction> output, String block, double previousBalance) {
        ParsedBankTransaction parsed = parseBankBlock(block, previousBalance);
        if (parsed == null) return Double.NaN;
        output.add(parsed.transaction);
        return parsed.balance;
    }

    private ParsedBankTransaction parseBankBlock(String block, double previousBalance) {
        Matcher dateMatcher = DATE_AT_LINE_START.matcher(block);
        if (!dateMatcher.find() || isBoilerplateLine(block)) return null;

        String rawDate = dateMatcher.group(2);
        String date = normalizeDate(rawDate);
        String upper = block.toUpperCase(Locale.ROOT);
        List<Double> values = extractMoneyValues(block);
        if (values.isEmpty()) return null;

        /*
         * In the Kotak statement, the final money value in each transaction
         * block is the running balance. When a previous balance is available,
         * infer the amount and direction from the change in balance.
         */
        double currentBalance = values.get(values.size() - 1);
        double delta = Double.isFinite(previousBalance)
                ? round2(currentBalance - previousBalance) : Double.NaN;
        double amount;
        String type;

        if (Double.isFinite(delta) && Math.abs(delta) >= 0.005) {
            amount = Math.abs(delta);
            type = delta > 0 ? "INCOME" : "EXPENSE";
        } else {
            // Fallback for a statement where a previous/current balance is absent.
            if (values.size() < 2) return null;
            amount = values.get(values.size() - 2);
            type = detectTypeFromKeywords(block);
            if ("UNKNOWN".equals(type)) return null;
        }

        if (upper.contains("INT.PD") || upper.contains("INTEREST CREDITED")) type = "INCOME";

        String description = cleanDescription(block, rawDate);
        if (description.isEmpty()) description = "Bank Transaction";

        ExtractedTransaction tx = new ExtractedTransaction();
        tx.setDate(date);
        tx.setType(type);
        tx.setDescription(description);
        tx.setCategory(detectCategory(description, type));
        tx.setAmount(round2(amount));
        tx.setBalance(round2(currentBalance));

        ParsedBankTransaction parsed = new ParsedBankTransaction();
        parsed.transaction = tx;
        parsed.balance = currentBalance;
        return parsed;
    }

    private static class ParsedBankTransaction {
        ExtractedTransaction transaction;
        double balance;
    }

    private String normalizeDate(String raw) {
        String date = raw.trim().replaceAll("\\s+", " ");
        Matcher m = Pattern.compile("^(\\d{2})\\s+([A-Za-z]{3})\\s+(\\d{4})$").matcher(date);
        if (m.matches()) return m.group(1) + " " + capitalize(m.group(2)) + " " + m.group(3);
        return date;
    }

    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1).toLowerCase(Locale.ROOT);
    }

    /**
     * Reads the opening balance from the actual statement text. Handles spaces,
     * line breaks, and hyphen placeholders between the label and amount. It does
     * not hard-code any account balance.
     */
    private double extractOpeningBalance(String text) {
        if (text == null || text.trim().isEmpty()) return Double.NaN;

        String normalized = text.replace('\u00A0', ' ').replace("\r", "");
        String[] lines = normalized.split("\n");

        // First preference: the exact row containing the Opening Balance label.
        // Kotak statements commonly render this as: - - Opening Balance - - - 641.11
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.toLowerCase(Locale.ROOT).contains("opening balance")) {
                List<Double> values = extractMoneyValues(line);
                if (!values.isEmpty()) {
                    double value = values.get(values.size() - 1);
                    System.out.println("Detected statement opening balance: "
                            + String.format(Locale.ROOT, "%.2f", value));
                    return round2(value);
                }

                // PDFBox may put the label and amount on adjacent lines.
                StringBuilder nearby = new StringBuilder(line);
                for (int j = i + 1; j < Math.min(lines.length, i + 4); j++) {
                    nearby.append(' ').append(lines[j].trim());
                    List<Double> nearbyValues = extractMoneyValues(nearby.toString());
                    if (!nearbyValues.isEmpty()) {
                        double value = nearbyValues.get(nearbyValues.size() - 1);
                        System.out.println("Detected statement opening balance (wrapped row): "
                                + String.format(Locale.ROOT, "%.2f", value));
                        return round2(value);
                    }
                }
            }
        }

        // Fallback: label and amount may be separated by whitespace/column text.
        Pattern fallback = Pattern.compile(
                "(?is)Opening\\s+Balance\\b.{0,350}?"
                + "(?<![A-Za-z0-9])([0-9]{1,3}(?:,[0-9]{3})*\\.[0-9]{2}|[0-9]+\\.[0-9]{2})(?![A-Za-z0-9])");
        Matcher matcher = fallback.matcher(normalized);
        if (matcher.find()) {
            double value = parseAmount(matcher.group(1));
            System.out.println("Detected statement opening balance (fallback): "
                    + String.format(Locale.ROOT, "%.2f", value));
            return round2(value);
        }

        System.err.println("Opening Balance label/amount was not found in extracted PDF text.");
        return Double.NaN;
    }

    private List<Double> extractMoneyValues(String text) {
        List<Double> values = new ArrayList<>();
        String cleaned = removeDates(text)
                .replaceAll("(?i)\\bUPI[-/]\\d+\\b", " ")
                .replaceAll("(?<![A-Za-z0-9])\\d{8,}(?![A-Za-z0-9])", " ");
        Matcher m = MONEY.matcher(cleaned);
        while (m.find()) values.add(parseAmount(m.group()));
        return values;
    }

    private String cleanDescription(String block, String rawDate) {
        String description = block.replaceFirst(
                "^\\s*(?:\\d{1,4}\\s+)?" + Pattern.quote(rawDate) + "\\s*", "");
        description = description.replaceAll(
                "(?<![A-Za-z0-9])-?(?:\\d{1,3}(?:,\\d{2,3})+|\\d+)\\.\\d{1,2}(?![A-Za-z0-9])", " ");
        description = removeDates(description);
        description = description.replaceAll("(?i)\\bUPI[-/]\\d{6,}\\b", " ");
        description = description.replaceAll("(?<![A-Za-z0-9])\\d{8,}(?![A-Za-z0-9])", " ");
        description = description.replaceAll(
                "(?i)\\b(Withdrawal \\(Dr\\.\\)|Deposit \\(Cr\\.\\)|Balance|Description|Chq/Ref\\. No\\.)\\b", " ");
        description = description.replaceAll("(?i)\\bNO\\s+REMARK\\b", " ");
        description = description.replaceAll("(?i)\\b(Statement Generated on|Account Statement|Savings Account Transactions|Page)\\b.*$", " ");
        return description.replaceAll("\\s+", " ")
                .replaceAll("^[\\s/|,-]+|[\\s/|,-]+$", "").trim();
    }

    private String removeDates(String text) {
        return text.replaceAll("\\b\\d{2}[-/]\\d{2}[-/]\\d{4}\\b", " ")
                   .replaceAll("\\b\\d{2}\\s+[A-Za-z]{3}\\s+\\d{4}\\b", " ")
                   .replaceAll("\\b\\d{2}-[A-Za-z]{3}-\\d{4}\\b", " ");
    }

    private String detectTypeFromKeywords(String block) {
        String t = block.toUpperCase(Locale.ROOT);
        if (t.contains("INT.PD") || t.contains("INTEREST CREDITED") || t.contains("NEFTINW")
                || t.contains("PAYMENT FROM") || t.contains("DEPOSIT")) return "INCOME";
        if (t.contains("WITHDRAWAL") || t.contains("CHRG:") || t.contains("FEE FOR THE MONTH")) return "EXPENSE";
        return "UNKNOWN";
    }

    private String detectCategory(String description, String type) {
        String t = description.toLowerCase(Locale.ROOT);
        if (containsAny(t, "int.pd", "interest credited", "interest")) return "Interest Income";
        if (containsAny(t, "cafe", "misal", "bhel", "restaurant", "food", "pizza", "burger", "biryani", "swiggy", "zomato", "hotel", "bakery", "mess", "tea stall")) return "Food";
        if (containsAny(t, "medical", "hospital", "pharmacy", "chemist", "doctor", "clinic", "medicine", "diagnostic")) return "Medical";
        if (containsAny(t, "makemytrip", "redbus", "irctc", "uber", "ola", "rapido", "petrol", "fuel", "metro", "railway", "transport", "parking", "fastag", "pmpml")) return "Transport";
        if (containsAny(t, "amazon", "flipkart", "myntra", "dmart", "supermarket", "shopping", "mall", "store")) return "Shopping";
        if (containsAny(t, "recharge", "airtel", "jio", "vodafone", "electricity", "light bill", "water bill", "gas bill", "broadband", "internet", "sms alert fee", "chrg:")) return "Bills & Charges";
        if (containsAny(t, "college", "university", "school", "education", "course", "udemy", "coursera", "exam", "fees", "fee")) return "Education";
        if (containsAny(t, "salary", "payroll")) return "Salary";
        if (containsAny(t, "atm", "cash withdrawal")) return "Cash Withdrawal";
        if (containsAny(t, "neft", "imps", "rtgs", "bank transfer")) return "Bank Transfer";
        if (containsAny(t, "rent")) return "Rent";
        return "INCOME".equals(type) ? "Other Income" : "Uncategorized";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase(Locale.ROOT))) return true;
        }
        return false;
    }

    private boolean isTableHeader(String line) {
        String t = line.toLowerCase(Locale.ROOT);
        return t.matches("#\\s+date\\s+description.*")
                || (t.contains("withdrawal (dr.)") && t.contains("deposit (cr.)"))
                || t.equals("-") || t.equals("- -");
    }

    private boolean isBoilerplateLine(String line) {
        String t = line.toLowerCase(Locale.ROOT).trim();
        return t.isEmpty()
                || t.contains("statement generated on")
                || t.contains("account statement")
                || t.contains("savings account transactions")
                || t.matches("page\\s+\\d+")
                || t.contains("withdrawal (dr.)")
                || t.contains("deposit (cr.)")
                || t.contains("chq/ref.");
    }

    private double parseAmount(String value) {
        if (value == null || value.trim().isEmpty()) return 0.0;
        try {
            return new BigDecimal(value.replace(",", "").trim())
                    .setScale(2, RoundingMode.HALF_UP).doubleValue();
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
