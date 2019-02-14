package bot.rss.model;

import java.util.List;

public class RSSMsg {

    private String guid;
    private String author;
    private String title;
    private String enclosure;
    private String link;
    private List<String> hashtags;


    public RSSMsg(String guid, String title, String author, String enclosure, String description, String link, List<String> hashtags) {
        this.guid = guid;
        this.author = author;
        this.title = title;
        this.enclosure = enclosure;
        this.description = description;
        this.hashtags = hashtags;
        this.link = link;

    }

    private String description;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getEnclosure() {
        return enclosure;
    }

    public void setEnclosure(String enclosure) {
        this.enclosure = enclosure;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    @Override
    public String toString() {
        return "RSSMsg{" +
            "title='" + title + '\'' +
            ", description='" + description + '\'' +
            ", enclosure='" + enclosure + '\'' +
            ", author='" + author + '\'' +
            ", guid='" + guid + '\'' +
            '}';
    }

}
