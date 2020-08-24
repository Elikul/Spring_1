package Chat.Client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.io.DataOutputStream;
import java.io.IOException;

public class PersonalController {
    @FXML
    Button btn;

    @FXML
    TextArea textArea;

    public void btnClick() {
        if(!((Chat.Client.MiniStage)btn.getScene().getWindow()).parentList.contains(textArea)) {
            ((Chat.Client.MiniStage)btn.getScene().getWindow()).parentList.add(textArea);
            System.out.println("1");
        }
        DataOutputStream out = ((Chat.Client.MiniStage)btn.getScene().getWindow()).out;
        String nickTo = ((Chat.Client.MiniStage)btn.getScene().getWindow()).nickTo;
        try {
            out.writeUTF("/w " + nickTo + " 111");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}