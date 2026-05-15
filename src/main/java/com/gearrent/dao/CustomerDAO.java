package com.gearrent.dao;
 
 import com.gearrent.entity.Customer;
 import java.sql.*;
 import java.util.*;
 
 public class CustomerDAO {
     private Connection conn;
 
     public CustomerDAO() throws Exception { conn = DBConnection.getConnection(); }
 
     public List<Customer> getAll() throws SQLException {
         List<Customer> list = new ArrayList<>();
         ResultSet rs = conn.prepareStatement("SELECT * FROM customer").executeQuery();
         while (rs.next()) list.add(mapRow(rs));
         return list;
     }
 
     public Customer getById(int id) throws SQLException {
         PreparedStatement ps = conn.prepareStatement(
             "SELECT * FROM customer WHERE customer_id=?");
         ps.setInt(1, id);
         ResultSet rs = ps.executeQuery();
         return rs.next() ? mapRow(rs) : null;
     }
 
     public void save(Customer c) throws SQLException {
         String sql = "INSERT INTO customer (name,nic_passport,contact_no,email,"
                    + "address,membership_level) VALUES (?,?,?,?,?,?)";
         PreparedStatement ps = conn.prepareStatement(sql);
         ps.setString(1, c.getName());
         ps.setString(2, c.getNicPassport());
         ps.setString(3, c.getContactNo());
         ps.setString(4, c.getEmail());
         ps.setString(5, c.getAddress());
         ps.setString(6, c.getMembershipLevel());
         ps.executeUpdate();
     }
 
     public void update(Customer c) throws SQLException {
         String sql = "UPDATE customer SET name=?,nic_passport=?,contact_no=?,"
                    + "email=?,address=?,membership_level=? WHERE customer_id=?";
         PreparedStatement ps = conn.prepareStatement(sql);
         ps.setString(1, c.getName());
         ps.setString(2, c.getNicPassport());
         ps.setString(3, c.getContactNo());
         ps.setString(4, c.getEmail());
         ps.setString(5, c.getAddress());
         ps.setString(6, c.getMembershipLevel());
         ps.setInt   (7, c.getCustomerId());
         ps.executeUpdate();
     }

     public void delete(int customerId) throws SQLException {
         PreparedStatement ps = conn.prepareStatement("DELETE FROM customer WHERE customer_id=?");
         ps.setInt(1, customerId);
         ps.executeUpdate();
     }
 
     // Total active deposits for a customer (for limit check)
     public double getTotalActiveDeposits(int customerId) throws SQLException {
         PreparedStatement ps = conn.prepareStatement(
             "SELECT COALESCE(SUM(security_deposit),0) FROM rental "
             + "WHERE customer_id=? AND rental_status='ACTIVE'");
         ps.setInt(1, customerId);
         ResultSet rs = ps.executeQuery();
         return rs.next() ? rs.getDouble(1) : 0;
     }
 
     private Customer mapRow(ResultSet rs) throws SQLException {
         return new Customer(
             rs.getInt("customer_id"),
             rs.getString("name"),
             rs.getString("nic_passport"),
             rs.getString("contact_no"),
             rs.getString("email"),
             rs.getString("address"),
             rs.getString("membership_level")
         );
     }
 }
