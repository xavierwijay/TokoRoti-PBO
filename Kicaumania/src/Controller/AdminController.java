package Controller;
import Model.Admin;
import java.sql.ResultSet;
import java.sql.Statement;

public class AdminController {
    public Statement stm;
    public ResultSet res;
    public String sql;
    
    public AdminController() {
        Koneksi db = new Koneksi();
        db.config();
        this.stm = db.stm;
    }
    
    public boolean cekLogin(String user, String pass) {
        try {
            this.sql = "SELECT * FROM tb_admin WHERE username = '" + user + "' AND password = '" + pass + "'";
            this.res = this.stm.executeQuery(sql);
            return this.res.next();
        } catch (Exception e) {
            return false;
        }
    }
}