import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.io.BufferedReader;
import java.io.FileReader;

public abstract class StartBot extends ListenerAdapter {
    public static void main(String[] args) throws Exception{
        String key;
        try {
            key = readLine("secret.txt");
        } catch (Exception e) {
            System.err.println("Could not read secret.txt");
            System.err.println("Please create a file called secret.txt with your bot's token in it");
            System.err.println("You can get your bot's token from https://discordapp.com/developers/applications/me");
            System.exit(-10);
            throw new RuntimeException(e);
        }

        System.out.println("Bot starting");
        //JDA(Bot) initialisieren
        JDA jda = JDABuilder.create(key,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_EMOJIS,
                        GatewayIntent.GUILD_MEMBERS)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build()
                .awaitReady();

        jda.addEventListener(new HandleChannelMovement(jda));
    }

    public static String readLine(String path) throws Exception{
        FileReader fr = new FileReader(path);
        BufferedReader br = new BufferedReader(fr);
        String line = br.readLine();
        br.close();
        return line;
    }
}
