package com.metasyntaxis.maxim.twoactivities;

import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

public class ResultActivity extends FragmentActivity implements LoaderCallbacks<Cursor> {

    private static final int CM_DETAIL_ID = 1, CM_SEARCH_ID = 2;
    ListView lvData;
    DB db;
    boolean isWorks;
    SimpleCursorAdapter scAdapter;
    private static final String GET_DATA_FOR_SEARCH =
            "com.metasyntaxis.maxim.twoactivities.authorforsearch";

    private String[] mDataForSearch;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        mDataForSearch = getIntent().getStringArrayExtra(GET_DATA_FOR_SEARCH);
        isWorks =(mDataForSearch[3].equals("true"));

        // открываем подключение к БД
        db = new DB(this);
        db.open();

        // формируем столбцы сопоставления
        String[] from = new String[] { DB.COLUMN_AUTHOR, DB.COLUMN_NAME };
        int[] to = new int[] {R.id.txtAuthor,  R.id.txtName};

        // создаем адаптер и настраиваем список
        scAdapter = new SimpleCursorAdapter(this, R.layout.item, null, from, to, 0);
        lvData = (ListView) findViewById(R.id.lstBook);
        lvData.setAdapter(scAdapter);

        // добавляем контекстное меню к списку
        registerForContextMenu(lvData);

        // создаем лоадер для чтения данных
        getSupportLoaderManager().initLoader(0, null, this);
    }


    public static Intent newIntent(Context c, String[] strData){
        Intent intent = new Intent(c, ResultActivity.class);    // Объект для общения между формами
        intent.putExtra(GET_DATA_FOR_SEARCH, strData);          // Берем данные из SearchActivity
        return intent;
    }

    // обработка нажатия кнопки
//    public void onButtonClick(View view) {
//        Toast toast = Toast.makeText(ResultActivity.this,
//                mDataForSearch[0] + " # " + mDataForSearch[1],
//                Toast.LENGTH_SHORT);
//        toast.show();
//        toast.setGravity(Gravity.BOTTOM, 1, 2);
//    }


    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CM_DETAIL_ID, 0, R.string.context_menu_details);
        menu.add(0, CM_SEARCH_ID, 0, R.string.context_menu_search);
    }


    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CM_DETAIL_ID) {
            // получаем из пункта контекстного меню данные по пункту списка
            AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
            long iData[] = {acmi.id, 0};
            if(isWorks) {
                iData[1] = 1;
            }
            Intent intent = DetailActivity.newIntent(ResultActivity.this, iData);
            startActivity(intent);  // Открываем форму для просмотра подробностей
            return true;
        } else if (item.getItemId() == CM_SEARCH_ID) {
            //
            return true;
        }
        return super.onContextItemSelected(item);
    }


    protected void onDestroy() {
        super.onDestroy();
        // закрываем подключение при выходе
        db.close();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bndl) {
        MyCursorLoader mcl = new MyCursorLoader(this, db);
        mcl.setSelection(" author like '%" + mDataForSearch[0].trim() +
                "%' and name like '%" + mDataForSearch[1].trim() + "%'");
        mcl.setLimit(mDataForSearch[2]);
        mcl.setType(isWorks);
        return mcl;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        scAdapter.swapCursor(cursor);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }


    static class MyCursorLoader extends CursorLoader {

        DB db;
        String selection = "";
        private String limit = "100";
        private boolean isWorks;

        public void setSelection(String s) {
            selection = s;
        }

        public void setType(boolean iW) {
            isWorks = iW;
        }
        public void setLimit(String s) {
            limit = s;
        }

        public MyCursorLoader(Context context, DB db) {
            super(context);
            this.db = db;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor;
            if (isWorks) {
                cursor = db.getAllWorks(selection, limit);
            } else {
                cursor = db.getAllBooks(selection, limit);
            }
            return cursor;
        }

    }

}
