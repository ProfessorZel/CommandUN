package ru.astakhovmd.commander;


import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
//import android.support.v4.content.LocalBroadcastManager;
//import androidx.appcompat.app.AppCompatActivity; import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import static java.lang.Integer.parseInt;


/**Класс Activity руководителя
 * @author Astachov Maxim */
public class UN extends AppCompatActivity {

    /** Текущий контекст Activity */
    Context activity = (UN.this);


    boolean tab_enabled[];
    /**
     *Cоздание меню из ресурса menumain.xml
     * @param menu обьект типа меню
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menumain, menu);
        return true;
    }
    boolean TB_state = false;
    SharedPreferences sPref;

    private BroadcastReceiver UI_updater = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
          Integer action = intent.getIntExtra("action",0);
          switch (action){
              case 0:
                  ((TextView)findViewById(R.id.text)).setText(load("timetable"));
                  ((TextView)findViewById(R.id.textView5)).setText(load("timetable_author"));
                  break;
              case 1:
                  ArrayList<User_profile> names = user_list();
                  ListView list = findViewById(R.id.list);
                  Grups_adapter adapter_grups = new Grups_adapter(activity, names);
                  list.setAdapter(adapter_grups);
                  Spinner spinner = findViewById(R.id.spinner);
                  Spinner_adapter adapter_spiner = new Spinner_adapter(activity, names);
                  spinner.setAdapter(adapter_spiner);
                  break;
              case 2:
                  TextView textview = findViewById(R.id.textviewun);
                  textview.setText(load("msg"));
                  break;
              case 3:
                  Intent intent16 = new Intent(activity, Login.class);//переход
                  startActivity(intent16);
                  break;
          }

        }
    };
    private void save(String slot, String text) {
        sPref = getSharedPreferences("myapp", MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(slot, text);
        ed.apply();
    }
    public void remove(String slot){
        SharedPreferences sPref;
        sPref = getSharedPreferences("myapp", MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.remove(slot);
        ed.apply();
    }
    private ArrayList<User_profile> user_list(){
        sPref = getSharedPreferences("myapp", MODE_PRIVATE);
        String json = sPref.getString("users_json_list","[]");
        ArrayList<User_profile> names = new ArrayList<>();


        try {
            JSONArray arr = new JSONArray(json);
            for (int i =0; i<arr.length();i++){
                JSONObject user = arr.getJSONObject(i);
                names.add(new User_profile(user.getInt("id"),user.getString("name"),user.getString("rang"),user.getInt("age")));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return names;
    }

    public String load(String slot) {
        sPref = getSharedPreferences("myapp", MODE_PRIVATE);
        return sPref.getString(slot,"");}

    /** Провереряет запущен ли сервис
     * @param servname обьект сервиса
     * @return true если сервис запущен и false если не запущен
     */
    public boolean isMyServiceRunning(Class<?> servname) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (servname.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Производит действия при старте Activity
     * @param savedInstanceState обьект Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_un);
        String name = load("name");
        setTitle(name.length() > 0 ? name:"ПриказПРО");


        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.cancel();
        }

        TabHost tabHost = findViewById(R.id.host);
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("tag1");
        tabSpec.setContent(R.id.tab1);
        tabSpec.setIndicator("Сообщения");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setContent(R.id.tab2);
        tabSpec.setIndicator("Расписание");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag3");
        tabSpec.setContent(R.id.tab3);
        tabSpec.setIndicator("Список");
        tabHost.addTab(tabSpec);

        tabHost.setCurrentTab(0);

        TextView textview = findViewById(R.id.textviewun);
        String text;
        if (!(text = load("msg")).equals("")){
            textview.setText(text);
        }

        if (!(isMyServiceRunning(Updater.class))) {
            Intent intent = new Intent(this, Updater.class);
            startService(intent);

        }
        ListView listView = findViewById(R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3)
            {
                String id = ((TextView) arg1.findViewById(R.id.list_id)).getText().toString().replace("ID: ","");
                String name = ((TextView) arg1.findViewById(R.id.list_name)).getText().toString();
                profile ff = new profile();
                ff.execute(id,name);
            }
        });
        LocalBroadcastManager.getInstance(this).registerReceiver(UI_updater,
                new IntentFilter("new_info_received"));

        NotificationManager mNotificationManager1 =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager1 != null) {
            mNotificationManager1.cancel(102);
            mNotificationManager1.cancel(103);
        }
        Spinner spinner = findViewById(R.id.spinlist);
        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                get_list(view);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        spinner.setOnItemSelectedListener(itemSelectedListener);

        tool_bar(false);
}

    /**Обновляет контент при продолжении работы*/
    @Override
    protected void onResume() {
        super.onResume();
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.cancel();
        }
        NotificationManager mNotificationManager1 =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager1 != null) {
            mNotificationManager1.cancel(102);
            mNotificationManager1.cancel(103);
        }
        ((TextView)findViewById(R.id.text)).setText(load("timetable"));
        ((TextView)findViewById(R.id.textView5)).setText(load("timetable_author"));
        ArrayList<User_profile> names = user_list();
        ListView list = findViewById(R.id.list);
        Grups_adapter adapter_grups = new Grups_adapter(activity, names);
        list.setAdapter(adapter_grups);
        Spinner spinner = findViewById(R.id.spinner);
        Spinner_adapter adapter_spiner = new Spinner_adapter(activity, names);
        spinner.setAdapter(adapter_spiner);
        TextView textview = findViewById(R.id.textviewun);
        textview.setText(load("msg"));
        String rang = load("rang");
        boolean[] acc =new boolean[10];
        Arrays.fill(acc, Boolean.FALSE);
        switch (rang){
            case "H":
                Arrays.fill(acc, Boolean.TRUE);
                break;
            case "K":
                Arrays.fill(acc, Boolean.TRUE);
                acc[0] = false;
                break;
            case "KM":
                Arrays.fill(acc, Boolean.TRUE);
                acc[0] = false;
                acc[1] = false;
                break;
            case "KS":
                Arrays.fill(acc, Boolean.TRUE);
                acc[0] = false;
                acc[2] = false;
                break;
            case "KT":
                Arrays.fill(acc, Boolean.TRUE);
                acc[0] = false;
                acc[3] = false;
                break;
            case "K1":
                Arrays.fill(acc, Boolean.TRUE);
                acc[0] = false;
                acc[4] = false;
                break;
            case "K2":
                Arrays.fill(acc, Boolean.TRUE);
                acc[0] = false;
                acc[5] = false;
                break;
            case "K3":
                Arrays.fill(acc, Boolean.TRUE);
                acc[0] = false;
                acc[6] = false;
                break;
            case "K4":
                Arrays.fill(acc, Boolean.TRUE);
                acc[0] = false;
                acc[7] = false;
                break;
            case "KP":
                break;
            case "KV":
                break;
            default:

                break;
        }


        findViewById(R.id.CallComand).setEnabled(acc[0]);
        findViewById(R.id.CallComissar).setEnabled(acc[1]);
        findViewById(R.id.CallSport).setEnabled(acc[2]);
        findViewById(R.id.CallTactic).setEnabled(acc[3]);
        findViewById(R.id.CallFirst).setEnabled(acc[4]);
        findViewById(R.id.CallSecond).setEnabled(acc[5]);
        findViewById(R.id.CallThird).setEnabled(acc[6]);
        findViewById(R.id.CallForth).setEnabled(acc[7]);
        findViewById(R.id.Callall).setEnabled(acc[8]);
        findViewById(R.id.CallMail).setEnabled(acc[9]);

        tool_bar(false);
    }
    public  void F5(View v){
        timetable d = new timetable();
        d.execute();
    }
    public void hide_tool_bar(View v){
        if (TB_state){
            tool_bar(false);
        }else{
            tool_bar(true);
        }

    }
    public void Call(View view) {
        tool_bar(false);


        sPref = getSharedPreferences("myapp", MODE_PRIVATE);
        String json = sPref.getString("users_json_list","");

        try {
            JSONArray arr = new JSONArray(json);
        Button v = (Button) view;
        switch (v.getId()) {
            case R.id.CallComand: {
                for (int i =0; i<arr.length();i++) {
                    JSONObject user = arr.getJSONObject(i);
                    String rang = user.getString("rang");
                    Integer id = user.getInt("id");
                    if (rang.equals("K")){
                        Get c = new Get();
                        c.execute(id.toString(), "Вызов от " + load("name")+".", "call");
                    }
                }
                break;
            }
            case R.id.CallComissar: {
                for (int i =0; i<arr.length();i++) {
                    JSONObject user = arr.getJSONObject(i);
                    String rang = user.getString("rang");
                    Integer id = user.getInt("id");
                    if (rang.equals("KM")){
                        Get c = new Get();
                        c.execute(id.toString(), "Вызов от " + load("name")+".", "call");
                    }
                }
                break;
            }
            case R.id.CallTactic: {
                for (int i =0; i<arr.length();i++) {
                    JSONObject user = arr.getJSONObject(i);
                    String rang = user.getString("rang");
                    Integer id = user.getInt("id");
                    if (rang.equals("KT")){
                        Get c = new Get();
                        c.execute(id.toString(), "Вызов от " + load("name")+".", "call");
                    }
                }
                break;
            }
            case R.id.CallSport: {
                for (int i =0; i<arr.length();i++) {
                    JSONObject user = arr.getJSONObject(i);
                    String rang = user.getString("rang");
                    Integer id = user.getInt("id");
                    if (rang.equals("KS")){
                        Get c = new Get();
                        c.execute(id.toString(), "Вызов от " + load("name")+".", "call");
                    }
                }
                break;
            }
            case R.id.CallFirst: {
                for (int i =0; i<arr.length();i++) {
                    JSONObject user = arr.getJSONObject(i);
                    String rang = user.getString("rang");
                    Integer id = user.getInt("id");
                    if (rang.equals("K1")){
                        Get c = new Get();
                        c.execute(id.toString(), "Вызов от " + load("name")+".", "call");
                    }
                }
                break;
            }
            case R.id.CallSecond: {
                for (int i =0; i<arr.length();i++) {
                    JSONObject user = arr.getJSONObject(i);
                    String rang = user.getString("rang");
                    Integer id = user.getInt("id");
                    if (rang.equals("K2")){
                        Get c = new Get();
                        c.execute(id.toString(), "Вызов от " + load("name")+".", "call");
                    }
                }
                break;
            }
            case R.id.CallThird: {
                for (int i =0; i<arr.length();i++) {
                    JSONObject user = arr.getJSONObject(i);
                    String rang = user.getString("rang");
                    Integer id = user.getInt("id");
                    if (rang.equals("K3")){
                        Get c = new Get();
                        c.execute(id.toString(), "Вызов от " + load("name")+".", "call");
                    }
                }
                break;
            }
            case R.id.CallForth: {
                for (int i =0; i<arr.length();i++) {
                    JSONObject user = arr.getJSONObject(i);
                    String rang = user.getString("rang");
                    Integer id = user.getInt("id");
                    if (rang.equals("K4")){
                        Get c = new Get();
                        c.execute(id.toString(), "Вызов от " + load("name")+".", "call");
                    }
                }
                break;
            }
            case R.id.CallMail: {
                for (int i =0; i<arr.length();i++) {
                    JSONObject user = arr.getJSONObject(i);
                    String rang = user.getString("rang");
                    Integer id = user.getInt("id");
                    if (rang.equals("KP")){
                        Get c = new Get();
                        c.execute(id.toString(), "Вызов от " + load("name")+".", "call");
                    }
                }
                break;
            }
            case R.id.Callall: {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity); //Alert Dialog
                builder.setTitle("Общий вызов?")
                        .setMessage("Вызвать всех?\n Все участники лагеря получат команду вызова!")
                        .setCancelable(true)
                        .setNeutralButton("ОТМЕНА",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("Вызов",

                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Get c = new Get();
                                        c.execute("-2", "Вызов от " + load("name")+".", "call");
                                        dialog.cancel();
                                    }
                                })
                .setPositiveButton("ОТМЕНА",

                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

                break;
            }
            default: {
                Toast.makeText(this,"Ошибка определения VIEW", Toast.LENGTH_LONG).show();
            }
        }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    /**Обновление надписи на кнопке отправки
     * @param view обьект вызывающий класс
     */
    public void updateorsend(View view) {
      Button buttononstartprof = findViewById(R.id.Senamsg);
        buttononstartprof.setText("Отправить");
    }

    /** Обработка нажатия на кнопку "Отправить" - отправка или вызов адресата
     * @param view обьект вызывающий класс
     */
    public void Send(View view) {
        Spinner spinner = findViewById(R.id.spinner); //селектор пользователей
        final String selected = ((TextView)spinner.getSelectedView().findViewById(R.id.sub_text)).getText().toString().replace(" ","").split(":")[1];
        final String  name_sel = ((TextView)spinner.getSelectedView().findViewById(R.id.spiner_name)).getText().toString();
        Button buttononstartprof = findViewById(R.id.Senamsg); //кнопка отправки и вызова
        EditText edit = findViewById(R.id.editText2); //поле ввода сообщения
        String msg = edit.getText().toString();
        buttononstartprof.setText(R.string.go);
        if (msg.length()>0){
        Get call = new Get();
        call.execute(selected, msg);
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(activity); //Alert Dialog
            builder.setTitle("Вызов - ("+selected+", "+name_sel+")")
                    .setMessage("Вызвать? ")
                    .setCancelable(true)
                    .setNegativeButton("Отмена",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .setPositiveButton("Вызов",

                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Get msger = new Get();
                                    msger.execute(selected, "Персональный вызов","call"); // "to","msg","call" заготовка для коментированного вызова
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();

        }
        edit.setText(""); //поле ввода сообщения к ""
    }

    /** Переход в {@link Marks} (Выставление оценок за курс)
     * @param item пункт меню вызывающий метод
     */
    public void mark(MenuItem item){
        Intent intent99 = new Intent(activity, marks_settings.class);//переход
        startActivity(intent99);
    }

    /** Переход в {@link loginxolop} (Добавление нового курсанта)
     * @param item пункт меню вызывающий метод
     */
    public void adduser(MenuItem item){
        Intent intent99 = new Intent(activity, loginxolop.class);//переход
        startActivity(intent99);
    }

    /** Переход в {@link zam} (Коментарий курсанту)
     * @param item пункт меню вызывающий метод
     */
    public void comm(MenuItem item){
        Intent intent99 = new Intent(activity, zam.class);//переход
        startActivity(intent99);
    }

    /** Выход из приожения. Удаление регистрационных данных из системы.
     * Пареметры name, rang, lagID, view, msg будут стерты
     * @param item пункт меню вызывающий метод
     */
    public void regout(MenuItem item){
        remove("name");
        remove("rang");
        remove("visit_time");
        remove("lagID");
        remove("view");
        remove("msg");
        remove("timetable");
        remove("timetable_author");
        remove("users_json_list");
        remove("ms_tmp_marks");
        Intent intent9 = new Intent(activity, Login.class);//переход
        startActivity(intent9);
    }
    public void myidgo (MenuItem d){
        profile sd = new profile();
        sd.execute(load("id"),load("name"));
    }



    public void get_list(View v){
        int select = ((Spinner)findViewById(R.id.spinlist)).getSelectedItemPosition();//.getSelectedItem().toString();
        ArrayList<User_profile> users = user_list();
        ListView list = findViewById(R.id.list);
        ArrayList<User_profile> names = new ArrayList<>();
        ArrayList<String> s = new ArrayList<>();
        s.add("H");
        s.add("K");
        s.add("KM");
        s.add("KS");
        s.add("KT");
        s.add("K1");
        s.add("K2");
        s.add("K3");
        s.add("K4");

        switch (select){
            case 1:
                for(int i=0;i<users.size();i++){
                    User_profile user = users.get(i);
                    if (user.rang.equals("P1")){
                        names.add(user);
                    }
                }
                break;
            case 2:
                for(int i=0;i<users.size();i++){
                    User_profile user = users.get(i);
                    if (user.rang.equals("P2")){
                        names.add(user);
                    }
                }
                break;
            case 3:
                for(int i=0;i<users.size();i++){
                    User_profile user = users.get(i);
                    if (user.rang.equals("P3")){
                        names.add(user);
                    }
                }
                break;
            case 4:
                for(int i=0;i<users.size();i++){
                    User_profile user = users.get(i);
                    if (user.rang.equals("P4")){
                        names.add(user);
                    }
                }
                break;
            case 5:
                for(int i=0;i<users.size();i++){
                    User_profile user = users.get(i);

                    if (s.contains(user.rang)){
                        names.add(user);
                    }
                }
                break;
                default:
                    names = users;
                    break;

        }
        Grups_adapter adapter_grups = new Grups_adapter(activity, names);
        list.setAdapter(adapter_grups);

    }

    public void editTimetable(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(this);
        edittext.setText(load( "timetable"));
        alert.setTitle("Редактирование расписания");

        alert.setView(edittext);

        alert.setPositiveButton("Отправить", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                String TextValue = edittext.getText().toString();
                if (TextValue.length()>0){
                timetable d = new timetable();
                d.execute(TextValue);
                }
            }
        });

        alert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        alert.show();
    }
    public void hide_view(View v,boolean vis){
        if (v!=null){
            if (vis){
                if (v.isEnabled()){
                    v.setVisibility(View.VISIBLE);
                }
            }else{
                v.setVisibility(View.GONE);
            }
        }
    }
    public void tool_bar(boolean visible){
        TB_state = visible;
        hide_view(findViewById(R.id.CallComand),visible);
        hide_view(findViewById(R.id.CallComissar),visible);
        hide_view(findViewById(R.id.CallSport),visible);
        hide_view(findViewById(R.id.CallTactic),visible);
        hide_view(findViewById(R.id.CallFirst),visible);
        hide_view(findViewById(R.id.CallSecond),visible);
        hide_view(findViewById(R.id.CallThird),visible);
        hide_view(findViewById(R.id.CallForth),visible);
        hide_view(findViewById(R.id.Callall),visible);
        hide_view(findViewById(R.id.CallMail),visible);
    }
    /** Отправка сообщений серверу (server/msg.php) в отдельном потоке*/
    public class Get extends AsyncTask<String, Void, String> {
        /**иницаизация нового обьекта ProgressDialog в текущем Activity */
        ProgressDialog p = new ProgressDialog(activity);
        /**указатель на кнопку отправки или вызова*/
        Button button = (Button) findViewById(R.id.Senamsg);
        /** показывает ProgresDialog и делает кнопку не активной*/
        protected void onPreExecute() {

            button.setEnabled(false);
            p.setMessage("Отправка сообщения...");
            p.setIndeterminate(false);
            p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            p.setCancelable(false);
            p.show();
        }

        /** Взаимодействие в потоке с msg.php
         * @param Strings Strings[0]-обязательно имя пользователя назначения, Strings[1]-обязательно текст сообщения (если содержит "call" то будет заменено на "саll"), Strings[2]-опционно маркер вызова ("call" / нет)
         * использует {@link UN#hasConnection()} для определения есть связь или нет
         * @return текст ответа сервера
         */
        @Override
        protected String doInBackground(String... Strings) {
            String a;

            a = "";
            try {
                if(Strings.length >= 2){
                    if (Strings[0].length() > 0) {
                        Strings[1] = Strings[1].replaceAll("call","саll");
                        //заготовка для коментированного вызова
                        if (hasConnection()) {
                            JSONObject msg = new JSONObject();
                            msg.put("lagID",load("lagID"));
                            msg.put("id",load("id"));
                            msg.put("to_id",parseInt(Strings[0]));
                            msg.put("msg",Strings[1]);
                            if (Strings.length == 3){
                                if(Strings[2].equals("call")){
                                    msg.put("iscall", true);
                                }else{
                                    msg.put("iscall",false);
                                }
                            }else{
                                msg.put("iscall",false);
                            }

                            String url = "https://camps.astachov.ru/msg.php?json_msg="+URLEncoder.encode(msg.toString(),"utf8");
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
                        } else {

                            a = "Error ,No Internet...\r\n";
                        }
                    }}
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


            String msgfile;
            if (!((msgfile = load( "msg")).equals(""))) {
                if (msgfile.length() > 5000) {
                    msgfile = msgfile.substring(0, 5000);
                }
            }

            TextView textviewonstart = findViewById(R.id.textviewun);
            if (result.length() > 0) {

                Calendar cal = Calendar.getInstance();
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat sdf = new SimpleDateFormat("HH-mm-ss");
                String time = sdf.format(cal.getTime());
                String textus = "[" + time + "]" + result + msgfile;
                save( "msg", textus);

                textviewonstart.setText(textus);
            } else {
                textviewonstart.setText(msgfile);
            }


            p.dismiss();
            button.setEnabled(true);
        }


    }

    /**Проверяет есть ли подключение к Internet
     * @return true - подключено, false- не подключено
     */
    public boolean hasConnection() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo;

        if (cm != null) {
            wifiInfo = cm.getActiveNetworkInfo();
            return wifiInfo != null && wifiInfo.isConnected();
        }
        return false;
    }

    /**Получение личного дела курсанта с сервера в отдельном потоке */
    private class profile extends AsyncTask<String, Void, String> {
        /**иницаизация нового обьекта ProgressDialog в текущем Activity */
        ProgressDialog p = new ProgressDialog(activity);
        /**Создает ProgressDialog*/
        protected void onPreExecute() {
            p.setMessage("Получение Л/Д...");
            p.setIndeterminate(false);
            p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            p.setCancelable(true);
            p.show();
        }

        /**Отправляет запрос серверу и получает личное дело
         * @param str строка вида группа:фио
         * @return личное дело курсанта
         */
        @Override
        protected String doInBackground(String... str) {
            String a = "";


            if (hasConnection()&& str.length==2) {
                try {
                    JSONObject d = new JSONObject();
                    d.put("lagID",load("lagID"));
                    d.put("id",parseInt(str[0]));
                    d.put("name",str[1]);

                    String url = "https://camps.astachov.ru/myid.php?json_myid="+URLEncoder.encode(d.toString(), "UTF-8");
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
                        sb.append(line).append("\n");
                    }
                    br.close();
                    a = sb.toString();
                    a = a.replaceAll("<br>", "\n");
                } catch (IOException | JSONException ignored) {
                    ignored.printStackTrace();
                }

            }
            return a;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try{
                Log.e("profile", result);
                JSONArray arr = new JSONArray(result);
                JSONObject user_data = arr.getJSONObject(0);
                String lk =""
                        .concat("ФИО: ")
                        .concat(user_data.getString("name"))
                        .concat("\n")
                        .concat("Ранг: ")
                        .concat(user_data.getString("rang"))
                        .concat("\n")
                        .concat("Возраст: ")
                        .concat(user_data.getString("age"))
                        .concat("\n")
                        .concat("Телефон: ")
                        .concat(user_data.getString("tel"))
                        .concat("\n")
                        .concat("ID:")
                        .concat(user_data.getString("id"))
                        .concat("\n")
                        .concat("Web-пароль (beta):")
                        .concat(user_data.getString("web_pass"))
                        .concat("\n")
                        .concat("Версия: ")
                        .concat(user_data.getString("last_visit"))
                        .concat("\n")
                        .concat("Особые отметки: \n")
                        .concat(user_data.getString("sm"));
                LinearLayout ll = new LinearLayout(activity);
                ll.setOrientation(LinearLayout.VERTICAL);

                final ListView listView= new ListView(activity);
                ArrayList<Courses_marks_ID> marks_list = new ArrayList<>();
                if (!arr.isNull(1)) {
                try {
                    JSONArray marks = arr.getJSONArray(1);
                    for (int i =0; i<marks.length();i++){
                        JSONObject mark = marks.getJSONObject(i);
                        try{
                            marks_list.add(new Courses_marks_ID(mark.getInt("id"), mark.getString("date"), mark.getString("author"), mark.getString("name"), mark.getString("mark")));
                        }catch (JSONException ignored){
                        }
                    }
                }catch (JSONException ignored){

                }
                }
                Courses_marks_adapter ad = new Courses_marks_adapter(activity,marks_list);
                listView.setAdapter(ad);
                TextView textView = new TextView(activity);
                textView.setPadding(20,5,20,20);
                textView.setText(lk);
                textView.setTextSize(16);
                textView.setTextColor(Color.BLACK);
                ll.addView(textView);
                ll.addView(listView);
                AlertDialog.Builder builder = new AlertDialog.Builder(activity); //Alert Dialog
                builder.setTitle("Личное дело")
                        .setView(ll)

                        .setCancelable(true)
                        .setPositiveButton("ОК",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }catch (JSONException e){
                e.printStackTrace();

                AlertDialog.Builder builder = new AlertDialog.Builder(activity); //Alert Dialog
                builder.setTitle("Личное дело")
                        .setMessage("Не удалось получить.\n "+result)
                        .setCancelable(true)
                        .setPositiveButton("ОК",

                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
            p.dismiss();
        }


    }


    /**Поучает расписание с сервера в отдельном потоке*/
    class timetable extends AsyncTask<String, Void, String> {
        /**иницаизация нового обьекта ProgressDialog в текущем Activity */
        ProgressDialog p = new ProgressDialog(activity);
        /**Отображает ProgressDialog и делает кнопку не активной */
        protected void onPreExecute() {

            Button f;
            f = findViewById(R.id.f51);
            f.setEnabled(false);

            p.setMessage("Получение расписания...");
            p.setIndeterminate(false);
            p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            p.setCancelable(false);
            p.show();
        }

        /**Получает расписание с сервера(server/timetable.php) в отдельном потоке */
        @Override
        protected String doInBackground(String... s) {

            String a;
            a = "";

            if (hasConnection()) {
                try {
                    JSONObject zam = new JSONObject();
                    zam.put("lagID",load("lagID"));
                    if (s.length>0){
                        zam.put("new_timetable",s[0]);
                        zam.put("id",load("id"));
                    }
                    String url = "https://camps.astachov.ru/timetable.php?json_timetable="+URLEncoder.encode(zam.toString(),"Utf-8");//timetable
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
                        sb.append(line).append("\n");
                    }
                    br.close();
                    a = sb.toString();
                } catch (IOException ignored) {
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            return a;
        }
        /**Обновляет контент TextView с расписанием или загружает из памяти предыдущее
         * @param result текст расписания полученого с сервера
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            TextView texte = findViewById(R.id.text);
            TextView author_view = findViewById(R.id.textView5);
            String author;
            try {
                JSONObject rep = new JSONObject(result);
                result = rep.getString("timetable_text");
                author = rep.getString("timetable_author");

                save("timetable",result);
                save("timetable_author",author);

                texte.setText(result);
                author_view.setText(author);


                if (rep.has("status")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity); //Alert Dialog
                    builder.setTitle("Результат сохранения")
                            .setMessage(rep.getString("status"))
                            .setCancelable(true)
                            .setPositiveButton("ОК",

                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();}
            } catch (JSONException e) {
                result = "Из памяти!\n"+load("timetable");
                texte.setText(result);
                author = load("timetable_author");
                author_view.setText(author);
                e.printStackTrace();
            }
            p.dismiss();
            Button f;
            f = findViewById(R.id.f51);
            f.setEnabled(true);
        }


    }

    /** Выполняет действия при разрушении Activity*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!(isMyServiceRunning(Updater.class))) {
            Intent intent = new Intent(this, Updater.class);
            startService(intent);

        }
    }

    @Override
    public void onBackPressed() {
        tool_bar(false);
    }
}