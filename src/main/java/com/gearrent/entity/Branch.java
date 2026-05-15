package com.gearrent.entity;
 
 public class Branch {
     private int branchId;
     private String branchCode;
     private String name;
     private String address;
     private String contact;
 
     public Branch() {}
 
     public Branch(int branchId, String branchCode,
                   String name, String address, String contact) {
         this.branchId   = branchId;
         this.branchCode = branchCode;
         this.name       = name;
         this.address    = address;
         this.contact    = contact;
     }
 
     // Getters and Setters (use IntelliJ Alt+Insert to generate)
     public int    getBranchId()   { return branchId; }
     public void   setBranchId(int v)   { branchId = v; }
     public String getBranchCode() { return branchCode; }
     public void   setBranchCode(String v) { branchCode = v; }
     public String getName()    { return name; }
     public void   setName(String v)    { name = v; }
     public String getAddress() { return address; }
     public void   setAddress(String v) { address = v; }
     public String getContact() { return contact; }
     public void   setContact(String v) { contact = v; }
 
     @Override
     public String toString() { return name + " (" + branchCode + ")"; }
 }
