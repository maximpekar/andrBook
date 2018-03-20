package com.metasyntaxis.maxim.twoactivities;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.database.Cursor;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    private static final String GET_DATA_FOR_DETAIL =
            "com.metasyntaxis.maxim.twoactivities.datafordetail";
    private  long iDetails[];
    String txtDetails, txtWorks;
    TextView txtViewDetails, txtViewWorks;
    DB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        db = new DB(this);
        db.open();

        txtViewWorks = (TextView) findViewById(R.id.TxtWorks);
        txtViewDetails = (TextView) findViewById(R.id.txtDetail);
        iDetails = getIntent().getLongArrayExtra(GET_DATA_FOR_DETAIL);

        if(iDetails[1]==1) {
            txtDetails = db.getWorkInfo(iDetails[0]);
            txtWorks = "";
        } else {
            txtDetails = db.getBookInfo(iDetails[0]);
            txtWorks = db.getWorksOfBook(iDetails[0]);
        }
        txtViewDetails.setText(txtDetails);
        txtViewWorks.setText(txtWorks);


    }


    public static Intent newIntent(Context c, long[] iData){
        Intent intent = new Intent(c, DetailActivity.class);
        intent.putExtra(GET_DATA_FOR_DETAIL, iData);
        return intent;
    }

}
