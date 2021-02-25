# Modus Rollens Dice Bot

## Introduction
_Modus Rollens_ is discord bot for rolling dice for TTRPGs, inspired by [Dice Maiden](https://top.gg/bot/377701707943116800).
This bot supports several kinds of dice rolls and tallying methods, as well as the ability to define custom
rolls by name for your Discord server.

## Example Commands

### Roll Dice

#### Simple Dice Pools
| Command   | Meaning                  |
|-----------|--------------------------|
|`!mr 5d10` | Roll five ten-sided dice |
|`!mr 2d6 + 3d4` | Roll two six-sided dice and three four-sided dice (can also use `-`, `*`, and `/`).|
|`!mr 2d6 + 1`| Roll two six-sided dice and add one (also supports `-`, `*`, `/`); must come after other flags.|
|`!mr 4 (2d10 + d6)`| Repeat roll of two ten-sided dice and one six-sided die four times. Repeated roll can include other modifiers.|

#### Count Successes
| Command   | Meaning                  |
|-----------|--------------------------|
|`!mr 5d10 t6`| Roll five ten-sided dice and count successes (six and above).|
|`!mr 5d10 t6 f2`| As above, but subtract failures (2 or below) from successes.|

#### Re-roll Modifiers
| Command   | Meaning                  |
|-----------|--------------------------|
|`!mr 5d10 e10`| Roll five ten-sided dice but roll one additional die for each ten (sometimes called "exploding" dice).|
|`!mr 5d10 ie10`| As above, but explode on tens indefinitely (capped at one-hundred times to prevent abuse).|
|`!mr 5d10 r3`| Roll five ten-sided dice, take any results less than or equal to three, and re-roll them once.|

#### Keeping and Dropping Dice
| Command   | Meaning                  |
|-----------|--------------------------|
|`!mr 5d10 k3`| Roll five ten-sided dice, keep the three highest results.|
|`!mr 5d10 d3`| Roll five ten-sided dice, drop the lowest three results.|

### Save Custom Rolls by Name
Save rolls by name, with arguments (number of dice, number of sides of dice, etc.) that can be provided
when the custom roll is used. Rolls are saved to a discord server; anyone in a server can see and use
the same rolls.

| Command   | Meaning                  |
|-----------|--------------------------|
|`!mr save (werewolf num diff) = {num}d10 t{diff} f1`| Save a roll called werewolf.|
|`!mr save (werewolf num) = {num}d10 t6 f1`| Save a roll called werewolf that has a different number of inputs from the other saved roll.|

### Use a Saved Roll

| Command   | Meaning                  |
|-----------|--------------------------|
|`!mr werewolf 5 6`| Use the saved roll, werewolf, with `num=5` and `diff=6`. With the definition above this is equivalent to `!mr 5d10 t6 f1`.|

### Manage Saved Rolls
| Command   | Meaning                  |
|-----------|--------------------------|
|`!mr list`| Lists all saved rolls.|
|`!mr delete werewolf 2`| Delete the saved roll called werewolf that has two inputs. Note that this would not delete a roll called `werewolf` with only one, or more than two arguments.|

### Misc
| Command   | Meaning                  |
|-----------|--------------------------|
|`!mr help`| Show help message with these examples.|

## Developer Guide

Instructions below describe how to run this bot.

### Requirements

1. [JDK 15](https://jdk.java.net/15/) (or newer)
2. [Maven 3](https://maven.apache.org/download.cgi)

### Build Instructions

To build a fat jar, run
```bash
mvn package
```

On success, you'll find the compiled jar at `target/modus-rollens-1.0-SNAPSHOT.jar`.

### Run Bot

To run this bot, you'll need a Discord bot token. You'll probably also want to add your bot to a
discord server to try it out. You can find instructions on both these tasks [here](https://github.com/reactiflux/discord-irc/wiki/Creating-a-discord-bot-&-getting-a-token).

Assuming you've built the bot jar with the instructions above, and you have created a bot token with the value `12345`,
run the following:
```bash
DISCORD_TOKEN=12345 java -jar target/modus-rollens-1.0-SNAPSHOT.jar
```

#### Optional Configuration

On first run, the bot will create a sqlite database file. By default, it will be a file called `app.db`
in the working directory of the program. You can change this with the environment variable `DB_FILE`.

### Running Tests

Unit tests are run as part of building the jar, but can also be run with
```bash
mvn test
```

### How Does It Work?

This bot uses the following libraries:
* [JDA](https://github.com/DV8FromTheWorld/JDA) for all interactions with Discord
* [Antlr](https://www.antlr.org/) for parsing commands
* [SQLite](https://www.sqlite.org/index.html) for persisting saved rolls
* [Liquibase](https://www.liquibase.org/) for managing the database schema
