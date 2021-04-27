package ru.astakhovmd.commander;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class loginxolop extends AppCompatActivity {
    public EditText fioform,telform;






    @Override
    protected void onResume() {
        super.onResume();

    }
    @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginxolop);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        fioform = findViewById(R.id.editText);
        telform = findViewById(R.id.editText3);

        //This method is called to notify you that, within s,
        // the count characters beginning at start have just replaced old text that had length before.
        // It is an error to attempt to make changes to s from this callback.
        final TextWatcher tellistner = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() < 2) {
                    telform.setText("8(");
                    telform.setSelection(2);
                }
                if (s.length() == 5 && count > before) {
                    telform.setText(s + ")");
                    telform.setSelection(6);
                }
                if (s.length() == 9 && count > before) {
                    telform.setText(s + "-");
                    telform.setSelection(10);
                }
                if (s.length() == 12 && count > before) {
                    telform.setText(s + "-");
                    telform.setSelection(13);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        telform.addTextChangedListener(tellistner);
    }

    //автовход
    SharedPreferences sPref;
    public String load(String arr, String slot) {
        sPref = getSharedPreferences(arr, MODE_PRIVATE);
        if (sPref.contains(slot)) {
            return sPref.getString(slot, "");
        }
        return "";
    }
    public void Transd(View view){
        EditText accesskey;
        accesskey = findViewById(R.id.compin);
        fioform = findViewById(R.id.editText);
        telform = findViewById(R.id.editText3);
        Spinner ageform= findViewById(R.id.spinner2);
        String fio,tel;
        int age, pos = ageform.getSelectedItemPosition();
        fio=fioform.getText().toString();
        tel=telform.getText().toString();
        String key = (accesskey.getText().toString());
        if(key.length()==8&&fio.length()>3&&tel.length()==15&&pos!=0){
            age=pos+5;
        reg dd = new reg();
        dd.execute(key,fio,age+"",tel);
        }
    }


    private class reg extends AsyncTask<String, Void, String> {

        boolean hasConnection() {
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiInfo;
            wifiInfo = cm.getActiveNetworkInfo();
            return wifiInfo != null && wifiInfo.isConnected();
        }
        @Override
        protected String doInBackground(String... s) {
            String a;
            a = "";

            if (hasConnection()) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("lagID",load("myapp","lagID"));
                    json.put("name",s[1]);
                    json.put("age",s[2]);
                    json.put("tel",s[3]);
                    json.put("key",s[0]);
                    String url = "https://camps.astachov.ru/reg.php?json_reg="+URLEncoder.encode(json.toString(), "UTF-8");
                    HttpURLConnection connect = (HttpURLConnection) new URL(url).openConnection();
                    connect.setUseCaches(false);
                    connect.setAllowUserInteraction(true);
                    connect.connect();
                    connect.setConnectTimeout(7000);

                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            connect.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    br.close();
                    a = sb.toString();
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return a;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            AlertDialog.Builder builder = new AlertDialog.Builder(loginxolop.this);
            try {
                JSONArray arr_json = new JSONArray(result);
                String view = arr_json.get(0).toString();
                String rang = arr_json.get(1).toString();
//                save("myapp","view",arr_json.get(0).toString());
//                save("myapp","rang",arr_json.get(1).toString());
                builder.setTitle("Ответ сервера:")
                        .setMessage("Регистрация пройдена!\n"+"View: "+view+"\nRang:"+rang)
                        .setCancelable(false)
                        .setIcon(R.mipmap.ic1_launcher)
                        .setNegativeButton("ОК",

                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();

            } catch (JSONException e) {
                e.printStackTrace();
                builder.setTitle(R.string.sgw)
                        .setMessage("Ошибка. Попробуйте снова! \n"+result)
                        .setIcon(R.drawable.alert_icon)
                        .setCancelable(false)
                        .setNegativeButton("ОК",

                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }


    }

}


