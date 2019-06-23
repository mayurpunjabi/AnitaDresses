/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level; 
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import res.Bill;
import res.Bill_Item;
import res.DatabaseHelper;
import res.Utils;

public class MainWindowController implements Initializable {
    @FXML 
    private Button btnHome, btnGenerateBill, btnAllItems, btnSalesReport, btnAbout;
    private ArrayList<Button> navButtons;
    private Button activeButton;
    @FXML
    private AnchorPane root, rightPane;
    private ArrayList<AnchorPane> panes;
    private Stage myStage;
    private EventHandler<KeyEvent> onKeyPressedHandler, onScanningStarted;
    private String barcode = "";
    private long lastEventTimeStamp = 0L;
    private DatabaseHelper db;
    private TableView billTable;
    private Label lblScanning;
    private ChangeListener<Boolean> onFocusChange;
    
    public void setStage(Stage stage) {
        myStage = stage;
    }
    
    private void setShortcutKeys(){
        myStage.addEventHandler(KeyEvent.KEY_PRESSED, onKeyPressedHandler);
        billTable = (TableView)getActivePane().getChildren().get(11);
        lblScanning = (Label)getActivePane().getChildren().get(22);
        if(lblScanning.isVisible())
            lblScanning.requestFocus();
    }
    
    private void removeShortcutKeys(){
        myStage.removeEventHandler(KeyEvent.KEY_PRESSED, onKeyPressedHandler);
    }
    
    private void reloadBills(){
        TableView allBills = (TableView) activeButton.getScene().lookup("#allBills");
        try {
            ResultSet rs = db.getBills();
            ObservableList data = FXCollections.observableArrayList();
            rs.afterLast();
            while(rs.previous()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    if(i == 1 || i == 2)
                        row.add(rs.getString(i));
                    else
                        row.add(String.valueOf(rs.getInt(i)));
                }
                data.add(row);
            }
            allBills.setItems(data);
        } catch (SQLException ex) {
            Utils.errorMessage(ex);
        }
    }
    
    public void onBillShow(){
        TextField billNo = (TextField) rightPane.getScene().lookup("#billNo");
        DatePicker billDate = (DatePicker) rightPane.getScene().lookup("#billDate");
        if(DatabaseHelper.Bill_Id == 0){
            //Create New Bill
            billNo.setText(String.valueOf(db.getBillId()));
            billDate.setValue(LocalDate.now());
            
            GenerateBillController.billData = new Bill();
            GenerateBillController.billData.Bill_Id = BigInteger.valueOf(Long.parseLong(billNo.getText()));
            GenerateBillController.billData.Bill_Date = Date.valueOf(billDate.getValue());
            GenerateBillController.billData.Items = new ArrayList<Bill_Item>();
            GenerateBillController.billData.Discount = BigDecimal.ZERO;
            GenerateBillController.billData.Total_Amount = BigDecimal.ZERO;
        }
        else{
            TextField custName = (TextField) rightPane.getScene().lookup("#custName");
            TextField custNumber = (TextField) rightPane.getScene().lookup("#custNumber");
            TextField gst = (TextField) rightPane.getScene().lookup("#gst");
            TextField discAmount = (TextField) rightPane.getScene().lookup("#discAmount");
            TextField grandTotal = (TextField) rightPane.getScene().lookup("#grandTotal");
            //See Existing Bill 
            GenerateBillController.billData = db.getBillData(DatabaseHelper.Bill_Id);
            billNo.setText(String.valueOf(GenerateBillController.billData.Bill_Id));
            billDate.setValue(GenerateBillController.billData.Bill_Date.toLocalDate());
            custName.setText(GenerateBillController.billData.Customer_Name);
            custNumber.setText(GenerateBillController.billData.Mobile_No);
            gst.setText(String.valueOf(GenerateBillController.billData.GST));
            discAmount.setText(String.valueOf(GenerateBillController.billData.Discount));
            grandTotal.setText(String.valueOf(GenerateBillController.billData.Total_Amount));
            reloadItems();
        }
    }
    
    @FXML
    private void onNavButtonClick(ActionEvent ae){
        if(activeButton != ae.getSource()){
            activeButton.getStyleClass().clear();
            activeButton.getStyleClass().add("nav-button");
            if(activeButton.getText().equals("Generate Bill"))
                removeShortcutKeys();
            rightPane.getChildren().remove(getActivePane());
            activeButton = (Button)ae.getSource();
            activeButton.getStyleClass().clear();
            activeButton.getStyleClass().add("active-nav-button");
            rightPane.getChildren().add(getActivePane());
            
            if(activeButton.getText().equals("Generate Bill")){
                setShortcutKeys();
                onBillShow();
            }
            else if(activeButton.getText().equals("Sales Report")){
                reloadBills();
            }
        }
    }
    
    @FXML
    private void onClose(ActionEvent ae){
        Button btn = (Button) ae.getSource();
        Stage stage = (Stage) btn.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void onMinimize(ActionEvent ae){
        Button btn = (Button) ae.getSource();
        Stage stage = (Stage) btn.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void handleButtonAction(ActionEvent event){
    
    }
    
    private void toggleScanning(){
        try{
            if(lblScanning.isVisible()){
                lblScanning.setVisible(false);
                lblScanning.focusedProperty().removeListener(onFocusChange);
                lblScanning.getScene().removeEventHandler(KeyEvent.KEY_TYPED, onScanningStarted);
            }
            else{
                lblScanning.setVisible(true);
                lblScanning.focusedProperty().addListener(onFocusChange);
                lblScanning.requestFocus();
                lblScanning.getScene().getWindow().addEventHandler(KeyEvent.KEY_TYPED, onScanningStarted);
            }
        }
        catch(Exception e){
            Utils.errorMessage(e);
        }
    }
    
    private void addItemInBill(Bill_Item item){
        for(Bill_Item i: GenerateBillController.billData.Items) {
            if(i.Item_Id.equals(item.Item_Id)){
                i.Quantity += 1;
                i.TPrice = i.TPrice.add(i.UPrice);
                
                reloadItems();
                
                TextField grandTotal = (TextField)getActivePane().getChildren().get(17);
                BigDecimal tempTotal = GenerateBillController.billData.Total_Amount;
                BigDecimal discount = GenerateBillController.billData.Discount;
                GenerateBillController.billData.Total_Amount = tempTotal.add(item.UPrice);
                grandTotal.setText(String.valueOf(GenerateBillController.billData.Total_Amount.subtract(discount)));
                return;
            }
        }
        ObservableList<String> row = FXCollections.observableArrayList();
        row.add(item.Description);
        row.add(String.valueOf(item.Quantity));
        row.add(String.valueOf(item.UPrice));
        row.add(String.valueOf(item.TPrice));
        billTable.getItems().add(row);
        GenerateBillController.billData.Items.add(item);
        
        TextField grandTotal = (TextField)getActivePane().getChildren().get(17);
        BigDecimal tempTotal = GenerateBillController.billData.Total_Amount;
        BigDecimal discount = GenerateBillController.billData.Discount;
        GenerateBillController.billData.Total_Amount = tempTotal.add(item.UPrice);
        grandTotal.setText(String.valueOf(GenerateBillController.billData.Total_Amount.subtract(discount)));
    }
    
    private void reloadItems(){
        ObservableList data = FXCollections.observableArrayList();
        for(int i = 0; i < GenerateBillController.billData.Items.size(); i++) {
            ObservableList<String> row = FXCollections.observableArrayList();
            row.add(GenerateBillController.billData.Items.get(i).Description);
            row.add(String.valueOf(GenerateBillController.billData.Items.get(i).Quantity));
            row.add(String.valueOf(GenerateBillController.billData.Items.get(i).UPrice));
            row.add(String.valueOf(GenerateBillController.billData.Items.get(i).TPrice));
            data.add(row);
        }
        billTable.setItems(data);
    }
    
    private AnchorPane getActivePane(){
        return panes.get(navButtons.indexOf(activeButton));
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            db = new DatabaseHelper();
        } catch (SQLException ex) {
            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
        navButtons = new ArrayList<Button>();
        navButtons.add(btnHome);
        navButtons.add(btnGenerateBill);
        navButtons.add(btnAllItems);
        navButtons.add(btnSalesReport);
        navButtons.add(btnAbout);
        
        panes = new ArrayList<AnchorPane>();
        try {
            for(int i=0; i<navButtons.size(); i++){
                panes.add(FXMLLoader.load(getClass().getResource(navButtons.get(i).getText().replace(" ", "") + ".fxml")));
            }
        } catch (IOException ex) {
            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        activeButton = btnHome;
        rightPane.getChildren().add(getActivePane());
        
        onKeyPressedHandler = new EventHandler<KeyEvent>(){
            @Override
            public void handle(KeyEvent ke){
                if((ke.isAltDown() && ke.getCode() == KeyCode.S) || ke.getCode() == KeyCode.F9 || ke.getCode() == KeyCode.BACK_QUOTE){
                    toggleScanning();
                }
            }
        };
        
        onScanningStarted = new EventHandler<KeyEvent>(){
            @Override
            public void handle(KeyEvent ke){
                long now = Instant.now().toEpochMilli();
                char c = ke.getCharacter().charAt(0);
                if(Character.isDigit(c) && (now - lastEventTimeStamp) < 10000){
                    barcode += c;
                }
                else{
                    barcode = String.valueOf(c);
                }
                if(barcode.length() == 6){
                    try {
                        
                        ResultSet rs = db.getConnection().createStatement().executeQuery("SELECT Descr, Price FROM Items WHERE Id = " + barcode);
                        rs.next();
                        
                        Bill_Item item = new Bill_Item();
                        item.Item_Id = BigInteger.valueOf(Long.parseLong(barcode));
                        item.Description = rs.getString("Descr");
                        //System.out.println("Added");
                        item.Quantity = 1;
                        item.UPrice = rs.getBigDecimal("Price");
                        item.TPrice = BigDecimal.valueOf(item.Quantity).multiply(item.UPrice);
                        
                        addItemInBill(item);
                        
                    } catch (Exception ex) {
                        Utils.errorMessage(ex);
                    }
                    barcode = "";
                }
                lastEventTimeStamp = now;
            }
        };
        
        onFocusChange = new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (!newPropertyValue)
                {
                    lblScanning.requestFocus();
                }
            }
        };
    }       
}
