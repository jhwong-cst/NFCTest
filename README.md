# NFC Mifare Ultralight Read/Write Test

This project is for learning purposes, and will not be developed as a full application.

This is not really even functional, the UI is not even linked up to the code.

All the output is outputted via Logcat, so refer to that to see the output.
This is meant to just provide sample code for certain key features of handling UL cards.

Features:
- Can Read Mifare UL Cards and output Serial Number, and Data (sample is reading 48 bytes with 6 pages).
- Can Write to Mifare UL cards (sample is writing a 48 bytes into 6 pages).

----

References:
- https://developer.android.com/reference/android/nfc/tech/MifareUltralight
- https://itnext.io/how-to-use-nfc-tags-with-android-studio-detect-read-and-write-nfcs-42f1d60b033