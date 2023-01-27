package handlers

import eu.vendeli.tgbot.types.Message
import eu.vendeli.tgbot.types.Sticker
import eu.vendeli.tgbot.types.User

interface RestrictionPolicy {
    fun isRestricted(msg: Message): Boolean
}

class StickerRestriction(
    private val sticker: Sticker,
    private val pack: Boolean = false
): RestrictionPolicy {
    override fun isRestricted(msg: Message): Boolean {
        val curSticker = msg.sticker ?: return false
        return (curSticker.fileUniqueId == sticker.fileUniqueId) ||
                (pack && curSticker.setName == sticker.setName)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is StickerRestriction) return false
        return (pack && other.pack && sticker.setName == other.sticker.setName)
                || (pack == other.pack && sticker.fileUniqueId == other.sticker.fileUniqueId)
    }

    override fun hashCode(): Int {
        return if (pack) {
            sticker.setName.hashCode()
        } else {
            sticker.fileUniqueId.hashCode()
        }
    }
}


class ViaRestriction(user: User): RestrictionPolicy {
    private val botId: Long

    init {
        botId = user.id
    }

    override fun isRestricted(msg: Message): Boolean {
        return msg.viaBot?.id == botId
    }

    override fun equals(other: Any?): Boolean {
        return other is ViaRestriction && botId == other.botId
    }

    override fun hashCode(): Int {
        return botId.hashCode()
    }
}

class Restrictions: RestrictionPolicy {
    private val stickers = HashSet<StickerRestriction>()
    private val viaBots = HashSet<ViaRestriction>()

    override fun isRestricted(msg: Message): Boolean {
        return stickers.any { it.isRestricted(msg) } || viaBots.any { it.isRestricted(msg) }
    }

    fun addStickerRestriction(sr: StickerRestriction) {
        stickers.add(sr)
    }

    fun removeStickerRestriction(sr: StickerRestriction) {
        stickers.remove(sr)
    }

    fun addViaBotRestriction(vb: ViaRestriction) {
        viaBots.add(vb)
    }

    fun removeViaBotRestriction(vb: ViaRestriction) {
        viaBots.remove(vb)
    }

}
