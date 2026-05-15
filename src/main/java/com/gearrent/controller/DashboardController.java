package com.gearrent.controller;
 
 import com.gearrent.entity.SystemUser;
 import com.gearrent.service.AuthService;
 import javafx.fxml.*;
 import javafx.scene.Scene;
 import javafx.scene.control.Button;
 import javafx.scene.control.Label;
 import javafx.scene.layout.BorderPane;
 import javafx.stage.Stage;
 import java.net.URL;
 
 public class DashboardController implements Initializable {
     @FXML private Button menuBranches;
     @FXML private BorderPane mainPane;
     @FXML private Label userGreeting;

     @Override
     public void initialize(URL url, java.util.ResourceBundle rb) {
         SystemUser user = AuthService.getCurrentUser();
         
         // Set user greeting
         if (user != null) {
             userGreeting.setText("Welcome, " + user.getUsername());
         }
         
         // Hide Admin-only buttons from non-admins
        if (user != null && !user.isAdmin()) {
             menuBranches.setDisable(true);
             menuBranches.setVisible(false);
         }
     }
 
     // Each menu item loads a new FXML into the centre of the BorderPane
     @FXML private void openEquipment()  { loadPane("/ui/Equipment.fxml"); }
     @FXML private void openCustomers()  { loadPane("/ui/Customer.fxml"); }
     @FXML private void openRentals()    { loadPane("/ui/Rental.fxml"); }
     @FXML private void openReturns()    { loadPane("/ui/Return.fxml"); }
     @FXML private void openOverdue()    { loadPane("/ui/Overdue.fxml"); }
    @FXML private void openBranches()   { loadPane("/ui/Branch.fxml"); }
     @FXML private void openReports()    { loadPane("/ui/Reports.fxml"); }
     
     @FXML private void logout() {
         try {
             AuthService.logout();
             // Load login screen
             Stage stage = (Stage) mainPane.getScene().getWindow();
             FXMLLoader loader = new FXMLLoader(
                 getClass().getResource("/ui/Login.fxml"));
             stage.setScene(new Scene(loader.load(), 420, 320));
             stage.setTitle("GearRent Pro — Login");
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private void loadPane(String fxml) {
         try {
             mainPane.setCenter(
                 new FXMLLoader(getClass().getResource(fxml)).load());
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
