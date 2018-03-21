package com.metasyntaxis.maxim.twoactivities;

//import android.content.DialogInterface;
//import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class SearchActivity extends AppCompatActivity implements OnClickListener{

    final String LOG_TAG = "dbLogs";
    final Random rnd = new Random();
    Button btnSearch, btnWorks, btnReload;
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
        txtLimit.setText(getResources().getString(R.string.limit_text));

        hndl = new Handler() {
            public void handleMessage(Message msg) {
                afterUpdate(msg.getData().getString("s"), 1);
            };
        };

        queue = Volley.newRequestQueue(this);

        url = getResources().getString(R.string.url_get_info_json); //"http://www.homelibr.ru/andr/";
        urlW = getResources().getString(R.string.url_get_info_work_json);

        stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        lblMessage.setText(getResources().getString(R.string.label_response));
                        RunThread runnable = new RunThread(response, 0);
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
                        RunThread runnable = new RunThread(response, 1);
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
        private String strResp; // Для этого необходимо передать в поток большую строку ДжСон
        private int iType;      // и тип загружаемой таблицы
        public RunThread(String s, int i) {
            this.strResp = s;
            this.iType = i;
        }
        @Override
        public void run() {
            //Message msg = handler.obtainMessage();
            String s;
            if(iType==0) {
                s = db.updateAllFromJSON(strResp);      // Загрузка книг
            } else {
                s = db.updateAllWorksFromJSON(strResp); // Загрузка произведений
            }
            Message msg = hndl.obtainMessage();         // Сообщение, чтобы сообщить результат
            Bundle bundle = new Bundle();
            bundle.putString("s", s);                   // Запаковывыем строку для передачи
            msg.setData(bundle);                        // и помещаем в сообщение.
            hndl.sendMessage(msg);                      // Сообщение передается объектом Handler
        }
    }

    public void beforUpdate() { //  Прежде, чем запустить длинный процесс, надо подготовиться
        lblMessage.setText(getResources().getString(R.string.label_loading)); // Пишем, что делаем
        start = Calendar.getInstance().getTime(); // Запоминаем время старта
        sec = System.currentTimeMillis();         // Запоминаем милисек. с начала старта
        progress.setVisibility(ProgressBar.VISIBLE); // Показываем ProgressBar
        btnWorks.setEnabled(false);  // Отрубаем все кнопки, чтобы ненароком не нажать
        btnReload.setEnabled(false); //
        btnSearch.setEnabled(false); //
        tglWorks.setEnabled(false);  //
        txtAuthor.setEnabled(false); // А также отрубаем все текстовые поля
        txtName.setEnabled(false);   //
        txtLimit.setEnabled(false);  //
    }

    public void afterUpdate(String s, int iType) {
        btnWorks.setEnabled(true);  // Обратно включаем все кнопки, чтобы ненароком не нажать
        btnReload.setEnabled(true); //
        btnSearch.setEnabled(true); //
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
            case R.id.btnWorks:      //  Загрузка произведений
                beforUpdate();       //  Приготовились
                queue.add(stringRequestW);//  Поставили в очередь HTTP-запросов
                break;
            case R.id.btnReload:     //  Загрузка книг
                beforUpdate();       //  Приготовились
                queue.add(stringRequest);//  Поставили в очередь HTTP-запросов
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
                break;
            default:
                break;
        }
    }

}



