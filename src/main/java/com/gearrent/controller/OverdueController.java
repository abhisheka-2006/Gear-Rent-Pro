package com.gearrent.controller;

import com.gearrent.dao.RentalDAO;
import com.gearrent.entity.Rental;
import com.gearrent.entity.SystemUser;
import com.gearrent.service.AuthService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;

public class OverdueController implements Initializable {

    @FXML private TableView<Rental> overdueTable;
    @FXML private TableColumn<Rental, Integer> rentalIdCol;
    @FXML private TableColumn<Rental, String> customerCol;
    @FXML private TableColumn<Rental, String> equipmentCol;
    @FXML private TableColumn<Rental, String> branchCol;
    @FXML private TableColumn<Rental, LocalDate> endDateCol;
    @FXML private TableColumn<Rental, Long> daysOverdueCol;
    @FXML private TableColumn<Rental, String> contactCol;
    @FXML private Label errorLabel;

    private RentalDAO rentalDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            rentalDAO = new RentalDAO();
            setupColumns();
            loadOverdueRentals();
        } catch (Exception e) {
            errorLabel.setText("Error: " + e.getMessage());
        }
    }

    private void setupColumns() {
        rentalIdCol.setCellValueFactory(new PropertyValueFactory<>("rentalId"));
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        equipmentCol.setCellValueFactory(new PropertyValueFactory<>("equipmentInfo"));
        branchCol.setCellValueFactory(new PropertyValueFactory<>("branchName"));
        endDateCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        daysOverdueCol.setCellValueFactory(cellData -> {
            Rental rental = cellData.getValue();
            long days = ChronoUnit.DAYS.between(rental.getEndDate(), LocalDate.now());
            return new SimpleObjectProperty<>(days);
        });

        daysOverdueCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(item));
                    if (item > 7) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else if (item > 3) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        contactCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
    }

    private void loadOverdueRentals() {
        try {
            List<Rental> overdue;
            SystemUser currentUser = AuthService.getCurrentUser();

            if (currentUser != null && currentUser.isBranchManager()) {
                Integer branchId = currentUser.getBranchId();
                if (branchId == null) {
                    overdueTable.getItems().clear();
                    errorLabel.setStyle("-fx-text-fill: red;");
                    errorLabel.setText("Your manager account is not assigned to a branch.");
                    return;
                }
                overdue = rentalDAO.getOverdueByBranch(branchId);
            } else {
                overdue = rentalDAO.getOverdue();
            }

            overdueTable.setItems(FXCollections.observableArrayList(overdue));

            if (overdue.isEmpty()) {
                errorLabel.setStyle("-fx-text-fill: green;");
                errorLabel.setText("No overdue rentals found.");
            } else {
                errorLabel.setText("");
            }
        } catch (Exception e) {
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("Error loading overdue: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadOverdueRentals();
    }
}