package com.gearrent.controller;
 
 import com.gearrent.dao.RentalDAO;
 import com.gearrent.entity.Rental;
 import com.gearrent.service.RentalService;
 import javafx.collections.FXCollections;
 import javafx.fxml.*;
 import javafx.scene.control.*;
 import java.net.URL;
 import java.time.LocalDate;
 import java.util.ResourceBundle;
 
 public class ReturnController implements Initializable {
     @FXML private ComboBox<Rental> rentalBox;
     @FXML private DatePicker       returnDatePicker;
     @FXML private CheckBox         damagedCheckBox;
     @FXML private TextField        damageChargeField;
     @FXML private TextArea         damageDescArea;
     @FXML private Label            lateFeeLabel;
     @FXML private Label            settlementLabel;
     @FXML private Label            errorLabel;
 
     private RentalService rentalService;
     private RentalDAO rentalDAO;
 
     @Override
     public void initialize(URL url, ResourceBundle rb) {
         try {
             rentalService = new RentalService();
             rentalDAO     = new RentalDAO();
             rentalBox.setItems(FXCollections.observableArrayList(
                 rentalDAO.getActive()));
             damagedCheckBox.setOnAction(e ->
                 damageChargeField.setDisable(!damagedCheckBox.isSelected()));
             damageChargeField.setDisable(true);
         } catch (Exception e) {
             errorLabel.setText(e.getMessage());
         }
     }
 
     @FXML
     private void handleProcessReturn() {
         errorLabel.setText("");
         Rental r     = rentalBox.getValue();
         LocalDate rd = returnDatePicker.getValue();
         if (r == null || rd == null) {
             errorLabel.setText("Select a rental and return date.");
             return;
         }
         try {
             double damageCharge = 0;
             String damageDesc   = "";
             if (damagedCheckBox.isSelected()) {
                 damageCharge = Double.parseDouble(damageChargeField.getText().trim());
                 damageDesc   = damageDescArea.getText().trim();
             }
             double settlement = rentalService.processReturn(
                 r.getRentalId(), rd, damageCharge, damageDesc);
 
             if (settlement >= 0) {
                 settlementLabel.setText(String.format(
                     "REFUND to customer: LKR %.2f", settlement));
                 settlementLabel.setStyle("-fx-text-fill: green;");
             } else {
                 settlementLabel.setText(String.format(
                     "CUSTOMER OWES: LKR %.2f", Math.abs(settlement)));
                 settlementLabel.setStyle("-fx-text-fill: red;");
             }
             errorLabel.setStyle("-fx-text-fill: green;");
             errorLabel.setText("Return processed successfully.");
         } catch (Exception e) {
             errorLabel.setStyle("-fx-text-fill: red;");
             errorLabel.setText(e.getMessage());
         }
     }
 }
