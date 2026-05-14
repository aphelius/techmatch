package com.techmatch.resume.service.extract;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

@Component
public class PdfResumeTextExtractor implements ResumeTextExtractor {

    @Override
    public boolean supports(String extension) {
        return "pdf".equalsIgnoreCase(extension);
    }

    @Override
    public String extract(byte[] content) {
        try (PDDocument document = Loader.loadPDF(content)) {
            return new PDFTextStripper().getText(document);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to extract PDF text", exception);
        }
    }
}
