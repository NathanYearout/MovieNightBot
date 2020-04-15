import Commands.Search;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.MovieDb;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class Bot {

    public static void main(String[] arguments) throws Exception {
        JDA jda = JDABuilder.createDefault("NjcwNDkyNzk1MTk0MTc5NTk2.XpYE0Q.nlCUS3eWqu934DxAH5ZjY0smTEA")
                .build();

        EventWaiter waiter = new EventWaiter();

        CommandClientBuilder builder = new CommandClientBuilder();
        builder.setOwnerId("670492795194179596");
        builder.setPrefix("~");
        builder.setHelpWord("help");
        builder.setActivity(Activity.watching("your moms tits"));
        builder.addCommand(new Search(waiter));

        CommandClient client = builder.build();

        jda.addEventListener(client);

        jda.addEventListener(waiter);

    }

}