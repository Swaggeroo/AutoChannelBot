# AutoChannelBot
A Discord bot that ensures that there is always a free voice channel in the server.
## Features
Creates for specified voice channels a new channel with the same name when someone joins, so that you always have a free channel to talk in.

It also deletes the channel when the last person leaves.

And some small nice to have features.

## Requirements
You need Java 15 or higher

## Installation
1. Download the jar in the [release section](https://github.com/Swaggeroo/AutoChannelBot/releases/latest)
2. Create a secret.txt with your bot token in the same folder as the jar
3. ```java -jar AutoChannelBot.jar```

## Usage
Set Debug Channel\
``>setDebugChannel <channelid>``
### AutoChannel
Add Master Channel\
``>addMaster <channelid>``

Remove Master Channel\
``>removeMaster <channelid>``

List all current Tmp-Channels\
``>getTmpChannels``

List all current Master-Channels\
``>getMasters``

### Nice to have features
Ping command\
``>ping``

Get Profile Picture\
``>myPicture``

Magic Conch Shell\
``>Miesmuschel``

Random Answer\
``>rand <Arg> <Arg> <Arg> ...``