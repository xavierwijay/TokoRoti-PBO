package Controller;
import Config.Koneksi;
import Model.Kasir;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class KasirController {
    private Connection conn;
    private Statement  stm;
    private ResultSet  res;
    private String     sql;

    public KasirController() {
        try { conn = Koneksi.configDB(); }
        catch (SQLException e) { System.out.println("Koneksi gagal: " + e.getMessage()); }
    }

    private static String esc(String s) { return s == null ? "" : s.replace("'", "''").trim(); }

    // KASIR: username + password, role=cashier
    public boolean cekLogin(String un, String pw) {
        Kasir k = new Kasir();
        k.setUsername(un);
        k.setPassword(pw);
        boolean status = false;

        try {
            this.sql = "SELECT * FROM users WHERE "
                     + "username='" + esc(k.getUsername()) + "' "
                     + "AND password='" + esc(k.getPassword()) + "' "
                     + "AND role='cashier' LIMIT 1";

            this.stm = this.conn.createStatement();
            this.res = this.stm.executeQuery(this.sql);
            status = res.next();
        } catch (Exception e) {
            System.out.println("Query gagal (kasir): " + e.getMessage());
            System.out.println("SQL -> " + this.sql);
            status = false;
        } finally {
            try { if (res != null) res.close(); } catch (Exception ignore) {}
            try { if (stm != null) stm.close(); } catch (Exception ignore) {}
        }
        return status;
    }
}