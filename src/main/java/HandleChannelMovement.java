import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class HandleChannelMovement extends ListenerAdapter {
    JDA jda;
    final static String prefix = ">";
    Random rand;
    ArrayList<VoiceChannel> tmpChannelList = new ArrayList<>();
    HashMap<VoiceChannel,VoiceChannel> tmpChannelMapMaster = new HashMap<>();
    HashMap<VoiceChannel,Integer> tmpChannelCount = new HashMap<>();
    ArrayList<VoiceChannel> tmpMasterChannelList = new ArrayList<>();
    HashMap<VoiceChannel,String> masterNames = new HashMap<>();

    //Miesmuschel Antworten
    final static String[] positiveAntworten = {"Ja","Auf jeden Fall","Na Klar","Mach doch"};
    final static String[] neutraleAntworten = {"Wenn du meinst","Vielleicht","Frag doch nochmal","Solltest du das wirklich eine Miesmuschel fragen?"};
    final static String[] negativeAntworten = {"Nein","Auf keinen Fall","Lieber nicht","Tu das nicht"};


    public HandleChannelMovement(JDA jda) {
        this.jda = jda;
        rand = new Random();
        //TODO delete every temp Channel
        //TODO Create Tmp Channels if someone is in
        //TODO Restore Masters
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        //TODO Clear Command
        //TODO Remove Command
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
            //_____________________________
            //Add Master
            //_____________________________
            else if (content.startsWith(prefix + "addMaster")){
                if (member != null && member.hasPermission(Permission.ADMINISTRATOR)) {
                    long channelid = -1L;
                    try {
                        channelid = Long.parseLong(content.substring(10+prefix.length()));
                    } catch (Exception ignore){
                        channel.sendMessage("No valid Channel-ID provided").queue();
                        return;
                    }
                    if(channelid != -1L){
                        VoiceChannel vc = (VoiceChannel) event.getGuild().getGuildChannelById(ChannelType.VOICE,channelid);
                        if (vc != null){
                            tmpMasterChannelList.add(vc);
                            tmpChannelCount.put(vc,0);
                            masterNames.put(vc,vc.getName());
                            channel.sendMessage("Added Master: <#" + channelid +">").queue();
                        }else {
                            channel.sendMessage("Channel not found").queue();
                        }
                    }else {
                        channel.sendMessage("Channel not found").queue();
                    }
                }else {
                    channel.sendMessage("You are not an Administrator").queue();
                }
            }
        }
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event){
        VoiceChannel vc = event.getChannelJoined();
        int usersInVC = vc.getMembers().size();
        if (usersInVC == 1){
            if (tmpChannelList.contains(vc)){
                VoiceChannel master = tmpChannelMapMaster.get(vc);
                createTmpChannel(event.getGuild(), masterNames.get(master),tmpChannelCount.get(master)+1,vc.getPosition()+1,vc.getUserLimit(),master,vc.getParent());
                tmpChannelCount.put(master,tmpChannelCount.get(master)+1);
            }else if (tmpMasterChannelList.contains(vc) && tmpChannelCount.get(vc) == 0){
                System.out.println("Create Channel");
                createTmpChannel(event.getGuild(), vc.getName(),1,vc.getPosition()+1,vc.getUserLimit(),vc,vc.getParent());
                tmpChannelCount.put(vc,1);
            }
        }
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        VoiceChannel vc = event.getChannelLeft();
        int usersInVC = vc.getMembers().size();
        if (vc.getMembers().size() <= 0){
            if (tmpChannelList.contains(vc)){
                tmpChannelList.remove(vc);
                System.out.println(event.getGuild().getChannels().get(vc.getPosition()).getName());
                VoiceChannel master = tmpChannelMapMaster.get(vc);
                int count = tmpChannelCount.get(master);
                count--;
                String name = vc.getName();
                int pos = Integer.parseInt(name.split(";")[1]);
                if (count <= 0){
                    if (!isSomeoneInChannel(master)){
                        deleteChannel(vc);
                    }
                }else if(pos <= count){
                    deleteChannel(vc);
                    tmpChannelCount.put(master, count);
                    for (VoiceChannel vcTmp : tmpChannelList){
                        if (tmpChannelMapMaster.get(vcTmp) == master){
                            if (count == 1 && !isSomeoneInChannel(master)){
                                tmpChannelCount.put(master, 0);
                                deleteChannel(vcTmp);
                            }else {
                                int tmppos = Integer.parseInt(vcTmp.getName().split(";")[1]);
                                tmppos--;
                                vcTmp.getManager().setName("⏰;"+tmppos+";"+masterNames.get(master)).queue();
                            }
                        }
                    }
                }
            }else if(tmpMasterChannelList.contains(vc) && tmpChannelCount.get(vc) <= 1){
                for (VoiceChannel vc2 : tmpChannelList){
                    if (tmpChannelMapMaster.get(vc2) == vc){
                        deleteChannel(vc2);
                        tmpChannelCount.put(vc, 0);
                    }
                }
            }
        }
    }

    public boolean isSomeoneInChannel(VoiceChannel vc){
        return vc.getMembers().size() > 0;
    }

    public void deleteChannel(VoiceChannel vc){
        vc.delete().reason("TMP Voice no longer Required").queue();
        System.out.println("Deleted channel: "+ vc.getName());
    }

    public void createTmpChannel(Guild guild, String name, int num, int pos, int memberCount, VoiceChannel master, Category category){
        guild.createVoiceChannel("⏰;"+num+";"+name).setParent(category).setPosition(pos).setUserlimit(memberCount).queue((voiceChannel) -> {
            tmpChannelList.add(voiceChannel);
            tmpChannelMapMaster.put(voiceChannel, master);
        });
        System.out.println("Created channel: "+ "⏰;"+num+";"+name);
    }

    //TODO fix on channel change
}
