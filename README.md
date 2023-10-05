# Telegram Bot to roll any combination of dice

## Features

* Roll up to 32 dice with up to 1 billion sides
* Bot can be used in group chats
* Bot calculates sum of similar dice

## Demo

https://t.me/everydicebot

# Supported commands

* `/d2` - flip a coin
* `/d6` - roll a D6 dice
* `/d20` - roll a D20 dice
* `/dice <dice configuration>`- roll any dice configuration
* `<dice configuration>` - roll any dice configuration, without special command

`<dice configuration>` - list of dice to roll. Supported formats: `20`, `d20`, `2d20`. 
Multiple dice types in one message are allowed - `2d6 2d20`

## Configuration

* `BOT_TOKEN` - (required) bot token from @BotFather
* `WEBHOOK_URL` - (required) webhook url
* `WEBHOOK_TOKEN` - (required) secret token to authenticate webhook requests. 
See `secret_token` in Telegram API [documentation](https://core.telegram.org/bots/api#setwebhook)
* `PORT` - port for server to listen to (optional, default 8888)