import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

public class koneksi {
    static String JDBC_DRIVER="com.mysql.cj.jdbc.Driver";
    static String DB_USER="root";
    static String DB_PASSWORD="";
    static String DB_URL="jdbc:mysql://localhost:3306/mahasiswa";
    
    public static Connection koneksi(){
        try{
            Class.forName(JDBC_DRIVER);
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            
        }catch(ClassNotFoundException | SQLException err){
            System.out.println("Gagal Koneksi Ke Database");
        }
        return null;
    }
}