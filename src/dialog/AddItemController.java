package dialog;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TextField;
import res.DatabaseHelper;

public class AddItemController implements Initializable {
    @FXML
    private Label message;
    @FXML
    private TextField desc, price; 
    private Timeline timeline;
    private DatabaseHelper db;
    
    @FXML
    private void onClose(ActionEvent ae) throws SQLException{
        db.close();
        Button btn = (Button) ae.getSource();
        Stage stage = (Stage) btn.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void onAdd(ActionEvent e) throws InterruptedException, SQLException{
        db.insertInItems(desc.getText(), Integer.parseInt(price.getText()));
        message.setText("Item Added!");
        timeline.playFromStart();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            db = new DatabaseHelper();
        } catch (SQLException ex) {
            Logger.getLogger(AddItemController.class.getName()).log(Level.SEVERE, null, ex);
        }
        timeline = new Timeline(new KeyFrame(Duration.millis(4000), ae -> message.setText("")));
    }    
    
}
