package soma.data;

/**
 * Created by pkcyr on 6/16/2016.
 */
public class SQLQueryBuilder {
    public static String buildSelectQuery(String table, String[] projection, String selection, String[] selectionArgs){
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
