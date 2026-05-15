package com.gearrent;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/ui/Login.fxml"));
        stage.setScene(new Scene(loader.load(), 420, 320));
        stage.setTitle("GearRent Pro — Login");
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
