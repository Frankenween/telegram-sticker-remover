import com.soywiz.korio.file.VfsOpenMode
import com.soywiz.korio.file.std.localCurrentDirVfs
import com.soywiz.korio.stream.readStringz
import com.soywiz.korio.stream.writeString
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.*
import kotlinx.serialization.json.*

object BotCache {
    suspend fun<T> writeObject(obj: T, serializer: SerializationStrategy<T>, fileName: String) {
        localCurrentDirVfs[fileName].delete()
        val file = localCurrentDirVfs[fileName].open(VfsOpenMode.CREATE)
        try {
            file.writeString(Json.encodeToString(serializer, obj))
        } catch (exc: Exception) {
            System.err.println("Failed writing a object in file $fileName")
            System.err.println(exc)
        } finally {
            file.close()
        }
    }

    suspend fun<T> readObj(fileName: String,
                           deserializer: DeserializationStrategy<T>): T? {
        kotlin.runCatching {
            val file = localCurrentDirVfs[fileName].open(VfsOpenMode.READ)
            kotlin.runCatching {
                val result = Json.decodeFromString(deserializer, file.readStringz())
                file.close()
                return result
            }.onFailure {
                file.close()
                return null
            }
        }.onFailure {
            return null
        }
        return null
    }
}