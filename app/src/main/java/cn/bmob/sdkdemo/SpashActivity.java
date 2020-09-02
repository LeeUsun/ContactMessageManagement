package cn.bmob.sdkdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import cn.bmob.sdkdemo.activity.Main2Activity;

public class SpashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ("client".equals(BuildConfig.FLAVOR)) {
            startActivity(new Intent(this, MainActivity.class));
        } else if ("business".equals(BuildConfig.FLAVOR)) {
            startActivity(new Intent(this, Main2Activity.class));
        }
        finish();
    }
}
