package nfc.argen666.ru.nfcreader;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Developer on 24.01.2015.
 */
class NFCReader extends AsyncTask<Object, Void, String> {
    String LOG_TAG = "WWWWWWWW";
    ProgressBar pgb;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Toast.makeText(pgb.getContext(),"Card Detected",Toast.LENGTH_SHORT).show();
        pgb.setVisibility(View.VISIBLE);
        Log.d(LOG_TAG, "Begin");
    }

    public void setPg(ProgressBar pg) {
        this.pgb = pg;
    }

    @Override
    protected String doInBackground(Object... params) {
        Tag tag = (Tag) params[0];
        Boolean full = (Boolean) params[1];
        Log.i(LOG_TAG, "resolving intent");
        //Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String metaInfo = "";
        if (tag != null) {
            Log.i(LOG_TAG, "found a tag");
            //System.out.println(tag.getTechList().length);

            metaInfo += "Supported technologies:\n";
            for (String tech : tag.getTechList()) {
                metaInfo += tech + "\n";
            }
            boolean auth = false;
            MifareClassic mfc = MifareClassic.get(tag);
            if (mfc == null) {
                //textView1.setText("Unsupported card! (No Mifare/NfcA)");
                return "Unsupported card! (No Mifare/NfcA)";
            }
            try {

                //Enable I/O operations to the tag from this TagTechnology object.
                mfc.connect();
                int type = mfc.getType();
                int sectorCount = mfc.getSectorCount();
                String typeS = "";
                switch (type) {
                    case MifareClassic.TYPE_CLASSIC:
                        typeS = "TYPE_CLASSIC";
                        break;
                    case MifareClassic.TYPE_PLUS:
                        typeS = "TYPE_PLUS";
                        break;
                    case MifareClassic.TYPE_PRO:
                        typeS = "TYPE_PRO";
                        break;
                    case MifareClassic.TYPE_UNKNOWN:
                        typeS = "TYPE_UNKNOWN";
                        break;
                }
                metaInfo += "Card type：" + typeS + "\n with" + sectorCount + " Sectors,\n "
                        + mfc.getBlockCount() + " Blocks\nStorage Space: " + mfc.getSize() + "B\n";
                if (full) {
                    for (int j = 0; j < sectorCount; j++) {
                        //Authenticate a sector with key A.
                        auth = mfc.authenticateSectorWithKeyA(j,
                                MifareClassic.KEY_DEFAULT);
                        int bCount;
                        int bIndex;
                        if (auth) {
                            metaInfo += "Sector " + j + ": Verified successfully\n";
                            bCount = mfc.getBlockCountInSector(j);
                            bIndex = mfc.sectorToBlock(j);
                            for (int i = 0; i < bCount; i++) {
                                byte[] data = mfc.readBlock(bIndex);
                                metaInfo += "Block " + bIndex + " : "
                                        + bytesToHex(data) + "\n";
                                bIndex++;
                            }
                        } else {
                            metaInfo += "Sector " + j + ": Verified failure\n";
                        }
                    }
                } else {
                    auth = mfc.authenticateSectorWithKeyA(0,
                            MifareClassic.KEY_DEFAULT);
                    int bCount;
                    int bIndex;
                    String uid = "";
                    String fcode = "";
                    if (auth) {
                        metaInfo="";
                        bCount = mfc.getBlockCountInSector(0);
                        bIndex = mfc.sectorToBlock(0);
                        for (int i = 0; i < 1; i++) {
                            byte[] data = mfc.readBlock(bIndex);
                            metaInfo += bytesToHex(data);
                            bIndex++;
                        }

                        fcode=metaInfo.substring(4, 6);
                        uid=metaInfo.substring(0, 4);
                        uid = uid.substring(2, 4)+uid.substring(0, 2);
                        metaInfo="";
                        metaInfo += String.valueOf(Integer.parseInt(fcode,16));
                        String uuid=String.valueOf(Integer.parseInt(uid,16));
                        metaInfo += uuid.length()<5 ? "0"+uuid : uuid ;
                    } else {
                        metaInfo = "Sector " + 0 + ": Verified failure\n";
                    }
                }

                //textView1.setText(metaInfo);

            } catch (Exception e) {
                metaInfo ="Lost connection!";
                e.printStackTrace();
            }
        }

        return metaInfo;
    }

    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Override
    protected void onPostExecute(String s) {
        pgb.setVisibility(View.GONE);
    }
}
