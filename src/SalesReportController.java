
import java.io.File;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import res.DatabaseHelper;
import res.Utils;

public class SalesReportController implements Initializable {
    @FXML
    private TableView allBills;
    
    private DatabaseHelper db;
    private ObservableList<ObservableList> data;
    
    @FXML
    private void onClearDB(MouseEvent me){
        db.clear();
    }
    
    @FXML
    private void onClearCache(MouseEvent me){
        File file = new File("cache/PrintServiceName.txt"); 
        file.delete(); 
    }
    
    private void reloadBills(){
        try {
            ResultSet rs = db.getBills();
            data = FXCollections.observableArrayList();
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
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            db = new DatabaseHelper();
            
            for (int i = 0; i < allBills.getColumns().size(); i++) {
                final int j = i;
                TableColumn col = (TableColumn)allBills.getColumns().get(i);
                col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });
            }
            
            allBills.setRowFactory(tableView -> {
                final TableRow<ObservableList> row = new TableRow<>();
                final ContextMenu rowMenu = new ContextMenu();
                MenuItem removeItem = new MenuItem("Delete");
                removeItem.setOnAction(e -> {
                    if(db.deleteBill(db.getBillIdByIndex(row.getIndex())))
                        allBills.getItems().remove(row.getIndex());
                });

                rowMenu.getItems().addAll(removeItem);
                row.contextMenuProperty().bind(
                    Bindings.when(Bindings.isNotNull(row.itemProperty()))
                            .then(rowMenu)
                            .otherwise((ContextMenu)null));

                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                        DatabaseHelper.Bill_Id = db.getBillIdByIndex(row.getIndex());
                        Button btn = (Button) allBills.getScene().lookup("#btnGenerateBill");
                        btn.fire();
                    }
                });

                return row;
            });
            
            reloadBills();
            
        } catch (Exception ex) {
            Utils.errorMessage(ex);
        }
            
    }    
    
}
