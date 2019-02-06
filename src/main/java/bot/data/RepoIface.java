package bot.data;

import bot.rss.model.RSSMsg;

public interface RepoIface {
    void addMessage(RSSMsg message);

    boolean isPosted(RSSMsg message);
}
