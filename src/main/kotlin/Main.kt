import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.api.deleteMessage
import handlers.banSticker
import handlers.unbanSticker
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) = runBlocking {
    val botToken = readToken()
    val bot = TelegramBot(botToken)

    bot.handleUpdates {
        onMyChatMember {
            if (BotConfig.chatMeetRequirements(data.newChatMember) && !BotDatabase.isModerated(data.chat)) {
                // A bot was added or promoted, but it was unable to moderate chat. Now it is able
                BotDatabase.addChat(data.from, data.chat)
            } else if (!BotConfig.chatMeetRequirements(data.newChatMember) && BotDatabase.isModerated(data.chat)) {
                // A bot was able to moderate chat, now it can't
                BotDatabase.removeChat(data.chat)
            }
        }
        onCommand("/ban_s") {
            banSticker(update, bot, false)
        }
        onCommand("/unban_s") {
            unbanSticker(update, bot, false, this@runBlocking)
        }
        onCommand("/ban_p") {
            banSticker(update, bot, true)
        }
        onCommand("/unban_p") {
            unbanSticker(update, bot, true, this@runBlocking)
        }
        onMessage {
            BotDatabase.processSpecialMessages(data)
            if (BotDatabase.restrictedMessage(data)) {
                deleteMessage(data.messageId).send(data.chat.id, bot)
            }
        }
    }
}

private fun readToken(): String {
    val file = File("token")
    if (!file.isFile || !file.canRead()) {
        System.err.println("Cannot find or read token from file")
        exitProcess(1)
    }
    return file.readText()
}