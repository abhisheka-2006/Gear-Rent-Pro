package com.gearrent.entity;
 
 import java.time.LocalDate;
 
 public class Reservation {
     private int reservationId;
     private int equipmentId;
     private int customerId;
     private int branchId;
     private LocalDate startDate;
     private LocalDate endDate;
     private String status; // ACTIVE / CONVERTED / CANCELLED
     // Display helpers
     private String customerName;
     private String equipmentInfo;
 
     public Reservation() {}
 
     public int  getReservationId()      { return reservationId; }
     public void setReservationId(int v)  { reservationId = v; }
     public int  getEquipmentId()        { return equipmentId; }
     public void setEquipmentId(int v)    { equipmentId = v; }
     public int  getCustomerId()         { return customerId; }
     public void setCustomerId(int v)     { customerId = v; }
     public int  getBranchId()           { return branchId; }
     public void setBranchId(int v)       { branchId = v; }
     public LocalDate getStartDate()     { return startDate; }
     public void setStartDate(LocalDate v){ startDate = v; }
     public LocalDate getEndDate()       { return endDate; }
     public void setEndDate(LocalDate v) { endDate = v; }
     public String getStatus()           { return status; }
     public void setStatus(String v)     { status = v; }
     public String getCustomerName()     { return customerName; }
     public void setCustomerName(String v){ customerName = v; }
     public String getEquipmentInfo()    { return equipmentInfo; }
     public void setEquipmentInfo(String v){ equipmentInfo = v; }
 }
