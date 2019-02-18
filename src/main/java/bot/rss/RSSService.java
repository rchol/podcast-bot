package bot.rss;

import bot.data.Repository;
import bot.rss.model.RSSChannel;
import bot.rss.model.RSSMsg;
import bot.telegram.message.TgMsg;
import java.util.ArrayList;
import java.util.List;

public class RSSService {

    private final Repository repo;

    public RSSService(Repository repo) {
        this.repo = repo;
    }


    public List<TgMsg> getPosts() {
        List<RSSChannel> channels = repo.getAllChannels();
        List<RSSMsg> newMsgs = new ArrayList<>();
        channels.stream().map(channel -> new RSSFeedParser(channel, repo)).map(RSSFeedParser::readFeed)
            .forEach(newMsg -> newMsg.ifPresent(newMsgs::add));
        List<TgMsg> posts = new ArrayList<>();
        if (!newMsgs.isEmpty()) {
            newMsgs.forEach(msg -> {
                TgMsg tgMsg = new TgMsg();
                tgMsg.setTitle(msg.getTitle())
                    .setDescription(msg.getDescription())
                    .setLink(msg.getLink(), "SOURCE")
                    .setHashtags(msg.getHashtags())
                    .setMp3link(msg.getEnclosure());
                tgMsg.setGuid(msg.getGuid());
                posts.add(tgMsg);
            });
        }
        return posts;
    }
}
