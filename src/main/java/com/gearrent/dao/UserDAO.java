package com.gearrent.dao;
 
 import com.gearrent.entity.SystemUser;
 import java.sql.*;
 
 public class UserDAO {
     private Connection conn;
 
     public UserDAO() throws Exception { conn = DBConnection.getConnection(); }
 
     public SystemUser findByUsername(String username) throws SQLException {
         PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM `system_user` WHERE username=?");
         ps.setString(1, username);
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
             SystemUser u = new SystemUser();
             u.setUserId(rs.getInt("user_id"));
             u.setUsername(rs.getString("username"));
             u.setPassword(rs.getString("password"));
             u.setFullName(rs.getString("full_name"));
             u.setRole(rs.getString("role"));
             int bid = rs.getInt("branch_id");
             if (!rs.wasNull()) u.setBranchId(bid);
             return u;
         }
         return null;
     }
 }
