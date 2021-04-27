package ru.astakhovmd.commander;

public class Mark_item {

    User_profile user;
    String mark_s;
    Double mark_i;
    Boolean mark_b;

    Mark_item(User_profile _user, String _mark_s, Double _mark_i,boolean _mark_b) {
        user = _user;
        mark_s = _mark_s;
        mark_i = _mark_i;
        mark_b = _mark_b;
    }
}