package Controller;

import Config.Koneksi;
import Model.User;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement; 
import javax.swing.table.DefaultTableModel; 

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
    
    public DefaultTableModel getTableKasir() {
        String[] kolom = {"ID", "Username", "Nama Kasir", "Role"};
        DefaultTableModel model = new DefaultTableModel(null, kolom);

        String sql = "SELECT user_id, username, fullname, role "
                   + "FROM users WHERE role = 'cashier'";

        try (Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery(sql)) {

            while (rs.next()) {
                Object[] row = new Object[4];
                row[0] = rs.getInt("user_id");
                row[1] = rs.getString("username");
                row[2] = rs.getString("fullname");
                row[3] = rs.getString("role");
                model.addRow(row);
            }
        } catch (SQLException e) {
            System.out.println("Load kasir gagal: " + e.getMessage());
        }

        return model;
    }

    // Insert akun kasir baru
    public boolean buatAkunKasir(String username, String fullname, String password) {
        String sql = "INSERT INTO users (username, password, fullname, role) "
                   + "VALUES (?, ?, ?, 'cashier')";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, username.trim().toLowerCase());
            pst.setString(2, password);
            pst.setString(3, fullname.trim());
            return pst.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Buat akun kasir gagal: " + e.getMessage());
            return false;
        }
    }

    // Update data kasir
    public boolean updateKasir(int userId, String username, String fullname, String password) {
        String sql = "UPDATE users SET username = ?, password = ?, fullname = ? "
                   + "WHERE user_id = ? AND role = 'cashier'";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, username.trim().toLowerCase());
            pst.setString(2, password);
            pst.setString(3, fullname.trim());
            pst.setInt(4, userId);
            return pst.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Update kasir gagal: " + e.getMessage());
            return false;
        }
    }

    // Hapus kasir
    public boolean deleteKasir(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ? AND role = 'cashier'";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, userId);
            return pst.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Hapus kasir gagal: " + e.getMessage());
            return false;
        }
    }
}