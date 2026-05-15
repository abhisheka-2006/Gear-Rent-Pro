package com.gearrent.service;
 
 import com.gearrent.dao.*;
 import com.gearrent.entity.*;
 import java.sql.Connection;
 import java.time.LocalDate;
 import java.time.temporal.ChronoUnit;
 
 public class ReservationService {
     private ReservationDAO reservationDAO;
     private RentalDAO rentalDAO;
     private EquipmentDAO equipmentDAO;
     private CustomerDAO customerDAO;
     private RentalService rentalService;
 
     public ReservationService() throws Exception {
         reservationDAO = new ReservationDAO();
         rentalDAO      = new RentalDAO();
         equipmentDAO   = new EquipmentDAO();
         customerDAO    = new CustomerDAO();
         rentalService  = new RentalService();
     }
 
     public void createReservation(Reservation r) throws Exception {
         long days = ChronoUnit.DAYS.between(r.getStartDate(), r.getEndDate());
         if (days > 30) throw new Exception("Reservation cannot exceed 30 days.");
         if (days < 1)  throw new Exception("End date must be after start date.");
 
         if (reservationDAO.checkOverlap(r.getEquipmentId(),
                 r.getStartDate(), r.getEndDate()))
             throw new Exception("Equipment already reserved for that period.");
 
         if (rentalDAO.checkOverlap(r.getEquipmentId(),
                 r.getStartDate(), r.getEndDate()))
             throw new Exception("Equipment already rented for that period.");
 
         reservationDAO.save(r);
         equipmentDAO.updateStatus(r.getEquipmentId(), "RESERVED");
     }
 
     // Convert reservation to rental (TRANSACTION)
     public void convertToRental(int reservationId, Rental rental) throws Exception {
         Connection conn = DBConnection.getConnection();
         try {
             conn.setAutoCommit(false);
 
             // Re-validate dates still available
             rentalService.validate(
                 rental.getEquipmentId(), rental.getCustomerId(),
                 rental.getStartDate(), rental.getEndDate(),
                 rental.getSecurityDeposit());
 
             reservationDAO.updateStatus(reservationId, "CONVERTED");
             rentalDAO.save(rental);
             equipmentDAO.updateStatus(rental.getEquipmentId(), "RENTED");
 
             conn.commit();
         } catch (Exception e) {
             conn.rollback();
             throw e;
         } finally {
             conn.setAutoCommit(true);
         }
     }
 
     public void cancelReservation(int reservationId,
             int equipmentId) throws Exception {
         reservationDAO.updateStatus(reservationId, "CANCELLED");
         equipmentDAO.updateStatus(equipmentId, "AVAILABLE");
     }
 }
