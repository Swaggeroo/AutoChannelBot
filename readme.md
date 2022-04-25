# AutoChannelBot
A Discord bot that ensures that there is always a free voice channel in the server.
## Features
Creates for specified voice channels a new channel with the same name when someone joins, so that you always have a free channel to talk in.

It also deletes the channel when the last person leaves.

And some small nice to have features.

**Note:** The created channel only get the same bitrate and member-limits as the original channel. If you want to keep the permissions of the original channel, you should put the channel in a Category with its permissions.

## Requirements
You need Java 15 or higher

## Installation
1. Download the jar in the [release section](https://github.com/Swaggeroo/AutoChannelBot/releases/latest) or clone the repository and compile it yourself.
2. Create a secret.txt with your bot token in the same folder as the jar
3. ```java -jar AutoChannelBot.jar```
4. send the `>setup` command to ensure that the bot is working correctly

## Usage
Setup the bot\
```>setup```

Set Debug Channel\
``>setDebugChannel <channelid>``

Get Debug Channel\
``>getDebugChannel``
### AutoChannel
Add Master Channel\
``>addMaster <channelid>``

Remove Master Channel\
``>removeMaster <channelid>``

List all current Tmp-Channels\
``>getTmpChannels``

List all current Master-Channels\
``>getMasters``

Recreate Tmp-Channels\
``>recreateTmpChannels``

### Nice to have features
Ping command\
``>ping``

Get Profile Picture\
``>myPicture``

Magic Conch Shell\
``>Miesmuschel``

Random Answer\
``>rand <Arg> <Arg> <Arg> ...``
