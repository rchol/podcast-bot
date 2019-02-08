package bot.rss;

import bot.data.RepoIface;
import bot.rss.model.RSSMsg;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class RSSFeedParser {

    private final URL url;
    private final RepoIface repo;


    public RSSFeedParser(String feedUrl, RepoIface repo) {
        try {
            this.repo = repo;
            this.url = new URL(feedUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<RSSMsg> readFeed() {
        List<RSSMsg> newMsgs = new ArrayList<>();
        try {
            SyndFeedInput in = new SyndFeedInput();
            SyndFeed feed = in.build(new XmlReader(url));
            List<SyndEntry> entries = feed.getEntries();
            entries.forEach(entry -> {

                RSSMsg msg = new RSSMsg(entry.getUri(),
                    entry.getTitle(),
                    entry.getAuthor(),
                    entry.getEnclosures().get(0).getUrl(),
                    entry.getDescription().getValue());

                if (!repo.isPosted(msg)){
                    newMsgs.add(msg);
                }
            });
        } catch (FeedException | IOException e) {
            throw new RuntimeException(e);
        }
        return newMsgs;
    }
}
