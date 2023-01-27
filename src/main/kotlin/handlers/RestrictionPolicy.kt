package handlers

import eu.vendeli.tgbot.types.Message
import eu.vendeli.tgbot.types.Sticker
import eu.vendeli.tgbot.types.User
import kotlinx.serialization.Serializable

interface RestrictionPolicy {
    fun isRestricted(msg: Message): Boolean
}

@Serializable
class StickerRestriction: RestrictionPolicy {
    private val pack: Boolean
    private val setName: String?
    private val fileUniqueId: String
    constructor(sticker: Sticker, pack: Boolean = false) {
        this.pack = pack
        setName = sticker.setName
        fileUniqueId = sticker.fileUniqueId
    }
    override fun isRestricted(msg: Message): Boolean {
        val curSticker = msg.sticker ?: return false
        return (curSticker.fileUniqueId == fileUniqueId) ||
                (pack && curSticker.setName == setName)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is StickerRestriction) return false
        return (pack && other.pack && setName == other.setName)
                || (pack == other.pack && fileUniqueId == other.fileUniqueId)
    }

    override fun hashCode(): Int {
        return if (pack) {
            setName.hashCode()
        } else {
            fileUniqueId.hashCode()
        }
    }
}


@Serializable
class ViaRestriction: RestrictionPolicy {
    private val botId: Long

    constructor(user: User) {
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

@Serializable
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
