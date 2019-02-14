package bot.data;

import bot.rss.model.RSSMsg;
import java.sql.ResultSet;

public interface RepoIface {
    void addMessage(RSSMsg message);

    boolean isPosted(RSSMsg message);

    void addChannel(String url, String hashtags);

    void addChannel(String url);

    ResultSet getAllChannels();

    ResultSet getAllUnregisteredChannels();

    ResultSet getChannelTags(String url);

    void updateRegistration(String url);
}
