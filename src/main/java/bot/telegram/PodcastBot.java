package bot.telegram;

import bot.data.H2DataSource;
import bot.data.Repo;
import bot.data.RepoIface;
import bot.rss.RSSFeedParser;
import bot.rss.model.RSSChannel;
import bot.rss.model.RSSMsg;
import bot.telegram.audio.AudioPreparator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class PodcastBot extends TelegramLongPollingBot {

    private final List<RSSFeedParser> parsers;
    private final List<RSSChannel> channelList = new ArrayList<>();
    private final RepoIface repo;
    private static final String BOT_USERNAME = "";
    private static final String BOT_TOKEN = "6193544";
    private static final String CHANEL_ID = "@p";
    private Timer timer = null;

    @Inject
    public PodcastBot() {
        repo = new Repo(new H2DataSource());
        parsers = new ArrayList<>();
        channelList.add(new RSSChannel("http://javapubhouse.libsyn.com/rss"));
        channelList.add(new RSSChannel("https://corecursive.libsyn.com/feed"));
        channelList.forEach(channel -> parsers.add(new RSSFeedParser(channel, repo)));
    }

    @Override
    public void onUpdateReceived(Update update) {
        String command = update.getMessage().getText();
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new PodcastTask(), 0, 28800L);
        }
    }

    public void checkPodcasts() {
        parsers.forEach(parser -> {
            parser.readFeed().forEach(msg -> {
                StringBuilder textPodcast = new StringBuilder();
                textPodcast.append(msg.getTitle())
                    .append("\n\n")
                    .append(msg.getDescription())
                    .append("\n\n")
                    .append(String.format("<a href=\"%s\">%s</a>", msg.getLink(), "SOURCE"))
                    .append("\n\n")
                    .append(getHashtagsAsString(msg.getHashtags()));
                int replyId = sendMessage(textPodcast.toString());
                sendAudio(msg, replyId);
                repo.addMessage(msg);
            });
        });
    }

    private int sendMessage(String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(false);
        sendMessage.setChatId(CHANEL_ID);
        sendMessage.disableWebPagePreview();
        sendMessage.setParseMode("HTML");
        sendMessage.setText(text);
        try {
           Message sent = execute(sendMessage);
           return sent.getMessageId();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendAudio(RSSMsg rssMsg, int replyId) {
        AudioPreparator preparator = new AudioPreparator();
        String audioFile = preparator.retrive(rssMsg.getEnclosure());
        List<String> filesToSend = preparator.splitAudio(audioFile, 49);
        AtomicInteger idx = new AtomicInteger(1);
        filesToSend.forEach(filename -> {
            File file = new File(filename);
            try (FileInputStream ain = new FileInputStream(file) ) {
                SendAudio sendAudio = new SendAudio();
                sendAudio.setChatId(CHANEL_ID);
                String audioName = String.format("%s - %2d", rssMsg.getTitle(), idx.getAndIncrement());
                sendAudio.setAudio(audioName, ain);
                sendAudio.setCaption(audioName);
                sendAudio.setReplyToMessageId(replyId);
                execute(sendAudio);
            } catch (IOException | TelegramApiException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String getHashtagsAsString(List<String> hashtags){
        StringBuilder builder = new StringBuilder();
        hashtags.forEach(tag -> builder.append("&#35")
            .append(tag)
            //.append(CHANEL_ID)
            .append(" "));
        return builder.toString().toLowerCase();
    }



    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    private class PodcastTask extends TimerTask{

        @Override
        public void run() {
            checkPodcasts();
        }
    }
}
