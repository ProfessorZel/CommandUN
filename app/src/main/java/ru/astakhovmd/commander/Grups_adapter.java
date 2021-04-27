package ru.astakhovmd.commander;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import static java.lang.Integer.parseInt;

public class Grups_adapter extends BaseAdapter {
    private LayoutInflater lInflater;
    private ArrayList<User_profile> objects;


    Grups_adapter(Context context, ArrayList<User_profile> user_list) {
        objects = user_list;
        lInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return objects.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }


    // id по позиции
    @Override
    public long getItemId(int position) {
        return ((User_profile)getItem(position)).id;
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.list_grup, parent, false);
        }

        User_profile user = objects.get(position);
        ((TextView) view.findViewById(R.id.list_name)).setText(user.name);
        ((TextView) view.findViewById(R.id.list_id)).setText("ID: "+user.id);
        ((TextView) view.findViewById(R.id.list_rang)).setText("Отд.: "+user.rang);
        ((TextView) view.findViewById(R.id.list_age)).setText(user.age+" лет.");
        return view;
    }
}
