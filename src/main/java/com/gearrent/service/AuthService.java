package com.gearrent.service;
 
 import com.gearrent.dao.UserDAO;
 import com.gearrent.entity.SystemUser;
 
 public class AuthService {
     private static SystemUser currentUser;
     private UserDAO userDAO;
 
     public AuthService() throws Exception {
         userDAO = new UserDAO();
     }
 
     public SystemUser login(String username, String password) throws Exception {
         SystemUser user = userDAO.findByUsername(username);
         if (user != null && user.getPassword().equals(password)) {
             currentUser = user;
             return user;
         }
         throw new Exception("Invalid username or password.");
     }
 
     public static SystemUser getCurrentUser() { return currentUser; }
     
     public static void logout() { 
         currentUser = null; 
     }
 
     public static boolean isAdmin() {
         return currentUser != null && currentUser.isAdmin();
     }
 
     public static boolean isBranchManager() {
         return currentUser != null && currentUser.isBranchManager();
     }

     public static boolean isStaff() {
         return currentUser != null && currentUser.isStaff();
     }
 }

