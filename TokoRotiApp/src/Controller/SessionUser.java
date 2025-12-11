package Controller;

public class SessionUser {
    private static int userId;        
    private static String username;   
    private static String namaUser;   

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
