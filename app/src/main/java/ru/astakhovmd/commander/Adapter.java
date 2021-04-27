package ru.astakhovmd.commander;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

public class Adapter extends BaseAdapter {
    private LayoutInflater lInflater;
    private ArrayList<Mark_item> objects;
    private int input;

    Adapter(Context context, ArrayList<Mark_item> products,int input_type) {
        objects = products;
        lInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        input = input_type;
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
        return position;
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.listviewedit, parent, false);
        }else{
            CharSequence s1 = ((EditText) view.findViewById(R.id.mark)).getText();
            String mark = (s1==null)?null:s1.toString();
            CharSequence s2 = ((EditText) view.findViewById(R.id.numer)).getText();
            String num = (s2==null)?null:s2.toString();
            boolean check = ((CheckBox) view.findViewById(R.id.checkbox)).isChecked();
            Integer row_num = parseInt(view.getTag().toString());
            switch (input){
                case 0:
                    objects.set(row_num,new Mark_item(objects.get(row_num).user,mark,null,false));
                    break;
                case 1:
                    try {
                        objects.set(row_num, new Mark_item(objects.get(row_num).user, null, (num==null)?null:parseDouble(num.replace(",",".")), false));
                    }catch (NumberFormatException e){
                        objects.set(row_num, new Mark_item(objects.get(row_num).user, null, null, false));
                    }
                    break;
                case 2:
                    objects.set(row_num,new Mark_item(objects.get(row_num).user,null,null,check));
                    break;
            }

        }


        ((TextView) view.findViewById(R.id.textname)).setText(objects.get(position).user.name);
        ((TextView) view.findViewById(R.id.id)).setText(String.format("%s : %d", objects.get(position).user.rang, objects.get(position).user.id));
       Mark_item s = objects.get(position);
        switch (input){
            case 0:
                ((EditText) view.findViewById(R.id.mark)).setText(s.mark_s);

                view.findViewById(R.id.checkbox).setVisibility(View.GONE);
                view.findViewById(R.id.numer).setVisibility(View.GONE);
                view.findViewById(R.id.mark).setVisibility(View.VISIBLE);
                break;
            case 1:
                ((EditText) view.findViewById(R.id.numer)).setText((s.mark_i==null)?null:s.mark_i.toString());

                view.findViewById(R.id.checkbox).setVisibility(View.GONE);
                view.findViewById(R.id.mark).setVisibility(View.GONE);
                view.findViewById(R.id.numer).setVisibility(View.VISIBLE);
                break;
            case 2:
                ((CheckBox)view.findViewById(R.id.checkbox)).setChecked(s.mark_b);

                view.findViewById(R.id.checkbox).setVisibility(View.VISIBLE);
                view.findViewById(R.id.mark).setVisibility(View.GONE);
                view.findViewById(R.id.numer).setVisibility(View.GONE);
                break;
        }
        view.setTag(position);
        return view;
    }
}
