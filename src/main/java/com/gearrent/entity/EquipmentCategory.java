package com.gearrent.entity;
 
 public class EquipmentCategory {
     private int categoryId;
     private String name;
     private String description;
     private double basePriceFactor;
     private double weekendMultiplier;
     private double lateFeePerDay;
     private boolean isActive;
 
     public EquipmentCategory() {}
 
     public EquipmentCategory(int categoryId, String name, String description,
                              double basePriceFactor, double weekendMultiplier,
                              double lateFeePerDay, boolean isActive) {
         this.categoryId       = categoryId;
         this.name             = name;
         this.description      = description;
         this.basePriceFactor  = basePriceFactor;
         this.weekendMultiplier = weekendMultiplier;
         this.lateFeePerDay    = lateFeePerDay;
         this.isActive         = isActive;
     }
 
     public int    getCategoryId()       { return categoryId; }
     public void   setCategoryId(int v)  { categoryId = v; }
     public String getName()             { return name; }
     public void   setName(String v)     { name = v; }
     public String getDescription()      { return description; }
     public void   setDescription(String v) { description = v; }
     public double getBasePriceFactor()  { return basePriceFactor; }
     public void   setBasePriceFactor(double v) { basePriceFactor = v; }
     public double getWeekendMultiplier(){ return weekendMultiplier; }
     public void   setWeekendMultiplier(double v){ weekendMultiplier = v; }
     public double getLateFeePerDay()    { return lateFeePerDay; }
     public void   setLateFeePerDay(double v){ lateFeePerDay = v; }
     public boolean isActive()           { return isActive; }
     public void   setActive(boolean v)  { isActive = v; }
 
     @Override
     public String toString() { return name; }
 }
