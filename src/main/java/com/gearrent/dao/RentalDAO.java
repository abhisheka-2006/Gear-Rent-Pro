package com.gearrent.dao;
 
 import com.gearrent.entity.Rental;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.time.LocalDate;
 import java.util.ArrayList;
 import java.util.List;
 
 public class RentalDAO {
     private Connection conn;
 
     public RentalDAO() throws Exception { conn = DBConnection.getConnection(); }
 
     // Check for date overlap before creating any rental/reservation
     // Returns true if overlap exists (BLOCK the operation)
     public boolean checkOverlap(int equipmentId, LocalDate start,
                                 LocalDate end) throws SQLException {
         String sql = "SELECT COUNT(*) FROM rental "
                    + "WHERE equipment_id=? "
                    + "AND rental_status NOT IN ('RETURNED','CANCELLED') "
                    + "AND NOT (end_date < ? OR start_date > ?)";
         PreparedStatement ps = conn.prepareStatement(sql);
         ps.setInt(1, equipmentId);
         ps.setDate(2, Date.valueOf(start));
         ps.setDate(3, Date.valueOf(end));
         ResultSet rs = ps.executeQuery();
         return rs.next() && rs.getInt(1) > 0;
     }
 
     public void save(Rental r) throws SQLException {
         String sql = "INSERT INTO rental (equipment_id,customer_id,branch_id,"
             + "reservation_id,start_date,end_date,rental_amount,security_deposit,"
             + "long_rental_discount,membership_discount,final_payable,"
             + "payment_status,rental_status) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
         PreparedStatement ps = conn.prepareStatement(sql);
         ps.setInt   (1, r.getEquipmentId());
         ps.setInt   (2, r.getCustomerId());
         ps.setInt   (3, r.getBranchId());
         if (r.getReservationId() != null)
             ps.setInt(4, r.getReservationId());
         else
             ps.setNull(4, Types.INTEGER);
         ps.setDate  (5, Date.valueOf(r.getStartDate()));
         ps.setDate  (6, Date.valueOf(r.getEndDate()));
         ps.setDouble(7, r.getRentalAmount());
         ps.setDouble(8, r.getSecurityDeposit());
         ps.setDouble(9, r.getLongRentalDiscount());
         ps.setDouble(10, r.getMembershipDiscount());
         ps.setDouble(11, r.getFinalPayable());
         ps.setString(12, r.getPaymentStatus());
         ps.setString(13, r.getRentalStatus());
         ps.executeUpdate();
     }
 
     // Get all active rentals (for overdue check and display)
     public List<Rental> getActive() throws SQLException {
         return getByStatus("ACTIVE");
     }
 
     public List<Rental> getOverdue() throws SQLException {
         String sql = "SELECT r.*, c.name AS cust_name, "
             + "CONCAT(eq.brand,' ',eq.model) AS eq_info, b.name AS br_name "
             + "FROM rental r "
             + "JOIN customer c ON r.customer_id=c.customer_id "
             + "JOIN equipment eq ON r.equipment_id=eq.equipment_id "
             + "JOIN branch b ON r.branch_id=b.branch_id "
             + "WHERE r.end_date < CURDATE() "
             + "AND r.rental_status='ACTIVE'";
         return executeListQuery(sql);
     }

     public List<Rental> getOverdueByBranch(int branchId) throws SQLException {
         String sql = "SELECT r.*, c.name AS cust_name, "
             + "CONCAT(eq.brand,' ',eq.model) AS eq_info, b.name AS br_name "
             + "FROM rental r "
             + "JOIN customer c ON r.customer_id=c.customer_id "
             + "JOIN equipment eq ON r.equipment_id=eq.equipment_id "
             + "JOIN branch b ON r.branch_id=b.branch_id "
             + "WHERE r.end_date < CURDATE() "
             + "AND r.rental_status='ACTIVE' "
             + "AND r.branch_id=?";
         PreparedStatement ps = conn.prepareStatement(sql);
         ps.setInt(1, branchId);
         return mapList(ps.executeQuery());
     }
 
     public List<Rental> getByStatus(String status) throws SQLException {
         String sql = "SELECT r.*, c.name AS cust_name, "
             + "CONCAT(eq.brand,' ',eq.model) AS eq_info, b.name AS br_name "
             + "FROM rental r "
             + "JOIN customer c ON r.customer_id=c.customer_id "
             + "JOIN equipment eq ON r.equipment_id=eq.equipment_id "
             + "JOIN branch b ON r.branch_id=b.branch_id "
             + "WHERE r.rental_status=?";
         PreparedStatement ps = conn.prepareStatement(sql);
         ps.setString(1, status);
         return mapList(ps.executeQuery());
     }
 
     public Rental getById(int id) throws SQLException {
         String sql = "SELECT r.*, c.name AS cust_name, "
             + "CONCAT(eq.brand,' ',eq.model) AS eq_info, b.name AS br_name "
             + "FROM rental r "
             + "JOIN customer c ON r.customer_id=c.customer_id "
             + "JOIN equipment eq ON r.equipment_id=eq.equipment_id "
             + "JOIN branch b ON r.branch_id=b.branch_id "
             + "WHERE r.rental_id=?";
         PreparedStatement ps = conn.prepareStatement(sql);
         ps.setInt(1, id);
         ResultSet rs = ps.executeQuery();
         return rs.next() ? mapRowFull(rs) : null;
     }
 
     // Process return: update status, return date, fees
     public void processReturn(int rentalId, LocalDate returnDate,
                               double lateFee, double damageCharge,
                               String damageDesc) throws SQLException {
         String sql = "UPDATE rental SET actual_return_date=?,late_fee=?,"
             + "damage_charge=?,damage_description=?,rental_status='RETURNED' "
             + "WHERE rental_id=?";
         PreparedStatement ps = conn.prepareStatement(sql);
         ps.setDate  (1, Date.valueOf(returnDate));
         ps.setDouble(2, lateFee);
         ps.setDouble(3, damageCharge);
         ps.setString(4, damageDesc);
         ps.setInt   (5, rentalId);
         ps.executeUpdate();
     }
 
     // Branch revenue report
     public ResultSet getBranchRevenue(int branchId,
             LocalDate from, LocalDate to) throws SQLException {
         String sql = "SELECT COUNT(*) AS total_rentals, "
             + "SUM(final_payable) AS total_income, "
             + "SUM(late_fee) AS total_late_fees, "
             + "SUM(damage_charge) AS total_damage "
             + "FROM rental "
             + "WHERE branch_id=? AND start_date BETWEEN ? AND ?";
         PreparedStatement ps = conn.prepareStatement(sql);
         ps.setInt(1, branchId);
         ps.setDate(2, Date.valueOf(from));
         ps.setDate(3, Date.valueOf(to));
         return ps.executeQuery();
     }
 
     private List<Rental> executeListQuery(String sql) throws SQLException {
         return mapList(conn.prepareStatement(sql).executeQuery());
     }
 
     private List<Rental> mapList(ResultSet rs) throws SQLException {
         List<Rental> list = new ArrayList<>();
         while (rs.next()) list.add(mapRowFull(rs));
         return list;
     }
 
     private Rental mapRowFull(ResultSet rs) throws SQLException {
         Rental r = new Rental();
         r.setRentalId(rs.getInt("rental_id"));
         r.setEquipmentId(rs.getInt("equipment_id"));
         r.setCustomerId(rs.getInt("customer_id"));
         r.setBranchId(rs.getInt("branch_id"));
         r.setStartDate(rs.getDate("start_date").toLocalDate());
         r.setEndDate(rs.getDate("end_date").toLocalDate());
         Date ret = rs.getDate("actual_return_date");
         if (ret != null) r.setActualReturnDate(ret.toLocalDate());
         r.setRentalAmount(rs.getDouble("rental_amount"));
         r.setSecurityDeposit(rs.getDouble("security_deposit"));
         r.setLongRentalDiscount(rs.getDouble("long_rental_discount"));
         r.setMembershipDiscount(rs.getDouble("membership_discount"));
         r.setFinalPayable(rs.getDouble("final_payable"));
         r.setLateFee(rs.getDouble("late_fee"));
         r.setDamageCharge(rs.getDouble("damage_charge"));
         r.setPaymentStatus(rs.getString("payment_status"));
         r.setRentalStatus(rs.getString("rental_status"));
         try { r.setCustomerName(rs.getString("cust_name")); } catch(Exception e) {}
         try { r.setEquipmentInfo(rs.getString("eq_info")); } catch(Exception e) {}
         try { r.setBranchName(rs.getString("br_name")); } catch(Exception e) {}
         return r;
     }
 }
