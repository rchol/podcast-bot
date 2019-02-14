package bot.rss;

import bot.data.RepoIface;
import bot.rss.model.RSSChannel;
import bot.rss.model.RSSMsg;
import com.rometools.modules.itunes.EntryInformation;
import com.rometools.modules.itunes.FeedInformationImpl;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class RSSFeedParser {
    private static final String ITUNES = "http://www.itunes.com/dtds/podcast-1.0.dtd";

    private final URL url;

    private final RepoIface repo;
    private final SyndFeed feed;
    private final RSSChannel channel;

    public RSSFeedParser(RSSChannel channel, RepoIface repo) {
        try {
            this.repo = repo;
            this.url = new URL(channel.getUrl());
            SyndFeedInput in = new SyndFeedInput();
            feed = in.build(new XmlReader(url));

            Module itunesModule = feed.getModule(ITUNES);
            FeedInformationImpl infoTunes = (FeedInformationImpl) itunesModule;
            channel.setHashtag(infoTunes.getKeywords());
            channel.addHashtag(infoTunes.getAuthor());

            this.channel = channel;
        } catch (FeedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<RSSMsg> readFeed() {
        List<RSSMsg> newMsgs = new ArrayList<>();
        List<SyndEntry> entries = feed.getEntries();
        entries.forEach(entry -> {
            String htmlDesc = entry.getDescription().getValue();
            Document document = Jsoup.parse(htmlDesc);
            String text = document.text();
            RSSMsg msg = new RSSMsg(entry.getUri(),
                entry.getTitle(),
                entry.getAuthor(),
                entry.getEnclosures().get(0).getUrl(),
                text,
                entry.getLink(),
                genHashtags(entry));

            if (!repo.isPosted(msg)) {
                newMsgs.add(msg);
            }
        });
        return newMsgs;
    }

    private List<String> genHashtags(SyndEntry entry){
        Module itunes = entry.getModule(ITUNES);
        EntryInformation info = (EntryInformation) itunes;
        List<String> hastagsMsg = new ArrayList<>(Arrays.asList(info.getKeywords()));
        hastagsMsg.addAll(channel.getHashtag());
        return hastagsMsg;
    }
}
