package com.gearrent.controller;
 
 import com.gearrent.dao.*;
 import com.gearrent.entity.Branch;
 import javafx.collections.FXCollections;
 import javafx.fxml.*;
 import javafx.scene.control.*;
 import java.net.URL;
 import java.sql.ResultSet;
 import java.time.LocalDate;
 import java.util.ResourceBundle;
 
 public class ReportController implements Initializable {
     @FXML private ComboBox<Branch> branchBox;
     @FXML private DatePicker       fromDatePicker;
     @FXML private DatePicker       toDatePicker;
     @FXML private Label            totalRentalsLabel;
     @FXML private Label            totalIncomeLabel;
     @FXML private Label            totalLateFeesLabel;
     @FXML private Label            totalDamageLabel;
     @FXML private Label            errorLabel;
 
     private RentalDAO rentalDAO;
     private BranchDAO branchDAO;
 
     @Override
     public void initialize(URL url, ResourceBundle rb) {
         try {
             rentalDAO = new RentalDAO();
             branchDAO = new BranchDAO();
             branchBox.setItems(FXCollections.observableArrayList(branchDAO.getAll()));
         } catch (Exception e) {
             errorLabel.setText(e.getMessage());
         }
     }
 
     @FXML
     private void handleGenerateReport() {
         Branch    br   = branchBox.getValue();
         LocalDate from = fromDatePicker.getValue();
         LocalDate to   = toDatePicker.getValue();
         if (br == null || from == null || to == null) {
             errorLabel.setText("Please select branch and date range.");
             return;
         }
         try {
             ResultSet rs = rentalDAO.getBranchRevenue(
                 br.getBranchId(), from, to);
             if (rs.next()) {
                 totalRentalsLabel.setText(String.valueOf(rs.getInt("total_rentals")));
                 totalIncomeLabel.setText(String.format(
                     "LKR %.2f", rs.getDouble("total_income")));
                 totalLateFeesLabel.setText(String.format(
                     "LKR %.2f", rs.getDouble("total_late_fees")));
                 totalDamageLabel.setText(String.format(
                     "LKR %.2f", rs.getDouble("total_damage")));
             }
         } catch (Exception e) {
             errorLabel.setText(e.getMessage());
         }
     }
 }
