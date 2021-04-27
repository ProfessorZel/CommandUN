package ru.astakhovmd.commander;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class Login extends AppCompatActivity {

    //public EditText accesskey;//для автовхода и входа

    SharedPreferences sPref;
   public boolean save(String arr,String slot, String text) {
        sPref = getSharedPreferences(arr, MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(slot,text);
        ed.apply();
        return true;
    }
    public String load(String arr,String slot) {
        sPref = getSharedPreferences(arr, MODE_PRIVATE);
        if (sPref.contains(slot)) {
            return sPref.getString(slot, "");}
        return "";
    }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        comp();
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onResume() {
        super.onResume();

        comp();
    }

//////////////////////////////////////////////////////////////////////////////////////////////////

    public void comp() {

        String view;
        if(!((view =load("myapp","view")).equals("")) && !((load("myapp","rang")).equals("")) && !((load("myapp","name")).equals("")) &&!((load("myapp","lagID")).equals("")) ) {
            Intent intent = new Intent(Login.this, UN.class);//переход
            startActivity(intent);//переход
            Toast.makeText(this, "Автовход выполнен...", Toast.LENGTH_SHORT).show();
                /*switch (view) {//выбор вариантов перехода
                    case "Tactic":
                        Intent intent0 = new Intent(Login.this, Tactic.class);//переход
                        startActivity(intent0);//переход
                        Toast.makeText(this, "Автовход выполнен...", Toast.LENGTH_SHORT).show();
                        break;
                    case "University":

                        break;
                    case "Komand":
                        Intent intent1 = new Intent(Login.this, Komand1.class);//переход
                        startActivity(intent1);//переход
                        Toast.makeText(this, "Автовход выполнен...", Toast.LENGTH_SHORT).show();
                        break;

                    case "Komissar":
                        Intent intent3 = new Intent(Login.this, Komissar.class);//переход
                        startActivity(intent3);//переход
                        Toast.makeText(this, "Автовход выполнен...", Toast.LENGTH_SHORT).show();
                        break;
                    case "Sport":
                        Intent intent4 = new Intent(Login.this, Sport.class);//переход
                        startActivity(intent4);//переход
                        Toast.makeText(this, "Автовход выполнен...", Toast.LENGTH_SHORT).show();
                        break;
                    case "Kom1":
                        Intent intent5 = new Intent(Login.this, K1.class);//переход
                        startActivity(intent5);//переход
                        Toast.makeText(this, "Автовход выполнен...", Toast.LENGTH_SHORT).show();
                        break;
                    case "Kom2":
                        Intent intent6 = new Intent(Login.this, K2.class);//переход
                        startActivity(intent6);//переход
                        Toast.makeText(this, "Автовход выполнен...", Toast.LENGTH_SHORT).show();
                        break;
                    case "Kom3":
                        Intent intent9 = new Intent(Login.this, K3.class);//переход
                        startActivity(intent9);//переход
                        Toast.makeText(this, "Автовход выполнен...", Toast.LENGTH_SHORT).show();
                        break;
                    case "Kom4":
                        Intent intent11 = new Intent(Login.this, K4.class);//переход
                        startActivity(intent11);//переход
                        Toast.makeText(this, "Автовход выполнен...", Toast.LENGTH_SHORT).show();
                        break;
                    case "Mailer":{
                        Intent intent16 = new Intent(Login.this, Mail.class);//переход
                        startActivity(intent16);
                        Toast.makeText(this, "Автовход выполнен...", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case "Vid":
                        Intent intent12 = new Intent(Login.this, Vid.class);//переход
                        startActivity(intent12);//переход
                        Toast.makeText(this, "Автовход выполнен...", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(this,"NO USER!",Toast.LENGTH_LONG).show();
                        break;

                }*/
            }else{Toast.makeText(this,"Осуществите регистрацию!",Toast.LENGTH_LONG).show();}
         }
///////////////////////////////////////////////////////////////////////////////////////

    public void Trans(View view){
        EditText accesskey = findViewById(R.id.pininput);
        String key = accesskey.getText().toString();
        accesskey = findViewById(R.id.editText5);
        String lagID = accesskey.getText().toString();
        if(key.length()==8 &&lagID.length()>0){
            //accesskey.setText(null);
            save("myapp","lagID",lagID);
            reg dd = new reg();
            dd.execute(key);
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
            builder.setTitle(R.string.sgw)
                    .setMessage(R.string.empty)
                    .setCancelable(true)
                    .setNegativeButton("ОK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class reg extends AsyncTask<String, Void, String> {

        boolean hasConnection() {
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiInfo;
            if (cm != null) {
                wifiInfo = cm.getActiveNetworkInfo();
                return (wifiInfo != null) && wifiInfo.isConnected();
            }
            return false;
        }
        @Override
        protected String doInBackground(String... s) {
            String a;

            if (hasConnection()) {
                try {
                    JSONObject reg_json = new JSONObject();
                    reg_json.put("key",s[0]);
                    reg_json.put("lagID",load("myapp","lagID"));
                    String url = "https://camps.astachov.ru/reg.php?json_reg="+ URLEncoder.encode(reg_json.toString(),"utf8");
                    HttpURLConnection connect = (HttpURLConnection) new URL(url).openConnection();
                    connect.setRequestMethod("GET");
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
                    return a;
                } catch (IOException | JSONException ignored) {
                    ignored.printStackTrace();
                    return "NI";
                }
            }else {return "NI";}

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String responce;
            try {
                JSONArray arr_json = new JSONArray(result);
                if ((arr_json.get(0)).toString().equals("DUB")){
                    responce ="Дублирование пользователей!\n Сообщи администратору лагеря!";
                }else{
                    responce ="Регистрация пройдена!";
                    save("myapp","view",arr_json.get(0).toString());
                    save("myapp","name",arr_json.get(1).toString());
                    save("myapp","rang",arr_json.get(2).toString());
                    save("myapp","id",arr_json.get(3).toString());
                    save("myapp","min_time","5");
                }

            } catch (JSONException e) {
                e.printStackTrace();
                save("myapp","view","");
                save("myapp","name","");
                save("myapp","rang","");
                if (result.equals("NI")){responce="NI";}else{responce="-";}
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
            if (responce.equals("-")) {
                save("myapp","lagID","");
                save("myapp","view","");
                builder.setTitle(R.string.sgw)
                        .setMessage("Access denied!")
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
            }else{
                if (result.equals("NI")){
                    save("myapp","lagID","");
                    save("myapp","view","");
                    builder.setTitle("Ошибка:")
                            .setMessage("Нет соединения с сервером!")
                            .setCancelable(false)
                            .setNegativeButton("ОК",

                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
            }else{
                    builder.setTitle("Ответ сервера:")
                            .setMessage(responce+"\n "+load("myapp","name")+" "+load("myapp","rang"))
                            .setCancelable(false)
                            .setNegativeButton("ОК",

                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            comp();
                                            String manufacturer = "xiaomi";
                                            if(manufacturer.equalsIgnoreCase(android.os.Build.MANUFACTURER)) {
                                                Intent intent = new Intent();
                                                intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                                                startActivity(intent); }
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }

        }


    }
}