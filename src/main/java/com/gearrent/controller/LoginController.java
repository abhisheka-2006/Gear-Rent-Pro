package com.gearrent.controller;
 
 import com.gearrent.entity.SystemUser;
 import com.gearrent.service.AuthService;
 import javafx.fxml.FXML;
 import javafx.fxml.FXMLLoader;
 import javafx.scene.Scene;
 import javafx.scene.control.*;
 import javafx.stage.Stage;
 
 public class LoginController {
     @FXML private TextField     usernameField;
     @FXML private PasswordField passwordField;
     @FXML private Label         errorLabel;
 
     @FXML
     private void handleLogin() {
         errorLabel.setText("");
         String user = usernameField.getText().trim();
         String pass = passwordField.getText().trim();
         if (user.isEmpty() || pass.isEmpty()) {
             errorLabel.setText("Please enter username and password.");
             return;
         }
         try {
             AuthService auth = new AuthService();
             SystemUser loggedIn = auth.login(user, pass);
             // Open dashboard
             FXMLLoader loader = new FXMLLoader(
                 getClass().getResource("/ui/Dashboard.fxml"));
             Stage stage = (Stage) usernameField.getScene().getWindow();
             stage.setScene(new Scene(loader.load(), 900, 600));
             stage.setTitle("GearRent Pro - " + loggedIn.getFullName());
         } catch (Exception e) {
             String msg = e.getMessage();
             if (msg == null || msg.isBlank()) {
                 Throwable cause = e.getCause();
                 msg = (cause != null && cause.getMessage() != null)
                     ? cause.getMessage()
                     : e.toString();
             }
             errorLabel.setText(msg);
             e.printStackTrace();
         }
     }
 }
