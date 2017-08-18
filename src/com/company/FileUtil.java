package com.company;

/**
 * Created by neal1 on 2017/8/17.
 */

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.xmlbeans.XmlException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class FileUtil {

    public static List<FileBean> getFolderFiles(String folder) throws Exception {
        List<FileBean> fileBeans = new LinkedList<FileBean>();
        File file = new File(folder);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File file1 : files) {
                    fileBeans.addAll(getFolderFiles(file1.getAbsolutePath()));
                }
            }
        } else {
            FileBean bean = new FileBean();
            String filePath = file.getAbsolutePath();
            bean.setPath(filePath);
            bean.setModified(file.lastModified());
            String content = "";
            if (filePath.endsWith(".doc") || filePath.endsWith(".docx")) {
                content = readDoc(file);
            } else if (filePath.endsWith(".xls") || filePath.endsWith(".xlsx")) {
                content = readExcel(file);
            } else if (filePath.endsWith(".pdf")) {
                content = readPdf(file);
            } else {
                content = new String(Files.readAllBytes(Paths.get(folder)));
            }
            bean.setContent(content);
            fileBeans.add(bean);
        }
        return fileBeans;
    }

    public static String readExcel(File file) throws Exception {
        String filePath = file.getAbsolutePath();
        StringBuffer content = new StringBuffer("");
        if (filePath.endsWith(".xls")) {
            InputStream inp = new FileInputStream(filePath);
            Workbook wb = new HSSFWorkbook(inp);
            Sheet sheet = wb.getSheetAt(0);
            for (int i = sheet.getFirstRowNum(); i <= sheet.getPhysicalNumberOfRows(); i ++) {
                HSSFRow row = (HSSFRow) sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                for (int j = row.getFirstCellNum(); j <= row.getPhysicalNumberOfCells(); j ++) {
                    if (j < 0) {
                        continue;
                    }
                    HSSFCell cell = row.getCell(j);
                    if (cell == null) {
                        continue;
                    }
                    content.append(cell.getStringCellValue());
                }
            }
            wb.close();
            inp.close();
        } else {
            XSSFWorkbook xwb = new XSSFWorkbook(file.getAbsolutePath());
            XSSFSheet sheet = xwb.getSheetAt(0);
            XSSFRow row;
            String cell;
            for (int i = sheet.getFirstRowNum(); i < sheet.getPhysicalNumberOfRows(); i ++) {
                row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                for (int j = row.getFirstCellNum(); j < row.getPhysicalNumberOfCells(); j ++) {
                    if (j < 0) {
                        continue;
                    }
                    XSSFCell xsfcell = row.getCell(j);
                    if (xsfcell == null) {
                        continue;
                    }
                    cell = xsfcell.getStringCellValue();
                    content.append(cell + "");
                }
            }
        }
        return content.toString();
    }

    public static String readDoc (File file) throws IOException, XmlException, OpenXML4JException {
        String filePath = file.getAbsolutePath();
        if (filePath.endsWith(".doc")) {
            InputStream is = new FileInputStream(file);
            WordExtractor ex = new WordExtractor(is);
            String text2003 = ex.getText();
            ex.close();
            is.close();
            return text2003;
        } else {
            OPCPackage opcPackage = POIXMLDocument.openPackage(filePath);
            POIXMLTextExtractor extractor = new XWPFWordExtractor(opcPackage);
            String text2007 = extractor.getText();
            extractor.close();
            return text2007;
        }
    }

    public static String readPdf(File file) throws IOException {
        PDDocument doc = PDDocument.load(file.getAbsolutePath());
        PDFTextStripper stripper = new PDFTextStripper();
        String content = stripper.getText(doc);
        doc.close();
        return content;
    }

}
