import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

object AESUtil {
    private const val SECRET_KEY = "0123456789abcdef0123456789abcdef" // 32자 (256bit)
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"

    fun encrypt(plainText: String): String {
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(), "AES")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(iv))
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val encryptedData = Base64.encodeToString(encrypted, Base64.NO_WRAP)
        val encodedIV = Base64.encodeToString(iv, Base64.NO_WRAP)
        return "$encodedIV:$encryptedData"
    }

    fun decrypt(encryptedText: String): String {
        val parts = encryptedText.split(":")
        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val cipherText = Base64.decode(parts[1], Base64.NO_WRAP)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(), "AES")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iv))
        val decrypted = cipher.doFinal(cipherText)
        return String(decrypted, Charsets.UTF_8)
    }
}
