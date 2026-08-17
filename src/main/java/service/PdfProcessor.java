//package service;
//
//import java.io.File;
//import java.io.IOException;
//
//import org.apache.pdfbox.Loader;
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.text.PDFTextStripper;
//
//public class PdfProcessor {
//
//    public String extractText(File file)
//            throws IOException {
//
//        String text;
//
//        try (PDDocument document =
//                Loader.loadPDF(file)) {
//
//            PDFTextStripper stripper =
//                    new PDFTextStripper();
//
//            text =
//                    stripper.getText(document);
//        }
//
//        return text;
//    }
//}

package service;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PdfProcessor {

    public String extractText(File file) throws IOException {

        if (file == null) {
            throw new IOException("PDF file is null.");
        }

        if (!file.exists()) {
            throw new IOException(
                    "PDF file does not exist: "
                    + file.getAbsolutePath()
            );
        }

        if (!file.isFile()) {
            throw new IOException(
                    "Path is not a file: "
                    + file.getAbsolutePath()
            );
        }

        try (PDDocument document =
                     Loader.loadPDF(file)) {

            PDFTextStripper stripper =
                    new PDFTextStripper();

            /*
             * Preserve PDF text order.
             */
            stripper.setSortByPosition(true);

            /*
             * Extract all pages.
             */
            stripper.setStartPage(1);

            stripper.setEndPage(
                    document.getNumberOfPages()
            );

            String text =
                    stripper.getText(document);

            System.out.println(
                    "========== EXTRACTED PDF TEXT =========="
            );

            System.out.println(text);

            System.out.println(
                    "========================================="
            );

            return text;
        }
    }
}