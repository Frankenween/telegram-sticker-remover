import eu.vendeli.tgbot.types.Chat
import eu.vendeli.tgbot.types.Message
import eu.vendeli.tgbot.types.User
import handlers.Restrictions
import handlers.StickerRestriction

object BotDatabase {
    private val userModerates = HashMap<Long, HashSet<Long>>()
    private val chatModeratedBy = HashMap<Long, Long>()
    private val moderatedChats = HashSet<Long>()

    private val chatRestrictions = HashMap<Long, Restrictions>()
    private val chatSpecialMessages = HashMap<Long, HashMap<Long, (Message) -> Unit>>()

    fun addChat(moderator: User, chat: Chat) {
        userModerates.getOrPut(moderator.id) { hashSetOf() }.add(chat.id)
        chatModeratedBy[chat.id] = moderator.id
        moderatedChats.add(chat.id)
        chatRestrictions[chat.id] = Restrictions()
        chatSpecialMessages[chat.id] = hashMapOf()

        println("Chat ${chat.id} is now moderated. Admin is ${moderator.firstName} with id ${moderator.id}")
    }

    fun removeChat(chat: Chat) {
        val moderator = chatModeratedBy[chat.id]
        userModerates[moderator]?.remove(chat.id)
        moderatedChats.remove(chat.id)
        chatModeratedBy.remove(chat.id)
        chatRestrictions.remove(chat.id)
        chatSpecialMessages.remove(chat.id)

        println("Chat ${chat.id} is now removed. $moderator was an Admin")
    }

    fun isModerated(chat: Chat): Boolean = chat.id in moderatedChats

    fun isModeratedBy(chat: Chat, user: User) = chatModeratedBy[chat.id] == user.id

    // Returns true iff chat is moderated and this message doesn't meet message policy
    fun restrictedMessage(msg: Message): Boolean {
        return chatRestrictions[msg.chat.id]?.isRestricted(msg) ?: false
    }

    fun addStickerRestriction(sr: StickerRestriction, chat: Chat) {
        chatRestrictions[chat.id]!!.addStickerRestriction(sr)
    }

    fun removeStickerRestriction(sr: StickerRestriction, chat: Chat) {
        chatRestrictions[chat.id]!!.removeStickerRestriction(sr)
    }

    fun addSpecialMessage(msg: Message, action: (Message) -> Unit) {
        chatSpecialMessages[msg.chat.id]!![msg.messageId] = action
    }

    fun processSpecialMessages(msg: Message) {
        val reply = msg.replyToMessage
        if (reply == null || msg.chat.id != reply.chat.id) return
        chatSpecialMessages[msg.chat.id]!![reply.messageId]?.invoke(msg)
    }
}