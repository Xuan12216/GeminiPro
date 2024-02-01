package com.example.geminipro.Util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.geminipro.R;

import java.util.ArrayList;
import java.util.Locale;

public class RecordFunc {
    private Context context;
    private ComponentActivity activity;
    private final ActivityResultRegistry activityResultRegistry;

    public RecordFunc(ComponentActivity activity, Context context){
        this.activity = activity;
        this.context = context;
        this.activityResultRegistry = activity.getActivityResultRegistry();
    }

    public void startRecordFunc(RecordResultCallback callback) {
        if (null != context && null != activity) {
            try {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, context.getString(R.string.record_value));

                ActivityResultLauncher<Intent> launcher = activityResultRegistry.register("key", new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            ArrayList<String> resultArray = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                            if (resultArray != null && !resultArray.isEmpty()) {
                                String recognizedText = resultArray.get(0);
                                callback.onResult(recognizedText);
                            }
                            else callback.onResult("");
                        }
                    }
                });
                launcher.launch(intent);
            } catch (Exception e) {
                Toast.makeText(context, " " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public interface RecordResultCallback{
        void onResult(String result);
    }
}
