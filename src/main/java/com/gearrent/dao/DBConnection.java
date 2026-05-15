package com.gearrent.dao;
 
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
 
 public class DBConnection {
     private static Connection connection;
 
     private DBConnection() {}
 
     public static Connection getConnection() throws Exception {
         if (connection == null || connection.isClosed()) {
             Properties props = new Properties();
            try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in != null) {
                    props.load(in);
                }
            }

            if (!props.containsKey("db.url")) {
                props.setProperty("db.url", "jdbc:h2:file:./data/gearrentpro;MODE=MySQL;DATABASE_TO_UPPER=false;AUTO_SERVER=TRUE");
                props.setProperty("db.username", "sa");
                props.setProperty("db.password", "");
            }

            connection = DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.username"),
                props.getProperty("db.password")
            );
            ensureSchemaInitialized(connection);
            ensureDefaultUsers(connection);
         }
         return connection;
     }

    private static void ensureSchemaInitialized(Connection conn) throws Exception {
        if (tableExists(conn, "system_user") && tableExists(conn, "membership_config")) {
            return;
        }

        runSqlScript(conn, Paths.get("sql", "schema.sql"));
        runSqlScript(conn, Paths.get("sql", "sample_data.sql"));
    }

    private static boolean tableExists(Connection conn, String tableName) throws Exception {
        String sql = "SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE LOWER(TABLE_NAME) = LOWER(?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void runSqlScript(Connection conn, Path filePath) throws Exception {
        String sql = Files.readString(filePath, StandardCharsets.UTF_8);
        sql = sql.replaceAll("(?im)^\\s*CREATE\\s+DATABASE\\s+IF\\s+NOT\\s+EXISTS\\s+[^;]+;\\s*", "");
        sql = sql.replaceAll("(?im)^\\s*USE\\s+[^;]+;\\s*", "");

        StringBuilder cleaned = new StringBuilder();
        for (String line : sql.replace("\r", "").split("\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                continue;
            }
            int commentIndex = line.indexOf("--");
            if (commentIndex >= 0) {
                line = line.substring(0, commentIndex);
            }
            cleaned.append(line.trim()).append(' ');
        }

        try (Statement jdbcStatement = conn.createStatement()) {
            for (String statement : cleaned.toString().split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    jdbcStatement.execute(trimmed);
                }
            }
        }
    }

    private static void ensureDefaultUsers(Connection conn) throws Exception {
        upsertUser(conn, "admin", "admin123", "System Administrator", "ADMIN", null);
        upsertUser(conn, "manager1", "mgr123", "Panadura Manager", "BRANCH_MANAGER", 1);
        upsertUser(conn, "manager2", "mgr123", "Galle Manager", "BRANCH_MANAGER", 2);
        upsertUser(conn, "staff1", "staff123", "Panadura Staff", "STAFF", 1);
        upsertUser(conn, "staff2", "staff123", "Galle Staff", "STAFF", 2);
    }

    private static void upsertUser(Connection conn, String username, String password,
                                   String fullName, String role, Integer branchId) throws Exception {
        Integer safeBranchId = branchId;
        if (branchId != null && !branchExists(conn, branchId)) {
            safeBranchId = null;
        }

        try (PreparedStatement check = conn.prepareStatement(
            "SELECT user_id FROM `system_user` WHERE username=?")) {
            check.setString(1, username);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    String updateSql = "UPDATE `system_user` SET password=?, full_name=?, role=?, branch_id=? WHERE username=?";
                    try (PreparedStatement update = conn.prepareStatement(updateSql)) {
                        update.setString(1, password);
                        update.setString(2, fullName);
                        update.setString(3, role);
                        if (safeBranchId == null) {
                            update.setNull(4, java.sql.Types.INTEGER);
                        } else {
                            update.setInt(4, safeBranchId);
                        }
                        update.setString(5, username);
                        update.executeUpdate();
                    }
                    return;
                }
            }
        }

        String sql = "INSERT INTO `system_user` (username, password, full_name, role, branch_id) "
            + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement insert = conn.prepareStatement(sql)) {
            insert.setString(1, username);
            insert.setString(2, password);
            insert.setString(3, fullName);
            insert.setString(4, role);
            if (safeBranchId == null) {
                insert.setNull(5, java.sql.Types.INTEGER);
            } else {
                insert.setInt(5, safeBranchId);
            }
            insert.executeUpdate();
        }
    }

    private static boolean branchExists(Connection conn, int branchId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("SELECT branch_id FROM branch WHERE branch_id=?")) {
            ps.setInt(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
 }
