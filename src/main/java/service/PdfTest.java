package service;

import java.io.File;

public class PdfTest {

    public static void main(String[] args) {

        try {

            PdfProcessor processor = new PdfProcessor();

            File file = new File(
                "F:/SmartExpenseTracker/test-statement.pdf"
            );

            String text = processor.extractText(file);

            System.out.println("========== PDF TEXT ==========");
            System.out.println(text);
            System.out.println("========== END ==========");

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}