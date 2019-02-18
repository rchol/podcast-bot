package bot.data;

import bot.rss.model.RSSChannel;
import bot.rss.model.RSSMsg;
import bot.telegram.message.TgMsg;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class H2Repository implements Repository {

    private final SimpleDataSource ds;

    public H2Repository(SimpleDataSource ds) {
        this.ds = ds;
        initDb();
    }

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
            + ");"
            + "CREATE UNIQUE INDEX IF NOT EXISTS channels_url_uindex ON channels (url);");
    }

    @Override
    public void addMessage(TgMsg message) {
        String guid = message.getGuid();
        String media = message.getMp3link();
        String title = message.getTitle();

        ds.executeUpdate("INSERT INTO podcasts (guid, title, media) VALUES"
            + "(?,?,?)", guid, title, media);
    }

    @Override
    public boolean isPosted(String guid) {
        try {
            ResultSet rs = ds.executeQuery("SELECT title FROM podcasts WHERE guid = '" + guid + "'");
            return rs.first();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addChannel(String url, List<String> hashtagsList) {
        StringBuilder hashtags = new StringBuilder();
        hashtagsList.forEach(tag -> hashtags.append(tag).append(" "));
        ds.executeUpdate("INSERT INTO channels (url, hashtags) VALUES"
            + "(?,?)", url, hashtags.toString());
    }

    @Override
    public void addChannel(String url) {
        ds.executeUpdate("INSERT INTO channels (url) VALUES"
            + "(?)", url);
    }

    @Override
    public List<RSSChannel> getAllChannels() {
        List<RSSChannel> channels = new ArrayList<>();
        try {
            ResultSet rs = ds.executeQuery("SELECT url, hashtags FROM channels");
            collect(channels, rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return channels;
    }

    @Override
    public List<RSSChannel> getAllUnregisteredChannels() {
        List<RSSChannel> channels = new ArrayList<>();
        try {
            ResultSet rs = ds.executeQuery("SELECT url, hashtags FROM channels WHERE is-active = false");
            collect(channels, rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return channels;
    }

    @Override
    public String getChannelTags(String url) {
        try {
            ResultSet rs = ds.executeQuery("SELECT hashtags FROM channels WHERE url ='"+url+"'");
            if (rs.first()){
                return rs.getString("hashtags");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    @Override
    public void updateRegistration(String url) {
        ds.executeUpdate("UPDATE podcasts SET is-active=true WHERE url='(?)'", url);
    }

    private void collect(List<RSSChannel> channels, ResultSet rs) throws SQLException {
        while (rs.next()) {
            String url = rs.getString("url");
            String hashtags = rs.getString("hashtags");
            RSSChannel channel = new RSSChannel(url);
            channel.addHashtag(hashtags);
            channels.add(channel);
        }
    }
}
