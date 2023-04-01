package com.apply.sawit.controller;

import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Controller
public class OCRController {


    @Value("classpath:material/*")
    private Resource[] resources;

    @Value("${transcript.english.target}")
    private String englishTranscriptLocation;

    @Value("${transcript.chinese.target}")
    private String chineseTranscriptLocation;

    ClassLoader classLoader = getClass().getClassLoader();

    @GetMapping(value = { "/ocr" })
    @ResponseBody
    public String readTextImages() throws TesseractException, IOException {
        List<String> fileNames = getFilesInFolder(resources);
        String englishWords = "";
        String chineseWords = "";
        System.out.println("Transcribing documents ...");
        for(String fileName : fileNames){
            englishWords += transcribeDocument("material/"+fileName, "eng");
            chineseWords += transcribeDocument("material/"+fileName, "chi_sim_vert");
        }
        System.out.println("Generating PDF Files");
        generatePdfFile(englishWords, "eng");
        generatePdfFile(chineseWords, "chinese");
        System.out.println("success");
        return "file transcription success";
    }

    private String transcribeDocument(String filePath, String language) throws TesseractException {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(classLoader.getResource("lstm-models").getPath());
        tesseract.setLanguage(language);
        String transcription = tesseract.doOCR(new File(classLoader.getResource(filePath).getFile()));
        return transcription;
    }

    private List<String> getFilesInFolder(Resource[] folderPath){
        System.out.println("Fetching all files ...");
        List<String> fileNames = new ArrayList<>();
        for (Resource res : folderPath){
            fileNames.add(res.getFilename());
        }
        return fileNames;
    }

    private void generatePdfFile(String txt, String language) throws IOException {
        if(language.equals("eng")){
            txt = txt.replaceAll("[^a-zA-Z0-9]", " ");
            String dest = englishTranscriptLocation;
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(dest));
            Document doc = new Document(pdfDoc);
            String[] splitWords = txt.split(" ");
            Paragraph paragraph = new Paragraph();
            for(String word : splitWords){
                if(word.length()>2){
                    paragraph.add(processWords(word));
                }
            }
            doc.add(paragraph);
            doc.close();
        }else{
            String dest = chineseTranscriptLocation;
            String fontLocation = classLoader.getResource("fonts/NotoSansCJKsc-Regular.otf").getPath();
            PdfFont font = PdfFontFactory.createFont(fontLocation, PdfEncodings.IDENTITY_H);

            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(dest));
            Document doc = new Document(pdfDoc);
            doc.setFont(font);
            doc.add(
                    new Paragraph(
                            new Text(txt)
            ));
            doc.close();
        }
    }

    private Text processWords(String word){
        if(word.contains("o") || word.contains("O")){
            return new Text(word+" ")
                    .setFontColor(ColorConstants.BLUE);
        }
        return  new Text(word+" ")
                .setFontColor(ColorConstants.BLACK);
    }

}
