/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package res;

import java.io.FileOutputStream;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

public class BarcodeCreator {
    public static String FILE_PATH = "cache/Barcode.pdf";
    
    public static boolean create(String barcode, String item_name, String mrp) throws DocumentException {
        final String COMPANY_NAME = "Anita Dresses";
        final int WIDTH = 210, HEIGHT = 290;

        PdfWriter writer;

        Document document = new Document(new Rectangle(Utilities.millimetersToPoints(WIDTH), Utilities.millimetersToPoints(HEIGHT)));    
        document.setMargins(7, 0, 18, 9);
        try
        {
            writer = PdfWriter.getInstance(document, new FileOutputStream(FILE_PATH));
            document.open();
                   
            float[] columnWidths = {10, 10, 10};
            PdfPTable table = new PdfPTable(columnWidths);
            table.setWidthPercentage(100);
            
            PdfPTable nesTable = new PdfPTable(1);
            nesTable.setWidthPercentage(100);            
            addCell(nesTable, COMPANY_NAME, 18, 13, 2, Element.ALIGN_CENTER, Font.BOLD);
            addCell(nesTable, item_name + "      MRP: "+mrp+"/-", 10, 0, 5, Element.ALIGN_CENTER, Font.NORMAL);
            Barcode128 code128 = new Barcode128();
            code128.setCode(barcode);
            code128.setX(1.5f);
            code128.setBarHeight(25);
            Image codeImg = code128.createImageWithBarcode(writer.getDirectContent(), null, null);
            codeImg.setAlignment(Element.ALIGN_CENTER);
            PdfPCell cell = new PdfPCell(codeImg);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorderWidth(0);
            nesTable.addCell(cell);
            
            cell = new PdfPCell(nesTable);
            cell.setFixedHeight(Utilities.millimetersToPoints(35));
            cell.setBorder(0);
            for(int i=0; i<24; i++){ 
                table.addCell(cell);
            }
            
            document.add(table);
            document.close();
            return true;
        }
        catch(Exception e)
        {
            Utils.errorMessage(e);
            return false;
        }
    }
    
    public static void print(int copies){
        PdfPrinter.print(FILE_PATH, copies);
    }

    private static void addCell(PdfPTable table, String text, int fontsize, int top, int bottom, int alignment, int style){
        Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN, fontsize, style, BaseColor.BLACK);
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setBorderWidth(0);
        cell.setPaddingTop(top);
        cell.setPaddingBottom(bottom);
        cell.setHorizontalAlignment(alignment);
        table.addCell(cell);
    }
}
