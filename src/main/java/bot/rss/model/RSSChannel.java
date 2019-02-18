package bot.rss.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RSSChannel {
    private final String url;
    private List<String> hashtag;

    public RSSChannel(String url) {
        this.url = url;
        this.hashtag = new ArrayList<>();
    }

    public void addHashtag(String newHashtag){
        String[] hashtags = newHashtag.split(",");
        Arrays.stream(hashtags).forEach(tag -> {
            tag = tag.replaceAll("[;/:*?\"<>|&' ]", "_");
            tag = tag.replaceAll("_{2,}", "_");
            this.hashtag.add(tag.toLowerCase());
        });

    }

    public void addHashtag(String[] tags){
        Arrays.stream(tags).forEach(this::addHashtag);
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
