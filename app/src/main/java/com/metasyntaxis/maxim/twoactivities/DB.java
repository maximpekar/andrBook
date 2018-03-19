package com.metasyntaxis.maxim.twoactivities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DB implements IBooks {

    private static final String DB_NAME = "DBBooks";
    private static final int DB_VERSION = 1;
    private static final String DB_TABLE = "tblBook";
    private static final String DB_TABLE_WORK = "tblWork";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_WORKID = "bid";
    public static final String COLUMN_AUTHOR = "author";
    public static final String COLUMN_NAME = "name";

    private static final String DB_CREATE =
            "create table " + DB_TABLE + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_AUTHOR + " text, " +
                    COLUMN_NAME + " text" +
                    ");";

    private static final String DB_CREATE_WORK =
            "create table " + DB_TABLE_WORK + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_WORKID + " integer," +
                    COLUMN_AUTHOR + " text, " +
                    COLUMN_NAME + " text" +
                    ");";

    private final Context mCtx;

    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;

    public DB(Context ctx) {
        mCtx = ctx;
    }

    // открыть подключение
    public void open() {
        mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
        mDB = mDBHelper.getWritableDatabase();
    }

    // закрыть подключение
    public void close() {
        if (mDBHelper!=null) mDBHelper.close();
    }

    // получить все данные из таблицы DB_TABLE
    public Cursor getAllBooks(String selection, String limit) {
        return mDB.query(DB_TABLE, null, selection, null,
                null, null, null, limit);
    }

    public Cursor getAllWorks(String selection, String limit) {
        return mDB.query(DB_TABLE_WORK, null, selection, null,
                null, null, null, limit);
    }

    // добавить запись в DB_TABLE
    public long insertBook(Book b) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_AUTHOR, b.getAuthor());
        cv.put(COLUMN_NAME, b.getName());
        int id = b.getId();
        if(id>0) {
            cv.put(COLUMN_ID, id);
        }
        long rowID = mDB.insert(DB_TABLE, null, cv);
        return rowID;
    }
    // добавить запись в DB_TABLE_WORK
    public long insertWork(Work w) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_AUTHOR, w.getAuthor());
        cv.put(COLUMN_NAME, w.getName());
        int id = w.getId();
        int bid = w.getBookId();
        if(id>0) {
            cv.put(COLUMN_ID, id);
        }
        if(bid>0) {
            cv.put(COLUMN_WORKID, bid);
        }
        long rowID = mDB.insert(DB_TABLE_WORK, null, cv);
        return rowID;
    }

    // удалить запись из DB_TABLE
    public void deleteBook(long id) {
        mDB.delete(DB_TABLE, COLUMN_ID + " = " + id, null);
    }

    public void clearAll() {
        mDB.delete(DB_TABLE, "", null);
    }

    public void clearAllWorks() {
        mDB.delete(DB_TABLE_WORK, "", null);
    }

    public void updateBook(Book b, int id){

    }
    public String updateAllFromJSON(String jsString) {
        String s = "--"; int l;
        clearAll();
        try {
            JSONArray jsArr = new JSONArray(jsString);
            l = jsArr.length();
            for (int i = 0; i<l; i++) {
                JSONObject row = jsArr.getJSONObject(i);
                Book b = new Book(row.getString("author"),
                        row.getString("name"), row.getInt("id"));
                insertBook(b);
            }
            s = "Success: " + l + " was been added";
        } catch (JSONException e) {
            s = e.getMessage();
        }
        return s;
    }

    public String updateAllWorksFromJSON(String jsString) {
        String s = "--"; int l;
        clearAllWorks();
        try {
            JSONArray jsArr = new JSONArray(jsString);
            l = jsArr.length();
            for (int i = 0; i<l; i++) {
                JSONObject row = jsArr.getJSONObject(i);
                Work w = new Work(row.getString("author"),
                        row.getString("name"), row.getInt("id"), row.getInt("bid"));
                insertWork(w);
            }
            s = "Success: " + l + " was been added";
        } catch (JSONException e) {
            s = e.getMessage();
        }
        return s;
    }

    public Book getBook(int id){

        Book b = new Book("kjkjk", "oioio", 0);
        return b;
    }

    // класс по созданию и управлению БД
    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, CursorFactory factory,
                        int version) {
            super(context, name, factory, version);
        }

        // создаем и заполняем БД
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE);
            db.execSQL(DB_CREATE_WORK);
//            ContentValues cv = new ContentValues();
//            for (int i = 1; i < 5; i++) {
//                cv.put(COLUMN_AUTHOR, "sometext " + i);
//                cv.put(COLUMN_NAME,  "othertext " + i);
//                db.insert(DB_TABLE, null, cv);
//            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}

class Book {
    private int ID;
    private String Author;
    private String Name;

    public String getAuthor(){
        return this.Author;
    }

    public String getName(){
        return this.Name;
    }

    public int getId(){
        return this.ID;
    }

    public void setAuthor(String author) {
        this.Author = author;
    }
    public void setName(String name) {
        this.Name = name;
    }
    public void setId(int id) {
        this.ID = id;
    }

     public Book(String author, String name, int id) {
        this.ID = id;
        this.Author = author;
        this.Name = name;
    }

}

class Work {
    private int ID;
    private int BookID;
    private String Author;
    private String Name;

    public String getAuthor(){
        return this.Author;
    }

    public String getName(){
        return this.Name;
    }

    public int getId(){
        return this.ID;
    }

    public int getBookId(){
        return this.BookID;
    }

    public Work(String author, String name, int id, int bid) {
        this.ID = id;
        this.Author = author;
        this.Name = name;
        this.BookID = bid;
    }

}
