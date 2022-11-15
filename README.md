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
