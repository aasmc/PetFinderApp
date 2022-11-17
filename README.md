# Pet Finder App

Educational app based on the book Real World Android by Tutorials 2nd edition.

### Security Features

Should enforce a secure location for app's install directory. Installing apps on external SD cards
is convenient, but also a security flaw. Anyone with access to the SD card also has access to the
app's data - and that data could hold sensitive information. This is why it's a best practice to
restrict your app to internal storage. To do this, add the following line to AndroidManifest.xml:

```text
android:installLocation="internalOnly"
```

Disable backup: disabling backup prevents users from accessing the contents of the app's private
data folder using **ADB backup**. To do this, add the following line to AndroidManifest.xml (NB:
it has been deprecated since Android 12):

```text
android:allowBackup="false"
```

The best practice to pass data via IPC is to use Intents:

```kotlin
val intent = Intent()
// tha package name of the app where the Intent will be sent
val packageName = "com.example.app"
// qualified class name in the target app that receives the intent
val activityClass = "com.example.app.TheActivity"
intent.component = ComponentName(packageName, activityClass)
// data sent with the intent
intent.putExtra("UserInfo", "Example string")
startActivityForResult(intent)
```

To broadcast data to more than one app, enforce that only apps signed with your signing key will get
the data. Otherwise, any app that registers to receive the broadcast can read the sent information.
Likewise, a malicious app could send a broadcast to your app if you've registered to receive its
broadcast.

**Keyboard security tips:**

- disable autocorrect with android:inputType="textNoSuggestions|textVisiblePassword|textFilter"
- mark password fields as secureTextOnly

**Logging**. Android saves debug logs to a file that you can retrieve for the production builds of
your app. Even when writing code and debugging the app, be sure not to log sensitive information
such as passwords and keys to the console.

**Disabling Screenshots**
The OS take screenshots of an app. It uses them for the animation it plays when it puts an app into
the background or for the list of open apps in the task switcher. Those screenshots are stored on
the device. We can disable this feature for views revealing sensitive data:

```kotlin
window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
```

**Wiping Memory Securely**
When an OS deletes a file, it only removes the reference, not the data. To completely remove that
data, you must overwrite the file with random data:

```kotlin
fun wipeFile(file: File) {
    if (file.exists()) {
        val length = file.length()
        val random = SecureRandom()
        val randomAccessFile = RandomAccessFile(file, "rws")
        randomAccesFile.seek(0)
        randomAccessFile.filePointer
        val data = ByteArray(64)
        var position = 0
        while (position < length) {
            random.nextBytes(data)
            randomAccessFile.write(data)
            position += data.size
        }
        randomAccessFile.close()
        file.delete()
    }
}
```
However, depending on the platform, some types of solid-state storage drives such as SSD won't 
write to the same area of memory each time. Therefore the above method may not work... A better solution
is to encrypt data in the first place. 

**Advanced Encryption Standard (AES)**
AES uses substitution-permutation network to encrypt your data with a key. Using this approach, it
replaces bytes from one table with the bytes from another, and so creates permutations of data.
AES requires an encryption key. 
**Creating a key**
We need to use the same key to encrypt and decrypt data. This is called symmetric encryption. You can
use different specific lengths for the key, but 256 bits is standard.  
Directly using the user's password for encryption is dangerous because it likely won't be random
or large enough. A function called **Password-Based Key Derivation Function PBKDF2** comes to
the rescue. It takes a password and by hashing it with random data many times over, creates a key.
That random data is called a **salt**. PBKDF2 creates a strong and unique key, even if someone else
uses the same or a very simple password.

**Choosing an Encryption Mode**
The mode defines how the data is processed. One example is Electronic Code Book (ECB). It's simplistic
in that it splits up the data and repeats the encryption process for every chink with the same key.
Because each block uses the same key, this mode is highly insecure. DON'T USE THIS MODE.

On the other hand, *Counter Mode (CTR)* uses a counter so each block encrypts differently. CTR is
efficient and safe to use. 

There are a few other modes that are useful: *GCM* offers authentication in addition to encryption,
whereas *XTS* is optimized for full disk encryption. 

This app uses *Cipher Block Chaining (CBC)* to XOR each block of plaintext with the previous block.

***Adding an Initialization Vector*
When using CBC to XOR each block of data in the pipeline with the previous block we need to define
a block that will be used to XOR the first block of our data. This is called an Initialization Vector
(IV). 

Example:
```kotlin
        fun encrypt(
            dataToEncrypt: ByteArray,
            password: CharArray
        ): HashMap<String, ByteArray> {
            val map = HashMap<String, ByteArray>()

            try {
                // 1
                // Random salt for next step
                val random = SecureRandom()
                val salt = ByteArray(256)
                random.nextBytes(salt)

                // 2
                // PBKDF2 - derive the key from the password, don't use password directly
                val pbKeySpec = PBEKeySpec(password, salt, 1324, 256)
                val secretKeyFactory = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA1")
                // generate the key as ByteArray
                val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
                // wrap the key ByteArray into a SecretKeySpec object
                // Now we have a secure key to be used for encryption of data
                val keySpec = SecretKeySpec(keyBytes, "AES")

                // 3
                // Create initialization vector for AES
                val ivRandom =
                    SecureRandom() // not caching previous seeded instance of SecureRandom
                val iv = ByteArray(16)
                ivRandom.nextBytes(iv)
                val ivSpec = IvParameterSpec(iv)

                // 4
                // Encrypt
                // AES/CBC chooses AES with cipher block chaining mode. 
                // PKCS7Padding is a well-known standard for padding. Since we are working with
                // blocks, not all data will fit perfectly into the block size,
                // so we need to pad the remaining space. Blocks are 128 bits long and 
                // AES adds padding before encryption. 
                val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
                
                // this performs the actual encryption.
                val encrypted = cipher.doFinal(dataToEncrypt)

                // 5
                map["salt"] = salt
                map["iv"] = iv
                map["encrypted"] = encrypted

            } catch (e: Exception) {
                Log.e("MYAPP", "encryption exception", e)
            }

            return map
        }
```
Encryption may also be performed after prefixing the ciphertext with the IV instead of saving
it to the Map. When needed, it may be stripped off and used for decryption. 

Example of decryption:
```kotlin
        fun decrypt(map: HashMap<String, ByteArray>, password: CharArray): ByteArray? {

            var decrypted: ByteArray? = null

            try {
                // 1
                val salt = map["salt"]
                val iv = map["iv"]
                val encrypted = map["encrypted"]

                // 2
                // regenerate key from password
                val pbKeySpec  = PBEKeySpec(password, salt, 1324, 256)
                val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
                val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
                val keySpec = SecretKeySpec(keyBytes, "AES")
                // 3
                // Decrypt
                val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
                val ivSpec = IvParameterSpec(iv)
                cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
                decrypted = cipher.doFinal(encrypted)
                
            } catch (e: Exception) {
                Log.e("MYAPP", "decryption exception", e)
            }

            return decrypted
        }
```