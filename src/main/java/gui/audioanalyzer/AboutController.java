package gui.audioanalyzer;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.io.*;
import java.util.Objects;
import java.util.stream.Collectors;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

public class AboutController {

    // Objects
    @FXML
    private Label aboutTextLabel;

    // Miscellaneous methods
    @FXML
    private void initialize() throws IOException {
        // aboutTextLabel.setPrefSize(550, 700);
        aboutTextLabel.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        aboutTextLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, FontPosture.REGULAR, 14));

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("README.md"))));
        String text =  bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
        aboutTextLabel.setText(text);
        bufferedReader.close();
    }

}
