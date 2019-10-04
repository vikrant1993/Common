package vk.help.common.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import vk.help.MasterActivity;
import vk.help.common.R;

public class StatusActivity extends MasterActivity {

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_status);
        Intent mainIntent = getIntent();
        TextView tv4 = findViewById(R.id.textView1);
        tv4.setText(mainIntent.getStringExtra("transStatus"));
    }
}