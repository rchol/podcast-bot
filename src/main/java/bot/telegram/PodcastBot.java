package bot.telegram;

import bot.data.H2DataSource;
import bot.data.Repo;
import bot.data.RepoIface;
import bot.rss.RSSFeedParser;
import bot.rss.model.RSSMsg;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class PodcastBot extends TelegramLongPollingBot {
    private final List<RSSFeedParser> parsers;
    private final List<String> urlList = new ArrayList<>();
    private final RepoIface repo;
    private static final String BOT_USERNAME = "";
    private static final String BOT_TOKEN = "";
    private static final String CHANEL_ID = "";

    @Inject
    public PodcastBot(){
        repo = new Repo(new H2DataSource());
        parsers = new ArrayList<>();
        urlList.add("https://corecursive.libsyn.com/feed");
        urlList.add("http://javapubhouse.libsyn.com/rss");
        urlList.forEach(url -> parsers.add(new RSSFeedParser(url, repo)));
    }

    @Override
    public void onUpdateReceived(Update update) {
        checkPodcasts();
    }

    public void checkPodcasts(){
        parsers.forEach(parser -> {
            parser.readFeed().forEach(msg -> {
                StringBuilder textPodcast = new StringBuilder();
                textPodcast.append(msg.getTitle())
                    .append("\n\n")
                    .append(msg.getDescription())
                    .append("\n\n")
                    .append(msg.getAuthor());

                sendMessage(textPodcast.toString());
                sendAudio(msg);
                repo.addMessage(msg);
            });
        });
    }

    private void sendMessage(String text){
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(CHANEL_ID);
        sendMessage.setText(text);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendAudio(RSSMsg rssMsg){
        try {
            MpegAudioFileReader mp = new MpegAudioFileReader();
            AudioInputStream ain = mp.getAudioInputStream(new URL(rssMsg.getEnclosure()));//AudioSystem.getAudioInputStream(new URL(rssMsg.getEnclosure()));

            SendAudio sendAudio = new SendAudio();
            sendAudio.setChatId(CHANEL_ID);
            sendAudio.setAudio(rssMsg.getTitle(), ain);

            execute(sendAudio);
        } catch (UnsupportedAudioFileException | IOException | TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }



    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }
}
