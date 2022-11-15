import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

public class HandleChannelMovement extends ListenerAdapter {
    JDA jda;
    final static String prefix = ">";
    Random rand;
    TextChannel debugChannel = null;
    Guild guild = null;

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
        addGlobalSlashCommands();
        rand = new Random();
        readConfig();
        if (debugChannel != null){
            debugChannel.sendMessage("Bot started").queue();
        }else {
            System.out.println("Debug Channel not set");
        }

        if (guild != null){
            recreateTmpChannels(guild);
        }else {
            System.err.println("Please run the command \">setup\" on the server you want to use the bot on");
        }

        if (tmpMasterChannelList.size() != 0){
            System.out.println("Master Channel List: ");
            StringBuilder sb = new StringBuilder();
            if (debugChannel != null){
                sb.append("Master Channel List: \n");
            }
            for (VoiceChannel vc : tmpMasterChannelList){
                System.out.println(vc.getName() + " - " + vc.getId());
                if (debugChannel != null){
                    sb.append("<#").append(vc.getId()).append(">\n");
                }
            }
            if (debugChannel != null){
                debugChannel.sendMessage(sb.toString()).queue();
            }
        }else {
            System.out.println("No Master Channels found");
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                if (debugChannel != null) {
                    debugChannel.sendMessage("Bot Shutting down").complete();
                }
            }
        });
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        //if left
        if (event.getChannelLeft() != null && event.getChannelJoined() == null){
            handleLeft(event);
        }
        //if joined
        else if (event.getChannelLeft() == null && event.getChannelJoined() != null){
            handleJoin(event);
        }
        //if moved
        else if (event.getChannelLeft() != null && event.getChannelJoined() != null){
            if (!event.getChannelLeft().equals(event.getChannelJoined())){
                handleLeft(event);
                handleJoin(event);
            }
        }
        super.onGuildVoiceUpdate(event);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        User user = event.getUser();
        String command = event.getName();

        //_____________________________
        //Ping
        //_____________________________
        if (command.equals("ping")){
            event.reply("Pong!").queue();
        }

        //_____________________________
        //mypicture
        //_____________________________
        else if (command.equals("mypicture")) {
            if (user.getAvatarUrl() != null){
                event.reply(user.getAvatarUrl()).queue();
            }else {
                event.reply("No Profile Picture found").queue();
            }
        }

        //_____________________________
        //miesmuschel
        //_____________________________
        else if (command.equals("miesmuschel")){
            int antwortType = rand.nextInt(3);
            if (antwortType == 0){
                //positive Antwort
                event.reply(positiveAntworten[rand.nextInt(positiveAntworten.length)]).queue();
            }else if (antwortType == 1){
                //neutrale Antwort
                event.reply(neutraleAntworten[rand.nextInt(neutraleAntworten.length)]).queue();
            }else {
                //negative Antwort
                event.reply(negativeAntworten[rand.nextInt(negativeAntworten.length)]).queue();
            }
        }

        //_____________________________
        //random
        //_____________________________
        else if (command.equals("rand")) {
            String arguments =  event.getOption("options").getAsString();
            if (arguments.equals("")){
                event.reply("No Options provided").queue();
                return;
            }
            String[] auswahl = arguments.split(",");
            event.reply(auswahl[rand.nextInt(auswahl.length)]).queue();
        }

        else if (command.equals("setup") && !event.isFromGuild()){
            event.reply("You have to be on an Server").queue();
        }

        //_____________________________
        //Guild Commands
        //_____________________________
        else if (event.isFromGuild()){
            Member member = event.getMember();
            //_____________________________
            //Setup
            //_____________________________
            if (command.equals("setup")){
                guild = event.getGuild();
                saveConfig();
                addGuildSlashCommands();
                event.reply("Setup done").queue();
            }

            //_____________________________
            //Add Master
            //_____________________________
            else if (command.equals("addmaster")){
                if (member != null && member.hasPermission(Permission.ADMINISTRATOR)) {
                    Channel channel = event.getOption("channel").getAsChannel();
                    if (channel.getType() == ChannelType.VOICE){
                        VoiceChannel vc = event.getOption("channel").getAsChannel().asVoiceChannel();
                        if (!tmpMasterChannelList.contains(vc)){
                            tmpMasterChannelList.add(vc);
                            tmpChannelCount.put(vc,0);
                            masterNames.put(vc,vc.getName());
                            recreateTmpChannels(event.getGuild());
                            saveConfig();
                            event.reply("Added <#" + vc.getId() + "> as Master Channel").queue();
                        }else {
                            event.reply("<#" + vc.getId() + "> is already a Master Channel").queue();
                        }
                    }else {
                        event.reply("Channel is not a Voice Channel").queue();
                    }
                }else {
                    event.reply("You are not an Administrator").queue();
                }
            }

            //_____________________________
            //Remove Master
            //_____________________________
            else if (command.equals("removemaster")){
                if (member != null && member.hasPermission(Permission.ADMINISTRATOR)) {
                    Channel channel = event.getOption("channel").getAsChannel();
                    if (channel.getType() == ChannelType.VOICE){
                        VoiceChannel vc = event.getOption("channel").getAsChannel().asVoiceChannel();
                        if (tmpMasterChannelList.contains(vc)){
                            tmpMasterChannelList.remove(vc);
                            tmpChannelCount.remove(vc);
                            masterNames.remove(vc);
                            event.reply("Removed <#" + vc.getId() + "> as Master Channel").queue();
                            saveConfig();
                            recreateTmpChannels(event.getGuild());
                        }
                        else {
                            event.reply("Channel is not a Master Channel").queue();
                        }
                    }else {
                        event.reply("Channel is not a Voice Channel").queue();
                    }
                }else {
                    event.reply("You are not an Administrator").queue();
                }
            }

            //_____________________________
            //Get TmpChannels
            //_____________________________
            else if (command.equals( "gettmpchannels")){
                if (member != null && member.hasPermission(Permission.MANAGE_ROLES)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("tmpChannels: \n");
                    for (VoiceChannel vc : tmpChannelList){
                        sb.append("<#").append(vc.getId()).append("> \n");
                    }
                    event.reply(sb.toString()).queue();
                }else {
                    event.reply("You haven't got enough Permissions").queue();
                }
            }

            //_____________________________
            //Get Masters
            //_____________________________
            else if (command.equals("getmasterchannels")){
                if (member != null && member.hasPermission(Permission.MANAGE_ROLES)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Masters: \n");
                    for (VoiceChannel vc : tmpMasterChannelList){
                        sb.append("<#").append(vc.getId()).append("> \n");
                    }
                    event.reply(sb.toString()).queue();
                }
                else {
                    event.reply("You haven't got enough Permissions").queue();
                }
            }

            //_____________________________
            //Set Debug channel
            //_____________________________
            else if (command.equals("setdebugchannel")){
                if (member != null && member.hasPermission(Permission.ADMINISTRATOR)) {
                    Channel channel = event.getOption("channel").getAsChannel();
                    if (channel.getType() == ChannelType.TEXT){
                        TextChannel tc = event.getOption("channel").getAsChannel().asTextChannel();
                        debugChannel = tc;
                        saveConfig();
                        event.reply("Set <#" + tc.getId() + "> as Debug Channel").queue();
                        tc.sendMessage("This is now the Debug Channel").queue();
                    }else {
                        event.reply("Channel is not a Text Channel").queue();
                    }
                }
                else {
                    event.reply("You are not an Administrator").queue();
                }
            }

            //_____________________________
            //Get Debug channel
            //_____________________________
            else if (command.equals("getdebugchannel")){
                if (member != null && member.hasPermission(Permission.MANAGE_ROLES)) {
                    if (debugChannel != null){
                        event.reply("Debug Channel is <#" + debugChannel.getId() + ">").queue();
                    }
                    else {
                        event.reply("No Debug Channel set\nSet a debug channel with /setdebugchannel").queue();
                    }
                }
                else {
                    event.reply("You haven't got enough Permissions").queue();
                }
            }

            //_____________________________
            //Recreate Tmp-Channels
            //_____________________________
            else if (command.equals("recreatetmpchannels")){
                if (member != null && member.hasPermission(Permission.ADMINISTRATOR)) {
                    recreateTmpChannels(event.getGuild());
                    event.reply("All Tmp-Channels deleted").queue();
                }
                else {
                    event.reply("You are not an Administrator").queue();
                }
            }
        }
        super.onSlashCommandInteraction(event);
    }

    public void handleJoin(GuildVoiceUpdateEvent event){
        if (event.getChannelJoined() != null){
            VoiceChannel vc = event.getChannelJoined().asVoiceChannel();
            int usersInVC = vc.getMembers().size();
            if (usersInVC == 1){
                if (tmpChannelList.contains(vc)){
                    VoiceChannel master = tmpChannelMapMaster.get(vc);
                    createTmpChannel(event.getGuild(), masterNames.get(master),tmpChannelCount.get(master)+1,vc.getPosition(),vc.getUserLimit(),master,vc.getParentCategory(),vc.getBitrate());
                    tmpChannelCount.put(master,tmpChannelCount.get(master)+1);
                }else if (tmpMasterChannelList.contains(vc) && tmpChannelCount.get(vc) == 0){
                    createTmpChannel(event.getGuild(), vc.getName(),1,vc.getPosition(),vc.getUserLimit(),vc,vc.getParentCategory(),vc.getBitrate());
                    tmpChannelCount.put(vc,1);
                }
            }
        }
    }

    public void handleLeft(GuildVoiceUpdateEvent event){
        if (event.getChannelLeft() != null){
            VoiceChannel vc = event.getChannelLeft().asVoiceChannel();
            int usersInVC = vc.getMembers().size();
            if (vc.getMembers().size() <= 0){
                if (tmpChannelList.contains(vc)){
                    VoiceChannel master = tmpChannelMapMaster.get(vc);
                    int count = tmpChannelCount.get(master);
                    count--;
                    String name = vc.getName();
                    int pos = Integer.parseInt(name.split(";")[1]);
                    if (count <= 0){
                        if (!isSomeoneInChannel(master)){
                            deleteChannel(vc);
                            tmpChannelCount.put(master, 0);
                        }
                    }else if(pos <= count){
                        deleteChannel(vc);
                        tmpChannelCount.put(master, count);
                        List<VoiceChannel> tmpChannelListClone = new ArrayList<>(tmpChannelList);;
                        for (VoiceChannel vcTmp : tmpChannelListClone){
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
                    List<VoiceChannel> tmpChannelListClone = new ArrayList<>(tmpChannelList);
                    for (VoiceChannel vc2 : tmpChannelListClone){
                        if (tmpChannelMapMaster.get(vc2) == vc){
                            deleteChannel(vc2);
                            tmpChannelCount.put(vc, 0);
                        }
                    }
                }
            }
        }
    }

    public boolean isSomeoneInChannel(VoiceChannel vc){
        return vc.getMembers().size() > 0;
    }

    public void deleteChannel(VoiceChannel vc){
        tmpChannelList.remove(vc);
        tmpChannelMapMaster.remove(vc);
        vc.delete().reason("TMP Voice no longer Required").queue();
    }

    public void createTmpChannel(Guild guild, String name, int num, int pos, int memberCount, VoiceChannel master, Category category, int bitrate){
        VoiceChannel vc = guild.createVoiceChannel("⏰;"+num+";"+name).setParent(category).setPosition(pos-num+1).setUserlimit(memberCount).setBitrate(bitrate).complete();
        tmpChannelList.add(vc);
        tmpChannelMapMaster.put(vc, master);
    }

    public void recreateTmpChannels(Guild guild){
        deleteAllTmpChannels(guild);
        checkMasterChannels();
    }

    public void deleteAllTmpChannels(Guild guild){
        guild.getVoiceChannels().forEach(vc -> {
            if (vc.getName().startsWith("⏰")){
                deleteChannel(vc);
            }
        });
    }

    public void checkMasterChannels(){
        for (VoiceChannel vc : tmpMasterChannelList){
            if (vc.getMembers().size() > 0){
                createTmpChannel(vc.getGuild(), vc.getName(),1,vc.getPosition(),vc.getUserLimit(),vc,vc.getParentCategory(),vc.getBitrate());
                tmpChannelCount.put(vc,1);
            }
        }
    }

    public void saveConfig(){
        try {
            FileWriter fw = new FileWriter("config.cfg", false);
            if (guild != null){
                fw.write("GuildId:");
                fw.write( guild.getId()+ "\n");
            }
            if (debugChannel != null){
                fw.write("DebugChannelId:");
                fw.write(debugChannel.getId() + "\n");
            }
            if (tmpMasterChannelList.size() > 0){
                fw.write("MasterChannels:");
                for (VoiceChannel vc : tmpMasterChannelList){
                    fw.write(vc.getId()+";");
                }
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //read config
    public void readConfig(){
        try {
            FileReader fr = new FileReader("config.cfg");
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            while (line != null){
                if (line.startsWith("GuildId:")){
                    guild = jda.getGuildById(line.split(":")[1]);
                    if (guild != null){
                        addGuildSlashCommands();
                    }
                }else if (line.startsWith("DebugChannelId:")){
                    debugChannel = jda.getTextChannelById(line.split(":")[1]);
                }else if (line.startsWith("MasterChannels:")){
                    String[] tmp = line.split(":")[1].split(";");
                    for (String s : tmp){
                        if (!s.equals("")){
                            VoiceChannel vc = jda.getVoiceChannelById(s);
                            if (vc != null){
                                tmpMasterChannelList.add(vc);
                                tmpChannelCount.put(vc,0);
                                masterNames.put(vc,vc.getName());
                            }else{
                                System.err.println("MasterChannel not found: " + s + " deleted it from config");
                            }
                        }
                    }
                }
                line = br.readLine();
            }
            br.close();
            saveConfig();
        } catch (FileNotFoundException e){
            System.err.println("No Config File found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addGlobalSlashCommands(){
        //check if command exists
        List<Command> commands = jda.retrieveCommands().complete();
        final String[] commandNames = {"ping","myPicture","miesmuschel","rand","setup"};
        int complete = 0;
        for (Command c : commands){
            if (Arrays.binarySearch(commandNames,c.getName()) >= 0){
                complete++;
            }
        }
        if (complete < commandNames.length){
            //add commands
            jda.updateCommands().addCommands(
                    Commands.slash("ping", "Ping the bot"),
                    Commands.slash("mypicture", "Get your profile picture"),
                    Commands.slash("miesmuschel", "Ask the magic miesmuschel"),
                    Commands.slash("rand", "Get a random Answer of the given Options")
                            .addOption(OptionType.STRING, "options", "The Options you want to choose from (Comma Seperated)", true),
                    Commands.slash("setup", "Run this on your Server for Setup")
            ).queue();
        }
    }

    public void addGuildSlashCommands(){
        //check if command exists
        List<Command> commands = guild.retrieveCommands().complete();
        final String[] commandNames = {"addmaster","removemaster","gettmpchannels","getmasterchannels","setdebugchannel","getdebugchannel"};
        int complete = 0;
        for (Command c : commands){
            if (Arrays.binarySearch(commandNames,c.getName()) >= 0){
                complete++;
            }
        }
        if (complete < commandNames.length){
            //add commands
            guild.updateCommands().addCommands(
                    Commands.slash("addmaster", "Adding a Master Channel which will be always available")
                            .addOption(OptionType.CHANNEL, "channel", "The Channel you want to add", true),
                    Commands.slash("removemaster", "Removing a Master Channel")
                            .addOption(OptionType.CHANNEL, "channel", "The Channel you want to remove", true),
                    Commands.slash("gettmpchannels", "Get a list of all tmp Channels"),
                    Commands.slash("getmasterchannels", "Get a list of all master Channels"),
                    Commands.slash("setdebugchannel", "Set the Debug Channel")
                            .addOption(OptionType.CHANNEL, "channel", "The Channel you want to set", true),
                    Commands.slash("getdebugchannel", "Get the Debug Channel"),
                    Commands.slash("recreatetmpchannels", "Recreate all tmp Channels (WARNING: All Members will be kicked from the tmp and Master Channels)")
            ).queue();
        }
    }
}
