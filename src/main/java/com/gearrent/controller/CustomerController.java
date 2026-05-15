package com.gearrent.controller;

import com.gearrent.dao.CustomerDAO;
import com.gearrent.entity.Customer;
import com.gearrent.service.AuthService;
import com.gearrent.service.CustomerService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.Node;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CustomerController implements Initializable {

    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, Integer> idCol;
    @FXML private TableColumn<Customer, String> nameCol;
    @FXML private TableColumn<Customer, String> nicCol;
    @FXML private TableColumn<Customer, String> contactCol;
    @FXML private TableColumn<Customer, String> emailCol;
    @FXML private TableColumn<Customer, String> membershipCol;
    @FXML private TableColumn<Customer, String> addressCol;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> membershipFilterBox;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnRefresh;
    @FXML private Label errorLabel;
    @FXML private Label statsLabel;

    private CustomerDAO customerDAO;
    private CustomerService customerService;
    private List<Customer> currentCustomerList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            customerDAO = new CustomerDAO();
            customerService = new CustomerService();

            setupTableColumns();
            setupFilters();
            loadCustomerData();

            // Disable edit/delete buttons if no selection
            customerTable.getSelectionModel().selectedItemProperty().addListener(
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
        idCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nicCol.setCellValueFactory(new PropertyValueFactory<>("nicPassport"));
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contactNo"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        
        // Membership level with color styling
        membershipCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getMembershipLevel()));
        membershipCol.setCellFactory(col -> new TableCell<Customer, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "GOLD":
                            setStyle("-fx-text-fill: #C9A03D; -fx-font-weight: bold;");
                            break;
                        case "SILVER":
                            setStyle("-fx-text-fill: #808080; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #8B4513;");
                    }
                }
            }
        });
    }

    private void setupFilters() {
        membershipFilterBox.setItems(FXCollections.observableArrayList(
            "ALL", "REGULAR", "SILVER", "GOLD"));
        membershipFilterBox.setValue("ALL");
    }

    private void loadCustomerData() {
        try {
            String selectedMembership = membershipFilterBox.getValue();
            String searchKeyword = searchField.getText().trim();

            List<Customer> customers = customerDAO.getAll();

            // Apply membership filter
            if (selectedMembership != null && !"ALL".equals(selectedMembership)) {
                customers.removeIf(c -> !c.getMembershipLevel().equals(selectedMembership));
            }

            // Apply search filter
            if (!searchKeyword.isEmpty()) {
                customers.removeIf(c -> 
                    !c.getName().toLowerCase().contains(searchKeyword.toLowerCase()) &&
                    !c.getNicPassport().toLowerCase().contains(searchKeyword.toLowerCase()) &&
                    !c.getContactNo().toLowerCase().contains(searchKeyword.toLowerCase()));
            }

            currentCustomerList = customers;
            customerTable.setItems(FXCollections.observableArrayList(customers));
            
            // Update stats
            long goldCount = customers.stream().filter(c -> "GOLD".equals(c.getMembershipLevel())).count();
            long silverCount = customers.stream().filter(c -> "SILVER".equals(c.getMembershipLevel())).count();
            long regularCount = customers.stream().filter(c -> "REGULAR".equals(c.getMembershipLevel())).count();
            statsLabel.setText(String.format("Total: %d | Gold: %d | Silver: %d | Regular: %d",
                customers.size(), goldCount, silverCount, regularCount));
            
        } catch (Exception e) {
            errorLabel.setText("Error loading customers: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadCustomerData();
        errorLabel.setText("");
    }

    @FXML
    private void handleFilter() {
        loadCustomerData();
    }

    @FXML
    private void handleAdd() {
        showCustomerDialog(null);
    }

    @FXML
    private void handleEdit() {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showCustomerDialog(selected);
        }
    }

    @FXML
    private void handleDelete() {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // Check if customer has active rentals
        try {
            double activeDeposits = customerDAO.getTotalActiveDeposits(selected.getCustomerId());
            if (activeDeposits > 0) {
                Alert warn = new Alert(Alert.AlertType.WARNING);
                warn.setTitle("Cannot Delete");
                warn.setHeaderText("Customer Has Active Rentals");
                warn.setContentText(String.format(
                    "Customer %s has active rentals with total deposits of LKR %.2f.\n" +
                    "Return all equipment before deleting this customer.",
                    selected.getName(), activeDeposits));
                warn.showAndWait();
                return;
            }
        } catch (Exception e) {
            errorLabel.setText("Error checking customer status: " + e.getMessage());
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Customer: " + selected.getName());
        confirm.setContentText("Are you sure? This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                customerDAO.delete(selected.getCustomerId());
                loadCustomerData();
                errorLabel.setStyle("-fx-text-fill: green;");
                errorLabel.setText("Customer deleted successfully.");
            } catch (Exception e) {
                errorLabel.setStyle("-fx-text-fill: red;");
                errorLabel.setText("Delete failed: " + e.getMessage());
            }
        }
    }

    private void showCustomerDialog(Customer customer) {
        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle(customer == null ? "Add Customer" : "Edit Customer");
        
        // Create form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");
        
        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        
        TextField nicField = new TextField();
        nicField.setPromptText("NIC or Passport");
        
        TextField contactField = new TextField();
        contactField.setPromptText("Contact Number");
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email (optional)");
        
        TextArea addressArea = new TextArea();
        addressArea.setPromptText("Address");
        addressArea.setPrefRowCount(3);
        
        ComboBox<String> membershipBox = new ComboBox<>();
        membershipBox.setItems(FXCollections.observableArrayList("REGULAR", "SILVER", "GOLD"));
        membershipBox.setValue("REGULAR");
        
        if (customer != null) {
            nameField.setText(customer.getName());
            nicField.setText(customer.getNicPassport());
            contactField.setText(customer.getContactNo());
            emailField.setText(customer.getEmail());
            addressArea.setText(customer.getAddress());
            membershipBox.setValue(customer.getMembershipLevel());
        }
        
        grid.add(new Label("Name:*"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("NIC/Passport:*"), 0, 1);
        grid.add(nicField, 1, 1);
        grid.add(new Label("Contact:*"), 0, 2);
        grid.add(contactField, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Address:"), 0, 4);
        grid.add(addressArea, 1, 4);
        grid.add(new Label("Membership:"), 0, 5);
        grid.add(membershipBox, 1, 5);
        
        Label validationLabel = new Label();
        validationLabel.setStyle("-fx-text-fill: red;");
        grid.add(validationLabel, 0, 6, 2, 1);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Disable OK button initially for add mode
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(customer == null);
        
        // Real-time validation
        Runnable validate = () -> {
            boolean valid = true;
            StringBuilder errors = new StringBuilder();
            
            if (nameField.getText().trim().isEmpty()) {
                errors.append("Name required. ");
                valid = false;
            }
            if (nicField.getText().trim().isEmpty()) {
                errors.append("NIC required. ");
                valid = false;
            }
            if (contactField.getText().trim().isEmpty()) {
                errors.append("Contact required. ");
                valid = false;
            }
            if (contactField.getText().trim().length() < 10) {
                errors.append("Contact must be 10+ digits. ");
                valid = false;
            }
            String email = emailField.getText().trim();
            if (!email.isEmpty() && !email.contains("@")) {
                errors.append("Invalid email. ");
                valid = false;
            }
            
            validationLabel.setText(errors.toString());
            okButton.setDisable(!valid);
        };
        
        nameField.textProperty().addListener((obs, old, val) -> validate.run());
        nicField.textProperty().addListener((obs, old, val) -> validate.run());
        contactField.textProperty().addListener((obs, old, val) -> validate.run());
        emailField.textProperty().addListener((obs, old, val) -> validate.run());
        
        validate.run();
        
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                Customer c = new Customer();
                c.setName(nameField.getText().trim());
                c.setNicPassport(nicField.getText().trim());
                c.setContactNo(contactField.getText().trim());
                c.setEmail(emailField.getText().trim());
                c.setAddress(addressArea.getText().trim());
                c.setMembershipLevel(membershipBox.getValue());
                return c;
            }
            return null;
        });
        
        Optional<Customer> result = dialog.showAndWait();
        result.ifPresent(c -> {
            try {
                if (customer == null) {
                    customerService.addCustomer(c);
                    errorLabel.setStyle("-fx-text-fill: green;");
                    errorLabel.setText("Customer added successfully!");
                } else {
                    c.setCustomerId(customer.getCustomerId());
                    customerService.updateCustomer(c);
                    errorLabel.setStyle("-fx-text-fill: green;");
                    errorLabel.setText("Customer updated successfully!");
                }
                loadCustomerData();
            } catch (Exception e) {
                errorLabel.setStyle("-fx-text-fill: red;");
                errorLabel.setText("Error: " + e.getMessage());
            }
        });
    }
    
    @FXML
    private void handleExport() {
        // Simple export to console - in production, write to CSV file
        try {
            List<Customer> customers = customerTable.getItems();
            StringBuilder sb = new StringBuilder();
            sb.append("Customer ID,Name,NIC/Passport,Contact,Email,Membership Level,Address\n");
            for (Customer c : customers) {
                sb.append(String.format("%d,%s,%s,%s,%s,%s,%s\n",
                    c.getCustomerId(),
                    c.getName().replace(",", " "),
                    c.getNicPassport(),
                    c.getContactNo(),
                    c.getEmail() != null ? c.getEmail().replace(",", " ") : "",
                    c.getMembershipLevel(),
                    c.getAddress() != null ? c.getAddress().replace(",", " ") : ""
                ));
            }
            
            // Show export dialog
            TextArea textArea = new TextArea(sb.toString());
            textArea.setPrefSize(600, 400);
            
            Dialog<String> exportDialog = new Dialog<>();
            exportDialog.setTitle("Export Customers");
            exportDialog.getDialogPane().setContent(textArea);
            exportDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            exportDialog.showAndWait();
            
        } catch (Exception e) {
            errorLabel.setText("Export error: " + e.getMessage());
        }
    }
}