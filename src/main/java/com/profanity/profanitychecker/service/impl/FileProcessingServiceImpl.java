package com.profanity.profanitychecker.service.impl;

import com.profanity.profanitychecker.service.FileProcessingService;
import lombok.Setter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class FileProcessingServiceImpl implements FileProcessingService {
    @Override
    public String extractTextFromFile(MultipartFile file) {
        try{
            String fileType = file.getContentType();
            InputStream fileContent = file.getInputStream();

            if ("application/pdf".equals(fileType)){

                PDDocument document = PDDocument.load(fileContent);
                PDFTextStripper pdfTextStripper = new PDFTextStripper();
                String text = pdfTextStripper.getText(document);
                document.close();

                return text;

            } else if ("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(fileType)) {

                XWPFDocument document = new XWPFDocument(fileContent);
                return document.getParagraphs().stream()
                        .map(XWPFParagraph::getText)
                        .reduce("",(acc, text)-> acc + text + "\n");
            }else {
                throw new IllegalArgumentException("Unsupported File Type: "+fileType);
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Failed to extract text from file",e);
        }
    }
}
