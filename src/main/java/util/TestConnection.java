//package util;
//
//import java.sql.Connection;
//
//public class TestConnection {
//
//    public static void main(String[] args) {
//
//        Connection con = DBConnection.getConnection();
//
//        if(con != null) {
//
//            System.out.println("Database Connected Successfully");
//
//        } else {
//
//            System.out.println("Connection Failed");
//        }
//
//    }
//
//}


package util;

import java.sql.Connection;
import java.sql.SQLException;

public class TestConnection {

    public static void main(String[] args) {

        try (Connection con = DBConnection.getConnection()) {

            if (con != null && !con.isClosed()) {
                System.out.println("Database Connected Successfully!");
                System.out.println("Connected to: "
                        + con.getMetaData().getURL());
            } else {
                System.out.println("Connection Failed!");
            }

        } catch (SQLException e) {
            System.err.println("Database test failed:");
            e.printStackTrace();
        }
    }
}
