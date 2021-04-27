package ru.astakhovmd.commander;

public class Course_info {
    int id;
    String name;
    String author;
    boolean loaded = false;
    public Course_info(int _id, String _name,String _author) {
        id = _id;
        name = _name;
        author = _author;
    }
    public void setLoaded(boolean _loaded){
        loaded = _loaded;
    }
}
