package cn.bmob.sdkdemo.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("fafafafasfas", "BootCompletedReceiver   启动了");
        context.startService(new Intent(context, SMSInterceptReceiver.class));
    }

}
