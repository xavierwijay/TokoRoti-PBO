package Config;

import java.sql.Connection;

public class TesKoneksi {
    public static void main(String[] args) {
        try {
            Connection c = Koneksi.configDB();
            if (c != null) {
                System.out.println("konek!!!!");
            }
        } catch (Exception e) {
            System.out.println("belum connect");
            System.err.println(e.getMessage());
        }
    }
}