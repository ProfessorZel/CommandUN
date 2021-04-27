package ru.astakhovmd.commander;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import static java.lang.Integer.parseInt;

/** Класс написания замечаний
 * @author maxim
 */
public class zam extends AppCompatActivity {
    /** Переменная SharedPreferences используеться в
     * {@link zam#load(String, String)} */
    SharedPreferences sPref;

    /** извлекает строку из файла насторек
     * @param arr имя файла настроек
     * @param slot имя настройки в файле arr
     * @return строку или если отсутствует настройка то ""
     */
    public String load(String arr, String slot) {
        sPref = getSharedPreferences(arr, MODE_PRIVATE);
            return sPref.getString(slot, "");

    }
    private ArrayList<User_profile> user_list(){
        sPref = getSharedPreferences("myapp", MODE_PRIVATE);
        String json = sPref.getString("users_json_list","");
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

/** Осуществяет дейтвия на старте
 * @param savedInstanceState обьект Bundle
 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zam);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ArrayList<User_profile> names = user_list();
        Spinner spinner = findViewById(R.id.spinner3);
        Spinner_adapter adapter = new Spinner_adapter(activity, names);
        spinner.setAdapter(adapter);
    }
    /**Вызывает класс отправки сообщений {@link zam.listok} */
    public void sendzam(View view){
         String zam,name;
        zam=((EditText)(findViewById(R.id.editText4))).getText().toString();
         name=((TextView)((Spinner)(findViewById(R.id.spinner3))).getSelectedView().findViewById(R.id.sub_text)).getText().toString().replace(" ","").split(":")[1];;
         if(name.length()>0 && zam.length()>0) {
             listok ff = new listok();
             ff.execute(name, zam);
         }else{
             Toast.makeText(this, "Заполните текст и выбирете получателя", Toast.LENGTH_SHORT).show();
         }
        }
    /**Текущий контекст Activity */
    Context activity = (zam.this);
    /**Класс взаимодействия с сервером в отдельном потоке (Отправка коментария) */
    private class listok extends AsyncTask<String, Void, String> {
        /**Создание и инициализация обьекта ProgressDialog*/
        ProgressDialog p = new ProgressDialog(activity);
        /** Отображает ProgressDialog*/
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p.setMessage("Отправка...");
            p.setIndeterminate(false);
            p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            p.setCancelable(true);
            p.show();
        }

        /** Отправяет серверу (server/zam.php) коментарий в отдельном потоке
         * @param s s[0]- имя, s[1] - текст замечания
         */
        @Override
        protected String doInBackground(String... s) {
            String a;
            JSONObject res;
                try {
                    JSONObject zam = new JSONObject();
                    zam.put("id",parseInt(s[0]));
                    zam.put("zam",s[1]);
                    zam.put("lagID",load("myapp","lagID"));
                    String url = "https://camps.astachov.ru/zam.php?json_zam="+URLEncoder.encode(zam.toString(),"Utf-8");//listok
                    HttpURLConnection connect = (HttpURLConnection) new URL(url).openConnection();
                    connect.setUseCaches(false);
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
                    res = new JSONObject(a);
                    a = res.getString("status");
                } catch (IOException e) {
                    e.printStackTrace();
                    return "Ошибка связи с сервером!";
                } catch (JSONException e) {
                    e.printStackTrace();
                    return "Ошибка ра  сшифровки ответа!";
                }
            return "Ответ сервера->"+a;
        }
        /** */
        @Override
        protected void onPostExecute(String results) {
            super.onPostExecute(results);
            Toast.makeText(activity,results,Toast.LENGTH_LONG).show();

            p.setMessage(results);
p.dismiss();
        }
    }
}
