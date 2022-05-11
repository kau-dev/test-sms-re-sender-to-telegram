package com.example.myapplication;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class mySMS_RECEIVED extends BroadcastReceiver {
    private static final String TAG = mySMS_RECEIVED.class.getSimpleName();
    public static final String pdu_type = "pdus";
    SharedPreferences sPref;
    String token = "token";
    String chat_id = "chat_id";
    String str_url = "https://api.telegram.org/bot";
    Context mycontext;

    /**
     * Called when the BroadcastReceiver is receiving an Intent broadcast.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent received.
     */
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        mycontext = context;
        // Get the SMS message.
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs;
        String strMessage = "";
        String format = bundle.getString("format");
        // Retrieve the SMS message received.
        Object[] pdus = (Object[]) bundle.get(pdu_type);
        if (pdus != null) {
            // Check the Android version.
            boolean isVersionM = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
            // Fill the msgs array.
            msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++) {
                // Check Android version and use appropriate createFromPdu.
                if (isVersionM) {
                    // If Android version M or newer:
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                } else {
                    // If Android version L or older:
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                // Build the message to show.
                strMessage += "\r\n" + "SMS from " + msgs[i].getOriginatingAddress();
                strMessage += " :" + "\r\n" + msgs[i].getMessageBody() + "\r\n";
                // Log and display the SMS message.
                //  Log.d(TAG, "onReceive: " + strMessage);
                // Toast.makeText(context, strMessage, Toast.LENGTH_LONG).show();
                try {
                    sendOver_TelegramBot(strMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
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
            my_url = params[0];
            my_data = params[1];
            try {
                URL url = new URL(my_url + loadText(token) + bot_api_cmd);
                HttpsURLConnection httpURLConnection = (HttpsURLConnection) url.openConnection();

                // setting the  Request Method Type
                httpURLConnection.setRequestMethod("POST");
                // adding the headers for request
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                try {

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

    String loadText(String key_) {

        sPref = mycontext.getSharedPreferences(mycontext.getPackageName(), mycontext.MODE_PRIVATE);
        String savedText = sPref.getString(key_, "");
        return savedText;
    }

    static final String decode_str(final String in) {
        String working = in;
        int index;
        index = working.indexOf("\\u");
        while (index > -1) {
            int length = working.length();
            if (index > (length - 6)) break;
            int numStart = index + 2;
            int numFinish = numStart + 4;
            String substring = working.substring(numStart, numFinish);
            int number = Integer.parseInt(substring, 16);
            String stringStart = working.substring(0, index);
            String stringEnd = working.substring(numFinish);
            working = stringStart + ((char) number) + stringEnd;
            index = working.indexOf("\\u");
        }
        return working;
    }
}
