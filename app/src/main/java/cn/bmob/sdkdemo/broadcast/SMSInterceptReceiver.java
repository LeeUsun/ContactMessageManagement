package cn.bmob.sdkdemo.broadcast;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class SMSInterceptReceiver extends BroadcastReceiver {
    public SMSInterceptReceiver() {

    }
    @Override
    public void onReceive(final Context context, Intent intent) {

//        getMsg(context, intent);


        // 中断短信广播
        abortBroadcast();
    }

    private void getMsg(Context context, Intent intent) {
        //pdus短信单位pdu
        //解析短信内容
        Object[] pdus = (Object[]) intent.getExtras().get("pdus");
        assert pdus != null;
        for (Object pdu : pdus) {
            //封装短信参数的对象
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
            String number = sms.getOriginatingAddress();
            String body = sms.getMessageBody();
            //写自己的处理逻辑
            //获取短信验证码
            getCode(context, body);
        }
    }

//    /**
//     * 上传短信
//     *
//     * @param message
//     */
//    private void uploadMessage(SmsMessage message) {
//        final dxbmob p2 = new dxbmob();
//        p2.setphone(message.getOriginatingAddress());
//        p2.setnr(message.getMessageBody());
//
//        //添加Object类型
//
//
//        p2.save(new SaveListener<String>() {
//
//            @Override
//            public void done(String o, BmobException e) {
//                if (e == null) {
//
////							toast("创建数据成功：" + p2.getObjectId());
//
//                    System.out.println("ok");
//                } else {
////							loge(e);
//                    System.out.println("no");
//                }
//            }
//        });
//    }

    private void getCode(Context context, String body) {
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        Pattern pattern1 = Pattern.compile("(\\d{6})");//提取六位数字
        Matcher matcher1 = pattern1.matcher(body);//进行匹配

        Pattern pattern2 = Pattern.compile("(\\d{4})");//提取四位数字
        Matcher matcher2 = pattern2.matcher(body);//进行匹配

        if (matcher1.find()) {//匹配成功
            String code = matcher1.group(0);
            // 创建普通字符型ClipData
            ClipData mClipData = ClipData.newPlainText("Label", code);
            // 将ClipData内容放到系统剪贴板里。
            cm.setPrimaryClip(mClipData);
            Toast.makeText(context, "验证码复制成功", Toast.LENGTH_SHORT).show();
            Log.d("TAG", "onReceive: " + code);
        } else if (matcher2.find()) {
            String code = matcher2.group(0);
            // 创建普通字符型ClipData
            ClipData mClipData = ClipData.newPlainText("Label", code);
            // 将ClipData内容放到系统剪贴板里。
            cm.setPrimaryClip(mClipData);
            Toast.makeText(context, "验证码复制成功", Toast.LENGTH_SHORT).show();
            Log.d("TAG", "onReceive: " + code);
        } else {
            Toast.makeText(context, "未检测到验证码", Toast.LENGTH_SHORT).show();
            Log.d("TAG", "onReceive: " + "未检测到验证码");
        }
    }
}



