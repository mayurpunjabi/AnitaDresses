package res;

import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter; 
import java.io.IOException;
import java.util.Optional;
import javafx.scene.control.ChoiceDialog;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

public class PdfPrinter {
    private static String printServiceName;
    
    public static void print(String filePath, int copies){
        try {
            if(!isPrintServiceAvailable()){
                String[] temp = getAllPrintServiceNames();
                ChoiceDialog<String> dialog = new ChoiceDialog<String>(temp[0], temp);
                dialog.setTitle("Choice Printer");
                dialog.setHeaderText(null);
                dialog.setContentText("Choose your printer:");
 
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()){
                    setPrintService(result.get());
                }
                else{
                    return;
                }
            }
            
            PDDocument document = PDDocument.load(new File(filePath));
            PrintService myPrintService = findPrintService(printServiceName);
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPageable(new PDFPageable(document));
            job.setPrintService(myPrintService);
            job.print();
        } catch (Exception ex) {
            Utils.errorMessage(ex);
        }
    }
    
    public static boolean isPrintServiceAvailable(){
        int ch; 
        String serviceName = "";
        FileReader fr = null; 
        try
        {
            fr = new FileReader("cache/PrintServiceName.txt"); 
            while ((ch=fr.read())!=-1) 
                serviceName += (char)ch;
            if(isPrintService(serviceName)){
                printServiceName = serviceName;
                return true;
            }
            return false;
        } 
        catch (Exception fe) 
        { 
            return false;
        }
        finally{
            try {
                fr.close();
            } catch (Exception ex) {}
        } 
    }
    
    public static void setPrintService(String serviceName){
        FileWriter fw = null;
        try {
            fw = new FileWriter("cache/PrintServiceName.txt");
            for (int i = 0; i < serviceName.length(); i++) 
                fw.write(serviceName.charAt(i));
        } catch (IOException ex) {
            Utils.errorMessage(ex);
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
              
            }
        }
        printServiceName = serviceName;
    }
    
    public static PrintService findPrintService(String printerName) {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService printService : printServices) {
            if (printService.getName().trim().equals(printerName)) {
                return printService;
            }
        }
        return null;
    }
    
    public static boolean isPrintService(String printerName) {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService printService : printServices) {
            if (printService.getName().trim().equals(printerName)) {
                return true;
            }
        }
        return false;
    }
    
    public static String[] getAllPrintServiceNames() {
        PrintService[] ps = PrintServiceLookup.lookupPrintServices(null, null);
        String[] serviceNames = new String[ps.length];
        for(int i=0; i < ps.length; i++){
            serviceNames[i] = ps[i].getName().trim();
        }
        return serviceNames;
    }
}