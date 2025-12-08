package Controller;

import Config.Koneksi;
import Model.User;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement; 

public class UserController {
    private Connection conn;
    private Statement  stm;
    private ResultSet  res;
    private String     sql;

    public UserController() {
        try { conn = Koneksi.configDB(); }
        catch (SQLException e) { System.out.println("Koneksi gagal: " + e.getMessage()); }
    }

    private static String esc(String s) { return s == null ? "" : s.replace("'", "''").trim(); }

    // USER: username + password, role=customer
    public boolean cekLogin(String un, String pw) {
        User u = new User();
        u.setUsername(un);
        u.setPassword(pw);
        boolean status = false;

        try {
            this.sql = "SELECT * FROM users WHERE " + "username='" + esc(u.getUsername()) + "' " + "AND password='" + esc(u.getPassword()) + "' "
                     + "AND role='customer' LIMIT 1";

            this.stm = this.conn.createStatement();
            this.res = this.stm.executeQuery(this.sql);
            status = res.next();
        } catch (Exception e) {
            System.out.println("Query gagal (user): " + e.getMessage());
            System.out.println("SQL -> " + this.sql);
            status = false;
        } finally {
            try { if (res != null) res.close(); } catch (Exception ignore) {}
            try { if (stm != null) stm.close(); } catch (Exception ignore) {}
        }
        return status;
    }
    public boolean cekUsername(String username) {
    String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";
    try (PreparedStatement pst = conn.prepareStatement(sql)) {
        pst.setString(1, username.trim().toLowerCase());
        try (ResultSet rs = pst.executeQuery()) {
            return rs.next(); // true = sudah ada
        }
    } catch (SQLException e) {
        System.out.println("Cek username gagal: " + e.getMessage());
        return true; // anggap sudah ada supaya aman
    }
}

        // Cek apakah fullname sudah dipakai customer lain (khusus role customer)
    public boolean cekFullname(String fullname) {
        String sql = "SELECT 1 FROM users WHERE fullname = ? AND role = 'customer' LIMIT 1";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, fullname.trim());
        try (ResultSet rs = pst.executeQuery()) {
            return rs.next();
        }
    } catch (SQLException e) {
        System.out.println("Cek fullname gagal: " + e.getMessage());
        return true; // anggap sudah ada supaya aman
    }
}

// Buat akun user (role otomatis 'customer')
    public boolean buatAkunUser(String username, String fullname, String password) {
        String sql = "INSERT INTO users (username, password, fullname, role) "
               + "VALUES (?, ?, ?, 'customer')";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, username.trim().toLowerCase());
            pst.setString(2, password);           // NOTE: masih plaintext; sebaiknya pakai hash (BCrypt)
            pst.setString(3, fullname.trim());
            return pst.executeUpdate() == 1;
    } catch (SQLException e) {
        System.out.println("Buat akun user gagal: " + e.getMessage());
        return false;
        }
    }
}