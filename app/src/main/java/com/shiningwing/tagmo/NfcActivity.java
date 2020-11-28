package com.shiningwing.tagmo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

@EActivity(R.layout.activity_nfc)
public class NfcActivity extends AppCompatActivity {
    private static final String TAG = "NfcActivity";

    public static final String ACTION_SCAN_TAG = "com.shiningwing.tagmo.SCAN_TAG";
    public static final String ACTION_WRITE_TAG_FULL = "com.shiningwing.tagmo.WRITE_TAG_FULL";
    public static final String ACTION_WRITE_TAG_RAW = "com.shiningwing.tagmo.WRITE_TAG_RAW";
    public static final String ACTION_WRITE_TAG_DATA = "com.shiningwing.tagmo.WRITE_TAG_DATA";

    public static final String ACTION_NFC_SCANNED = "com.shiningwing.tagmo.NFC_SCANNED";

    public static final String EXTRA_TAG_DATA = "com.shiningwing.tagmo.EXTRA_TAG_DATA";
    public static final String EXTRA_IGNORE_TAG_ID = "com.shiningwing.tagmo.EXTRA_IGNORE_TAG_ID";

    @ViewById(R.id.txtMessage)
    TextView txtMessage;
    @ViewById(R.id.txtError)
    TextView txtError;

    @ViewById(R.id.imgNfcBar)
    ImageView imgNfcBar;
    @ViewById(R.id.imgNfcCircle)
    ImageView imgNfcCircle;

    @Pref
    Preferences_ prefs;

    NfcAdapter nfcAdapter;
    KeyManager keyManager;

    Animation nfcAnimation;

    @AfterViews
    void afterViews() {
        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        this.keyManager = new KeyManager(this);
        updateTitle();

        nfcAnimation = AnimationUtils.loadAnimation(this, R.anim.nfc_scanning);
    }

    @Override
    protected void onResume() {
        super.onResume();
        clearError();
        String action = getIntent().getAction();
        if (!this.keyManager.hasBothKeys() && (ACTION_WRITE_TAG_FULL.equals(action) || ACTION_WRITE_TAG_DATA.equals(action))) {
            showError(getString(R.string.error_keys_not_loaded));
            this.nfcAdapter = null;
        } else {
            startNfcMonitor();
        }
    }

    void updateTitle() {
        Intent commandIntent = this.getIntent();
        String mode = commandIntent.getAction();
        switch (mode) {
            case ACTION_WRITE_TAG_RAW:
                setTitle(getString(R.string.title_write_tag_raw));
                break;
            case ACTION_WRITE_TAG_FULL:
                setTitle(getString(R.string.title_write_tag_auto));
                break;
            case ACTION_WRITE_TAG_DATA:
                setTitle(getString(R.string.title_restore_tag));
                break;
            case ACTION_SCAN_TAG:
                setTitle(getString(R.string.title_scan_tag));
                break;
            default:
                setTitle(getString(R.string.title_error));
                finish();
        }
    }

    @Override
    protected void onPause() {
        stopNfcMonitor();
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            showMessage(getString(R.string.tag_detected));
            this.onTagDiscovered(intent);
        }
    }

    @UiThread
    void showMessage(String msg) {
        txtMessage.setText(msg);
    }

    @UiThread
    void showError(String msg) {
        txtError.setText(msg);
        txtError.setVisibility(View.VISIBLE);
        txtMessage.setVisibility(View.GONE);
        imgNfcCircle.setVisibility(View.GONE);
        imgNfcBar.setVisibility(View.GONE);
        imgNfcBar.clearAnimation();
    }

    @UiThread
    void clearError() {
        txtError.setVisibility(View.GONE);
        txtMessage.setVisibility(View.VISIBLE);
        imgNfcCircle.setVisibility(View.VISIBLE);
        imgNfcBar.setVisibility(View.VISIBLE);
        imgNfcBar.setAnimation(nfcAnimation);
    }

    @Background
    void onTagDiscovered(Intent intent) {
        Intent commandIntent = this.getIntent();
        String mode = commandIntent.getAction();
        try {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Log.d(TAG, tag.toString());
            NTAG215 mifare = NTAG215.get(tag);
            if (mifare == null) {
                throw new Exception(getString(R.string.error_not_ntag215));
            }
            mifare.connect();
            Intent result = null;
            int resultCode = Activity.RESULT_CANCELED;
            try {
                Log.d(TAG, mode);
                byte[] data;
                switch (mode) {
                    case ACTION_WRITE_TAG_RAW:
                        data = commandIntent.getByteArrayExtra(EXTRA_TAG_DATA);
                        if (data == null) {
                            throw new Exception(getString(R.string.error_no_data));
                        }
                        TagWriter.writeToTagRaw(mifare, data, prefs.enableTagTypeValidation().get());
                        resultCode = Activity.RESULT_OK;
                        showToast(getString(R.string.done));
                        break;
                    case ACTION_WRITE_TAG_FULL:
                        data = commandIntent.getByteArrayExtra(EXTRA_TAG_DATA);
                        if (data == null) {
                            throw new Exception(getString(R.string.error_no_data));
                        }
                        TagWriter.writeToTagAuto(mifare, data, this.keyManager, prefs.enableTagTypeValidation().get(), prefs.enablePowerTagSupport().get());
                        resultCode = Activity.RESULT_OK;
                        showToast(getString(R.string.done));
                        break;
                    case ACTION_WRITE_TAG_DATA:
                        data = commandIntent.getByteArrayExtra(EXTRA_TAG_DATA);
                        boolean ignoreUid = commandIntent.getBooleanExtra(EXTRA_IGNORE_TAG_ID, false);
                        if (data == null) {
                            throw new Exception(getString(R.string.error_no_data));
                        }
                        TagWriter.restoreTag(mifare, data, ignoreUid, this.keyManager, prefs.enableTagTypeValidation().get());
                        resultCode = Activity.RESULT_OK;
                        showToast(getString(R.string.done));
                        break;
                    case ACTION_SCAN_TAG:
                        data = TagWriter.readFromTag(mifare);
                        resultCode = Activity.RESULT_OK;
                        result = new Intent(ACTION_NFC_SCANNED);
                        result.putExtra(EXTRA_TAG_DATA, data);
                        showToast(getString(R.string.done));
                        break;
                    default:
                        throw new Exception(getString(R.string.error_invalid_action) + mode);
                }
            } finally {
                try {
                    mifare.close();
                } catch (Exception e) {
                    Log.d(TAG, getString(R.string.error_closing_tag), e);
                    throw new Exception(getString(R.string.error_closing_tag) + e.getMessage());
                }
            }
            finishActivityWithResult(resultCode, result);
        } catch (Exception e) {
            Log.d(TAG, "Error", e);
            String error = e.getMessage();
            if (e.getCause() != null) {
                error = error + "\n" + e.getCause().toString();
            }
            showError(error);
        }

    }

    @UiThread
    void finishActivityWithResult(int resultcode, Intent resultIntent) {
        setResult(resultcode, resultIntent);
        finish();
    }

    @UiThread
    void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    void startNfcMonitor() {
        if (nfcAdapter == null)
        {
            showError(getString(R.string.error_no_nfc));
            return;
        }

        if (!nfcAdapter.isEnabled()) {
            showError(getString(R.string.error_nfc_disabled));
            new AlertDialog.Builder(this)
                .setMessage(R.string.enable_nfc)
                .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                            startActivity(intent);
                        }
                    }
                })
                .setNegativeButton(R.string.button_no, null)
                .show();
        }

        //monitor nfc status
        IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
        this.registerReceiver(mReceiver, filter);
        listenForTags();
    }

    void stopNfcMonitor() {
        if (nfcAdapter == null) {
            return;
        }
        nfcAdapter.disableForegroundDispatch(this);
        this.unregisterReceiver(mReceiver);
    }
    void listenForTags() {
        Intent intent = new Intent(this.getApplicationContext(), this.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent nfcPendingIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, intent, 0);

        String[][] nfcTechList = new String[][]{};

        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);

        IntentFilter[] nfcIntentFilter = new IntentFilter[]{filter1};

        nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, nfcIntentFilter, nfcTechList);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)) {
                if (!nfcAdapter.isEnabled()) {
                    showError(getString(R.string.error_nfc_disabled));
                } else {
                    listenForTags();
                    clearError();
                }
            }
        }
    };


}
