package com.gearrent.service;
 
 import com.gearrent.dao.*;
 import com.gearrent.entity.*;
 import java.sql.Connection;
 import java.time.*;
 import java.time.temporal.ChronoUnit;
 
 public class RentalService {
     private static final double MAX_DEPOSIT_LIMIT = 500_000.0;
     private static final double LONG_RENTAL_DISCOUNT_PERCENT = 10.0;
 
     private RentalDAO rentalDAO;
     private EquipmentDAO equipmentDAO;
     private CustomerDAO customerDAO;
     private ReservationDAO reservationDAO;
 
     public RentalService() throws Exception {
         rentalDAO     = new RentalDAO();
         equipmentDAO  = new EquipmentDAO();
         customerDAO   = new CustomerDAO();
         reservationDAO = new ReservationDAO();
     }
 
     // ── PRICING ────────────────────────────────────────────────────────
     public double calculateRentalAmount(double dailyBasePrice,
             double categoryFactor, double weekendMultiplier,
             LocalDate startDate, LocalDate endDate) {
         double total = 0;
         LocalDate current = startDate;
         while (!current.isAfter(endDate)) {
             DayOfWeek day = current.getDayOfWeek();
             boolean isWeekend = (day==DayOfWeek.SATURDAY || day==DayOfWeek.SUNDAY);
             double mult = isWeekend ? weekendMultiplier : 1.0;
             total += dailyBasePrice * categoryFactor * mult;
             current = current.plusDays(1);
         }
         return total;
     }
 
     public double calcLongRentalDiscount(double amount, long days) {
         return days >= 7 ? amount * (LONG_RENTAL_DISCOUNT_PERCENT / 100.0) : 0;
     }
 
     public double getMembershipDiscount(double amount,
             String membershipLevel) throws Exception {
         // REGULAR=0%, SILVER=5%, GOLD=10%
         double pct = switch (membershipLevel) {
             case "SILVER" -> 5.0;
             case "GOLD"   -> 10.0;
             default       -> 0.0;
         };
         return amount * (pct / 100.0);
     }
 
     public double calculateLateFee(LocalDate endDate,
             LocalDate returnDate, double lateFeePerDay) {
         if (returnDate.isAfter(endDate)) {
             long days = ChronoUnit.DAYS.between(endDate, returnDate);
             return days * lateFeePerDay;
         }
         return 0;
     }
 
     // ── VALIDATION ─────────────────────────────────────────────────────
     public void validate(int equipmentId, int customerId,
             LocalDate startDate, LocalDate endDate,
             double depositAmount) throws Exception {
 
         long days = ChronoUnit.DAYS.between(startDate, endDate);
         if (days > 30)
             throw new Exception("Rental cannot exceed 30 days.");
         if (days < 1)
             throw new Exception("End date must be after start date.");
         if (startDate.isBefore(LocalDate.now()))
             throw new Exception("Start date cannot be in the past.");
 
         // Overlap check (rentals)
         if (rentalDAO.checkOverlap(equipmentId, startDate, endDate))
             throw new Exception("Equipment is already rented for that period.");
 
         // Overlap check (reservations)
         if (reservationDAO.checkOverlap(equipmentId, startDate, endDate))
             throw new Exception("Equipment has an active reservation for that period.");
 
         // Deposit limit
         double existing = customerDAO.getTotalActiveDeposits(customerId);
         if (existing + depositAmount > MAX_DEPOSIT_LIMIT)
             throw new Exception(String.format(
                 "Deposit limit exceeded. Current: LKR %.2f, Limit: LKR %.2f",
                 existing + depositAmount, MAX_DEPOSIT_LIMIT));
     }
 
     // ── CREATE RENTAL (TRANSACTION) ────────────────────────────────────
     public void createRental(Rental rental) throws Exception {
         Connection conn = DBConnection.getConnection();
         try {
             conn.setAutoCommit(false);
 
             validate(rental.getEquipmentId(), rental.getCustomerId(),
                      rental.getStartDate(), rental.getEndDate(),
                      rental.getSecurityDeposit());
 
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
 
     // ── PROCESS RETURN (TRANSACTION) ──────────────────────────────────
     public double processReturn(int rentalId, LocalDate returnDate,
             double damageCharge, String damageDesc) throws Exception {
         Connection conn = DBConnection.getConnection();
         try {
             conn.setAutoCommit(false);
 
             Rental rental = rentalDAO.getById(rentalId);
             if (rental == null) throw new Exception("Rental not found.");
 
             Equipment eq = equipmentDAO.getById(rental.getEquipmentId());
 
             // Get category for late fee rate
             EquipmentCategoryDAO catDAO = new EquipmentCategoryDAO();
             EquipmentCategory cat = catDAO.getById(eq.getCategoryId());
 
             double lateFee = calculateLateFee(
                 rental.getEndDate(), returnDate, cat.getLateFeePerDay());
 
             rentalDAO.processReturn(rentalId, returnDate,
                 lateFee, damageCharge, damageDesc);
 
             String newStatus = damageCharge > 0 ? "UNDER_MAINTENANCE" : "AVAILABLE";
             equipmentDAO.updateStatus(rental.getEquipmentId(), newStatus);
 
             conn.commit();
 
             // Return positive = refund to customer, negative = customer owes
             return rental.getSecurityDeposit() - lateFee - damageCharge;
         } catch (Exception e) {
             conn.rollback();
             throw e;
         } finally {
             conn.setAutoCommit(true);
         }
     }
 }
