import eu.vendeli.tgbot.types.ChatMember
import eu.vendeli.tgbot.types.ChatType
import eu.vendeli.tgbot.types.Update

object BotConfig {
    fun chatMeetRequirements(chatMember: ChatMember): Boolean {
        return chatMember is ChatMember.Administrator &&
                chatMember.canRestrictMembers &&
                chatMember.canDeleteMessages
    }

    // It must be in group
    private fun groupMessage(upd: Update): Boolean {
        val type = upd.message?.chat?.type ?: return false
        return type == ChatType.Group || type == ChatType.Supergroup
    }

    fun needListenToCommand(upd: Update): Boolean {
        return groupMessage(upd) &&
                BotDatabase.isModerated(upd.message!!.chat) &&
                BotDatabase.isModeratedBy(upd.message!!.chat, upd.message!!.from!!)
    }
}