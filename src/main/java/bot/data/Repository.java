package bot.data;

import bot.rss.model.RSSChannel;
import bot.telegram.message.TgMsg;
import java.util.List;

public interface Repository {
    void addMessage(TgMsg message);

    boolean isPosted(String guid);

    void addChannel(String url, List<String> hashtagsList);

    void addChannel(String url);

    List<RSSChannel> getAllChannels();

    List<RSSChannel> getAllUnregisteredChannels();

    String getChannelTags(String url);

    void updateRegistration(String url);
}
