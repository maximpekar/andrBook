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

public class DB {

    private static final String DB_NAME = "dbBooks";
    private static final int DB_VERSION = 1;
    private static final String DB_TABLE = "tblBook";
    private static final String DB_TABLE_WORK = "tblWork";

    private static final String COLUMN_ID = "_id";
    static final String COLUMN_AUTHOR = "author";
    static final String COLUMN_NAME = "name";
    // Only for table tblBook
    private static final String COLUMN_SERIA = "seria";
    private static final String COLUMN_YEAR = "year";
    private static final String COLUMN_PUBLISHER = "publ";
    // Only for table tblWork
    private static final String COLUMN_BOOKID = "bid";
    private static final String COLUMN_TRANSLATOR = "trans";

    private static final String DB_CREATE =
            "create table " + DB_TABLE + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_AUTHOR + " text, " +
                    COLUMN_NAME + " text, " +
                    COLUMN_SERIA + " text, " +
                    COLUMN_YEAR + " integer, " +
                    COLUMN_PUBLISHER + " text " +
                    ");";

    private static final String DB_CREATE_WORK =
            "create table " + DB_TABLE_WORK + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_BOOKID + " integer," +
                    COLUMN_AUTHOR + " text, " +
                    COLUMN_TRANSLATOR + " text, " +
                    COLUMN_NAME + " text" +
                    ");";

    private final Context mCtx;

    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;

    DB(Context ctx) {
        mCtx = ctx;
    }

    // открыть подключение
    void open() {
        mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
        mDB = mDBHelper.getWritableDatabase();
    }

    // закрыть подключение
    void close() {
        if (mDBHelper!=null) mDBHelper.close();
    }

    // получить все данные из таблицы DB_TABLE
    Cursor getAllBooks(String selection, String limit) {
        return mDB.query(DB_TABLE, null, selection, null,
                null, null, null, limit);
    }

    Cursor getAllWorks(String selection, String limit) {
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
        cv.put(COLUMN_SERIA, b.getSeria());
        cv.put(COLUMN_YEAR, b.getYear());
        cv.put(COLUMN_PUBLISHER, b.getPublisher());
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
            cv.put(COLUMN_BOOKID, bid);
        }
        cv.put(COLUMN_TRANSLATOR, w.getTranslator());
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
                Book b = new Book(row.getString("author"), row.getString("name"),
                        row.getInt("id"), row.getString("seria"),
                        row.getInt("year"), row.getString("publ"));
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
                Work w = new Work(row.getString("author"), row.getString("name"),
                        row.getInt("id"), row.getInt("bid"), row.getString("trans"));
                insertWork(w);
            }
            s = "Success: " + l + " was been added";
        } catch (JSONException e) {
            s = e.getMessage();
        }
        return s;
    }

    public String getBookInfo(long id){
        String s = "";
        Cursor c = mDB.query(DB_TABLE, null, COLUMN_ID + "=" + id,
                null, null, null, null);
        if (c.moveToFirst()) {
            int idColIndex = c.getColumnIndex(COLUMN_ID);
            int nameColIndex = c.getColumnIndex(COLUMN_NAME);
            int authorColIndex = c.getColumnIndex(COLUMN_AUTHOR);
            int seriaColIndex = c.getColumnIndex(COLUMN_SERIA);
            int yearColIndex = c.getColumnIndex(COLUMN_YEAR);
            int publisherColIndex = c.getColumnIndex(COLUMN_PUBLISHER);
            s += "(ID: " + c.getInt(idColIndex) +
                    ") " + c.getString(nameColIndex) +
                    "\n" + c.getString(authorColIndex) +
                    "\nСерия: " + c.getString(seriaColIndex) +
                    "\n" + c.getString(publisherColIndex) + ", " + c.getInt(yearColIndex);
        }
        return s;
    }

    public String getWorksOfBook(long id){
        String s = "";
        Cursor c = mDB.query(DB_TABLE_WORK, null, COLUMN_BOOKID + "=" + id,
                null, null, null, null);
        if (c.moveToFirst()) {
            int idColIndex = c.getColumnIndex(COLUMN_ID);
            int nameColIndex = c.getColumnIndex(COLUMN_NAME);
            int authorColIndex = c.getColumnIndex(COLUMN_AUTHOR);
            do {
                s += "ID: " + c.getInt(idColIndex) +
                        ", Название: " + c.getString(nameColIndex) +
                        ", Авторы: " + c.getString(authorColIndex) + "\n";
            } while (c.moveToNext());
        }
        return s;
    }

    public String getWorkInfo(long id) {
        String s = "";
        Cursor c = mDB.query(DB_TABLE_WORK, null,
                COLUMN_ID + " = " +  Long.toString(id),
                null, null, null, null);
        if(c.moveToFirst()) {
            s += "ID: " + c.getInt(c.getColumnIndex(COLUMN_ID)) + "\n" +
                "Название: " + c.getString(c.getColumnIndex(COLUMN_NAME)) + "\n" +
                "Авторы: " + c.getString(c.getColumnIndex(COLUMN_AUTHOR)) + "\n" +
                "Перевод: " + c.getString(c.getColumnIndex(COLUMN_TRANSLATOR));
        }
        return s;
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
    private String Seria;
    private int Year;
    private String Publisher;

    public String getAuthor(){
        return this.Author;
    }

    public String getName(){
        return this.Name;
    }

    public String getSeria(){
        return this.Seria;
    }

    public int getYear(){
        return this.Year;
    }

    public String getPublisher(){
        return this.Publisher;
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

     public Book(String author, String name, int id, String seria, int year, String publ) {
        this.ID = id;
        this.Author = author;
        this.Name = name;
        this.Seria = seria;
        this.Year = year;
        this.Publisher = publ;
    }

}

class Work {
    private int ID;
    private int BookID;
    private String Author;
    private String Name;
    private String Translator;

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

    public String getTranslator() {return this.Translator;}

    public Work(String author, String name, int id, int bid, String trans) {
        this.ID = id;
        this.Author = author;
        this.Name = name;
        this.BookID = bid;
        this.Translator = trans;
    }

}
