package com.sagalyze.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Ignore
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import android.util.Base64   // ✅ FIX: Works on API 24+

@Entity(
    tableName = "clinician",
    indices = [Index(value = ["email"], unique = true)]
)
data class Clinician(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "email")
    val email: String = "",

    @ColumnInfo(name = "password_hash")
    val passwordHash: String? = null,

    @ColumnInfo(name = "name")
    val name: String? = null,

    @ColumnInfo(name = "license_number")
    val licenseNumber: String? = null,

    @ColumnInfo(name = "fitzpatrick_type")
    val fitzpatrickType: Int? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {

    constructor() : this(
        id = 0,
        email = "",
        passwordHash = null,
        name = null,
        licenseNumber = null,
        fitzpatrickType = null,
        createdAt = System.currentTimeMillis()
    )

    companion object {
        private const val PBKDF2_ITERATIONS = 100_000
        private const val KEY_LENGTH_BITS = 256
        private const val SALT_BYTES = 16

        private val secureRandom = SecureRandom()

        fun create(
            email: String,
            plainPassword: CharArray,
            name: String? = null,
            licenseNumber: String? = null,
            fitzpatrickType: Int? = null
        ): Clinician {
            val salt = ByteArray(SALT_BYTES)
            secureRandom.nextBytes(salt)
            val hash = hashPassword(plainPassword, salt)
            val saltB64 = base64Encode(salt)
            val hashB64 = base64Encode(hash)
            val stored = "$saltB64:$hashB64"
            return Clinician(
                email = email,
                passwordHash = stored,
                name = name,
                licenseNumber = licenseNumber,
                fitzpatrickType = fitzpatrickType
            )
        }

        fun verifyPassword(storedPasswordHash: String?, candidate: CharArray): Boolean {
            if (storedPasswordHash == null) return false
            val parts = storedPasswordHash.split(":")
            if (parts.size != 2) return false
            val salt = base64Decode(parts[0]) ?: return false
            val hash = base64Decode(parts[1]) ?: return false
            val candidateHash = hashPassword(candidate, salt)
            return constantTimeEquals(hash, candidateHash)
        }

        private fun hashPassword(password: CharArray, salt: ByteArray): ByteArray {
            val spec = PBEKeySpec(password, salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
            val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            return skf.generateSecret(spec).encoded
        }

        // ✅ FIXED: Now uses android.util.Base64 (API 24 safe)
        private fun base64Encode(bytes: ByteArray): String =
            Base64.encodeToString(bytes, Base64.NO_WRAP)

        private fun base64Decode(s: String): ByteArray? =
            try { Base64.decode(s, Base64.NO_WRAP) } catch (e: IllegalArgumentException) { null }

        private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
            if (a.size != b.size) return false
            var result = 0
            for (i in a.indices) {
                result = result or (a[i].toInt() xor b[i].toInt())
            }
            return result == 0
        }

        fun isValidEmail(email: String): Boolean {
            val regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
            return regex.matches(email)
        }

        fun isStrongPassword(password: CharArray): Boolean {
            if (password.size < 8) return false
            var hasUpper = false
            var hasLower = false
            var hasDigit = false
            var hasSpecial = false
            for (c in password) {
                when {
                    c.isUpperCase() -> hasUpper = true
                    c.isLowerCase() -> hasLower = true
                    c.isDigit() -> hasDigit = true
                    !c.isLetterOrDigit() -> hasSpecial = true
                }
            }
            return hasUpper && hasLower && hasDigit && hasSpecial
        }
    }

    @Ignore
    fun emailDomain(): String? {
        val parts = email.split("@")
        return if (parts.size == 2) parts[1] else null
    }
}