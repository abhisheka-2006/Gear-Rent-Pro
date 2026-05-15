package com.gearrent.dao;
 
 import com.gearrent.entity.Reservation;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.time.LocalDate;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ReservationDAO {
     private Connection conn;
 
     public ReservationDAO() throws Exception { conn = DBConnection.getConnection(); }
 
     public boolean checkOverlap(int equipmentId,
             LocalDate start, LocalDate end) throws SQLException {
         String sql = "SELECT COUNT(*) FROM reservation "
             + "WHERE equipment_id=? AND status='ACTIVE' "
             + "AND NOT (end_date < ? OR start_date > ?)";
         PreparedStatement ps = conn.prepareStatement(sql);
         ps.setInt(1, equipmentId);
         ps.setDate(2, Date.valueOf(start));
         ps.setDate(3, Date.valueOf(end));
         ResultSet rs = ps.executeQuery();
         return rs.next() && rs.getInt(1) > 0;
     }
 
     public void save(Reservation r) throws SQLException {
         String sql = "INSERT INTO reservation "
             + "(equipment_id,customer_id,branch_id,start_date,end_date,status) "
             + "VALUES (?,?,?,?,?,'ACTIVE')";
         PreparedStatement ps = conn.prepareStatement(sql);
         ps.setInt (1, r.getEquipmentId());
         ps.setInt (2, r.getCustomerId());
         ps.setInt (3, r.getBranchId());
         ps.setDate(4, Date.valueOf(r.getStartDate()));
         ps.setDate(5, Date.valueOf(r.getEndDate()));
         ps.executeUpdate();
     }
 
     public List<Reservation> getActive() throws SQLException {
         String sql = "SELECT res.*, c.name AS cust_name, "
             + "CONCAT(e.brand,' ',e.model) AS eq_info "
             + "FROM reservation res "
             + "JOIN customer c ON res.customer_id=c.customer_id "
             + "JOIN equipment e ON res.equipment_id=e.equipment_id "
             + "WHERE res.status='ACTIVE'";
         List<Reservation> list = new ArrayList<>();
         ResultSet rs = conn.prepareStatement(sql).executeQuery();
         while (rs.next()) {
             Reservation r = new Reservation();
             r.setReservationId(rs.getInt("reservation_id"));
             r.setEquipmentId(rs.getInt("equipment_id"));
             r.setCustomerId(rs.getInt("customer_id"));
             r.setBranchId(rs.getInt("branch_id"));
             r.setStartDate(rs.getDate("start_date").toLocalDate());
             r.setEndDate(rs.getDate("end_date").toLocalDate());
             r.setStatus(rs.getString("status"));
             r.setCustomerName(rs.getString("cust_name"));
             r.setEquipmentInfo(rs.getString("eq_info"));
             list.add(r);
         }
         return list;
     }
 
     public void updateStatus(int reservationId, String status) throws SQLException {
         PreparedStatement ps = conn.prepareStatement(
             "UPDATE reservation SET status=? WHERE reservation_id=?");
         ps.setString(1, status);
         ps.setInt(2, reservationId);
         ps.executeUpdate();
     }
 }
