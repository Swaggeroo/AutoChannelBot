import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class HandleChannelMovement extends ListenerAdapter {
    JDA jda;
    final static String prefix = ">";
    Random rand;

    //Miesmuschel Antworten
    final static String[] positiveAntworten = {"Ja","Auf jeden Fall","Na Klar","Mach doch"};
    final static String[] neutraleAntworten = {"Wenn du meinst","Vielleicht","Frag doch nochmal","Solltest du das wirklich eine Miesmuschel fragen?"};
    final static String[] negativeAntworten = {"Nein","Auf keinen Fall","Lieber nicht","Tu das nicht"};


    public HandleChannelMovement(JDA jda) {
        this.jda = jda;
        rand = new Random();
        //TODO delete every temp Channel
        //TODO Create Tmp Channels if someone is in
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        Message message = event.getMessage();
        MessageChannel channel = event.getChannel();
        String content = message.getContentRaw();
        User user = event.getAuthor();
        Member member = event.getMember();
        if (!event.isFromType(ChannelType.PRIVATE) && !user.isBot()){
            //_____________________________
            //Ping
            //-----------------------------
            if (content.equalsIgnoreCase(prefix + "ping")){
                channel.sendMessage("Pong").queue();
            }
            //_____________________________
            //ProfilePic
            //-----------------------------
            else if (content.equalsIgnoreCase(prefix + "myPicture")){
                System.out.println("PIC");
                if (user.getAvatarUrl() != null){
                    channel.sendMessage(user.getAvatarUrl()).queue();
                }else {
                    channel.sendMessage("Dieser Benutzer hat kein Profilbild").queue();
                }
            }
            //_____________________________
            // Magische Miesmuschel
            //_____________________________
            else if (content.equalsIgnoreCase(prefix + "miesmuschel")){
                int antwortType = rand.nextInt(3);
                if (antwortType == 0){
                    //positive Antwort
                    channel.sendMessage(positiveAntworten[rand.nextInt(positiveAntworten.length)]).queue();
                }else if (antwortType == 1){
                    //neutrale Antwort
                    channel.sendMessage(neutraleAntworten[rand.nextInt(neutraleAntworten.length)]).queue();
                }else {
                    //negative Antwort
                    channel.sendMessage(negativeAntworten[rand.nextInt(negativeAntworten.length)]).queue();
                }
            }
            //_____________________________
            // Random Antwort
            //_____________________________
            else if (content.toLowerCase().startsWith(prefix + "rand")){
                String arguments = "";
                try {
                    arguments = content.substring(6);
                } catch (Exception ignore){}
                System.out.println("test"+arguments);
                if (arguments.equals("")){
                    channel.sendMessage("Please give Arguments").queue();
                    return;
                }
                String[] auswahl = arguments.split(",");
                channel.sendMessage(auswahl[rand.nextInt(auswahl.length)]).queue();
            }
        }
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event){
        VoiceChannel vc = event.getChannelJoined();
        int usersInVC = vc.getMembers().size();
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        VoiceChannel vc = event.getChannelLeft();
        int usersInVC = vc.getMembers().size();
    }
}
