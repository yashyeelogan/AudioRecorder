package edu.gatech.ylogan3.audiorecorder;

import edu.gatech.team4180.dsp.*;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.SharedPreferences;
import android.media.AudioRecord;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;



import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
//import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
//import android.widget.TextView;

public class AudioRecorder extends AppCompatActivity {
    //private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };
    final int SAMPLE_RATE = 44100;
    float[] audioBuffer;
    final String LOG_TAG = "Output";
    boolean shouldContinue = true;

    public void recordAudio(){
        new Thread(new Runnable(){

            @Override
            public void run(){
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

                //AudioRecord record = findAudioRecord();

                // buffer size in bytes
//                int bufferSize = AudioRecord.getMinBufferSize(record.getSampleRate (),
//                        record.getChannelConfiguration (),
//                        record.getAudioFormat());
                int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_FLOAT);

                if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                    bufferSize = SAMPLE_RATE * 2;
                }
                audioBuffer = new float[bufferSize / 2];

                AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_FLOAT,
                        bufferSize);
                int result = record.getState();
                if (record.getState() != AudioRecord.STATE_INITIALIZED) {
                    Log.e(LOG_TAG, "Audio Record can't initialize!");
                    return;
                }
                record.startRecording();

                Log.v(LOG_TAG, "Start recording");

                long shortsRead = 0;
                while (shouldContinue) {
                    int numberOfShort = record.read(audioBuffer, 0, audioBuffer.length, record.READ_BLOCKING);
                    shortsRead += numberOfShort;
                    for (float f: audioBuffer){
                        Log.d(LOG_TAG, "" + f);
                    }
                    shouldContinue = false;
                }

                record.stop();
                record.release();

                Log.v(LOG_TAG, String.format("Recording stopped. Samples read: %d", shortsRead));
                AudioRecorder.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        convertArray();
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recorder);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        recordAudio();
    }

    private void convertArray() {
        double[] doubles = DSP.float_to_double_array(audioBuffer);

        for (double d : doubles) {
            Log.d(LOG_TAG, "" + d);
        }
        //Get voice features from the signal
        SpeechFeatures reference = new SpeechFeatures(doubles);

        //Convert the obtained features into a format that can easily be saved into a file.
        //double[] savable_format = reference.toStorableArray();
        //SharedPreferences sp =
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_audio_recorder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
