package bot.data;

import java.sql.ResultSet;

public interface SimpleDataSource {
    ResultSet executeQuery(String query);
    long executeUpdate(String query, Object... params);
    long executeUpdate(String query);

}
