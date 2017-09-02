package com.halohoop.haloflowlayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private WidthFixedFlowLayout fl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fl = (WidthFixedFlowLayout) findViewById(R.id.fl);
    }

    public void add(View view){
        TextView textView = new TextView(this);
        textView.setText("haha");
        fl.addView(textView);
    }

}
