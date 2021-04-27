package ru.astakhovmd.commander;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

public class Marks extends AppCompatActivity {
    ListView myList;
    static int ERROR = -99;
    static int ERROR_SEND = -98;


    SharedPreferences sPref;

    public String load(String arr, String slot) {
        sPref = getSharedPreferences(arr, MODE_PRIVATE);
        return sPref.getString(slot, "");

    }
    public Integer getminusID() {
        sPref = getSharedPreferences("myapp", MODE_PRIVATE);
        Integer id = sPref.getInt("minus_task_id",-1);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt("minus_task_id", id-1);
        ed.apply();
        return id;
    }
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
    public ArrayList<String> loadmarks() {
        try {
            ArrayList<String> result = new ArrayList<>();
            sPref = getSharedPreferences("myapp", MODE_PRIVATE);
            JSONArray d = new JSONArray(sPref.getString("ms_tmp_marks", "[]"));
            for (int i = 0; i < d.length(); i++){
                result.add(d.getString(i));
            }
            SharedPreferences.Editor ed = sPref.edit();
            ed.remove("ms_tmp_marks");
            ed.apply();
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    private void savemark(String arr) {


            if (s_course_id==0){
                try {
                Integer task_count = getminusID();
                String t_name = new JSONArray(arr).getJSONObject(0).getString("name");
                String t_author = new JSONArray(arr).getJSONObject(0).getString("author");
                save("course_data_"+task_count,arr);

                        JSONArray arry;
                        try {
                            arry = new JSONArray(load("myapp", "minus_courses_json_list"));
                        }catch (JSONException ignored){
                            arry = new JSONArray();
                        }

                        JSONObject task = new JSONObject();
                        task.put("id",task_count);
                        task.put("name",t_name);
                        task.put("author",t_author);
                        arry.put(task);
                        save("minus_courses_json_list",arry.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else{
                save("course_data_"+s_course_id,arr);

            }

        setResult(ERROR_SEND);
        finish();
    }
    public boolean hasConnection() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo;
        if (cm != null) {
            wifiInfo = cm.getActiveNetworkInfo();
            return wifiInfo != null && wifiInfo.isConnected();
        }
        return false;
    }

    private ArrayList<Mark_item> course_marks(int id){
        sPref = getSharedPreferences("myapp", MODE_PRIVATE);
        String json = sPref.getString("course_data_"+id,"");
        ArrayList<Mark_item> names = new ArrayList<>();
        try {
            JSONArray course = new JSONArray(json);
            int input_type = course.getJSONObject(0).getInt("input_type");
            JSONArray marks = course.getJSONArray(1);
            for (int i =0; i<marks.length();i++){
                JSONObject mark_item = marks.getJSONObject(i);
                User_profile user = new User_profile(mark_item.getInt("id"),mark_item.getString("name"),mark_item.getString("rang"),mark_item.getInt("age"));
                switch (input_type){
                    case 0:
                        names.add(new Mark_item(user,(mark_item.isNull("mark"))?null:mark_item.getString("mark"),null,false));
                        break;
                    case 1:
                        names.add(new Mark_item(user,null,(mark_item.isNull("mark"))?null:mark_item.getDouble("mark"),false));
                        break;
                    case 2:
                        names.add(new Mark_item(user,null,null, (mark_item.isNull("mark"))?false:mark_item.getBoolean("mark")));
                        break;
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
            setResult(ERROR);
            finish();
        }
        return names;
    }
    private ArrayList<String> course_data(int id){
        sPref = getSharedPreferences("myapp", MODE_PRIVATE);
        String json = sPref.getString("course_data_"+id,"");
        ArrayList<String> names = new ArrayList<>();
        try {
            JSONObject course = new JSONArray(json).getJSONObject(0);
            names.add(course.getString("input_type"));
            names.add(course.getString("name"));
            if (course.has("rating")){
                names.add((course.getBoolean("rating"))?"T":"F");
            }else{
                names.add("F");
            }


        } catch (JSONException e) {
            e.printStackTrace();
            setResult(ERROR);
            finish();
        }
        return names;
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

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    Integer s_input_type = 0;
    String s_course_name = "";
    Boolean s_rating = false;
    Integer s_course_id = 0;
    ArrayList<Mark_item> names =new ArrayList<>();

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marks);


        myList = findViewById(R.id.list10);

        Integer id;
        if ((id =getIntent().getIntExtra("id",0))!=0){
            if (id >0){
                Log.i("UN_Marks","id>0,"+id);
                s_course_id = id;
                names = course_marks(s_course_id);
                ArrayList<String> data = course_data(s_course_id);

                s_input_type = parseInt(data.get(0));
                s_course_name = data.get(1);
            }
            if (id < 0){
                Log.i("UN_marks","id<0,"+id);
                s_course_id = id;
                names = course_marks(s_course_id);
                ArrayList<String> data = course_data(s_course_id);

                s_input_type = parseInt(data.get(0));
                s_course_name = data.get(1);
                s_rating = data.get(2).equals("T");

            }
        }else{
            Log.i("UN_marks","id null");
            s_input_type = getIntent().getIntExtra("input_type",0);
            s_course_name =getIntent().getStringExtra("course_name");
            s_rating = getIntent().getBooleanExtra("rating",false);

            ArrayList<User_profile> users = user_list();
            for(int i =0;i<users.size();i++){
                names.add(new Mark_item(users.get(i),null,null,false));
            }
        }

        myList.setAdapter(new Adapter(this,names,s_input_type));
        setTitle(s_course_name);
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void sendmark(View view) {
        View row;

        int nrow=0;
        //сбор всех данных
        for (int i = 0; i < names.size(); i++){
            names.set(i,(Mark_item) myList.getAdapter().getItem(i));
        }

        //сбор возможно не сохраненных данных.
        while ((row = myList.getChildAt(nrow))!=null) {
            CharSequence s1 = ((EditText) row.findViewById(R.id.mark)).getText();
            String imark = (s1==null)?null:s1.toString();
            CharSequence s2 = ((EditText) row.findViewById(R.id.numer)).getText();
            String inum = (s2==null)?null:s2.toString();
            boolean check = ((CheckBox) row.findViewById(R.id.checkbox)).isChecked();
            Integer rownum = parseInt(row.getTag().toString());
            try {

                names.set(rownum, new Mark_item((names.get(rownum).user), imark, (inum==null)?null:parseDouble(inum.replace(",",".")), check));
            }catch (NumberFormatException e){
                names.set(parseInt(row.getTag().toString()), new Mark_item((names.get(rownum).user), imark, null, check));
            }
            nrow++;
        }

        try {
            JSONArray data=new JSONArray();
            JSONArray data1=new JSONArray();
            JSONObject obj;
            obj = new JSONObject();
            JSONObject obj1 = new JSONObject();

            if (s_course_id!=0){
                if (s_course_id >0){
                    obj.put("id",s_course_id);
                    obj.put("lagID",load("myapp","lagID"));
                    obj.put("name",s_course_name);
                    obj.put("input_type",s_input_type);

                    obj1.put("id",s_course_id);
                    obj1.put("lagID",load("myapp","lagID"));
                    obj1.put("name",s_course_name);
                    obj1.put("input_type",s_input_type);
                }
                if (s_course_id < 0){
                    obj.put("name",s_course_name);
                    obj.put("input_type",s_input_type);
                    obj.put("author",load("myapp","name"));
                    obj.put("rating",s_rating);
                    obj.put("lagID",load("myapp","lagID"));

                    obj1.put("name",s_course_name);
                    obj1.put("input_type",s_input_type);
                    obj1.put("author",load("myapp","name"));
                    obj1.put("rating",s_rating);
                    obj1.put("lagID",load("myapp","lagID"));
                }
            }else{
                obj.put("name",s_course_name);
                obj.put("input_type",s_input_type);
                obj.put("author",load("myapp","name"));
                obj.put("rating",s_rating);
                obj.put("lagID",load("myapp","lagID"));

                obj1.put("name",s_course_name);
                obj1.put("input_type",s_input_type);
                obj1.put("author",load("myapp","name"));
                obj1.put("rating",s_rating);
                obj1.put("lagID",load("myapp","lagID"));
            }
        data.put(obj);
            data1.put(obj1);

        JSONArray marks=new JSONArray();
        JSONArray marks1=new JSONArray();
        switch (s_input_type){
            case 0:
                for (int i = 0; i < names.size(); i++){
                    Mark_item it = names.get(i);
                    obj1 = new JSONObject();
                    obj1.put("id",it.user.id);
                    obj1.put("name",it.user.name);
                    obj1.put("rang",it.user.rang);
                    obj1.put("age",it.user.age);
                    if(it.mark_s!=null){
                        if(it.mark_s.length()>0){
                            obj = new JSONObject();
                            obj.put("id",it.user.id);
                            obj.put("name",it.user.name);
                            obj.put("rang",it.user.rang);
                            obj.put("age",it.user.age);
                            obj.put("mark",it.mark_s);
                            obj1.put("mark",it.mark_s);
                            marks.put(obj);
                        }else{
                            obj1.put("mark","");
                        }
                    }else{
                        obj1.put("mark",null);
                    }
                    marks1.put(obj1);

                }
                break;
            case 1:
                for (int i = 0; i < names.size(); i++){
                    Mark_item it = names.get(i);
                    obj1 = new JSONObject();
                    obj1.put("id",it.user.id);
                    obj1.put("name",it.user.name);
                    obj1.put("rang",it.user.rang);
                    obj1.put("age",it.user.age);
                    if(it.mark_i!=null){
                        obj = new JSONObject();
                        obj.put("id",it.user.id);
                        obj.put("name",it.user.name);
                        obj.put("rang",it.user.rang);
                        obj.put("age",it.user.age);
                        obj.put("mark",it.mark_i);
                        obj1.put("mark",it.mark_i);
                        marks.put(obj);
                    }else{
                        obj1.put("mark",null);}
                    marks1.put(obj1);
                }
                break;
            case 2:
                for (int i = 0; i < names.size(); i++){
                    Mark_item it = names.get(i);
                    obj = new JSONObject();
                    obj.put("id",it.user.id);
                    obj.put("name",it.user.name);
                    obj.put("rang",it.user.rang);
                    obj.put("age",it.user.age);
                    obj.put("mark",it.mark_b);
                    marks.put(obj);
                    obj1 = new JSONObject();
                    obj1.put("id",it.user.id);
                    obj1.put("name",it.user.name);
                    obj1.put("rang",it.user.rang);
                    obj1.put("age",it.user.age);
                    obj1.put("mark",it.mark_b);
                    marks1.put(obj1);
                }
                break;
        }
        if (marks.length()==0){
            Toast.makeText(this,"Заполните оценки!",Toast.LENGTH_LONG).show();
        }else{
            data.put(marks);
            data1.put(marks1);
            String sdata = data.toString();
            String sdata1 = data1.toString();
            bil ff = new bil();
            ff.execute(sdata,sdata1);
        }


        } catch (JSONException e) {
            e.printStackTrace();
        }



    }
    public void cancel(View v){
        setResult(RESULT_CANCELED);
        finish();
    }
    private class bil extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... mark_task) {
            //Log.e("bil","send ->"+mark_task[0]+";;;;;"+mark_task[1]);

                if(hasConnection()) {
                    try {
                        String url = "https://camps.astachov.ru/course.php";
                        URL obj = new URL(url);
                        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                        //add reuqest header
                        con.setRequestMethod("POST");
                        con.setRequestProperty("User-Agent", "Mozilla/5.0");
                        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

                        String urlParameters = "json_course="+ URLEncoder.encode(mark_task[0],"utf8");

                        // Send post request
                        con.setDoOutput(true);
                        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                        wr.writeBytes(urlParameters);
                        wr.flush();
                        wr.close();

                        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);

                        }
                        br.close();
                        Log.e("send marks",sb.toString());
                        JSONObject d = new JSONObject(sb.toString());
                        if (!(d.getString("status").equals("OK"))){
                            Log.e("bil_status","save task");
                            savemark(mark_task[1]);
                            return null;
                        }
                    } catch (IOException | JSONException ignored) {
                        ignored.printStackTrace();
                        savemark(mark_task[1]);

                        Log.e("bil_e","save task");
                        return null;
                    }
                }else{
                    savemark(mark_task[1]);
                    Log.e("bil_NI","save task");
                    return null;
                }

            Integer t_id = s_course_id;
            if (t_id>0){
                save("course_data_"+t_id,mark_task[1]);
            }
            if (t_id<0){
                remove("course_data_"+t_id);

            try {
                JSONArray arr = new JSONArray(load("myapp","minus_courses_json_list"));
                for (int n = 0; n<arr.length();n++) {
                    JSONObject course = arr.getJSONObject(n);
                    if (course.getInt("id")==t_id){
                        arr.remove(n);
                    }
                }
                save("minus_courses_json_list",arr.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            }
            setResult(RESULT_OK);
            finish();
            return null;
        }


    }

}