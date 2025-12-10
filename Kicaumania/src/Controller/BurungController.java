package Controller;
import Model.Burung;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.table.DefaultTableModel;

public class BurungController {
    public Statement stm;
    public ResultSet res;
    public String sql;
    DefaultTableModel dtm = new DefaultTableModel();

    public BurungController() {
        Koneksi db = new Koneksi();
        db.config();
        this.stm = db.stm;
    }

    public DefaultTableModel createTable() {
        dtm = new DefaultTableModel();
        dtm.addColumn("ID");
        dtm.addColumn("Pemilik");
        dtm.addColumn("Nama Burung");
        dtm.addColumn("Jenis");
        dtm.addColumn("No. Gantungan");
        return dtm;
    }

    public void tampilkanData() {
        try {
            dtm.getDataVector().removeAllElements();
            dtm.fireTableDataChanged();
            this.sql = "SELECT * FROM tb_burung";
            this.res = this.stm.executeQuery(sql);
            while (res.next()) {
                Object[] obj = new Object[5];
                obj[0] = res.getString("id");
                obj[1] = res.getString("nama_pemilik");
                obj[2] = res.getString("nama_burung");
                obj[3] = res.getString("jenis_burung");
                obj[4] = res.getString("nomor_gantungan");
                dtm.addRow(obj);
            }
        } catch (Exception e) {}
    }

    public boolean tambahBurung(String pemilik, String nBurung, String jenis, String gantungan) {
        try {
            this.sql = "INSERT INTO tb_burung (nama_pemilik, nama_burung, jenis_burung, nomor_gantungan) VALUES ('"+pemilik+"', '"+nBurung+"', '"+jenis+"', '"+gantungan+"')";
            this.stm.executeUpdate(sql);
            return true;
        } catch (Exception e) { return false; }
    }

    public boolean ubahBurung(int id, String pemilik, String nBurung, String jenis, String gantungan) {
        try {
            this.sql = "UPDATE tb_burung SET nama_pemilik='"+pemilik+"', nama_burung='"+nBurung+"', jenis_burung='"+jenis+"', nomor_gantungan='"+gantungan+"' WHERE id="+id;
            this.stm.executeUpdate(sql);
            return true;
        } catch (Exception e) { return false; }
    }

    public boolean hapusBurung(int id) {
        try {
            this.sql = "DELETE FROM tb_burung WHERE id=" + id;
            this.stm.executeUpdate(sql);
            return true;
        } catch (Exception e) { return false; }
    }
}