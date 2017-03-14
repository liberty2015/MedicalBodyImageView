package com.liberty.medicalbodyview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.liberty.library.MedicineBodyView;
import com.liberty.library.bean.BodyPartF;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final MedicineBodyView bodyView= (MedicineBodyView) findViewById(R.id.body);
        bodyView.setOnClickListener(new MedicineBodyView.OnClickListener() {
            @Override
            public void onClick(View view, BodyPartF.Part part) {
                Toast.makeText(MainActivity.this,part.getDesc(),Toast.LENGTH_LONG).show();
            }
        });
        Switch gender=((Switch)findViewById(R.id.gender));
        final Switch exchange=((Switch)findViewById(R.id.exchange));
        gender.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    bodyView.gender();
                }else {
                    bodyView.gender();
                }
                if (exchange.isChecked()){
                    exchange.setChecked(false);
                }
            }
        });
        exchange.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("xxxxx","isChecked="+isChecked);
                if (isChecked){
                    bodyView.exchange();
                }else {
                    bodyView.exchange();
                }
            }
        });
    }
}
