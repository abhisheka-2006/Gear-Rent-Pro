package com.gearrent.controller;
 
 import com.gearrent.dao.*;
 import com.gearrent.entity.*;
 import com.gearrent.service.*;
 import javafx.collections.FXCollections;
 import javafx.fxml.*;
 import javafx.scene.control.*;
 import java.net.URL;
 import java.time.*;
 import java.time.temporal.ChronoUnit;
 import java.util.ResourceBundle;
 
 public class RentalController implements Initializable {
     @FXML private ComboBox<Branch>    branchBox;
     @FXML private ComboBox<Equipment> equipmentBox;
     @FXML private ComboBox<Customer>  customerBox;
     @FXML private DatePicker          startDatePicker;
     @FXML private DatePicker          endDatePicker;
     @FXML private Label               rentalAmountLabel;
     @FXML private Label               depositLabel;
     @FXML private Label               finalPayableLabel;
     @FXML private Label               errorLabel;
     @FXML private ComboBox<String>    paymentStatusBox;
 
     private RentalService rentalService;
     private BranchDAO branchDAO;
     private EquipmentDAO equipmentDAO;
     private CustomerDAO customerDAO;
     private EquipmentCategoryDAO categoryDAO;
 
     @Override
     public void initialize(URL url, ResourceBundle rb) {
         try {
             rentalService = new RentalService();
             branchDAO    = new BranchDAO();
             equipmentDAO = new EquipmentDAO();
             customerDAO  = new CustomerDAO();
             categoryDAO  = new EquipmentCategoryDAO();
 
             branchBox.setItems(FXCollections.observableArrayList(branchDAO.getAll()));
             customerBox.setItems(FXCollections.observableArrayList(customerDAO.getAll()));
             paymentStatusBox.setItems(FXCollections.observableArrayList(
                 "PAID", "PARTIALLY_PAID", "UNPAID"));
             paymentStatusBox.setValue("PAID");
 
             // When branch selected, load available equipment
             branchBox.setOnAction(e -> loadEquipment());
             // When dates change, recalculate price
             startDatePicker.setOnAction(e -> calculatePrice());
             endDatePicker.setOnAction(e -> calculatePrice());
             equipmentBox.setOnAction(e -> calculatePrice());
             customerBox.setOnAction(e -> calculatePrice());
         } catch (Exception e) {
             errorLabel.setText("Init error: " + e.getMessage());
         }
     }
 
     private void loadEquipment() {
         Branch b = branchBox.getValue();
         if (b == null) return;
         try {
             equipmentBox.setItems(FXCollections.observableArrayList(
                 equipmentDAO.getByBranchAndStatus(b.getBranchId(), "AVAILABLE")));
         } catch (Exception e) {
             errorLabel.setText(e.getMessage());
         }
     }
 
     private void calculatePrice() {
         Equipment eq = equipmentBox.getValue();
         Customer  cu = customerBox.getValue();
         LocalDate sd = startDatePicker.getValue();
         LocalDate ed = endDatePicker.getValue();
         if (eq == null || cu == null || sd == null || ed == null) return;
         try {
             EquipmentCategory cat = categoryDAO.getById(eq.getCategoryId());
             long days = ChronoUnit.DAYS.between(sd, ed);
 
             double gross = rentalService.calculateRentalAmount(
                 eq.getDailyBasePrice(), cat.getBasePriceFactor(),
                 cat.getWeekendMultiplier(), sd, ed);
 
             double longDisc = rentalService.calcLongRentalDiscount(gross, days);
             double memDisc  = rentalService.getMembershipDiscount(
                 gross - longDisc, cu.getMembershipLevel());
             double finalAmt = gross - longDisc - memDisc;
 
             rentalAmountLabel.setText(String.format("LKR %.2f", gross));
             depositLabel.setText(String.format("LKR %.2f", eq.getSecurityDeposit()));
             finalPayableLabel.setText(String.format("LKR %.2f", finalAmt));
         } catch (Exception e) {
             errorLabel.setText(e.getMessage());
         }
     }
 
     @FXML
     private void handleCreateRental() {
         errorLabel.setText("");
         Equipment eq = equipmentBox.getValue();
         Customer  cu = customerBox.getValue();
         Branch    br = branchBox.getValue();
         LocalDate sd = startDatePicker.getValue();
         LocalDate ed = endDatePicker.getValue();
 
         if (eq==null || cu==null || br==null || sd==null || ed==null) {
             errorLabel.setText("Please fill all fields.");
             return;
         }
         try {
             EquipmentCategory cat = categoryDAO.getById(eq.getCategoryId());
             long days = ChronoUnit.DAYS.between(sd, ed);
 
             double gross    = rentalService.calculateRentalAmount(
                 eq.getDailyBasePrice(), cat.getBasePriceFactor(),
                 cat.getWeekendMultiplier(), sd, ed);
             double longDisc = rentalService.calcLongRentalDiscount(gross, days);
             double memDisc  = rentalService.getMembershipDiscount(
                 gross - longDisc, cu.getMembershipLevel());
             double finalAmt = gross - longDisc - memDisc;
 
             Rental r = new Rental();
             r.setEquipmentId(eq.getEquipmentId());
             r.setCustomerId(cu.getCustomerId());
             r.setBranchId(br.getBranchId());
             r.setStartDate(sd);
             r.setEndDate(ed);
             r.setRentalAmount(gross);
             r.setSecurityDeposit(eq.getSecurityDeposit());
             r.setLongRentalDiscount(longDisc);
             r.setMembershipDiscount(memDisc);
             r.setFinalPayable(finalAmt);
             r.setPaymentStatus(paymentStatusBox.getValue());
             r.setRentalStatus("ACTIVE");
 
             rentalService.createRental(r);
             errorLabel.setStyle("-fx-text-fill: green;");
             errorLabel.setText("Rental created successfully!");
         } catch (Exception e) {
             errorLabel.setStyle("-fx-text-fill: red;");
             errorLabel.setText(e.getMessage());
         }
     }
 }
