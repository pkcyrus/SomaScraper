package com.pskehagias.soma;

import com.pskehagias.soma.data.SomaSQLiteHelper;

import java.sql.*;
import java.time.Duration;

/**
 * Created by pkcyr on 8/15/2016.
 * A simple tool to update old timestamps from PDT to UTC by shifting them forward 7 hours.
 * The first versions of the scraper ignored timezones, simply converting soma's PDT times at face value.
 */
public class TimestampAdjuster {
    public static void main(String[] args){
        SomaSQLiteHelper sqh = new SomaSQLiteHelper();
        try(Connection c = sqh.getConnection();
            Statement count = c.createStatement();
            ResultSet resultSet = count.executeQuery("select count(*) from plays")){

            if(resultSet.next()) {
                System.out.println("There are " + resultSet.getLong(1) + " rows to update...");
            }else{
                System.err.println("Could not access the table 'plays'");
                return;
            }

            try(PreparedStatement statement = c.prepareStatement("update plays set timestamp = timestamp + ?")) {
                statement.setLong(1, Duration.ofHours(7).toMillis());

                System.out.println("Please wait, modifying timestamps...");

                long result = statement.executeUpdate();
                System.out.println("Updated " + result + " rows");
            }
        } catch (SQLException e) {
            System.err.println("Error accessing the database.");
            System.err.println(e.getMessage());
        }
    }
}
