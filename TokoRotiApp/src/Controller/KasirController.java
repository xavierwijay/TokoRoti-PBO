/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;
import Config.Koneksi;
import Model.Kasir;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class KasirController {
    private Connection conn;

    public KasirController() {
        try {
            conn = Koneksi.configDB();
        } catch (SQLException e) {
            System.out.println("Koneksi gagal: " + e.getMessage());
        }
    }

    public boolean cekLogin(String fullname, String pw, String role) {
        Kasir kr = new Kasir();
        kr.setFullname(fullname);
        kr.setPassword(pw);
        kr.setRole(role);

        boolean status = false;

        String sql = "SELECT * FROM users WHERE "+"fullname ='"+kr.getFullname()+"'"+"AND password ='"+kr.getPassword() + "'"+"AND role ='"+kr.getRole()+"'";

        try {
            Statement stm = conn.createStatement();
            ResultSet res = stm.executeQuery(sql);

            status = res.next();        
        } catch (SQLException e) {
            System.out.println("Login gagal: " + e.getMessage());
        }

        return status;
    }

    
}
