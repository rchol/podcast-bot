package bot.telegram;

import bot.data.H2DataSource;
import bot.data.Repo;
import bot.data.RepoIface;
import bot.rss.RSSFeedParser;
import java.util.ArrayList;
import java.util.List;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class PodcastBot extends TelegramLongPollingBot {
    private final List<RSSFeedParser> parsers;
    private final List<String> urlListte
    private final RepoIface repo;
    private String botUsername = "";
    private String botToken = "";

    public PodcastBot(){
        repo = new Repo(new H2DataSource());
        parsers = new ArrayList<>();

    }

    @Override
    public void onUpdateReceived(Update update) {

    }

    public void checkPodcasts(){

    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
