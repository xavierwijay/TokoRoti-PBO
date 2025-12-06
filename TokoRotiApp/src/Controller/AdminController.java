package Controller;

import Config.Koneksi;
import Model.Admin;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;

public class AdminController {
    private Connection conn;

    public AdminController() {
        try {
            conn = Koneksi.configDB();
        } catch (SQLException e) {
            System.out.println("Koneksi gagal: " + e.getMessage());
        }
    }

    public boolean cekLogin(String namaInput, String pw, String role) {
        Admin adm = new Admin();
        adm.setUsername(namaInput);
        adm.setFullname(namaInput);
        adm.setPassword(pw);
        adm.setRole(role);

    String kolom = "admin".equalsIgnoreCase(role) ? "username" : "fullname";
    String nilai = "admin".equalsIgnoreCase(role) ? adm.getUsername().trim() : adm.getFullname().trim();

    String sql = "SELECT * FROM users WHERE " + kolom + " = '" + nilai + "' " + "AND password = '" + adm.getPassword() + "' "+ "AND role = '" + adm.getRole().toLowerCase() + "'";

    try (java.sql.Statement stm = conn.createStatement();
         java.sql.ResultSet rs = stm.executeQuery(sql)) {
        return rs.next();
    } catch (SQLException e) {
        System.out.println("Login gagal: " + e.getMessage());
        return false;
    }
}

}
