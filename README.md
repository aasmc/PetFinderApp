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
