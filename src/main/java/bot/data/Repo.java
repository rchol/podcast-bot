package bot.data;

import bot.rss.model.RSSMsg;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class Repo implements RepoIface {

    private final SimpleDataSource ds;

    @Inject
    public Repo(SimpleDataSource ds) {
        this.ds = ds;
        initDb();
    }

    @PostConstruct
    private void initDb(){
        ds.executeUpdate("CREATE TABLE IF NOT EXISTS podcasts\n"
            + "(\n"
            + "    id int AUTO_INCREMENT PRIMARY KEY NOT NULL,\n"
            + "    guid varchar(32) NOT NULL,\n"
            + "    title varchar(100),\n"
            + "    media varchar(200),\n"
            + "    is_active boolean DEFAULT false NOT NULL"
            + ");\n"
            + "CREATE UNIQUE INDEX IF NOT EXISTS podcasts_id_uindex ON podcasts (id);"
            + "CREATE TABLE IF NOT EXISTS channels\n"
            + "(\n"
            + "    id int AUTO_INCREMENT PRIMARY KEY NOT NULL,\n"
            + "    url varchar(200) NOT NULL,\n"
            + "    hashtags varchar(400)\n"
            + ");");
    }

    @Override
    public void addMessage(RSSMsg message) {
        String guid = message.getGuid();
        String media = message.getEnclosure();
        String title = message.getTitle();

        ds.executeUpdate("INSERT INTO podcasts (guid, title, media) VALUES"
            + "(?,?,?)", guid, title, media);
    }

    @Override
    public boolean isPosted(RSSMsg message) {
        try {
            String guid = message.getGuid();
            ResultSet rs = ds.executeQuery("SELECT title FROM podcasts WHERE guid = '" + guid + "'");
            return rs.first();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addChannel(String url, String hashtags) {
        ds.executeUpdate("INSERT INTO channels (url, hashtags) VALUES"
            + "(?,?)", url, hashtags);
    }

    @Override
    public void addChannel(String url) {
        ds.executeUpdate("INSERT INTO channels (url) VALUES"
            + "(?)", url);
    }

    @Override
    public ResultSet getAllChannels() {
        return ds.executeQuery("SELECT url, hashtags FROM channels");
    }

    @Override
    public ResultSet getAllUnregisteredChannels() {
        return ds.executeQuery("SELECT url, hashtags FROM channels WHERE is-active = false");
    }

    @Override
    public ResultSet getChannelTags(String url) {
        return ds.executeQuery("SELECT hashtags FROM channels WHERE url ='"+url+"'");
    }

    @Override
    public void updateRegistration(String url) {
        ds.executeUpdate("UPDATE podcasts SET is-active=true WHERE url='(?)'", url);
    }
}
