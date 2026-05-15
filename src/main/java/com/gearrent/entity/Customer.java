package com.gearrent.entity;

public class Customer {
    private int customerId;
    private String name;
    private String nicPassport;
    private String contactNo;
    private String email;
    private String address;
    private String membershipLevel;

    public Customer() {}

    public Customer(int customerId, String name, String nicPassport,
                    String contactNo, String email, String address,
                    String membershipLevel) {
        this.customerId = customerId;
        this.name = name;
        this.nicPassport = nicPassport;
        this.contactNo = contactNo;
        this.email = email;
        this.address = address;
        this.membershipLevel = membershipLevel;
    }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int v) { customerId = v; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public String getNicPassport() { return nicPassport; }
    public void setNicPassport(String v) { nicPassport = v; }
    public String getContactNo() { return contactNo; }
    public void setContactNo(String v) { contactNo = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { email = v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { address = v; }
    public String getMembershipLevel() { return membershipLevel; }
    public void setMembershipLevel(String v) { membershipLevel = v; }

    @Override
    public String toString() { return name + " | " + membershipLevel; }
}