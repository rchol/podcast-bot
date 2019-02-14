package bot.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcConnectionPool;

public class H2DataSource implements SimpleDataSource {
    private final ThreadLocalConnection thread;

    @Inject
    public H2DataSource(){
        DataSource ds = JdbcConnectionPool.create("jdbc:h2:file:/Users/r.chernyshev/Projects/podcast-bot/db", "", "");
        this.thread = new ThreadLocalConnection(ds);
    }

    @Override
    public ResultSet executeQuery(String query) {
        try {
            Statement st = thread.get().createStatement();
            ResultSet rs = st.executeQuery(query);
            return rs;
        } catch (SQLException e) {
            closeConn(thread.get());
            thread.set(null);
            throw new RuntimeException("Unable to create SQL connection or statement to H2 DB", e);
        }
    }

    @Override
    public long executeUpdate(String query, Object... params) {
        try {
            PreparedStatement ps = thread.get().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            int i = 1;
            for (Object param : params){
                ps.setObject(i++, param);
            }
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getLong(1) : -1L;
            }
        } catch (SQLException e) {
            closeConn(thread.get());
            thread.set(null);
            throw new RuntimeException("Unable to create SQL connection or statement to H2 DB", e);
        }
    }

    @Override
    public long executeUpdate(String query) {
        try {
            Statement st = thread.get().createStatement();
            st.executeUpdate(query);
            return 1L;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeConn(Connection c){
        try {
            if (c != null) {
                c.close();
            }
        } catch (Exception ignored) {
        }
    }

    static class ThreadLocalConnection extends ThreadLocal<Connection>{
        private final DataSource ds;

        ThreadLocalConnection(DataSource ds){this.ds = ds;}

        @Override
        protected Connection initialValue() {
            try {
                return ds.getConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
