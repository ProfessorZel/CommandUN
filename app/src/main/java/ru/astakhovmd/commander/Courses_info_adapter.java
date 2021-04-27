package ru.astakhovmd.commander;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

import static android.content.Context.MODE_PRIVATE;
import static java.lang.Integer.parseInt;

public class Courses_info_adapter extends BaseAdapter {
    static int MARKS = 1;
    private LayoutInflater lInflater;
    private ArrayList<Course_info> objects;
    private Context context;

    public boolean hasConnection() {
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo;

        if (cm != null) {
            wifiInfo = cm.getActiveNetworkInfo();
            return wifiInfo != null && wifiInfo.isConnected();
        }
        return false;
    }

    private void save(String slot, String text) {
        SharedPreferences sPref = context.getSharedPreferences("myapp", MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(slot, text);
        ed.apply();
    }

    public String load(String slot) {
        SharedPreferences sPref = context.getSharedPreferences("myapp",MODE_PRIVATE);
        return sPref.getString(slot,"");}
    public Boolean has_course(String slot) {
        SharedPreferences sPref = context.getSharedPreferences("myapp",MODE_PRIVATE);
        return sPref.contains("course_data_"+slot);
    }

    public Courses_info_adapter(Context _context, ArrayList<Course_info> products) {
        objects = products;
        lInflater = (LayoutInflater) _context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        context = _context;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return objects.get(position).id;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null){
            view = lInflater.inflate(R.layout.row_courses_info, parent, false);
        }

        ((TextView) view.findViewById(R.id.textViewID)).setText(String.format("%d", objects.get(position).id));
        ((TextView) view.findViewById(R.id.textViewName)).setText(objects.get(position).name);
        ((TextView) view.findViewById(R.id.textViewAuthor)).setText(objects.get(position).author);
        View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                final String id = ((TextView) v.findViewById(R.id.textViewID)).getText().toString();
                if (parseInt(id)>0){
                    if (has_course(id)){
                        AlertDialog.Builder builder = new AlertDialog.Builder(context); //Alert Dialog
                        builder.setTitle("Обновить курс?")
                                .setMessage("Курс ID:"+id)
                                .setCancelable(true)
                                .setPositiveButton("ОК",

                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int L_id) {
                                                Course_get dd = new Course_get();
                                                dd.execute(id);
                                                dialog.cancel();
                                            }
                                        })
                                .setNegativeButton("Отмена",

                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int L_id) {
                                                dialog.cancel();
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(context); //Alert Dialog
                        builder.setTitle("Получить курс?")
                                .setMessage("Курс ID:"+id)
                                .setCancelable(true)
                                .setPositiveButton("ОК",

                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int L_id) {
                                                Course_get dd = new Course_get();
                                                dd.execute(id);
                                                dialog.cancel();
                                            }
                                        })
                                .setNegativeButton("Отмена",

                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int L_id) {
                                                dialog.cancel();
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }}
                return false;
            }
        };
        view.setOnLongClickListener(longClickListener);
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String id = ((TextView) v.findViewById(R.id.textViewID)).getText().toString();

                    if (has_course(id)){
                        Intent settings = new Intent(context, Marks.class);
                        settings.putExtra("id",parseInt(id));
                        ((marks_settings)context).startActivityForResult(settings,MARKS);
                    }else{
                        if (parseInt(id)>0){
                        AlertDialog.Builder builder = new AlertDialog.Builder(context); //Alert Dialog
                        builder.setTitle("Получить курс?")
                                .setMessage("Курс ID:"+id)
                                .setCancelable(true)
                                .setPositiveButton("ОК",

                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int L_id) {
                                                Course_get dd = new Course_get();
                                                dd.execute(id);
                                                dialog.cancel();
                                            }
                                        })
                                .setNegativeButton("Отмена",

                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int L_id) {
                                                dialog.cancel();
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }}
            }
        };
        view.setOnClickListener(clickListener);

        if (objects.get(position).loaded){
            view.setBackgroundColor(context.getResources().getColor(R.color.loadbackground));
            //btn.setVisibility(View.VISIBLE);
        }else{
            view.setBackgroundColor(context.getResources().getColor(R.color.trans));
            //btn.setVisibility(View.GONE);
        }
        return view;

    }
    public class Course_get extends AsyncTask<String, String, String[]> {

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

        }

        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String[] doInBackground(String... Strings) {
            String a;
            onProgressUpdate("Формирование запроса!");
            a = "";
            try {
                        //заготовка для коментированного вызова
                        if (hasConnection()) {
                            JSONObject msg = new JSONObject();
                            msg.put("lagID",load("lagID"));
                            msg.put("id",parseInt(Strings[0]));

                            String url = "https://camps.astachov.ru/course.php?get_course="+ URLEncoder.encode(msg.toString(),"utf8");
                            HttpURLConnection connect = (HttpURLConnection) new URL(url).openConnection();
                            onProgressUpdate("Подключение!");
                            connect.setUseCaches(false);
                            connect.setAllowUserInteraction(true);
                            connect.connect();
                            connect.setConnectTimeout(7000);

                            BufferedReader br = new BufferedReader(new InputStreamReader(
                                    connect.getInputStream()));
                            StringBuilder sb = new StringBuilder();
                            String line;
                            onProgressUpdate("Получение ответа!");
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
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            onProgressUpdate("Расшифровка ответа!");
            String[] arr = new String[2];
            arr[0]=Strings[0];
            arr[1]=a;
            return arr;
        }

        /** Сохраняет ответ сервера и обновляет содержимое TextView
         * @param result ответ сервера из doInBackground.
         * использует {@link UN#hasConnection()} для определения есть связь или нет
         */
        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            if (result[1].length()>0) {
                try {
                    new JSONArray(result[1]);
                onProgressUpdate("Успешно!");
                save("course_data_" + result[0], result[1]);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context); //Alert Dialog
                    builder.setTitle("Получены данные курса!")
                            .setMessage("Курс ID:"+result[0])
                            .setCancelable(true)
                            .setPositiveButton("ОК",

                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("course_info",result[1]);
                    onProgressUpdate("Ошибка!");
                }
            }
        }

    }

}
