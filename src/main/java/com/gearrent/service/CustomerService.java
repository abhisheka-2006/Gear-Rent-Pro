package com.gearrent.service;

import com.gearrent.dao.CustomerDAO;
import com.gearrent.entity.Customer;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class CustomerService {
    
    private CustomerDAO customerDAO;
    
    // Validation patterns
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z\\s]{2,100}$");
    private static final Pattern NIC_PATTERN = Pattern.compile("^[0-9]{9,12}[vV]?$|^[A-Z0-9]{8,12}$");
    private static final Pattern CONTACT_PATTERN = Pattern.compile("^[0-9]{10,15}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    public CustomerService() throws Exception {
        customerDAO = new CustomerDAO();
    }
    
    //CRUD OPERATIONS
    
    public List<Customer> getAllCustomers() throws SQLException {
        return customerDAO.getAll();
    }
    
    public Customer getCustomerById(int id) throws SQLException {
        return customerDAO.getById(id);
    }
    
    public void addCustomer(Customer customer) throws Exception {
        validateCustomer(customer);
        
        // Check for duplicate NIC/Passport
        List<Customer> existing = customerDAO.getAll();
        for (Customer c : existing) {
            if (c.getNicPassport().equalsIgnoreCase(customer.getNicPassport())) {
                throw new Exception("Customer with this NIC/Passport already exists.");
            }
        }
        
        customerDAO.save(customer);
    }
    
    public void updateCustomer(Customer customer) throws Exception {
        validateCustomer(customer);
        customerDAO.update(customer);
    }
    
    public void deleteCustomer(int id) throws SQLException {
        // Check if customer has active rentals before deleting
        double activeDeposits = customerDAO.getTotalActiveDeposits(id);
        if (activeDeposits > 0) {
            throw new SQLException("Cannot delete customer with active rentals.");
        }
        // In a real implementation, you'd also check for unpaid fees
        customerDAO.delete(id);
    }
    
    // ── VALIDATION ──────────────────────────────────────────────────────────
    
    private void validateCustomer(Customer c) throws Exception {
        if (c.getName() == null || c.getName().trim().isEmpty()) {
            throw new Exception("Customer name is required.");
        }
        if (!NAME_PATTERN.matcher(c.getName()).matches()) {
            throw new Exception("Name must contain only letters and spaces (2-100 characters).");
        }
        
        if (c.getNicPassport() == null || c.getNicPassport().trim().isEmpty()) {
            throw new Exception("NIC or Passport number is required.");
        }
        if (!NIC_PATTERN.matcher(c.getNicPassport()).matches()) {
            throw new Exception("Invalid NIC/Passport format.");
        }
        
        if (c.getContactNo() == null || c.getContactNo().trim().isEmpty()) {
            throw new Exception("Contact number is required.");
        }
        if (!CONTACT_PATTERN.matcher(c.getContactNo()).matches()) {
            throw new Exception("Contact number must contain 10-15 digits.");
        }
        
        if (c.getEmail() != null && !c.getEmail().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(c.getEmail()).matches()) {
                throw new Exception("Invalid email format.");
            }
        }
        
        // Validate membership level
        String level = c.getMembershipLevel();
        if (level == null || level.trim().isEmpty()) {
            c.setMembershipLevel("REGULAR");
        } else if (!level.equals("REGULAR") && !level.equals("SILVER") && !level.equals("GOLD")) {
            throw new Exception("Membership level must be REGULAR, SILVER, or GOLD.");
        }
    }
    
    // ── BUSINESS LOGIC ──────────────────────────────────────────────────────
    
    /*Get discount percentage for a customer based on membership level*/
    public double getDiscountPercentage(String membershipLevel) {
        switch (membershipLevel) {
            case "SILVER": return 5.0;
            case "GOLD": return 10.0;
            default: return 0.0;
        }
    }
    
    /*Calculate discount amount for a given rental amount*/
    public double calculateDiscount(double rentalAmount, String membershipLevel) {
        double pct = getDiscountPercentage(membershipLevel);
        return rentalAmount * (pct / 100.0);
    }
    
    /*Search customers by name or NIC*/
    public List<Customer> searchCustomers(String keyword) throws SQLException {
        // For a complete implementation, you'd add a search method to CustomerDAO
        // This is a simple filter on getAll() for now
        List<Customer> all = customerDAO.getAll();
        if (keyword == null || keyword.trim().isEmpty()) {
            return all;
        }
        
        String lowerKeyword = keyword.toLowerCase().trim();
        return all.stream()
            .filter(c -> c.getName().toLowerCase().contains(lowerKeyword) ||
                        c.getNicPassport().toLowerCase().contains(lowerKeyword))
            .collect(java.util.stream.Collectors.toList());
    }
    
    /*Check if customer can make a new rental based on deposit limit*/
    public boolean canMakeRental(int customerId, double newDeposit) throws SQLException {
        double existingDeposits = customerDAO.getTotalActiveDeposits(customerId);
        double MAX_DEPOSIT_LIMIT = 500_000.0; // Same as in RentalService
        return (existingDeposits + newDeposit) <= MAX_DEPOSIT_LIMIT;
    }
    
    /*Get remaining deposit capacity for a customer*/
    public double getRemainingDepositCapacity(int customerId) throws SQLException {
        double existingDeposits = customerDAO.getTotalActiveDeposits(customerId);
        double MAX_DEPOSIT_LIMIT = 500_000.0;
        return MAX_DEPOSIT_LIMIT - existingDeposits;
    }
}