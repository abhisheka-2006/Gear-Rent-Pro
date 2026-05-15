package com.gearrent.controller;

import com.gearrent.dao.*;
import com.gearrent.entity.*;
import com.gearrent.service.AuthService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class EquipmentController implements Initializable {

    @FXML private TableView<Equipment> equipmentTable;
    @FXML private TableColumn<Equipment, Integer> idCol;
    @FXML private TableColumn<Equipment, String> codeCol;
    @FXML private TableColumn<Equipment, String> brandCol;
    @FXML private TableColumn<Equipment, String> modelCol;
    @FXML private TableColumn<Equipment, String> categoryCol;
    @FXML private TableColumn<Equipment, String> branchCol;
    @FXML private TableColumn<Equipment, Double> priceCol;
    @FXML private TableColumn<Equipment, Double> depositCol;
    @FXML private TableColumn<Equipment, String> statusCol;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterBox;
    @FXML private ComboBox<Branch> branchFilterBox;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnRefresh;
    @FXML private Label errorLabel;

    // Dialog form fields
    @FXML private TextField equipmentCodeField;
    @FXML private ComboBox<EquipmentCategory> categoryBox;
    @FXML private ComboBox<Branch> branchBox;
    @FXML private TextField brandField;
    @FXML private TextField modelField;
    @FXML private TextField purchaseYearField;
    @FXML private TextField dailyPriceField;
    @FXML private TextField depositField;
    @FXML private ComboBox<String> statusBox;
    @FXML private DialogPane equipmentDialog;

    private EquipmentDAO equipmentDAO;
    private BranchDAO branchDAO;
    private EquipmentCategoryDAO categoryDAO;
    private List<Equipment> currentEquipmentList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            equipmentDAO = new EquipmentDAO();
            branchDAO = new BranchDAO();
            categoryDAO = new EquipmentCategoryDAO();

            setupTableColumns();
            setupFilters();
            loadEquipmentData();

            // Disable edit/delete buttons if no selection
            equipmentTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> {
                    boolean hasSelection = selected != null;
                    btnEdit.setDisable(!hasSelection);
                    btnDelete.setDisable(!hasSelection);
                }
            );

            // Role-based UI restrictions
            if (AuthService.isStaff()) {
                btnAdd.setDisable(true);
                btnEdit.setDisable(true);
                btnDelete.setDisable(true);
                btnAdd.setVisible(false);
                btnEdit.setVisible(false);
                btnDelete.setVisible(false);
            }

        } catch (Exception e) {
            errorLabel.setText("Init error: " + e.getMessage());
        }
    }

    private void setupTableColumns() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("equipmentId"));
        codeCol.setCellValueFactory(new PropertyValueFactory<>("equipmentCode"));
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brand"));
        modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        branchCol.setCellValueFactory(new PropertyValueFactory<>("branchName"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("dailyBasePrice"));
        depositCol.setCellValueFactory(new PropertyValueFactory<>("securityDeposit"));
        
        // Status with color styling
        statusCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus()));
        statusCol.setCellFactory(col -> new TableCell<Equipment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "AVAILABLE":
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            break;
                        case "RENTED":
                            setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                            break;
                        case "RESERVED":
                            setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
                            break;
                        case "UNDER_MAINTENANCE":
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
    }

    private void setupFilters() throws Exception {
        // Status filter options
        statusFilterBox.setItems(FXCollections.observableArrayList(
            "ALL", "AVAILABLE", "RESERVED", "RENTED", "UNDER_MAINTENANCE"));
        statusFilterBox.setValue("ALL");

        // Branch filter
        branchFilterBox.setItems(FXCollections.observableArrayList(branchDAO.getAll()));
        branchFilterBox.setConverter(new StringConverter<Branch>() {
            @Override
            public String toString(Branch b) { return b == null ? "All Branches" : b.getName(); }
            @Override
            public Branch fromString(String s) { return null; }
        });
        
        // Add "All Branches" option
        branchFilterBox.getItems().add(0, null);
        branchFilterBox.setValue(null);
    }

    private void loadEquipmentData() {
        try {
            Branch selectedBranch = branchFilterBox.getValue();
            String selectedStatus = statusFilterBox.getValue();
            String searchKeyword = searchField.getText().trim();

            List<Equipment> equipment;
            
            if (selectedBranch != null && !"ALL".equals(selectedStatus)) {
                equipment = equipmentDAO.getByBranchAndStatus(
                    selectedBranch.getBranchId(), selectedStatus);
            } else if (selectedBranch != null) {
                equipment = equipmentDAO.getByBranchAndStatus(
                    selectedBranch.getBranchId(), null);
            } else if (!"ALL".equals(selectedStatus)) {
                // Get all with status filter - need to filter manually
                equipment = equipmentDAO.getAll();
                equipment.removeIf(e -> !e.getStatus().equals(selectedStatus));
            } else {
                equipment = equipmentDAO.getAll();
            }

            // Apply search filter
            if (!searchKeyword.isEmpty()) {
                equipment.removeIf(e -> 
                    !e.getEquipmentCode().toLowerCase().contains(searchKeyword.toLowerCase()) &&
                    !e.getBrand().toLowerCase().contains(searchKeyword.toLowerCase()) &&
                    !e.getModel().toLowerCase().contains(searchKeyword.toLowerCase()));
            }

            currentEquipmentList = equipment;
            equipmentTable.setItems(FXCollections.observableArrayList(equipment));
            
        } catch (Exception e) {
            errorLabel.setText("Error loading equipment: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadEquipmentData();
        errorLabel.setText("");
    }

    @FXML
    private void handleFilter() {
        loadEquipmentData();
    }

    @FXML
    private void handleAdd() {
        showEquipmentDialog(null);
    }

    @FXML
    private void handleEdit() {
        Equipment selected = equipmentTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showEquipmentDialog(selected);
        }
    }

    @FXML
    private void handleDelete() {
        Equipment selected = equipmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Equipment: " + selected.getEquipmentCode());
        confirm.setContentText("Are you sure? This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                equipmentDAO.delete(selected.getEquipmentId());
                loadEquipmentData();
                errorLabel.setStyle("-fx-text-fill: green;");
                errorLabel.setText("Equipment deleted successfully.");
            } catch (Exception e) {
                errorLabel.setStyle("-fx-text-fill: red;");
                errorLabel.setText("Delete failed: " + e.getMessage());
            }
        }
    }

    private void showEquipmentDialog(Equipment equipment) {
        try {
            // Load dialog FXML (you'll need to create this FXML file)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/EquipmentDialog.fxml"));
            DialogPane dialogPane = loader.load();
            
            EquipmentDialogController dialogController = loader.getController();
            dialogController.initData(equipment, branchDAO, categoryDAO);
            
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle(equipment == null ? "Add Equipment" : "Edit Equipment");
            
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Equipment newEquipment = dialogController.getEquipment();
                if (equipment == null) {
                    equipmentDAO.save(newEquipment);
                } else {
                    newEquipment.setEquipmentId(equipment.getEquipmentId());
                    equipmentDAO.update(newEquipment);
                }
                loadEquipmentData();
                errorLabel.setStyle("-fx-text-fill: green;");
                errorLabel.setText(equipment == null ? "Equipment added!" : "Equipment updated!");
            }
            
        } catch (Exception e) {
            // Fallback to simple input dialog if FXML not available
            showSimpleEquipmentDialog(equipment);
        }
    }

    private void showSimpleEquipmentDialog(Equipment equipment) {
        Dialog<Equipment> dialog = new Dialog<>();
        dialog.setTitle(equipment == null ? "Add Equipment" : "Edit Equipment");
        
        // Create form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");
        
        TextField codeField = new TextField();
        ComboBox<EquipmentCategory> catBox = new ComboBox<>();
        ComboBox<Branch> branchBox = new ComboBox<>();
        TextField brandField = new TextField();
        TextField modelField = new TextField();
        TextField yearField = new TextField();
        TextField priceField = new TextField();
        TextField depositField = new TextField();
        ComboBox<String> statusBox = new ComboBox<>();
        
        try {
            catBox.setItems(FXCollections.observableArrayList(categoryDAO.getActive()));
            branchBox.setItems(FXCollections.observableArrayList(branchDAO.getAll()));
            statusBox.setItems(FXCollections.observableArrayList(
                "AVAILABLE", "RESERVED", "RENTED", "UNDER_MAINTENANCE"));
            
            if (equipment != null) {
                codeField.setText(equipment.getEquipmentCode());
                brandField.setText(equipment.getBrand());
                modelField.setText(equipment.getModel());
                yearField.setText(String.valueOf(equipment.getPurchaseYear()));
                priceField.setText(String.valueOf(equipment.getDailyBasePrice()));
                depositField.setText(String.valueOf(equipment.getSecurityDeposit()));
                statusBox.setValue(equipment.getStatus());
                
                for (EquipmentCategory cat : catBox.getItems()) {
                    if (cat.getCategoryId() == equipment.getCategoryId()) {
                        catBox.setValue(cat);
                        break;
                    }
                }
                for (Branch b : branchBox.getItems()) {
                    if (b.getBranchId() == equipment.getBranchId()) {
                        branchBox.setValue(b);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
        
        grid.add(new Label("Equipment Code:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(catBox, 1, 1);
        grid.add(new Label("Branch:"), 0, 2);
        grid.add(branchBox, 1, 2);
        grid.add(new Label("Brand:"), 0, 3);
        grid.add(brandField, 1, 3);
        grid.add(new Label("Model:"), 0, 4);
        grid.add(modelField, 1, 4);
        grid.add(new Label("Purchase Year:"), 0, 5);
        grid.add(yearField, 1, 5);
        grid.add(new Label("Daily Price (LKR):"), 0, 6);
        grid.add(priceField, 1, 6);
        grid.add(new Label("Security Deposit:"), 0, 7);
        grid.add(depositField, 1, 7);
        grid.add(new Label("Status:"), 0, 8);
        grid.add(statusBox, 1, 8);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                Equipment e = new Equipment();
                e.setEquipmentCode(codeField.getText());
                e.setCategoryId(catBox.getValue().getCategoryId());
                e.setBranchId(branchBox.getValue().getBranchId());
                e.setBrand(brandField.getText());
                e.setModel(modelField.getText());
                e.setPurchaseYear(Integer.parseInt(yearField.getText()));
                e.setDailyBasePrice(Double.parseDouble(priceField.getText()));
                e.setSecurityDeposit(Double.parseDouble(depositField.getText()));
                e.setStatus(statusBox.getValue());
                return e;
            }
            return null;
        });
        
        Optional<Equipment> result = dialog.showAndWait();
        result.ifPresent(e -> {
            try {
                if (equipment == null) {
                    equipmentDAO.save(e);
                } else {
                    e.setEquipmentId(equipment.getEquipmentId());
                    equipmentDAO.update(e);
                }
                loadEquipmentData();
                errorLabel.setStyle("-fx-text-fill: green;");
                errorLabel.setText(equipment == null ? "Equipment added!" : "Equipment updated!");
            } catch (Exception ex) {
                errorLabel.setStyle("-fx-text-fill: red;");
                errorLabel.setText("Error: " + ex.getMessage());
            }
        });
    }
}

// Inner controller class for dialog
class EquipmentDialogController {
    @FXML private TextField equipmentCodeField;
    @FXML private ComboBox<EquipmentCategory> categoryBox;
    @FXML private ComboBox<Branch> branchBox;
    @FXML private TextField brandField;
    @FXML private TextField modelField;
    @FXML private TextField purchaseYearField;
    @FXML private TextField dailyPriceField;
    @FXML private TextField depositField;
    @FXML private ComboBox<String> statusBox;
    
    private Equipment equipment;
    private BranchDAO branchDAO;
    private EquipmentCategoryDAO categoryDAO;
    
    public void initData(Equipment equipment, BranchDAO bDAO, EquipmentCategoryDAO cDAO) throws Exception {
        this.equipment = equipment;
        this.branchDAO = bDAO;
        this.categoryDAO = cDAO;
        
        categoryBox.setItems(FXCollections.observableArrayList(categoryDAO.getActive()));
        branchBox.setItems(FXCollections.observableArrayList(branchDAO.getAll()));
        statusBox.setItems(FXCollections.observableArrayList(
            "AVAILABLE", "RESERVED", "RENTED", "UNDER_MAINTENANCE"));
        
        if (equipment != null) {
            equipmentCodeField.setText(equipment.getEquipmentCode());
            brandField.setText(equipment.getBrand());
            modelField.setText(equipment.getModel());
            purchaseYearField.setText(String.valueOf(equipment.getPurchaseYear()));
            dailyPriceField.setText(String.valueOf(equipment.getDailyBasePrice()));
            depositField.setText(String.valueOf(equipment.getSecurityDeposit()));
            statusBox.setValue(equipment.getStatus());
            
            for (EquipmentCategory cat : categoryBox.getItems()) {
                if (cat.getCategoryId() == equipment.getCategoryId()) {
                    categoryBox.setValue(cat);
                    break;
                }
            }
            for (Branch b : branchBox.getItems()) {
                if (b.getBranchId() == equipment.getBranchId()) {
                    branchBox.setValue(b);
                    break;
                }
            }
        }
    }
    
    public Equipment getEquipment() {
        Equipment e = new Equipment();
        e.setEquipmentCode(equipmentCodeField.getText());
        e.setCategoryId(categoryBox.getValue().getCategoryId());
        e.setBranchId(branchBox.getValue().getBranchId());
        e.setBrand(brandField.getText());
        e.setModel(modelField.getText());
        e.setPurchaseYear(Integer.parseInt(purchaseYearField.getText()));
        e.setDailyBasePrice(Double.parseDouble(dailyPriceField.getText()));
        e.setSecurityDeposit(Double.parseDouble(depositField.getText()));
        e.setStatus(statusBox.getValue());
        return e;
    }
}