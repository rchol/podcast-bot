package bot.rss.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RSSChannel {
    private final String url;

    private List<String> hashtag;

    public RSSChannel(String url) {
        this.url = url;
    }

    public void addHashtag(String newHashtag){
        newHashtag = newHashtag.replaceAll("[;/:*?\"<>|&' ]", "_");
        newHashtag = newHashtag.replaceAll("_{2,}", "_");
        this.hashtag.add(newHashtag.toLowerCase());
    }

    public void setHashtag(String[] hashtags) {
        this.hashtag = new ArrayList<>(Arrays.asList(hashtags));
    }

    public String getUrl() {
        return url;
    }

    public List<String> getHashtag() {
        return hashtag;
    }
}
