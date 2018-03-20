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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.util.Calendar;
import java.util.Date;
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
    Button btnSearch, btnWorks, btnReload;
    ToggleButton tglWorks;
    EditText txtAuthor, txtName, txtLimit;
    TextView lblMessage;
    DB db;
    Boolean isWorks = false;
    RequestQueue queue;
    StringRequest stringRequest, stringRequestW;
    String url, urlW;
    Date start, stop;
    ProgressBar progress;
    //JsonObjectRequest jsonObjectRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(this);
        btnWorks = (Button) findViewById(R.id.btnWorks);
        btnWorks.setOnClickListener(this);
        btnReload = (Button) findViewById(R.id.btnReload);
        btnReload.setOnClickListener(this);
        tglWorks = (ToggleButton) findViewById(R.id.tglWorks);
        tglWorks.setOnClickListener(this);

        txtAuthor = (EditText) findViewById(R.id.txtAuthor);
        txtName = (EditText) findViewById(R.id.txtName);
        txtLimit = (EditText) findViewById(R.id.txtLimit);
        lblMessage = (TextView) findViewById(R.id.lblMessage);

        progress = (ProgressBar) findViewById(R.id.progressBar);

        db = new DB(this);
        db.open();
        txtLimit.setText("20");

        queue = Volley.newRequestQueue(this);

        url = getResources().getString(R.string.url_get_info_json); //"http://www.homelibr.ru/andr/";
        urlW = getResources().getString(R.string.url_get_info_work_json);
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
                        stop = Calendar.getInstance().getTime();
                        String sMess = s + "\n" + start.toString() + " - " + stop.toString();
                        progress.setVisibility(ProgressBar.INVISIBLE);
                        lblMessage.setText(sMess);
//                        Toast toast = Toast.makeText(SearchActivity.this,
//                                s, Toast.LENGTH_SHORT);
//                        toast.show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String sMess = error.getMessage();
                progress.setVisibility(ProgressBar.INVISIBLE);
                lblMessage.setText(sMess);
//                Toast toast = Toast.makeText(SearchActivity.this,
//                        error.getMessage(), Toast.LENGTH_SHORT);
//                toast.show();
            }
        });

        stringRequestW = new StringRequest(Request.Method.GET, urlW,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String s = db.updateAllWorksFromJSON(response.toString());
                        stop = Calendar.getInstance().getTime();
                        String sMess = s + "\n" + start.toString() + " - " + stop.toString();
                        lblMessage.setText(sMess);
                        progress.setVisibility(ProgressBar.INVISIBLE);
//                        Toast toast = Toast.makeText(SearchActivity.this,
//                                s, Toast.LENGTH_SHORT);
//                        toast.show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String sMess = error.getMessage();
                progress.setVisibility(ProgressBar.INVISIBLE);
                lblMessage.setText(sMess);
//                Toast toast = Toast.makeText(SearchActivity.this,
//                        error.getMessage(), Toast.LENGTH_SHORT);
//                toast.show();
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.tglWorks:
                isWorks = tglWorks.isChecked();
                break;
            case R.id.btnSearch:
                String[] strData = {txtAuthor.getText().toString(), txtName.getText().toString(),
                        txtLimit.getText().toString(), isWorks.toString()};
                Intent intent = ResultActivity.newIntent(SearchActivity.this, strData);
                startActivity(intent);
                break;
            case R.id.btnWorks:
                lblMessage.setText(getResources().getString(R.string.label_loading));
                start = Calendar.getInstance().getTime();
                progress.setVisibility(ProgressBar.VISIBLE);
                queue.add(stringRequestW);
                break;
            case R.id.btnReload:
                lblMessage.setText(getResources().getString(R.string.label_loading));
                start = Calendar.getInstance().getTime();
                progress.setVisibility(ProgressBar.VISIBLE);
                queue.add(stringRequest);
                // Это не функция, а полный привет! Осторожнее!
                 // db.clearAll(); // !!!!!!!!!!!!!!!!!!
                 // делаем запрос всех данных из таблицы mytable, получаем Cursor
//                 Cursor c = db.getAllBooks("", "20");
//
//                 if (c.moveToFirst()) {
//
//                     int idColIndex = c.getColumnIndex("_id");
//                     int nameColIndex = c.getColumnIndex("name");
//                     int authorColIndex = c.getColumnIndex("author");
//
//                     String s = "";
//
//                     do {
//                         s += "ID = " + c.getInt(idColIndex) +
//                                         ", name = " + c.getString(nameColIndex) +
//                                         ", author = " + c.getString(authorColIndex);
//                     } while (c.moveToNext());
//
//                     AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
//                     builder.setTitle("Важное сообщение!")
//                             .setMessage(s)
//                             .setIcon(R.drawable.ic_launcher_background)
//                             .setCancelable(false)
//                             .setNegativeButton("ОК, не вопрос",
//                                     new DialogInterface.OnClickListener() {
//                                         public void onClick(DialogInterface dialog, int id) {
//                                             dialog.cancel();
//                                         }
//                                     });
//                     AlertDialog alert = builder.create();
//                     alert.show();
//
//                 } else
//                     Log.d(LOG_TAG, "0 rows");
//                 c.close();
//
                break;
            default:
                break;
        }
    }

}



