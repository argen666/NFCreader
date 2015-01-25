package nfc.argen666.ru.nfcreader;

import android.app.ActionBar;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcF;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.ExecutionException;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    NfcAdapter mAdapter;
    IntentFilter[] intentFiltersArray;
    PendingIntent pendingIntent;
    String[][] techListsArray;
    Boolean mode=false;
    String data="";
TextView textView1;
    ProgressBar pg;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView1 = (TextView) findViewById(R.id.textView1);
        pg = (ProgressBar) findViewById(R.id.progressBar);
        // enable foreground dispatch
         mAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;

        }

        if (!mAdapter.isEnabled()) {
            textView1.setText("Please enable NFC");
            Toast.makeText(this, "NFC is disabled.", Toast.LENGTH_LONG).show();
        } else {
            textView1.setText("Жду карту");
        }

         pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        try {
            ndef.addDataType("*/*");    /* Handles all MIME based dispatches.
                                       You should specify only the ones that you need. */
        }
        catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }

         intentFiltersArray = new IntentFilter[] {ndef, };
         techListsArray = new String[][] { new String[] { NfcA.class.getName() } };
        if (savedInstanceState==null){
        resolveIntent(getIntent());
    }
    else {

}
    }

    void resolveIntent(Intent intent) {

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())){
        //textView1.setText("Please Wait...");
          //  Toast.makeText(getApplicationContext(),"Card Detected",Toast.LENGTH_SHORT).show();

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        NFCReader reader = new NFCReader();

        reader.setPg(pg);
        reader.execute(tag, mode);
            try {
                data =reader.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            textView1.setText(data);}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Log.i(TAG, "created options");
        return true;
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        resolveIntent(intent);
        Log.i(TAG, "new intent");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        // Операции для выбранного пункта меню
        switch (item.getItemId())
        {
            case R.id.mode:{
                if (!mode)
                {
                   mode =true;
                   item.setTitle("Full mode: ON");
                   textView1.setTextSize(20);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP);
                    textView1.setLayoutParams(params);
                    textView1.setText("Жду карту");
                }
                else {
                    mode=false;
                    item.setTitle("Full mode: OFF");
                    textView1.setTextSize(60);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
               textView1.setLayoutParams(params);
                    textView1.setText("Жду карту");
                }
            }



        }
        return true;
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("uid", data);
        outState.putBoolean("mode", mode);
        super.onSaveInstanceState(outState);

        Log.d(TAG, "onSaveInstanceState");
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        data = savedInstanceState.getString("uid");
        mode = savedInstanceState.getBoolean("mode");
        textView1.setText(data=="" ? "Жду карту" : data);
        if (!mode)
        {
            textView1.setTextSize(60);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
            textView1.setLayoutParams(params);
        }
        else {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP);

            textView1.setLayoutParams(params);
            textView1.setTextSize(20);
        }
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState");
    }
}