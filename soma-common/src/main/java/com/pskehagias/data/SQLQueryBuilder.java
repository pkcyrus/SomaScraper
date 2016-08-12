package com.pskehagias.data;

/**
 * Created by pkcyr on 6/16/2016.
 * SQLQueryBuilder contains methods for generating a simple SQL query from variable data.
 */
public class SQLQueryBuilder {
    /**
     * Constructs an SQL Select query string from the given parameters.
     * @param table The name of the table to select from.
     * @param projection The columns to select data from.
     * @param selection The WHERE clause of the SELECT query.
     * @return The SQL query as a String
     */
    public static String buildSelectQuery(String table, String[] projection, String selection){
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT ");
        for (int i = 0; i < projection.length; i++) {
            builder.append(projection[i]);
            builder.append(((projection.length - 1) != i) ? ", " : " ");
        }
        builder.append("from ").append(table).append(" where ").append(selection);
        return builder.toString();
    }
}
