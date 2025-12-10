package Controller;

public class SessionUser {
    private static int userId;        // user_id dari tabel users
    private static String username;   // username login
    private static String namaUser;   // fullname / nama tampilan

    // dipakai setelah login sukses
    public static void set(int id, String user, String nama) {
        userId = id;
        username = user;
        namaUser = nama;
    }

    public static int getUserId() {
        return userId;
    }

    public static String getUsername() {
        return username;
    }

    // supaya kode lama yang pakai setNamaUser/getNamaUser masih aman
    public static void setNamaUser(String nama) {
        namaUser = nama;
    }

    public static String getNamaUser() {
        return namaUser;
    }

    public static void clear() {
        userId = 0;
        username = null;
        namaUser = null;
    }
}
