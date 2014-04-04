package com.getpebble.example.logging;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import com.getpebble.android.kit.PebbleKit;
import com.google.common.primitives.UnsignedInteger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Sample code demonstrating how Android applications can receive data logs from Pebble.
 */
public class ExampleDataLoggingActivity extends Activity {
    //private static final UUID OCEAN_SURVEY_APP_UUID = UUID.fromString("0A5399d9-5693-4F3E-B768-9C99B5F5DCEA");
    private static final UUID OCEAN_SURVEY_APP_UUID = UUID.fromString("58a2c4f3-8edc-4663-82e9-100d74bef51c");
	//private static final UUID OCEAN_SURVEY_APP_UUID = UUID.fromString("7efbc8b2-1fbf-4037-a589-c292ca3bf19d");
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

    private final StringBuilder mDisplayText = new StringBuilder();

    private PebbleKit.PebbleDataLogReceiver mDataLogReceiver = null;
    int count = 0;
    
    static private String TAG = "MainActivity";
    File root = null;
	File dir = null;
	File file = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);
        DATE_FORMAT.setTimeZone(TimeZone.getDefault());
        
        root = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES); 

		dir = new File (root.getAbsolutePath() + "/../Download");
		Log.i(TAG,"File Path"+dir);
		dir.mkdirs();
		file = new File(dir, "myData2.txt");

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDataLogReceiver != null) {
            unregisterReceiver(mDataLogReceiver);
            mDataLogReceiver = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final Handler handler = new Handler();

        // To receive data logs, Android applications must register a "DataLogReceiver" to receive data.
        //
        // In this example, we're implementing a handler to receive unsigned integer data that was logged by a
        // corresponding watch-app. In the watch-app, three separate logs were created, one per animal. Each log was
        // tagged with a key indicating the animal to which the data corresponds. So, the tag will be used here to
        // look up the animal name when data is received.
        //
        // The data being received contains the seconds since the epoch (a timestamp) of when an ocean faring animal
        // was sighted. The "timestamp" indicates when the log was first created, and will not be used in this example.
        mDataLogReceiver = new PebbleKit.PebbleDataLogReceiver(OCEAN_SURVEY_APP_UUID) {
            @SuppressWarnings("unused")
			public void receiveData(Context context, UUID logUuid, UnsignedInteger timestamp, UnsignedInteger tag,
                                    int data){
            
            	 
            	/*if(count!=4)
                	mDisplayText.append("\t");
            	
                mDisplayText.append(data);
                 
                count ++;
                
               if(count == 4)
               {	   
            	   mDisplayText.append("\n");
                   count = 0;
               }  */
            	
            	
				
				try {
					BufferedWriter buf = new BufferedWriter(new FileWriter(file,true));
					
					
					buf.append(""+data);
					
					count ++;
					Log.i(TAG,"Count"+count);
					if(count!=3)
						buf.append(", ");
					
					if(count ==3)
					{	
						Log.i(TAG,"Coming here");	
					 buf.newLine();
					 count = 0;
					}
					
					buf.close();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
            	mDisplayText.append(data);
            	mDisplayText.append("\n");
            	
            	
               //mDisplayText.append(AnimalName.fromInt(tag.intValue()).getName());

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateUi();
                    }
                });
            }
        };

        PebbleKit.registerDataLogReceiver(this, mDataLogReceiver);

        PebbleKit.requestDataLogsForApp(this, OCEAN_SURVEY_APP_UUID);
    }

    private void updateUi() {
        TextView textView = (TextView) findViewById(R.id.log_data_text_view);
        textView.setText(mDisplayText.toString());
    }

    private String getUintAsTimestamp(UnsignedInteger uint) {
        return DATE_FORMAT.format(new Date(uint.longValue() * 1000L)).toString();
    }

    private static enum AnimalName {
        SEALION(0x5),
        DOLPHIN(0xd),
        PELICAN(0xb),
        UNKNOWN(0xff);

        public final int id;

        private AnimalName(final int id) {
            this.id = id;
        }

        public static AnimalName fromInt(final int id) {
            for (AnimalName animal : values()) {
                if (animal.id == id) {
                    return animal;
                }
            }
            return UNKNOWN;
        }

        public String getName() {
            return name().toLowerCase();
        }
    }
}
