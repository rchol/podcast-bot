package bot.data;

import bot.rss.model.RSSMsg;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class Repo implements RepoIface {

    private final SimpleDataSource ds;

    @Inject
    public Repo(SimpleDataSource ds) {
        this.ds = ds;
    }

    @PostConstruct
    private void initDb(){
        ds.executeUpdate("CREATE TABLE IF NOT EXISTpodcasts\n"
            + "(\n"
            + "    id int AUTO_INCREMENT PRIMARY KEY NOT NULL,\n"
            + "    guid varchar(100) NOT NULL,\n"
            + "    title varchar(50),\n"
            + "    media varchar(100),\n"
            + "    description varchar(200)\n"
            + ");\n"
            + "CREATE UNIQUE INDEX podcasts_id_uindex ON podcasts (id);");
    }

    @Override
    public void addMessage(RSSMsg message) {
        String guid = message.getGuid();
        String description = message.getDescription();
        String media = message.getEnclosure();
        String title = message.getTitle();
        String author = message.getAuthor();

        ds.executeUpdate("INSERT INTO podcasts (guid, title, author, media, description) VALUES"
            + "(?,?,?,?,?)", guid, title, author, media, description);
    }

    @Override
    public boolean isPosted(RSSMsg message) {
        try {
            String guid = message.getGuid();
            ResultSet rs = ds.executeQuery("SELECT title FROM podcasts WHERE guid = '" + guid + "'");
            return rs.first();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
