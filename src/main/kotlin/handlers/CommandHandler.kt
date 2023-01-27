package handlers

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.api.deleteMessage
import eu.vendeli.tgbot.api.message
import eu.vendeli.tgbot.interfaces.sendAsync
import eu.vendeli.tgbot.types.Message
import eu.vendeli.tgbot.types.Update
import eu.vendeli.tgbot.types.internal.getOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

suspend fun genericStickerCmd(update: Update,
                              bot: TelegramBot,
                              text: String,
                              action: (Message) -> Unit) {
    if (BotConfig.needListenToCommand(update)) {
        val result = message(text)
            .sendAsync(update.message?.chat?.id ?: 0, bot).await()
        BotDatabase.addSpecialMessage(result.getOrNull()!!, action)
    }
}

suspend fun banSticker(update: Update, bot: TelegramBot, pack: Boolean) {
    genericStickerCmd(update, bot,
        "Reply to this message with stickers and ${if (pack) "their pack" else "they"} will be restricted")
    { reply ->
        if (reply.sticker != null && BotDatabase.isModeratedBy(reply.chat, reply.from!!)) {
            BotDatabase.addStickerRestriction(StickerRestriction(reply.sticker!!, pack), reply.chat)
        }
    }
}

suspend fun unbanSticker(update: Update, bot: TelegramBot, pack: Boolean, csc: CoroutineScope) {
    genericStickerCmd(update, bot,
        "Reply to this message with stickers and ${if (pack) "their pack" else "they"} will be allowed")
    { reply ->
        if (reply.sticker != null && BotDatabase.isModeratedBy(reply.chat, reply.from!!)) {
            BotDatabase.removeStickerRestriction(StickerRestriction(reply.sticker!!, pack), reply.chat)
             csc.launch{ deleteMessage(reply.messageId).send(reply.chat.id, bot) }
        }
    }
}