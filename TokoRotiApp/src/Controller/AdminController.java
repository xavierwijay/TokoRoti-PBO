package Controller;

import Config.Koneksi;
import Model.Admin;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;

public class AdminController {
    private Connection conn;
    private Statement  stm;
    private ResultSet  res;
    private String     sql;

    public AdminController() {
        try { conn = Koneksi.configDB(); }
        catch (SQLException e) { System.out.println("Koneksi gagal: " + e.getMessage()); }
    }

    private static String esc(String s) { return s == null ? "" : s.replace("'", "''").trim(); }

    public boolean cekLogin(String un, String pw) {
        Admin adm = new Admin();
        adm.setFullname(un);
        adm.setPassword(pw);
        boolean status = false;

        try {
            this.sql = "SELECT * FROM users WHERE "
                     + "fullname='" + esc(adm.getFullname()) + "' "
                     + "AND password='" + esc(adm.getPassword()) + "' "
                     + "AND role='admin' LIMIT 1";

            this.stm = this.conn.createStatement();
            this.res = this.stm.executeQuery(this.sql);
            status = res.next();
        } catch (Exception e) {
            System.out.println("Query gagal (admin): " + e.getMessage());
            System.out.println("SQL -> " + this.sql);
            status = false;
        } finally {
            try { if (res != null) res.close(); } catch (Exception ignore) {}
            try { if (stm != null) stm.close(); } catch (Exception ignore) {}
        }
        return status;
    }

}