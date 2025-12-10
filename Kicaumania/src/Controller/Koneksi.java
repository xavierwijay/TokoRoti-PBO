package Controller;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import javax.swing.JOptionPane;

public class Koneksi {
    public Connection con;
    public Statement stm;
    
    public void config(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost/kicaumania_db"; 
            String user = "root"; 
            String pass = "";     
            con = DriverManager.getConnection(url, user, pass);
            stm = con.createStatement();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Koneksi Gagal: " + e.getMessage());
        }
    }
}