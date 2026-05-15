package com.gearrent.entity;

public class SystemUser {
    private int userId;
    private String username;
    private String password;
    private String fullName;
    private String role;
    private Integer branchId;

    public SystemUser() {}

    public int getUserId() { return userId; }
    public void setUserId(int v) { userId = v; }
    public String getUsername() { return username; }
    public void setUsername(String v) { username = v; }
    public String getPassword() { return password; }
    public void setPassword(String v) { password = v; }
    public String getFullName() { return fullName; }
    public void setFullName(String v) { fullName = v; }
    public String getRole() { return role; }
    public void setRole(String v) { role = v; }
    public Integer getBranchId() { return branchId; }
    public void setBranchId(Integer v) { branchId = v; }

    public boolean isAdmin() { return "ADMIN".equals(role); }
    public boolean isBranchManager() { return "BRANCH_MANAGER".equals(role); }
    public boolean isStaff() { return "STAFF".equals(role); }
}