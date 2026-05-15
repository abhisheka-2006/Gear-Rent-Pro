package com.gearrent.dao;
 
 import com.gearrent.entity.Equipment;
 import java.sql.*;
 import java.util.*;
 
 public class EquipmentDAO {
     private Connection conn;
 
     public EquipmentDAO() throws Exception {
         conn = DBConnection.getConnection();
     }
 
     // Get all equipment with joined category/branch names
     public List<Equipment> getAll() throws SQLException {
         List<Equipment> list = new ArrayList<>();
         String sql = "SELECT e.*, c.name AS cat_name, b.name AS br_name "
                    + "FROM equipment e "
                    + "JOIN equipment_category c ON e.category_id = c.category_id "
                    + "JOIN branch b ON e.branch_id = b.branch_id";
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery();
         while (rs.next()) {
             Equipment eq = mapRow(rs);
             eq.setCategoryName(rs.getString("cat_name"));
             eq.setBranchName(rs.getString("br_name"));
             list.add(eq);
         }
         return list;
     }
 
     // Filter by branch and/or status
     public List<Equipment> getByBranchAndStatus(int branchId, String status)
             throws SQLException {
         String sql = "SELECT e.*, c.name AS cat_name, b.name AS br_name "
                    + "FROM equipment e "
                    + "JOIN equipment_category c ON e.category_id = c.category_id "
                    + "JOIN branch b ON e.branch_id = b.branch_id "
                    + "WHERE e.branch_id = ? "
                    + (status != null ? "AND e.status = ? " : "");
         PreparedStatement ps = conn.prepareStatement(sql);
         ps.setInt(1, branchId);
         if (status != null) ps.setString(2, status);
         ResultSet rs = ps.executeQuery();
         List<Equipment> list = new ArrayList<>();
         while (rs.next()) {
             Equipment eq = mapRow(rs);
             eq.setCategoryName(rs.getString("cat_name"));
             eq.setBranchName(rs.getString("br_name"));
             list.add(eq);
         }
         return list;
     }
 
     public Equipment getById(int id) throws SQLException {
         PreparedStatement ps = conn.prepareStatement(
             "SELECT * FROM equipment WHERE equipment_id = ?");
         ps.setInt(1, id);
         ResultSet rs = ps.executeQuery();
         if (rs.next()) return mapRow(rs);
         return null;
     }
 
     public void save(Equipment e) throws SQLException {
         String sql = "INSERT INTO equipment (equipment_code,category_id,branch_id,"
                    + "brand,model,purchase_year,daily_base_price,security_deposit,status) "
                    + "VALUES (?,?,?,?,?,?,?,?,?)";
         PreparedStatement ps = conn.prepareStatement(sql);
         ps.setString(1, e.getEquipmentCode());
         ps.setInt   (2, e.getCategoryId());
         ps.setInt   (3, e.getBranchId());
         ps.setString(4, e.getBrand());
         ps.setString(5, e.getModel());
         ps.setInt   (6, e.getPurchaseYear());
         ps.setDouble(7, e.getDailyBasePrice());
         ps.setDouble(8, e.getSecurityDeposit());
         ps.setString(9, e.getStatus());
         ps.executeUpdate();
     }
 
     public void update(Equipment e) throws SQLException {
         String sql = "UPDATE equipment SET equipment_code=?,category_id=?,branch_id=?,"
                    + "brand=?,model=?,purchase_year=?,daily_base_price=?,"
                    + "security_deposit=?,status=? WHERE equipment_id=?";
         PreparedStatement ps = conn.prepareStatement(sql);
         ps.setString(1, e.getEquipmentCode());
         ps.setInt   (2, e.getCategoryId());
         ps.setInt   (3, e.getBranchId());
         ps.setString(4, e.getBrand());
         ps.setString(5, e.getModel());
         ps.setInt   (6, e.getPurchaseYear());
         ps.setDouble(7, e.getDailyBasePrice());
         ps.setDouble(8, e.getSecurityDeposit());
         ps.setString(9, e.getStatus());
         ps.setInt   (10, e.getEquipmentId());
         ps.executeUpdate();
     }
 
     public void updateStatus(int equipmentId, String status) throws SQLException {
         PreparedStatement ps = conn.prepareStatement(
             "UPDATE equipment SET status=? WHERE equipment_id=?");
         ps.setString(1, status);
         ps.setInt   (2, equipmentId);
         ps.executeUpdate();
     }
 
     public void delete(int id) throws SQLException {
         PreparedStatement ps = conn.prepareStatement(
             "DELETE FROM equipment WHERE equipment_id=?");
         ps.setInt(1, id);
         ps.executeUpdate();
     }
 
     private Equipment mapRow(ResultSet rs) throws SQLException {
         return new Equipment(
             rs.getInt("equipment_id"),
             rs.getString("equipment_code"),
             rs.getInt("category_id"),
             rs.getInt("branch_id"),
             rs.getString("brand"),
             rs.getString("model"),
             rs.getInt("purchase_year"),
             rs.getDouble("daily_base_price"),
             rs.getDouble("security_deposit"),
             rs.getString("status")
         );
     }
 }
