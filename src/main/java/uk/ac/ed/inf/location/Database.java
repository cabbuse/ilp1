package uk.ac.ed.inf.location;

import java.sql.*;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DriverManager;

public class Database {
    public Database(String jdbcString) {
        this.jdbcString = jdbcString;
    }

    public String getJdbcString() {
        return jdbcString;
    }

    public void setJdbcString(String jdbcString) {
        this.jdbcString = jdbcString;
    }


    public String jdbcString;
    Connection conn = null;
    Statement statement = null;


    public boolean createTableFlightpath() {
        DatabaseMetaData databaseMetadata = null;
        try {
            Connection conn = DriverManager.getConnection(jdbcString);
            Statement statement = conn.createStatement();
            databaseMetadata = conn.getMetaData();
            // Note: must capitalise deliveries in the call to getTables
            ResultSet resultSet =databaseMetadata.getTables(null, null,"FLIGHTPATH", null);
            // If the resultSet is not empty then the table exists, so we can drop it
            if (resultSet.next()){
                statement.execute("drop table FLIGHTPATH");
            }

            statement.execute("create table flightpath("+"orderNo varchar(8), "+"fromLongitude double, "+"fromLatitude double, "+"angle int, "+"toLongitude double, "+"toLatitude double)");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }



        return  false;

    }

    public boolean createTableDeliveries() {
        DatabaseMetaData databaseMetadata = null;
            try {
                Connection conn = DriverManager.getConnection(jdbcString);
                Statement statement = conn.createStatement();
                databaseMetadata = conn.getMetaData();
                // Note: must capitalise deliveries in the call to getTables
                ResultSet resultSet =databaseMetadata.getTables(null, null,"DELIVERIES", null);
                // If the resultSet is not empty then the table exists, so we can drop it
                if (resultSet.next()){
                    statement.execute("drop table DELIVERIES");
                }

                statement.execute("create table deliveries("+"orderNo varchar(8), "+"deliveredTo varchar(19), "+"costInPence int)");
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }



        return  false;

    }


    public boolean insertToTableDelivery(String orderno, String deliverTo, int costInPence){
        PreparedStatement delivery = null;
        try {
            delivery = conn.prepareStatement("insert into deliveries values (?, ?, ?)");
            delivery.setString(1, orderno);
            delivery.setString(2, deliverTo);
            delivery.setInt(3, costInPence);
            delivery.execute();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean insertToTableflightPath(String orderno, double fromLongitude, double fromLatitude, int angle, double toLongitude, double toLatitude){
        PreparedStatement flightpath = null;
        try {
            flightpath = conn.prepareStatement("insert into flightpath values (?, ?, ?, ?, ?, ?)");
            flightpath.setString(1, orderno);
            flightpath.setDouble(2,fromLongitude);
            flightpath.setDouble(3,fromLatitude);
            flightpath.setInt(4,angle);
            flightpath.setDouble(5,toLongitude);
            flightpath.setDouble(6,toLatitude);
            flightpath.execute();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public ArrayList<ArrayList<String>> findOrders(String date){
        try {

            try {

                String url = getJdbcString();
                conn = DriverManager.getConnection(url);
                statement = conn.createStatement();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            final String orderQuery = "select * from app.orders where deliveryDate=(?)";
            PreparedStatement psCourseQuery = conn.prepareStatement(orderQuery);
            psCourseQuery.setString(1, date);
            // Search for the dates and add them to a list
            ArrayList<ArrayList<String>> orderlist = new ArrayList<>();//make n by 3 array
            ResultSet rs = psCourseQuery.executeQuery();
            int i = 0;
            while (rs.next()) {
                String orderNo = null;
                String deliver = null;
                String customer = null;
                orderNo = rs.getString("orderNo");
                deliver =rs.getString("deliverTo");
                customer =rs.getString("customer");
                orderlist.add(new ArrayList<String>());
                orderlist.get(i).add(0,orderNo);//need to create n by 3 array here
                orderlist.get(i).add(1,deliver);
                orderlist.get(i).add(2,customer);
                i += 1;
            }
            return  orderlist;
        }
            catch (SQLException e) {
        e.printStackTrace();
    }
        return null;
    }

    public ArrayList<String> findOrdersdetails(String orderNo){
        try {
            final String coursesQuery = "select * from app.orderdetails where orderno=(?)";
            PreparedStatement psCourseQuery = conn.prepareStatement(coursesQuery);
            psCourseQuery.setString(1, orderNo);
            // Search for the dates and add them to a list
            ArrayList<String> orderlist = new ArrayList<>();//make  array
            ResultSet rs = psCourseQuery.executeQuery();
            while (rs.next()) {
                String orderitem = null;
                String orderNum = null;
                orderNum = rs.getString("orderNo");
                orderitem = rs.getString("item");
                orderlist.add(orderNum);
                orderlist.add(orderitem);//need to create n by 3 array here
            }
            return  orderlist;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }






}
