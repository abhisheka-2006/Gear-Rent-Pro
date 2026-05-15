package com.gearrent.dao;
 
 import com.gearrent.entity.EquipmentCategory;
 import java.sql.*;
 import java.util.*;
 
 public class EquipmentCategoryDAO {
     private Connection conn;
 
     public EquipmentCategoryDAO() throws Exception {
         conn = DBConnection.getConnection();
     }
 
     public List<EquipmentCategory> getAll() throws SQLException {
         List<EquipmentCategory> list = new ArrayList<>();
         ResultSet rs = conn.prepareStatement(
             "SELECT * FROM equipment_category").executeQuery();
         while (rs.next()) list.add(mapRow(rs));
         return list;
     }
 
     public EquipmentCategory getById(int id) throws SQLException {
         PreparedStatement ps = conn.prepareStatement(
             "SELECT * FROM equipment_category WHERE category_id=?");
         ps.setInt(1, id);
         ResultSet rs = ps.executeQuery();
         return rs.next() ? mapRow(rs) : null;
     }
 
     public List<EquipmentCategory> getActive() throws SQLException {
         List<EquipmentCategory> list = new ArrayList<>();
         ResultSet rs = conn.prepareStatement(
             "SELECT * FROM equipment_category WHERE is_active=1").executeQuery();
         while (rs.next()) list.add(mapRow(rs));
         return list;
     }
 
     public void save(EquipmentCategory c) throws SQLException {
         String sql = "INSERT INTO equipment_category "
             + "(name,description,base_price_factor,weekend_multiplier,"
             + "late_fee_per_day,is_active) VALUES (?,?,?,?,?,?)";
         PreparedStatement ps = conn.prepareStatement(sql);
         ps.setString(1, c.getName());
         ps.setString(2, c.getDescription());
         ps.setDouble(3, c.getBasePriceFactor());
         ps.setDouble(4, c.getWeekendMultiplier());
         ps.setDouble(5, c.getLateFeePerDay());
         ps.setInt   (6, c.isActive() ? 1 : 0);
         ps.executeUpdate();
     }
 
     public void update(EquipmentCategory c) throws SQLException {
         String sql = "UPDATE equipment_category SET name=?,description=?,"
             + "base_price_factor=?,weekend_multiplier=?,late_fee_per_day=?,"
             + "is_active=? WHERE category_id=?";
         PreparedStatement ps = conn.prepareStatement(sql);
         ps.setString(1, c.getName());
         ps.setString(2, c.getDescription());
         ps.setDouble(3, c.getBasePriceFactor());
         ps.setDouble(4, c.getWeekendMultiplier());
         ps.setDouble(5, c.getLateFeePerDay());
         ps.setInt   (6, c.isActive() ? 1 : 0);
         ps.setInt   (7, c.getCategoryId());
         ps.executeUpdate();
     }
 
     private EquipmentCategory mapRow(ResultSet rs) throws SQLException {
         return new EquipmentCategory(
             rs.getInt("category_id"),
             rs.getString("name"),
             rs.getString("description"),
             rs.getDouble("base_price_factor"),
             rs.getDouble("weekend_multiplier"),
             rs.getDouble("late_fee_per_day"),
             rs.getInt("is_active") == 1
         );
     }
 }
