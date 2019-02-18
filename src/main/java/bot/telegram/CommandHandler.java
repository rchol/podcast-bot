package bot.telegram;

import bot.data.Repository;
import bot.rss.RSSFeedParser;
import bot.rss.model.RSSChannel;
import bot.telegram.commands.AddCommand;
import bot.telegram.commands.Command;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CommandHandler {
    private final String command;
    private final Repository repo;

    public CommandHandler(String command,
        Repository repo) {
        this.command = command;
        this.repo = repo;
    }

    public Command parse() {
        List<String> separated = new LinkedList<>(Arrays.asList(command.split("\\s+")));
        if (separated.remove(0).equals("/add")){
            String url = separated.remove(0);
            return new AddCommand(url, separated, repo);
        }
        return null;
    }

}
