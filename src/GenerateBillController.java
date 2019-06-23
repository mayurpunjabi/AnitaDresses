import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import res.Bill;
import res.BillPrinter;
import res.Bill_Item;
import res.DatabaseHelper;

public class GenerateBillController implements Initializable {
    @FXML
    private TextField billNo, custName, custNumber, gst, discAmount, grandTotal, NewDesc, NewQty, NewUPrice, NewTPrice;
    @FXML
    private DatePicker billDate;
    @FXML
    private TableView billTable;
    @FXML
    private Label lblScanning; 
    
    public static Bill billData;
    private DatabaseHelper db;
    private int tempItemId = 0;

    @FXML
    private void onNewItemClick(MouseEvent me){
        addNewItem();
    }
    
    @FXML
    private void onNewItemEnter(KeyEvent ke){
        if(ke.getCode().toString().equals("ENTER")){
            addNewItem();
        }
    }
    
    @FXML
    private void onNewBill(MouseEvent me){
        DatabaseHelper.Bill_Id = 0;
        billNo.setText(String.valueOf(db.getBillId()));
        billDate.setValue(LocalDate.now());

        billData = new Bill();
        billData.Bill_Id = BigInteger.valueOf(Long.parseLong(billNo.getText()));
        billData.Bill_Date = Date.valueOf(billDate.getValue());
        billData.Items = new ArrayList<Bill_Item>();
        billData.Discount = BigDecimal.ZERO;
        billData.Total_Amount = BigDecimal.ZERO;
        custName.setText("");
        custNumber.setText(""); 
        discAmount.setText("0");
        grandTotal.setText("0.00");
        NewDesc.setText("");
        NewQty.setText("");
        NewUPrice.setText("");
        NewTPrice.setText("");
        reloadItems();
    }
    
    @FXML
    private void onSaveBill(MouseEvent me) throws SQLException{
        billData.Customer_Name = custName.getText();
        billData.Mobile_No = custNumber.getText();
        BigDecimal discount = BigDecimal.valueOf(Double.valueOf(discAmount.getText()));
        billData.Total_Amount = billData.Total_Amount.subtract(discount);
        db.saveBill(billData);
    }
    
    @FXML
    private void onPrintBill(MouseEvent me) throws SQLException{
        onSaveBill(me);
        BillPrinter billPrinter = new BillPrinter(billData);
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Bill Type");
        dialog.setHeaderText(null);
        dialog.setContentText("Type:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            if(result.get().toString().equals("1")){
                billData.GST = BigDecimal.ZERO;
            }else if(result.get().toString().equals("2")){
                billData.GST = BigDecimal.valueOf(5);
            }
            if(billPrinter.createBill()){
                billPrinter.printBill();
            }
        }
    }
     
    private void onNewItemChange(){
        if(!(NewQty.getText().toString().equals("") || NewUPrice.getText().toString().equals(""))){
            String value = String.valueOf(BigDecimal.valueOf(Double.valueOf(NewUPrice.getText())).multiply(BigDecimal.valueOf(Integer.valueOf(NewQty.getText()))));
            NewTPrice.setText(value);
        }
    }
    
    private void addNewItem(){
        if(!(NewDesc.getText().toString().equals("") || NewQty.getText().toString().equals("") || NewUPrice.getText().toString().equals("") || NewTPrice.getText().toString().equals(""))){
            Bill_Item item = new Bill_Item();
            item.Description = NewDesc.getText();
            item.Item_Id = BigInteger.valueOf(tempItemId);
            item.Quantity = Integer.parseInt(NewQty.getText());
            item.UPrice = BigDecimal.valueOf(Double.valueOf(NewUPrice.getText()));
            item.TPrice = BigDecimal.valueOf(Double.valueOf(NewTPrice.getText()));
            addItemInBill(item);
        }
    }
    
    private void addItemInBill(Bill_Item item){
        ObservableList<String> row = FXCollections.observableArrayList();
        row.add(item.Description);
        row.add(String.valueOf(item.Quantity));
        row.add(String.valueOf(item.UPrice));
        row.add(String.valueOf(item.TPrice));
        billTable.getItems().add(row);
        billData.Items.add(item);
        
        //Add Item for Total Amount
        BigDecimal tempTotal = billData.Total_Amount;
        BigDecimal discount = BigDecimal.valueOf(Double.valueOf(discAmount.getText()));
        
        billData.Total_Amount = tempTotal.add(item.TPrice);
        grandTotal.setText(String.valueOf(billData.Total_Amount.subtract(discount)));
    }
    
    private void reloadItems(){
        ObservableList data = FXCollections.observableArrayList();
        for(int i = 0; i < billData.Items.size(); i++) {
            ObservableList<String> row = FXCollections.observableArrayList();
            row.add(billData.Items.get(i).Description);
            row.add(String.valueOf(billData.Items.get(i).Quantity));
            row.add(String.valueOf(billData.Items.get(i).UPrice));
            row.add(String.valueOf(billData.Items.get(i).TPrice));
            data.add(row);
        }
        billTable.setItems(data);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            db = new DatabaseHelper();
        } catch (SQLException ex) {
            Logger.getLogger(GenerateBillController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        for (int i = 0; i < billTable.getColumns().size(); i++) {
            final int j = i;
            TableColumn col = (TableColumn)billTable.getColumns().get(i);
            col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                    return new SimpleStringProperty(param.getValue().get(j).toString());
                }
            });
        }
        
        billTable.setRowFactory(tableView -> {
        final TableRow<ObservableList> row = new TableRow<>();
        final ContextMenu rowMenu = new ContextMenu();
        MenuItem removeItem = new MenuItem("Delete");
        removeItem.setOnAction(e -> {
            billData.Items.remove(row.getIndex());
            billTable.getItems().remove(row.getIndex());
        });

        rowMenu.getItems().addAll(removeItem);
        row.contextMenuProperty().bind(
            Bindings.when(Bindings.isNotNull(row.itemProperty()))
                    .then(rowMenu)
                    .otherwise((ContextMenu)null));

        return row;
    });
        
        if(DatabaseHelper.Bill_Id == 0){
            //Create New Bill
            billNo.setText(String.valueOf(db.getBillId()));
            billDate.setValue(LocalDate.now());
            
            billData = new Bill();
            billData.Bill_Id = BigInteger.valueOf(Long.parseLong(billNo.getText()));
            billData.Bill_Date = Date.valueOf(billDate.getValue());
            billData.Items = new ArrayList<Bill_Item>();
            billData.Discount = BigDecimal.ZERO;
            billData.Total_Amount = BigDecimal.ZERO;
        }
        else{
            //See Existing Bill 
            billData = db.getBillData(DatabaseHelper.Bill_Id);
            billNo.setText(String.valueOf(billData.Bill_Id));
            billDate.setValue(billData.Bill_Date.toLocalDate());
            custName.setText(billData.Customer_Name);
            custNumber.setText(billData.Mobile_No);
            gst.setText(String.valueOf(billData.GST));
            discAmount.setText(String.valueOf(billData.Discount));
            grandTotal.setText(String.valueOf(billData.Total_Amount));
            reloadItems();
        }
        
        ChangeListener onChangeListener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                onNewItemChange();
            }
        };
        NewQty.textProperty().addListener(onChangeListener);
        NewUPrice.textProperty().addListener(onChangeListener);
        discAmount.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(!newValue.equals("")){
                    BigDecimal discount = BigDecimal.valueOf(Double.valueOf(newValue));
                    billData.Discount = discount;
                    grandTotal.setText(String.valueOf(billData.Total_Amount.subtract(discount)));
                }
            }
        });
    }
}
