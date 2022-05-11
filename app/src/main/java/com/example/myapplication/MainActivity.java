package com.example.myapplication;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarInputStream;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    private static final int MY_PERMISSIONS_REQUEST_INTERNET = 2;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE = 3;


    String str_url = "https://api.telegram.org/bot";
    SharedPreferences sPref;
    String token = "token";
    String chat_id = "chat_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            sendOver_TelegramBot("Программа запущена");
        } catch (IOException e) {
            e.printStackTrace();
        }
        checkForSmsPermission();
        loadSettings_buttonRead_OnClick(findViewById(R.id.button_read));
    }

    /**
     * Checks whether the app has SMS permission.
     */
    private void checkForSmsPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, getString(R.string.permission_not_granted));
            // Permission not yet granted. Use requestPermissions().
            // MY_PERMISSIONS_REQUEST_SEND_SMS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
        } else {

        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, getString(R.string.permission_not_granted));
            // Permission not yet granted. Use requestPermissions().
            // MY_PERMISSIONS_REQUEST_SEND_SMS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    MY_PERMISSIONS_REQUEST_INTERNET);
        } else {

        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, getString(R.string.permission_not_granted));
            // Permission not yet granted. Use requestPermissions().
            // MY_PERMISSIONS_REQUEST_SEND_SMS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                    MY_PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE);
        } else {

        }
    }

    /**
     * Processes permission request codes.
     *
     * @param requestCode  The request code passed in requestPermissions()
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        // For the requestCode, check if permission was granted or not.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (permissions[0].equalsIgnoreCase(Manifest.permission.SEND_SMS)
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    // Permission denied.
                    Log.d(TAG, getString(R.string.failure_permission_sms));
                    Toast.makeText(this, getString(R.string.failure_permission_sms),
                            Toast.LENGTH_LONG).show();
                }

            }
        }
    }

    public void loadSettings_buttonRead_OnClick(View view) {

        EditText eT_token = (EditText) findViewById(R.id.eT_token);
        eT_token.setText(loadText(token));
        EditText eT_chatId = (EditText) findViewById(R.id.eT_chatId);
        eT_chatId.setText(loadText(chat_id));
    }

    public void saveSettings_buttonRead_OnClick(View view) {

        EditText eT_token = (EditText) findViewById(R.id.eT_token);
        saveText(token, eT_token.getText().toString());
        EditText eT_chatId = (EditText) findViewById(R.id.eT_chatId);
        saveText(chat_id, eT_chatId.getText().toString());
        new MyHttpRequestTask().execute(str_url);
    }


    public void retryApp(View view) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        startActivity(intent);
    }


    public void sendOver_TelegramBot(String myMessage) throws IOException {
        if (loadText(chat_id) != "" & loadText(token) != "") {
            new MyHttpRequestTask().execute(str_url, getJsonData(myMessage).toString());
        }
    }

    public JSONObject getJsonData(String myMessage) {
        JSONObject json = new JSONObject();
        try {
            json.put("chat_id", loadText(chat_id));
            json.put("text", myMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }


    private class MyHttpRequestTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            String my_url = "";
            String my_data = "";
            String bot_api_cmd = "/sendMessage";
            if (params.length == 1) {
                my_url = params[0];
                bot_api_cmd = "/getUpdates";
            } else {
                my_url = params[0];
                my_data = params[1];
            }
            try {
                URL url = new URL(my_url + loadText(token) + bot_api_cmd);
                HttpsURLConnection httpURLConnection = (HttpsURLConnection) url.openConnection();

                // setting the  Request Method Type
                httpURLConnection.setRequestMethod("POST");
                // adding the headers for request
                if (params.length > 1) {
                    httpURLConnection.setRequestProperty("Content-Type", "application/json");
                }
                try {
                    BufferedReader br = null;
                    if (params.length > 1) {
                    //to tell the connection object that we will be wrting some data on the server and then will fetch the output result
                    httpURLConnection.setDoOutput(true);
                    // this is used for just in case we don't know about the data size associated with our request
                    httpURLConnection.setChunkedStreamingMode(0);

                    // to write tha data in our request
                    OutputStream outputStream = new BufferedOutputStream(httpURLConnection.getOutputStream());
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);

                        outputStreamWriter.write(my_data);

                    outputStreamWriter.flush();
                    outputStreamWriter.close();
                    }
//                    // to log the response code of your request
                    if (params.length == 1) {
                        TextView getUpdate_info = (TextView) findViewById(R.id.getUpdate_info);
//

                        if (httpURLConnection.getResponseCode() == 200) {
                            br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                            String strCurrentLine;
                            while ((strCurrentLine = br.readLine()) != null) {
                                my_data=my_data+"\r\n"+strCurrentLine;
                            }
                        } else {
                            br = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream()));
                            String strCurrentLine;
                            while ((strCurrentLine = br.readLine()) != null) {
                                my_data=my_data+"\r\n"+strCurrentLine;
                            }
                        }
                        getUpdate_info.setText(decode_str(my_data));
                    }
                    httpURLConnection.getResponseMessage();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // this is done so that there are no open connections left when this task is going to complete
                    httpURLConnection.disconnect();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    static final String decode_str(final String in)
    {
        String working = in;
        int index;
        index = working.indexOf("\\u");
        while(index > -1)
        {
            int length = working.length();
            if(index > (length-6))break;
            int numStart = index + 2;
            int numFinish = numStart + 4;
            String substring = working.substring(numStart, numFinish);
            int number = Integer.parseInt(substring,16);
            String stringStart = working.substring(0, index);
            String stringEnd   = working.substring(numFinish);
            working = stringStart + ((char)number) + stringEnd;
            index = working.indexOf("\\u");
        }
        return working;
    }
    void saveText(String key_, String value_) {
        sPref = getSharedPreferences(getPackageName(),MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(key_, value_);
        ed.commit();
    }

    String loadText(String key_) {
        sPref = getSharedPreferences(getPackageName(),MODE_PRIVATE);
        String savedText = sPref.getString(key_, "");
        return savedText;
    }
}