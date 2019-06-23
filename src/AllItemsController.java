import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import res.BarcodeCreator;
import res.DatabaseHelper;
import res.Utils;

public class AllItemsController implements Initializable {
    private Parent root;
    private double xOffset = 0;
    private double yOffset = 0;
    private ObservableList<ObservableList> data;
    private DatabaseHelper db;
    
    @FXML
    private AnchorPane tablePane;
    @FXML
    private TableView tableView;
    
    @FXML
    private void onAddItem(ActionEvent ae) throws IOException{
        Button btn = (Button) ae.getSource();
        Stage dialog = new Stage();
        root = FXMLLoader.load(getClass().getResource("/dialog/AddItem.fxml"));
        root.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });
        
        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                dialog.setX(event.getScreenX() - xOffset);
                dialog.setY(event.getScreenY() - yOffset);
            }
        });
        
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.setTitle("Add Item");
        dialog.initOwner((Stage) btn.getScene().getWindow());
        dialog.centerOnScreen();
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.show();
        dialog.setOnHiding(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                loadData();
            }
        });
    }
    
    private void loadData(){
        data = FXCollections.observableArrayList();
        try {
            ResultSet rs = db.getItems();
            
            while(rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    if(i == 1 || i == 2)
                        row.add(rs.getString(i));
                    else
                        row.add(String.valueOf(rs.getInt(i)));
                }
                data.add(row);
            }
            tableView.setItems(data);
            tableView.setMinWidth(867);
            tableView.setMinHeight(355);
            tableView.setMaxWidth(867);
            tableView.setMaxHeight(355);
            tablePane.getChildren().clear();
            tablePane.getChildren().add(tableView);
        } catch (Exception ex) {
            Utils.errorMessage(ex);
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            db = new DatabaseHelper();
            for (int i = 0; i < 2; i++) {
                final int j = i;
                TableColumn col = (TableColumn)tableView.getColumns().get(i);
                col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });
            }
 
            TableColumn<ObservableList, Void> colBtn = new TableColumn("Barcode");
            colBtn.setMinWidth(150);
            Callback<TableColumn<ObservableList, Void>, TableCell<ObservableList, Void>> cellFactory = new Callback<TableColumn<ObservableList, Void>, TableCell<ObservableList, Void>>() {
                @Override
                public TableCell<ObservableList, Void> call(final TableColumn<ObservableList, Void> param) {
                    final TableCell<ObservableList, Void> cell = new TableCell<ObservableList, Void>() {

                        private final Button btn = new Button("Print");
                        {
                            btn.setOnAction((ActionEvent event) -> {
                                try {
                                    ObservableList obj = (ObservableList)this.getTableRow().getItem();
                                    BarcodeCreator.create(String.valueOf(obj.get(2)), String.valueOf(obj.get(0)), String.valueOf(obj.get(1)));
                                    BarcodeCreator.print(1);
                                } catch (Exception ex) {
                                    Utils.errorMessage(ex);
                                }
                            });
                        }
                        
                        @Override
                        public void updateItem(Void item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setGraphic(null);
                            } else {
                                setGraphic(btn);
                            }
                        }
                    };
                    return cell;
                }
            };

            colBtn.setCellFactory(cellFactory);
            colBtn.setStyle( "-fx-alignment: CENTER;");
            tableView.getColumns().add(colBtn);
            
            tableView.setRowFactory(tableV -> {
                final TableRow<ObservableList> row = new TableRow<>();
                final ContextMenu rowMenu = new ContextMenu();
                MenuItem removeItem = new MenuItem("Delete");
                removeItem.setOnAction(e -> {
                    if(db.deleteItem(db.getItemIdByIndex(row.getIndex())))
                        tableView.getItems().remove(row.getIndex());
                });

                rowMenu.getItems().addAll(removeItem);                
                row.contextMenuProperty().bind(
                    Bindings.when(Bindings.isNotNull(row.itemProperty()))
                            .then(rowMenu)
                            .otherwise((ContextMenu)null));
                return row;
            });
        } catch (SQLException ex) {
            Logger.getLogger(AllItemsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        loadData();
    }
    
}
 