package Commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import info.movito.themoviedbapi.*;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Search extends Command {

    TmdbMovies movies = new TmdbApi("11cabbbb36bd7e198ad30a18b2c58b0b").getMovies();
    TmdbSearch search = new TmdbApi("11cabbbb36bd7e198ad30a18b2c58b0b").getSearch();

    private EventWaiter waiter;

    private static final String LEFT_ARROW = "\u2B05";
    private static final String RIGHT_ARROW = "\u27A1";
    private static final String NO_RESULT = ":blank:699823725192806500";

    private static final Long channelID = 99682514155077663L;

    public Search(EventWaiter w) {
        this.name = "search";
        this.aliases = new String[]{"s"};
        this.help = "Searches for a movie you retard, what did you expect?";
        this.arguments = "[movie]";
        this.waiter = w;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().length() <= 0) {
            event.reply(event.getAuthor().getAsMention() + " you need to actually give me a movie to search for you dense cabbage.");
        } else {
            try {
                String args = event.getArgs();
                MovieResultsPage result = search.searchMovie(args, null, "en", true, 0);

                MessageChannel channel = event.getChannel();
//                long channelID = channel.getIdLong();

                int movieNum = 0;

                displayMovie(result, movieNum, event, 0);

            } catch (Exception ex) {
                System.out.println("An exception has occurred.");
                event.reply("No movie found. :(");
            }
        }

    }

    private void initWaiter(long messageID, long channelID, MessageChannel channel, int movieNum, MovieResultsPage result, CommandEvent e, String m) {
        waiter.waitForEvent(
                GuildMessageReactionAddEvent.class,
                (event) -> {
                    MessageReaction.ReactionEmote emote = event.getReactionEmote();
                    User user = event.getUser();

                    return !user.isBot() && event.getMessageIdLong() == messageID && !emote.isEmote();
                },
                (event) -> {
//                    TextChannel channel = jda.getTextChannelById(channelID);
//                    User user = event.getUser();


                    if (LEFT_ARROW.equals(event.getReactionEmote().getName())) {
                        channel.retrieveMessageById(m).queue((message) -> {
                            message.delete().queue();
                        });
                        //channel.sendMessage("reacted to post with " + LEFT_ARROW + " to the message").queue();
                        displayMovie(result, movieNum, e, -1);

                    } else if (RIGHT_ARROW.equals(event.getReactionEmote().getName())) {
                        channel.retrieveMessageById(m).queue((message) -> {
                            message.delete().queue();
                        });
                        //channel.sendMessage("reacted to post with " + RIGHT_ARROW + " to the message").queue();
                        displayMovie(result, movieNum, e, 1);

                    } else {
                        System.out.println("An error has occurred, wrong emote?");
                    }
                },
                60, TimeUnit.SECONDS,
                () -> {
//                    TextChannel channel = jda.getTextChannelById(channelID);
                    channel.retrieveMessageById(m).queue((message) -> {
                        message.delete().queue();
                    });
                    //channel.sendMessage("I stopped listening for reactions :(").queue();
                }
        );
    }

    public String getMovie(int movieID) {
        MovieDb movie = this.movies.getMovie(movieID, "en", TmdbMovies.MovieMethod.keywords);
        String Name = movie.getTitle();
        return Name;
    }

    public EmbedBuilder embedGen(String movieName, String movieDescription, float popularity, int movieNum) {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle(movieName)
                .setDescription(movieDescription)
                .setAuthor(popularity + " \u2B50")
                .setFooter(Integer.toString(movieNum + 1));
        return builder;
    }

    public void displayMovie(MovieResultsPage result, int movieNum, CommandEvent event, int changeAmount) {
        int tempAmount;
        tempAmount = movieNum + changeAmount;
        event.getChannel().sendMessage(embedGen(result.getResults().get(tempAmount).getTitle(), result.getResults().get(tempAmount).getOverview(), result.getResults().get(tempAmount).getPopularity(), tempAmount).build()).queue((message) -> {
            String m = message.getTextChannel().getLatestMessageId();
            if (changeAmount != 0) {
                message.addReaction(LEFT_ARROW).queue();
            } else {
                message.addReaction(NO_RESULT).queue();
            }
            if (tempAmount + 1 != result.getResults().size()) {
                message.addReaction(RIGHT_ARROW).queue();
            } else {
                message.addReaction(NO_RESULT).queue();
            }
            message.addReaction("\u2764").queue();

            initWaiter(message.getIdLong(), channelID, event.getChannel(), tempAmount, result, event, m);
        });
    }
}
