package com.gearrent.controller;

import com.gearrent.dao.BranchDAO;
import com.gearrent.entity.Branch;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class BranchController implements Initializable {

    @FXML private TableView<Branch> branchTable;
    @FXML private TableColumn<Branch, Integer> idCol;
    @FXML private TableColumn<Branch, String> codeCol;
    @FXML private TableColumn<Branch, String> nameCol;
    @FXML private TableColumn<Branch, String> addressCol;
    @FXML private TableColumn<Branch, String> contactCol;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Label errorLabel;

    private BranchDAO branchDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            branchDAO = new BranchDAO();
            setupColumns();
            loadBranches();

            branchTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> {
                    boolean hasSelection = selected != null;
                    btnEdit.setDisable(!hasSelection);
                    btnDelete.setDisable(!hasSelection);
                }
            );
        } catch (Exception e) {
            errorLabel.setText("Init error: " + e.getMessage());
        }
    }

    private void setupColumns() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("branchId"));
        codeCol.setCellValueFactory(new PropertyValueFactory<>("branchCode"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
    }

    private void loadBranches() {
        try {
            branchTable.setItems(FXCollections.observableArrayList(branchDAO.getAll()));
        } catch (Exception e) {
            errorLabel.setText("Error loading branches: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        showBranchDialog(null);
    }

    @FXML
    private void handleEdit() {
        Branch selected = branchTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showBranchDialog(selected);
        }
    }

    @FXML
    private void handleDelete() {
        Branch selected = branchTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Branch: " + selected.getName());
        confirm.setContentText("Are you sure? This will affect all equipment in this branch.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                branchDAO.delete(selected.getBranchId());
                loadBranches();
                errorLabel.setStyle("-fx-text-fill: green;");
                errorLabel.setText("Branch deleted.");
            } catch (Exception e) {
                errorLabel.setStyle("-fx-text-fill: red;");
                errorLabel.setText("Delete failed: " + e.getMessage());
            }
        }
    }

    private void showBranchDialog(Branch branch) {
        Dialog<Branch> dialog = new Dialog<>();
        dialog.setTitle(branch == null ? "Add Branch" : "Edit Branch");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");

        TextField codeField = new TextField();
        codeField.setPromptText("e.g., PAN, COL, GAL");
        TextField nameField = new TextField();
        TextField addressField = new TextField();
        TextField contactField = new TextField();

        if (branch != null) {
            codeField.setText(branch.getBranchCode());
            nameField.setText(branch.getName());
            addressField.setText(branch.getAddress());
            contactField.setText(branch.getContact());
        }

        grid.add(new Label("Branch Code:*"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Branch Name:*"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Address:"), 0, 2);
        grid.add(addressField, 1, 2);
        grid.add(new Label("Contact:"), 0, 3);
        grid.add(contactField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                Branch b = new Branch();
                b.setBranchCode(codeField.getText().trim().toUpperCase());
                b.setName(nameField.getText().trim());
                b.setAddress(addressField.getText().trim());
                b.setContact(contactField.getText().trim());
                return b;
            }
            return null;
        });

        Optional<Branch> result = dialog.showAndWait();
        result.ifPresent(b -> {
            try {
                String code = b.getBranchCode() == null ? "" : b.getBranchCode().trim().toUpperCase();
                String name = b.getName() == null ? "" : b.getName().trim();

                if (code.isBlank() || name.isBlank()) {
                    errorLabel.setStyle("-fx-text-fill: red;");
                    errorLabel.setText("Branch Code and Branch Name are required.");
                    return;
                }

                Branch existingByCode = branchDAO.getByCode(code);
                boolean codeAlreadyUsed = existingByCode != null
                    && (branch == null || existingByCode.getBranchId() != branch.getBranchId());

                if (codeAlreadyUsed) {
                    errorLabel.setStyle("-fx-text-fill: red;");
                    errorLabel.setText("Branch code already exists. Use a unique code (e.g., KAN, NEG).");
                    return;
                }

                b.setBranchCode(code);
                b.setName(name);
                b.setAddress(b.getAddress() == null ? "" : b.getAddress().trim());
                b.setContact(b.getContact() == null ? "" : b.getContact().trim());

                if (branch == null) {
                    branchDAO.save(b);
                } else {
                    b.setBranchId(branch.getBranchId());
                    branchDAO.update(b);
                }
                loadBranches();
                errorLabel.setStyle("-fx-text-fill: green;");
                errorLabel.setText(branch == null ? "Branch added!" : "Branch updated!");
            } catch (Exception e) {
                errorLabel.setStyle("-fx-text-fill: red;");
                String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
                if (msg.contains("unique") || msg.contains("duplicate") || msg.contains("branch_code")) {
                    errorLabel.setText("Branch code already exists. Use a unique code.");
                } else {
                    errorLabel.setText("Error: " + e.getMessage());
                }
            }
        });
    }
}