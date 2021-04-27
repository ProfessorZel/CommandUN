package ru.astakhovmd.commander;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class marks_settings extends AppCompatActivity {
    static int MARKS = 1;
    static int ERROR = -99;
    static int ERROR_SEND = -98;

    SharedPreferences sPref;
    public String load( String slot) {
        sPref = getSharedPreferences("myapp", MODE_PRIVATE);
        return sPref.getString(slot, "");

    }
    private void save(String slot, String text) {
        sPref = getSharedPreferences("myapp", MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(slot, text);
        ed.apply();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mark_settings);

        SeekBar seekBar = findViewById(R.id.input_type);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String d="";
                switch (progress){
                    case 0:
                        d="Текст";
                        break;
                    case 1:
                        d="Числа";
                        break;
                    case 2:
                        d="Зач./незач.";
                        break;
                }
                ((TextView)findViewById(R.id.typeof)).setText(d);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        get_courses_list f = new get_courses_list();
        f.execute();

    }

    public void Createcourse(View v){
        Intent settings = new Intent(this, Marks.class);
       int progress = ((SeekBar)findViewById(R.id.input_type)).getProgress();
       String course_name = ((EditText) findViewById(R.id.course_name)).getText().toString();
       if (course_name.length()==0){((EditText) findViewById(R.id.course_name)).setError("Заполните поле!");
           return;}
       Boolean rating = ((CheckBox) findViewById(R.id.israting)).isChecked();

       settings.putExtra("input_type",progress);
        settings.putExtra("course_name",course_name);
        settings.putExtra("rating",rating);
       startActivityForResult(settings,MARKS);
}
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MARKS){
            if (resultCode == Activity.RESULT_OK){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Ура курс отправлен!")
                        .setCancelable(false)
                        .setNegativeButton("ОK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }

            if (resultCode == ERROR){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Ошибка открытия курса!\nПопробуйте загрузить снова!")
                        .setCancelable(false)
                        .setNegativeButton("ОK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
            if (resultCode == ERROR_SEND){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Ошибка отправки курса! Попробуйте позже!")
                        .setCancelable(false)
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
    }

    //////////////////////////////////////////////////
    public boolean hasConnection() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo;
        if (cm != null) {
            wifiInfo = cm.getActiveNetworkInfo();
            return wifiInfo != null && wifiInfo.isConnected();
        }
        return false;
    }

    public class get_courses_list extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... f) {
            String a;

            a = "";
            try {
                if (hasConnection()) {
                            JSONObject msg = new JSONObject();
                            msg.put("lagID",load("lagID"));

                            String url = "https://camps.astachov.ru/course.php?get_course_list="+URLEncoder.encode(msg.toString(),"utf8");
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
                                sb.append(line).append("\r\n");
                            }
                            br.close();
                            a = sb.toString();
                            if (a.length() == 0) {
                                a = "Ошибка связи с сервером...\r\n";
                            }
                        }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return a;
        }

        /** Сохраняет ответ сервера и обновляет содержимое TextView
         * @param result ответ сервера из doInBackground.
         * использует {@link UN#hasConnection()} для определения есть связь или нет
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //String course_list = load("courses_json_list");
            try {
                new JSONArray(result);
                save("courses_json_list",result);
            } catch (JSONException ignored) {

            }
            try {
                JSONArray arr;
                try {
                    arr = new JSONArray(load("courses_json_list"));
                }catch (JSONException e){
                    arr = new JSONArray();
                }
                ArrayList<Course_info> courses = new ArrayList<>();
                for (int i = 0; i<arr.length();i++) {
                    JSONObject course = arr.getJSONObject(i);
                    courses.add(new Course_info(course.getInt("id"),course.getString("name"),course.getString("author")));
                }
                JSONArray minus_id;
                try {
                    minus_id = new JSONArray(load("minus_courses_json_list"));
                }catch(JSONException e){
                    minus_id = new JSONArray();
                }
                for (int i = 0; i<minus_id.length();i++) {
                    JSONObject course = minus_id.getJSONObject(i);
                    Course_info co = new Course_info(course.getInt("id"),course.getString("name"),course.getString("author"));
                    co.setLoaded(true);
                    courses.add(co);
                }
                Courses_info_adapter adapter = new Courses_info_adapter(marks_settings.this,courses);
                ListView list = findViewById(R.id.courses_info_list);
                list.setAdapter(adapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            JSONArray arr;
            try {
                arr = new JSONArray(load("courses_json_list"));
            }catch (JSONException e){
                arr = new JSONArray();
            }
            ArrayList<Course_info> courses = new ArrayList<>();
            for (int i = 0; i<arr.length();i++) {
                JSONObject course = arr.getJSONObject(i);
                courses.add(new Course_info(course.getInt("id"),course.getString("name"),course.getString("author")));
            }
            JSONArray minus_id;
            try {
                minus_id = new JSONArray(load("minus_courses_json_list"));
            }catch(JSONException e){
                minus_id = new JSONArray();
            }
            for (int i = 0; i<minus_id.length();i++) {
                JSONObject course = minus_id.getJSONObject(i);
                Course_info co = new Course_info(course.getInt("id"),course.getString("name"),course.getString("author"));
                co.setLoaded(true);
                courses.add(co);
            }
            Courses_info_adapter adapter = new Courses_info_adapter(marks_settings.this,courses);
            ListView list = findViewById(R.id.courses_info_list);
            list.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
