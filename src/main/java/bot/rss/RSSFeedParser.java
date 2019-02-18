package bot.rss;

import bot.data.Repository;
import bot.rss.model.RSSChannel;
import bot.rss.model.RSSMsg;
import com.rometools.modules.itunes.EntryInformation;
import com.rometools.modules.itunes.FeedInformationImpl;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.synd.SyndEnclosure;
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
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class RSSFeedParser {
    private static final String ITUNES = "http://www.itunes.com/dtds/podcast-1.0.dtd";

    private final Repository repo;
    private final SyndFeed feed;
    private final RSSChannel channel;

    public RSSFeedParser(RSSChannel channel, Repository repo) {
        try {
            this.repo = repo;
            URL url = new URL(channel.getUrl());
            SyndFeedInput in = new SyndFeedInput();
            feed = in.build(new XmlReader(url));

            Module itunesModule = feed.getModule(ITUNES);
            FeedInformationImpl infoTunes = (FeedInformationImpl) itunesModule;
            channel.setHashtag(infoTunes.getKeywords());
            channel.addHashtag(infoTunes.getAuthor());
            channel.addHashtag(repo.getChannelTags(url.toString()).split(" "));

            this.channel = channel;
        } catch (FeedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<RSSMsg> readFeed() {
        RSSMsg msg = null;
        List<SyndEntry> entries = feed.getEntries();
        for (SyndEntry entry : entries) {
            String htmlDesc = entry.getDescription().getValue();
            Document document = Jsoup.parse(htmlDesc);
            String text = document.text();
            String mediaLink = null;
            Optional<SyndEnclosure> encl = entry.getEnclosures().stream().findFirst();
            if (encl.isPresent()){
                mediaLink = encl.get().getUrl();
            }
            if (!repo.isPosted(entry.getUri())) {
                msg = new RSSMsg(entry.getUri(),
                    entry.getTitle(),
                    entry.getAuthor(),
                    mediaLink,
                    text,
                    entry.getLink(),
                    genHashtags(entry));
            }
        }
        return Optional.ofNullable(msg);
    }

    private List<String> genHashtags(SyndEntry entry){
        Module itunes = entry.getModule(ITUNES);
        EntryInformation info = (EntryInformation) itunes;
        List<String> hastagsMsg = new ArrayList<>(Arrays.asList(info.getKeywords()));
        hastagsMsg.addAll(channel.getHashtag());
        return hastagsMsg;
    }
}
