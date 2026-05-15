package com.gearrent.entity;
 
 import java.time.LocalDate;
 
 public class Rental {
     private int rentalId;
     private int equipmentId;
     private int customerId;
     private int branchId;
     private Integer reservationId; // nullable
     private LocalDate startDate;
     private LocalDate endDate;
     private LocalDate actualReturnDate;
     private double rentalAmount;
     private double securityDeposit;
     private double longRentalDiscount;
     private double membershipDiscount;
     private double finalPayable;
     private double lateFee;
     private double damageCharge;
     private String damageDescription;
     private String paymentStatus;  // PAID / PARTIALLY_PAID / UNPAID
     private String rentalStatus;   // ACTIVE / RETURNED / OVERDUE / CANCELLED
     // Display helpers
     private String customerName;
     private String equipmentInfo;
     private String branchName;
 
     public Rental() {}
 
     // Core getters & setters
     public int  getRentalId()           { return rentalId; }
     public void setRentalId(int v)       { rentalId = v; }
     public int  getEquipmentId()        { return equipmentId; }
     public void setEquipmentId(int v)    { equipmentId = v; }
     public int  getCustomerId()         { return customerId; }
     public void setCustomerId(int v)     { customerId = v; }
     public int  getBranchId()           { return branchId; }
     public void setBranchId(int v)       { branchId = v; }
     public Integer getReservationId()   { return reservationId; }
     public void setReservationId(Integer v){ reservationId = v; }
     public LocalDate getStartDate()     { return startDate; }
     public void setStartDate(LocalDate v){ startDate = v; }
     public LocalDate getEndDate()       { return endDate; }
     public void setEndDate(LocalDate v) { endDate = v; }
     public LocalDate getActualReturnDate(){ return actualReturnDate; }
     public void setActualReturnDate(LocalDate v){ actualReturnDate = v; }
     public double getRentalAmount()     { return rentalAmount; }
     public void setRentalAmount(double v){ rentalAmount = v; }
     public double getSecurityDeposit()  { return securityDeposit; }
     public void setSecurityDeposit(double v){ securityDeposit = v; }
     public double getLongRentalDiscount(){ return longRentalDiscount; }
     public void setLongRentalDiscount(double v){ longRentalDiscount = v; }
     public double getMembershipDiscount(){ return membershipDiscount; }
     public void setMembershipDiscount(double v){ membershipDiscount = v; }
     public double getFinalPayable()     { return finalPayable; }
     public void setFinalPayable(double v){ finalPayable = v; }
     public double getLateFee()          { return lateFee; }
     public void setLateFee(double v)    { lateFee = v; }
     public double getDamageCharge()     { return damageCharge; }
     public void setDamageCharge(double v){ damageCharge = v; }
     public String getDamageDescription(){ return damageDescription; }
     public void setDamageDescription(String v){ damageDescription = v; }
     public String getPaymentStatus()    { return paymentStatus; }
     public void setPaymentStatus(String v){ paymentStatus = v; }
     public String getRentalStatus()     { return rentalStatus; }
     public void setRentalStatus(String v){ rentalStatus = v; }
     public String getCustomerName()     { return customerName; }
     public void setCustomerName(String v){ customerName = v; }
     public String getEquipmentInfo()    { return equipmentInfo; }
     public void setEquipmentInfo(String v){ equipmentInfo = v; }
     public String getBranchName()       { return branchName; }
     public void setBranchName(String v) { branchName = v; }
 }

