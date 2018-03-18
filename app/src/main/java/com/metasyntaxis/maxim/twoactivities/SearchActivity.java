package com.metasyntaxis.maxim.twoactivities;

import android.content.DialogInterface;
import android.content.Intent;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.database.Cursor;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Random;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

//import com.google.gson.Gson;
//import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;
//import com.google.gson.JsonParser;


public class SearchActivity extends AppCompatActivity implements OnClickListener{

    final String LOG_TAG = "dbLogs";
    final Random rnd = new Random();
    Button btnSearch, btnTest, btnReload;
    EditText txtAuthor, txtName, txtLimit;
    DB db;
    RequestQueue queue;
    StringRequest stringRequest;
    //JsonObjectRequest jsonObjectRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(this);
        btnTest = (Button) findViewById(R.id.btnTest);
        btnTest.setOnClickListener(this);
        btnReload = (Button) findViewById(R.id.btnReload);
        btnReload.setOnClickListener(this);

        txtAuthor = (EditText) findViewById(R.id.txtAuthor);
        txtName = (EditText) findViewById(R.id.txtName);
        txtLimit = (EditText) findViewById(R.id.txtLimit);

        db = new DB(this);
        db.open();
        txtLimit.setText("20");


        queue = Volley.newRequestQueue(this);

        String url = getResources().getString(R.string.url_get_info_json); //"http://www.homelibr.ru/andr/";

//        jsonObjectRequest = new JsonObjectRequest
//                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
//
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        Toast toast = Toast.makeText(SearchActivity.this,
//                            response.toString(), Toast.LENGTH_SHORT);
//                        toast.show();
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Toast toast = Toast.makeText(SearchActivity.this,
//                                error.getMessage(), Toast.LENGTH_SHORT);
//                        toast.show();
//                    }
//                });

        stringRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    String s = db.updateAllFromJSON(response.toString());
                    Toast toast = Toast.makeText(SearchActivity.this,
                            s, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(SearchActivity.this,
                        error.getMessage(), Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    @Override
    public void onClick(View v) {

         switch (v.getId()) {
            case R.id.btnSearch:
                String[] strData = {txtAuthor.getText().toString(), txtName.getText().toString(),
                        txtLimit.getText().toString()};
                Intent intent = ResultActivity.newIntent(SearchActivity.this, strData);
                startActivity(intent);
                break;
            case R.id.btnTest:
                Book ob = new Book("Автор" + rnd.nextInt(), "Наименование" + rnd.nextInt(), 0);
                long rowID = db.insertBook(ob);
                //Log.d(LOG_TAG, "row inserted, ID = " + rowID);

                break;
            case R.id.btnReload:

                queue.add(stringRequest);

                // Это не функция, а полный привет! Осторожнее!
                // db.clearAll(); // !!!!!!!!!!!!!!!!!!
                // делаем запрос всех данных из таблицы mytable, получаем Cursor
//                Cursor c = db.getAllBooks("", "20");
//
//                if (c.moveToFirst()) {
//
//                    int idColIndex = c.getColumnIndex("_id");
//                    int nameColIndex = c.getColumnIndex("name");
//                    int authorColIndex = c.getColumnIndex("author");
//
//                    String s = "";
//
//                    do {
//                        s += "ID = " + c.getInt(idColIndex) +
//                                        ", name = " + c.getString(nameColIndex) +
//                                        ", author = " + c.getString(authorColIndex);
//                    } while (c.moveToNext());
//
//                    AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
//                    builder.setTitle("Важное сообщение!")
//                            .setMessage(s)
//                            .setIcon(R.drawable.ic_launcher_background)
//                            .setCancelable(false)
//                            .setNegativeButton("ОК, не вопрос",
//                                    new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialog, int id) {
//                                            dialog.cancel();
//                                        }
//                                    });
//                    AlertDialog alert = builder.create();
//                    alert.show();
//
//                } else
//                    Log.d(LOG_TAG, "0 rows");
//                c.close();
//
                break;
            default:
                break;
        }
    }

}



