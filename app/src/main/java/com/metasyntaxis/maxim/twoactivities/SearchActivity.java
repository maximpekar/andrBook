package com.metasyntaxis.maxim.twoactivities;

//import android.content.DialogInterface;
//import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

public class SearchActivity extends AppCompatActivity implements OnClickListener{

    final String LOG_TAG = "dbLogs";
    final Random rnd = new Random();
    Button btnSearch;
    ToggleButton tglWorks;
    EditText txtAuthor, txtName, txtLimit;
    TextView lblMessage;
    DB db;
    Handler hndl;
    Boolean isWorks = false;
    long sec;
    RequestQueue queue;
    StringRequest stringRequest, stringRequestW;
    String url, urlW;
    Date start, stop;
    ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(this);
        tglWorks = (ToggleButton) findViewById(R.id.tglWorks);
        tglWorks.setOnClickListener(this);

        txtAuthor = (EditText) findViewById(R.id.txtAuthor);
        txtName = (EditText) findViewById(R.id.txtName);
        txtLimit = (EditText) findViewById(R.id.txtLimit);
        lblMessage = (TextView) findViewById(R.id.lblMessage);

        progress = (ProgressBar) findViewById(R.id.progressBar);

        db = new DB(this);
        db.open();
        txtLimit.setText(getResources().getString(R.string.limit_text));

        hndl = new Handler() {
            public void handleMessage(Message msg) {
                String s = msg.getData().getString("s");
                int iProgress = msg.getData().getInt("iProgress");
                if(iProgress>99) {
                    afterUpdate(s, 1);
                } else {
                    progress.setProgress(iProgress);
                    progress.setSecondaryProgress(iProgress + 5);
                    lblMessage.setText(getResources().getString(R.string.label_response) +
                    "\n" + s);
                }
            };
        };

        //queue = Volley.newRequestQueue(this);
        queue = MySingle.getInstance(this.getApplicationContext()).
                getRequestQueue();

        url = getResources().getString(R.string.url_get_info_json);
        urlW = getResources().getString(R.string.url_get_info_work_json);

        stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        lblMessage.setText(getResources().getString(R.string.label_response));
                        RunThread runnable = new RunThread(response, 0, true);
                        Thread thread = new Thread(runnable);
                        thread.start();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String sMess = error.getMessage();
                afterUpdate(sMess, 0);
            }
        });

        stringRequestW = new StringRequest(Request.Method.GET, urlW,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        lblMessage.setText(getResources().getString(R.string.label_response));
                        RunThread runnable = new RunThread(response, 1, true);
                        Thread thread = new Thread(runnable);
                        thread.start();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String sMess = error.getMessage();
                afterUpdate(sMess, 0);
            }
        });
    }


    // Класс для загрузки строки ДжСона в БД, основная функция работает долго, поэтому
    public class RunThread implements Runnable { // эта штука будет запускаться в отдельном потоке
        private String strResp; // Для этого необходимо раздербанить response ДжСон,
        private String strBooks;// передать в поток большую строку ДжСон,
        private boolean clearBefor;// 1 for clear all records,
        private int iType;      // тип загружаемой таблицы
        public RunThread(String s, int i, boolean clearBefor) {
            this.strResp = s;
            this.iType = i;
            this.clearBefor = clearBefor;
        }
        @Override
        public void run() {
            //Message msg = handler.obtainMessage();
            String s;
            JSONObject jsObj;
            JSONArray data;
            int i, l;
            if(iType==0) {      // Загрузка книг
                try {
//                    jsObj = new JSONObject(strResp);
                    data = new JSONArray(strResp);
                } catch (Exception e) {
                    s = e.getMessage();
                    data = new JSONArray();
                }
                l = data.length();
                if (clearBefor) {
                    db.clearAll();
                }

//                    data = jsObj.getJSONArray("data");
                try {
                    for (i = 0; i < l; i++) {
                        db.insertBook(data.getJSONObject(i));
                        if (i % 1000 == 0) {
                            s = "Загружено " + i + " записей";
                            sendMessToThread(hndl, s, i*100/l);
                        }
                    }
                } catch (Exception e) {
                    s = e.getMessage();
                } finally {
                    s = "Загрузка завершена: " + l + " записей";
                }
            } else { // Загрузка произведений
                try {
                    data = new JSONArray(strResp);
                } catch (Exception e) {
                    s = e.getMessage();
                    data = new JSONArray();
                }
                l = data.length();
                if (clearBefor) {
                    db.clearAllWorks();
                }
//                    data = jsObj.getJSONArray("data");
                try {
                    for (i = 0; i < l; i++) {
                        db.insertWork(data.getJSONObject(i));
                        if (i % 1000 == 0) {
                            s = "Загружено " + i + " записей";
                            sendMessToThread(hndl, s, i*100/l);
                        }
                    }
                } catch (Exception e) {
                    s = e.getMessage();
                } finally {
                    s = "Загрузка завершена: " + l + " записей";
                }
            }
            sendMessToThread(hndl, s, 100);
        }
    }


    public void sendMessToThread(Handler hndl, String strMsg, int iProgress) {
        Message msg = hndl.obtainMessage();         // Сообщение, чтобы сообщить результат
        Bundle bundle = new Bundle();               //
        bundle.putString("s", strMsg);              // Запаковывыем строку для передачи
        bundle.putInt("iProgress", iProgress);      // и число: 1, если все сделали
        msg.setData(bundle);                        // и помещаем в сообщение.
        hndl.sendMessage(msg);                      // Сообщение передается объектом Handler
    }


    public void beforeUpdate() { //  Прежде, чем запустить длинный процесс, надо подготовиться
        lblMessage.setText(getResources().getString(R.string.label_loading)); // Пишем, что делаем
        start = Calendar.getInstance().getTime(); // Запоминаем время старта
        sec = System.currentTimeMillis();         // Запоминаем милисек. с начала старта
        progress.setVisibility(ProgressBar.VISIBLE); // Показываем ProgressBar
        btnSearch.setEnabled(false); // Отрубаем все кнопки, чтобы ненароком не нажать
        tglWorks.setEnabled(false);  //
        txtAuthor.setEnabled(false); // А также отрубаем все текстовые поля
        txtName.setEnabled(false);   //
        txtLimit.setEnabled(false);  //
    }


    public void afterUpdate(String s, int iType) {
        btnSearch.setEnabled(true); // Обратно включаем все кнопки, чтобы нажать
        tglWorks.setEnabled(true);  //
        txtAuthor.setEnabled(true); // А также включаем все текстовые поля
        txtName.setEnabled(true);   //
        txtLimit.setEnabled(true);  //
        if(iType==1) {
            stop = Calendar.getInstance().getTime();
            String sMess = s + "\n" + start.toString() + " - " + stop.toString() + "\n";
            sec = System.currentTimeMillis() - sec;
            int min = 0, secund = (int)(sec/1000);
            sec = sec % 1000;
            if(secund>60) {
                min = secund/60;
                secund = secund % 60;
                sMess += "Количество времени: " + min + " минут, " + secund + " сек., " + sec + " мс.";
            } else {
                sMess += "Количество времени: " + secund + " сек., " + sec + " мс.";
            }
            progress.setVisibility(ProgressBar.INVISIBLE);
            lblMessage.setText(sMess);
        } else {
            progress.setVisibility(ProgressBar.INVISIBLE);
            lblMessage.setText(s);
        }
    }


    protected void onDestroy() {
        super.onDestroy();
        // закрываем подключение при выходе
        db.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public void onClick(View v) {
//  Обработка событий формы
        switch (v.getId()) {
            case R.id.tglWorks:    //  Выбор "книги" - "произведения"
                isWorks = tglWorks.isChecked();
                break;
            case R.id.btnSearch:    //  Запуск поиска
                String[] strData = {txtAuthor.getText().toString(), txtName.getText().toString(),
                        txtLimit.getText().toString(), isWorks.toString()};
                Intent intent = ResultActivity.newIntent(SearchActivity.this, strData);
                startActivity(intent);//  Открываем форму с результатами
                break;
            default:
                break;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        long lastId = 0;
        switch (id) {
            case R.id.mnuUpdateBooks: //  Загрузка всех книг
                beforeUpdate();       //  Приготовились для загрузки и потом
                MySingle.getInstance(this).addToRequestQueue(stringRequest); // лезем в инет за списком
                return true;
            case R.id.mnuAddBooks:    //  Догрузка только тех книг, которых не хватает
                lastId = db.getLastID(0); // Взяли последний ID
                StringRequest stringRequestAdd = new StringRequest(Request.Method.GET, url +
                        "?LastID=" + lastId,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                lblMessage.setText(getResources().getString(R.string.label_response));
                                RunThread runnable = new RunThread(response, 0, false);
                                Thread thread = new Thread(runnable);
                                thread.start();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String sMess = error.getMessage();
                        afterUpdate(sMess, 0);
                    }
                });
                beforeUpdate();       //  Приготовились для загрузки и потом
                MySingle.getInstance(this).addToRequestQueue(stringRequestAdd); // лезем в инет за списком
                return true;
            case R.id.mnuUpdateWorks: //  Загрузка всех произведений
                beforeUpdate();       //  Приготовились для загрузки и потом
                MySingle.getInstance(this).addToRequestQueue(stringRequestW); // лезем в инет за списком
                return true;
            case R.id.mnuAddWorks: //  Догрузка только тех произведений, которых не хватает
                lastId = db.getLastID(1);
                StringRequest stringRequestAddW = new StringRequest(Request.Method.GET, urlW +
                        "&LastID=" + lastId,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                lblMessage.setText(getResources().getString(R.string.label_response));
                                RunThread runnable = new RunThread(response, 1, false);
                                Thread thread = new Thread(runnable);
                                thread.start();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String sMess = error.getMessage();
                        afterUpdate(sMess, 0);
                    }
                });
                beforeUpdate();       //  Приготовились для загрузки и потом
                MySingle.getInstance(this).addToRequestQueue(stringRequestAddW); // лезем в инет за списком
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}



