/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package res;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.math.BigDecimal;

/**
 *
 * @author mayur
 */
public class BillPrinter {
    
    private final Bill bill;
    private final String FILE_PATH = "cache/NewBill.pdf", COMPANY_NAME = "ANITA DRESSES", HEADER = "Praise the lord", ADDRESS = "Trimurti Chowk, Chetak Ghoda Rd, Jawahar Colony, Aurangabad.",
            CONTACT = "Mob.: 9823799325, 9325994698 Tel.: 0240-2341716", GSTIN = "27ALTPP1019Q1ZC";
    private final int WIDTH = 210, HEIGHT = 297;
    private Document document;
    
    public BillPrinter(Bill bill){
        this.bill = bill;
    }
    
    public boolean createBill(){
        PdfWriter writer;
        BigDecimal totalAmount = bill.Total_Amount;
        
        document = new Document(new Rectangle(Utilities.millimetersToPoints(WIDTH), Utilities.millimetersToPoints(HEIGHT)));  
        document.setMargins(20, 20, 0, 0);
        try
        {
            writer = PdfWriter.getInstance(document, new FileOutputStream(FILE_PATH));
            document.open();
            
                        
            PdfPTable mainTable = new PdfPTable(new float[]{64, 36});
            mainTable.setWidthPercentage(100);
            
            PdfPTable nesTable = new PdfPTable(1);
            nesTable.setWidthPercentage(98);  
            
            addText(nesTable, HEADER, 16, 15, 15, Element.ALIGN_CENTER, Font.NORMAL);
            addText(nesTable, COMPANY_NAME, 21, 0, 0, Element.ALIGN_CENTER, Font.BOLD);
            if(!(bill.GST.equals(BigDecimal.ZERO))){
                addText(nesTable, "GSTIN: " + GSTIN, 10, 0, 0, Element.ALIGN_CENTER, Font.NORMAL);
            }
            addText(nesTable, ADDRESS, 13, 0, 0, Element.ALIGN_CENTER, Font.NORMAL);
            addText(nesTable, CONTACT, 13, 0, 15, Element.ALIGN_CENTER, Font.NORMAL);
            
            addText(nesTable, "Bill No.: " + bill.Bill_Id + "                                      Bill Date: " + bill.Bill_Date, 14, 0, 7, Element.ALIGN_LEFT, Font.NORMAL);
            addText(nesTable, "Customer Name: " + bill.Customer_Name, 14, 0, 0, Element.ALIGN_LEFT, Font.NORMAL);
            addText(nesTable, "Mobile No.: " + bill.Mobile_No, 14, 0, 7, Element.ALIGN_LEFT, Font.NORMAL);
            
            float[] columnWidths = {40, 10, 15, 15};
            String[] columns = {"Description", "Qty", "Unit", "Total"};
            PdfPTable table = new PdfPTable(columnWidths);

            table.setSpacingAfter(0);
            table.setWidthPercentage(100);
            for(int i = 0; i < columns.length; i++){
                addCell(table, columns[i], Font.BOLD, new int[]{1, 1, 1, 1});
            }
                     
            for(Bill_Item item : bill.Items){
                addCell(table, item.Description, Font.NORMAL, new int[]{0, 0, 1, 1});
                addCell(table, String.valueOf(item.Quantity), Font.NORMAL, new int[]{0, 0, 1, 1});
                addCell(table, item.UPrice.toString(), Font.NORMAL, new int[]{0, 0, 1, 1});
                addCell(table, item.TPrice.toString(), Font.NORMAL, new int[]{0, 0, 1, 1});
            }
            int j = 11;
            if(bill.GST.equals(BigDecimal.ZERO)){
                j += 1;
            }
            if(bill.Discount.doubleValue() > 0){
                j -= 1;
            }
            for(int i=0; i < (j - bill.Items.size()); i++){
                addCell(table, " ", Font.NORMAL, new int[]{0, 0, 1, 1});
                addCell(table, " ", Font.NORMAL, new int[]{0, 0, 1, 1});
                addCell(table, " ", Font.NORMAL, new int[]{0, 0, 1, 1});
                addCell(table, " ", Font.NORMAL, new int[]{0, 0, 1, 1});
            }
            addCell(table, " ", Font.NORMAL, new int[]{0, 1, 1, 1});
            addCell(table, " ", Font.NORMAL, new int[]{0, 1, 1, 1});
            addCell(table, " ", Font.NORMAL, new int[]{0, 1, 1, 1});
            addCell(table, " ", Font.NORMAL, new int[]{0, 1, 1, 1});
            nesTable.addCell(table);
            if(bill.Discount.doubleValue() > 0){
                addText(nesTable, "Discount: -" + String.valueOf(bill.Discount.doubleValue()), 15, 0, 0, Element.ALIGN_RIGHT, Font.NORMAL);
            }
            if(!(bill.GST.equals(BigDecimal.ZERO))){
                addText(nesTable, "CGST: 2.5%, SGST: 2.5%                                                             GST: 5%", 12, 0, 2, Element.ALIGN_LEFT, Font.NORMAL);
                totalAmount = bill.Total_Amount.multiply(BigDecimal.valueOf(1.05));
            }
            addText(nesTable, "Total Amount: " + String.valueOf(totalAmount.doubleValue()), 15, 0, 15, Element.ALIGN_RIGHT, Font.NORMAL);
            
            addText(nesTable, "• No Return No Exchange • No Color Guarantee", 12, 0, 3, Element.ALIGN_LEFT, Font.NORMAL);
            addText(nesTable, "• First Wash Dry Clean • Always New Look", 12, 0, 3, Element.ALIGN_LEFT, Font.NORMAL);
            addText(nesTable, "• Fixed Rate Shop • Reasonable Price                       From Anita Dresses", 12, 0, 0, Element.ALIGN_LEFT, Font.NORMAL);
            addText(nesTable, "-- Thank You • Visit Again --", 14, 10, 20, Element.ALIGN_CENTER, Font.BOLD);
          
            PdfPCell cell = new PdfPCell(nesTable);
            cell.setBorderWidth(0);
            mainTable.addCell(cell);
            cell = new PdfPCell(new Paragraph(" "));
            cell.setBorderWidth(0);
            mainTable.addCell(cell);
            document.add(mainTable);
            
            document.close();
            return true;
        }
        catch(Exception e)
        {
            Utils.errorMessage(e);
            return false;
        }
    }
    
    public void printBill(){
        PdfPrinter.print(FILE_PATH, 1);
    }

    private void addText(String text, float font_size, int marginTop, int marginBottom, int alignment, int style) throws DocumentException {
        Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN, font_size, style, BaseColor.BLACK);
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setSpacingBefore(marginTop);
        paragraph.setSpacingAfter(marginBottom);
        paragraph.setAlignment(alignment);
        document.add(paragraph);
    }
    
    private void addCell(PdfPTable table, String text, int style, int borderWidth[]){
        Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 15, style, BaseColor.BLACK);
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setBorderWidth(0);
        cell.setBorderWidthTop(borderWidth[0]);
        cell.setBorderWidthBottom(borderWidth[1]);
        cell.setBorderWidthLeft(borderWidth[2]);
        cell.setBorderWidthRight(borderWidth[2]);
        cell.setBorderColor(new BaseColor(100, 100, 100));
        table.addCell(cell);
    }
    
    private void addCell(PdfPTable table, String text, int style, int borderWidth[], int colsToSpan){
        Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 15, style, BaseColor.BLACK);
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setBorderWidth(0);
        cell.setBorderWidthTop(borderWidth[0]);
        cell.setBorderColor(BaseColor.BLACK);
        cell.setBorderWidthBottom(borderWidth[1]);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setColspan(colsToSpan);
        cell.setBorderColor(new BaseColor(100, 100, 100));
        table.addCell(cell);
    }
    
    private static void addText(PdfPTable table, String text, int fontsize, int top, int bottom, int alignment, int style){
        Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN, fontsize, style, BaseColor.BLACK);
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setBorderWidth(0);
        cell.setPaddingTop(top);
        cell.setPaddingBottom(bottom);
        cell.setHorizontalAlignment(alignment);
        table.addCell(cell);
    }
}
