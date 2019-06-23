import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;

public class HomeController implements Initializable {
    @FXML
    private void onIconButtonClick(ActionEvent ae){
        Button btn = (Button) ae.getSource();
        onButtonClick(btn);
    }
    
    @FXML
    private void onIconButtonEnter(KeyEvent ke){
        if(ke.getCode().toString().equals("ENTER")){
            Button btn = (Button)ke.getSource();
            onButtonClick(btn);
        }
    }
    
    private void onButtonClick(Button btn){
        btn = (Button) btn.getScene().lookup("#btn" + btn.getId());
        btn.fire();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }    
    
}
