package com.gearrent.entity;
 
 public class Equipment {
     private int equipmentId;
     private String equipmentCode;
     private int categoryId;
     private int branchId;
     private String brand;
     private String model;
     private int purchaseYear;
     private double dailyBasePrice;
     private double securityDeposit;
     private String status; // AVAILABLE / RESERVED / RENTED / UNDER_MAINTENANCE
     // Extra fields for display (joined from other tables)
     private String categoryName;
     private String branchName;
 
     public Equipment() {}
 
     // Full constructor
     public Equipment(int equipmentId, String equipmentCode, int categoryId,
                      int branchId, String brand, String model, int purchaseYear,
                      double dailyBasePrice, double securityDeposit, String status) {
         this.equipmentId    = equipmentId;
         this.equipmentCode  = equipmentCode;
         this.categoryId     = categoryId;
         this.branchId       = branchId;
         this.brand          = brand;
         this.model          = model;
         this.purchaseYear   = purchaseYear;
         this.dailyBasePrice = dailyBasePrice;
         this.securityDeposit= securityDeposit;
         this.status         = status;
     }
 
     // Getters & Setters
     public int    getEquipmentId()   { return equipmentId; }
     public void   setEquipmentId(int v){ equipmentId = v; }
     public String getEquipmentCode() { return equipmentCode; }
     public void   setEquipmentCode(String v){ equipmentCode = v; }
     public int    getCategoryId()    { return categoryId; }
     public void   setCategoryId(int v){ categoryId = v; }
     public int    getBranchId()      { return branchId; }
     public void   setBranchId(int v) { branchId = v; }
     public String getBrand()         { return brand; }
     public void   setBrand(String v) { brand = v; }
     public String getModel()         { return model; }
     public void   setModel(String v) { model = v; }
     public int    getPurchaseYear()  { return purchaseYear; }
     public void   setPurchaseYear(int v){ purchaseYear = v; }
     public double getDailyBasePrice(){ return dailyBasePrice; }
     public void   setDailyBasePrice(double v){ dailyBasePrice = v; }
     public double getSecurityDeposit(){ return securityDeposit; }
     public void   setSecurityDeposit(double v){ securityDeposit = v; }
     public String getStatus()        { return status; }
     public void   setStatus(String v){ status = v; }
     public String getCategoryName()  { return categoryName; }
     public void   setCategoryName(String v){ categoryName = v; }
     public String getBranchName()    { return branchName; }
     public void   setBranchName(String v){ branchName = v; }
 
     @Override
     public String toString(){ return brand + " " + model + " (" + equipmentCode + ")"; }
 }
