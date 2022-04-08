package com.joonhuiwong.nfctest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    private String serialNumber;
    private String type;
    private String data;

    static String TAG = "MainActivity.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfcAdapter = NfcAdapter.getDefaultAdapter(MainActivity.this);

        if (nfcAdapter == null) {
            String msg = "NO NFC Capabilities!";
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            Log.e(TAG, msg);
            return;
        }

        setupPendingIntent();
    }

    private void setupPendingIntent() {
        pendingIntent = PendingIntent.getActivity(
                MainActivity.this,
                0,
                new Intent(MainActivity.this, MainActivity.this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                0
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(MainActivity.this, pendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(MainActivity.this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        resolveIntent(intent);
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                serialNumber = TagUtils.detectTagSerialNumber(tag)
                        .replaceAll(" ", "");
                Log.d(TAG + "resolveIntent()", "Serial Number: " + serialNumber);

                type = TagUtils.detectTagTech(tag);
                Log.d(TAG + "resolveIntent()", "Type:" + type);

                if (TagUtils.MIFARE_ULTRALIGHT.equalsIgnoreCase(type)) {
                    readUltralightTag(tag);

                    String dataToWrite =
                            "11111111" +
                            "22222222" +
                            "33333333" +
                            "44444444" +
                            "55555555" +
                            "66666666";

                    writeUltralightTag(tag, dataToWrite);
                }

            }
        }
    }

    private void readUltralightTag(Tag tag) {
        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareUltralight.class.getName())) {
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                data = readTag(mifareUlTag, 48);
                Log.d(TAG + "readUltralightTag()", "Data (48 bytes):" + data);
                return;
            }
        }
        Log.e(TAG + "readUltralightTag()", "Not Ultralight Tag!");
    }

    private void writeUltralightTag(Tag tag, String dataToWrite) {
        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareUltralight.class.getName())) {
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                writeTag(mifareUlTag, dataToWrite);
                Log.d(TAG + "writeUltralightTag()", "Write Done");
                return;
            }
        }
        Log.e(TAG + "writeUltralightTag()", "Not Ultralight Tag!");
    }

    /**
     * MifareUltralight readPages reads 4 pages at a time.
     *
     * We use the 'length' param to determine how many "reads" to do.
     *
     * Example:
     * We want to read 48 "characters" of data.
     * We know that data starts at page 5 of the card, thus the first read is 4 (start from 0, so 4 is the fifth page).
     * Each read only gets 32 bytes, so to get 48, a second read is needed.
     * After the read, the spaces are removed, and we get the desired length by substring.
     * @param mifareUlTag
     * @param length
     * @return
     */
    public String readTag(MifareUltralight mifareUlTag, int length) {
        try {
            // e.g. 48
            int bytesPerRead = 32;
            int numberOfReads = (int) Math.ceil((double)length / bytesPerRead);
            int pageOffsetDefault = 4;

            mifareUlTag.connect();

            StringBuilder data = new StringBuilder();
            for (int i = 1; i <= numberOfReads; i++) {
                byte[] payload = mifareUlTag.readPages(pageOffsetDefault * i);
                data.append(CommonUtils.toReversedHex(payload).replaceAll(" ", ""));
            }

            return data.substring(0, length).toUpperCase();
        } catch (IOException e) {
            Log.e(TAG, "IOException while reading MifareUltralight message...", e);
        } finally {
            if (mifareUlTag != null) {
                try {
                    mifareUlTag.close();
                }
                catch (IOException e) {
                    Log.e(TAG, "Error closing tag...", e);
                }
            }
        }
        return null;
    }

    /**
     * We are assuming that the dataToWrite is a fixed 48 character string.
     * @param mifareUlTag
     * @param dataToWrite
     */
    public void writeTag(MifareUltralight mifareUlTag, String dataToWrite) {
        assert dataToWrite.length() == 48; // Crash application if not met

        try {
            int charactersPerPage = 8;
            int pageOffsetStart = 4;
            int pagesToWrite = dataToWrite.length() / charactersPerPage;

            String[] strings = CommonUtils.splitString(dataToWrite, charactersPerPage);

            mifareUlTag.connect();

            for (int i = 0; i < pagesToWrite; i++) {
                int pageOffset = pageOffsetStart + i;
                byte[] data = CommonUtils.stringToHex(strings[i]);
                mifareUlTag.writePage(pageOffset, data);
                Log.v(TAG + "writeTag()", "Written " + strings[i] + " to PageOffset " + pageOffset);
            }

        } catch (IOException e) {
            Log.e(TAG, "IOException while writing MifareUltralight...", e);
        } finally {
            try {
                mifareUlTag.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException while closing MifareUltralight...", e);
            }
        }
    }

}