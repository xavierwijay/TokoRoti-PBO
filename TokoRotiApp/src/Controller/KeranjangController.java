package Controller;

import Config.Koneksi;
import Model.KeranjangPemesanan;
import Model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class KeranjangController {
    private static KeranjangController instance;

    private final List<KeranjangPemesanan> items = new ArrayList<>();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Nama", "Harga", "Jumlah", "Subtotal"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            if (c == 1 || c == 3) return Double.class; // Harga, Subtotal
            if (c == 2) return Integer.class;         // Jumlah
            return String.class;                      // Nama
        }
    };

    private KeranjangController() {}

    public static KeranjangController getInstance() {
        if (instance == null) {
            instance = new KeranjangController();
        }
        return instance;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public List<KeranjangPemesanan> getItems() {
        return items;
    }

    // ====== RESOLVE user_id dari SessionUser ATAU tabel users ======

    /**
     * Kalau SessionUser.getUserId() sudah > 0, langsung pakai itu.
     * Kalau belum, cari ke tabel users berdasarkan username/fullname
     * yang disimpan di SessionUser.getNamaUser().
     */
    private int resolveCurrentUserId() {
        int id = SessionUser.getUserId();
        if (id > 0) return id;

        String nama = SessionUser.getNamaUser();
        if (nama == null || nama.trim().isEmpty()) return 0;

        try (Connection conn = Koneksi.configDB();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT user_id, username, fullname " +
                     "FROM users " +
                     "WHERE username = ? OR fullname = ? " +
                     "LIMIT 1")) {

            ps.setString(1, nama);
            ps.setString(2, nama);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt("user_id");
                    String username = rs.getString("username");
                    String fullname = rs.getString("fullname");

                    // simpan ke SessionUser supaya next time tidak perlu query lagi
                    SessionUser.set(id, username, fullname);
                }
            }
        } catch (SQLException e) {
            System.out.println("Gagal resolve user id: " + e.getMessage());
        }

        return id;
    }

    // pakai helper di atas
    private int getCurrentUserId() {
        return resolveCurrentUserId();
    }

    // ========= operasi di keranjang (memory + DB) =========

    public void tambahItem(Product p, int jumlahPesanan) {
        if (p == null || jumlahPesanan <= 0) return;

        for (KeranjangPemesanan ki : items) {
            if (ki.getProduct().getId() == p.getId()) {
                ki.setJumlahPesanan(ki.getJumlahPesanan() + jumlahPesanan);
                tampilkanTable();
                simpanAtauUpdateItemDiDb(p);  // update di DB
                return;
            }
        }

        items.add(new KeranjangPemesanan(p, jumlahPesanan));
        tampilkanTable();
        simpanAtauUpdateItemDiDb(p);          // insert di DB
    }

    public void hapusItem(int row) {
        if (row >= 0 && row < items.size()) {
            KeranjangPemesanan deleted = items.remove(row);
            tampilkanTable();
            hapusItemDiDb(deleted);
        }
    }

    public void clear() {
        items.clear();
        tampilkanTable();
        clearDbForUser();
    }

    public double getTotal() {
        double total = 0.0;
        for (KeranjangPemesanan ki : items) {
            total += ki.getSubtotal();
        }
        return total;
    }

    private void tampilkanTable() {
        tableModel.setRowCount(0);
        for (KeranjangPemesanan ki : items) {
            tableModel.addRow(new Object[]{
                    ki.getProduct().getName(),
                    ki.getProduct().getPrice(),
                    ki.getJumlahPesanan(),
                    ki.getSubtotal()
            });
        }
    }

    // ========= sinkron ke tabel cart_items =========

    // dipanggil setelah user login / saat buka form KeranjangPemesanan
    public void loadFromDatabaseForCurrentUser() {
        items.clear();

        int userId = getCurrentUserId();
        System.out.println("Load keranjang untuk userId = " + userId);

        if (userId <= 0) {
            tampilkanTable();
            return;
        }

        try (Connection conn = Koneksi.configDB();
             Statement st = conn.createStatement()) {

            String sql = "SELECT c.product_id, c.quantity, m.name, m.price " +
                         "FROM cart_items c " +
                         "JOIN tbmakanan m ON c.product_id = m.product_id " +
                         "WHERE c.user_id = " + userId;

            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String name   = rs.getString("name");
                double price  = rs.getDouble("price");
                int qty       = rs.getInt("quantity");

                // Product punya constructor:
                // Product(), Product(int,String,double,int,String,String)
                Product p = new Product(productId, name, price, 0, "", "");

                KeranjangPemesanan kp = new KeranjangPemesanan(p, qty);
                items.add(kp);
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println("Gagal load keranjang dari database: " + e.getMessage());
        }

        tampilkanTable();
        System.out.println("Jumlah item setelah load: " + items.size());
    }

    // simpan transaksi + detail, kosongkan cart_items
    public void checkout() throws SQLException {
        int userId = getCurrentUserId();
        if (userId <= 0) {
            throw new SQLException("User belum login.");
        }
        if (items.isEmpty()) {
            throw new SQLException("Keranjang masih kosong.");
        }

        Connection conn = Koneksi.configDB();
        try {
            conn.setAutoCommit(false);
            Statement st = conn.createStatement();

            double total = getTotal();

            // 1. insert ke transactions
            String sqlTrx = "INSERT INTO transactions(user_id, total_amount) VALUES(" +
                            userId + "," + total + ")";
            st.executeUpdate(sqlTrx);

            // 2. ambil id transaksi terbaru
            int transactionId = 0;
            ResultSet rsId = st.executeQuery("SELECT LAST_INSERT_ID() AS id");
            if (rsId.next()) {
                transactionId = rsId.getInt("id");
            }
            rsId.close();

            // 3. insert ke transaction_details + update stok
            for (KeranjangPemesanan ki : items) {
                int productId = ki.getProduct().getId();
                int qty       = ki.getJumlahPesanan();
                double subtotal = ki.getSubtotal();

                String sqlDetail =
                    "INSERT INTO transaction_details(transaction_id, product_id, quantity, subtotal) " +
                    "VALUES(" + transactionId + "," + productId + "," + qty + "," + subtotal + ")";
                st.executeUpdate(sqlDetail);

                String sqlStock =
                    "UPDATE tbmakanan SET stock = stock - " + qty +
                    " WHERE product_id = " + productId;
                st.executeUpdate(sqlStock);
            }

            // 4. hapus keranjang user di DB
            String sqlClearCart = "DELETE FROM cart_items WHERE user_id = " + userId;
            st.executeUpdate(sqlClearCart);

            conn.commit();

            // 5. kosongkan keranjang di memory juga
            items.clear();
            tampilkanTable();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    private void simpanAtauUpdateItemDiDb(Product p) {
        int userId = getCurrentUserId();
        if (userId <= 0 || p == null) return;

        // cari quantity terbaru di list items
        int qty = 0;
        for (KeranjangPemesanan ki : items) {
            if (ki.getProduct().getId() == p.getId()) {
                qty = ki.getJumlahPesanan();
                break;
            }
        }

        try (Connection conn = Koneksi.configDB();
             Statement st = conn.createStatement()) {

            String sqlCek = "SELECT * FROM cart_items " +
                            "WHERE user_id = " + userId +
                            " AND product_id = " + p.getId();
            ResultSet rs = st.executeQuery(sqlCek);
            if (rs.next()) {
                // update
                String sqlUpdate = "UPDATE cart_items SET quantity = " + qty +
                                   " WHERE user_id = " + userId +
                                   " AND product_id = " + p.getId();
                st.executeUpdate(sqlUpdate);
            } else {
                // insert baru
                String sqlInsert = "INSERT INTO cart_items(user_id, product_id, quantity) VALUES(" +
                                   userId + "," + p.getId() + "," + qty + ")";
                st.executeUpdate(sqlInsert);
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println("Gagal simpan keranjang ke database: " + e.getMessage());
        }
    }

    private void hapusItemDiDb(KeranjangPemesanan item) {
        int userId = getCurrentUserId();
        if (userId <= 0 || item == null) return;

        try (Connection conn = Koneksi.configDB();
             Statement st = conn.createStatement()) {

            String sql = "DELETE FROM cart_items " +
                         "WHERE user_id = " + userId +
                         " AND product_id = " + item.getProduct().getId();
            st.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println("Gagal hapus item keranjang di database: " + e.getMessage());
        }
    }

    private void clearDbForUser() {
        int userId = getCurrentUserId();
        if (userId <= 0) return;

        try (Connection conn = Koneksi.configDB();
             Statement st = conn.createStatement()) {

            String sql = "DELETE FROM cart_items WHERE user_id = " + userId;
            st.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println("Gagal clear keranjang di database: " + e.getMessage());
        }
    }
}
