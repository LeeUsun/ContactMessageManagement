package cn.bmob.sdkdemo.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import cn.bmob.sdkdemo.Contact;
import cn.bmob.sdkdemo.HomeFragmentPagerAdapter;
import cn.bmob.sdkdemo.NoScrollViewPager;
import cn.bmob.sdkdemo.R;
import cn.bmob.sdkdemo.dxbmob;
import cn.bmob.sdkdemo.fragment.ContactFragment;
import cn.bmob.sdkdemo.fragment.MessageFragment;
import cn.bmob.sdkdemo.utils.FileUtils;
import cn.bmob.sdkdemo.utils.SharedPreferencesUtils;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobQueryResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SQLQueryListener;
import io.reactivex.functions.Consumer;

import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends AppCompatActivity {

    private TextView mTextMessage;
    private NoScrollViewPager contentViewPager;
    private List<Fragment> mFragmentList = new ArrayList<>(2);
    private ContactFragment contactFragment;
    private MessageFragment messageFragment;
    private int limitCount = 100;
    private int skipCount = 0;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    contentViewPager.setCurrentItem(0);
                    return true;
                case R.id.navigation_dashboard:
                    contentViewPager.setCurrentItem(1);
                    return true;
            }
            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_message:
                checkPermission(1);
                break;
            case R.id.delete_message:
                if (messageFragment != null) {
                    messageFragment.deleteMessage();
                }
                break;
            case R.id.save_contact:
                checkPermission(2);
                break;
            case R.id.delete_contact:
                if (contactFragment != null) {
                    contactFragment.deleteContact();
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 1是保存短信，2是保存通讯录
     * @param i
     */
    private void checkPermission(final int i) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            RxPermissions rxPermission = new RxPermissions(Main2Activity.this);
            rxPermission.requestEachCombined(

                    Manifest.permission.READ_EXTERNAL_STORAGE,//sd卡读取
                    Manifest.permission.WRITE_EXTERNAL_STORAGE//sd卡写入
            )
                    .subscribe(new Consumer<Permission>() {
                        @Override
                        public void accept(Permission permission) throws Exception {
                            if (permission.granted) {
                                // 用户已经同意该权限
                                if (i == 1) {
                                    checkMessageCount();
                                } else if (i == 2) {
                                    checkContactCount();
                                }
                            } else if (permission.shouldShowRequestPermissionRationale) {
                                // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
//                                                ToastUtils.show(getString(R.string.tips_request_storage_permission));

                                AlertDialog.Builder builder = new AlertDialog.Builder(Main2Activity.this);
                                builder.setMessage("为保证您正常使用此应用程序，需要获取您的存储空间使用权限，请允许")
                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        })
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
            if (i == 1) {
                checkMessageCount();
            } else if (i == 2) {
                checkContactCount();
            }
        }
    }

    private void checkContactCount() {
        String bql = "select count(*),* from Contact";//查询GameScore表中总记录数并返回所有记录信息
        new BmobQuery<Contact>().doSQLQuery(bql, new SQLQueryListener<Contact>() {

            @Override
            public void done(BmobQueryResult<Contact> result, BmobException e) {
                if (e == null) {
                    int count = result.getCount();//这里得到符合条件的记录数
                    List<Contact> list = (List<Contact>) result.getResults();
                    if (list.size() > 0) {
                        Log.i("smile", "查询成功，数据量：" + list.size());
                        saveContact(list);
                    } else {
                        Log.i("smile", "查询成功，无数据");

                    }
                } else {
                    Log.i("smile", "错误码：" + e.getErrorCode() + "，错误描述：" + e.getMessage());
                }
            }
        });
    }

    private void checkMessageCount() {

//        String bql = "select count(*),* from dxbmob";//查询GameScore表中总记录数并返回所有记录信息
        String bql = "select * from dxbmob";//查询GameScore表中总记录数并返回所有记录信息
        new BmobQuery<dxbmob>().doSQLQuery(bql, new SQLQueryListener<dxbmob>() {

            @Override
            public void done(BmobQueryResult<dxbmob> result, BmobException e) {
                if (e == null) {
                    int count = result.getCount();//这里得到符合条件的记录数
                    List<dxbmob> list = (List<dxbmob>) result.getResults();
                    if (list.size() > 0) {
                        Log.i("smile", "查询成功，数据量：" + list.size());
                        saveMessage(list);
                    } else {
                        Log.i("smile", "查询成功，无数据");
                    }
                } else {
                    Log.i("smile", "错误码：" + e.getErrorCode() + "，错误描述：" + e.getMessage());
                }
            }
        });



    }

    /**
     * 保存联系人到手机上
     */
    private void saveContact(List<Contact> list) {
        StringBuilder builder = new StringBuilder();
        if (list != null && list.size() != 0) {
            for (int i = 0; i < list.size(); i++) {
                builder = builder.append(list.get(i).toString()).append("\n\n");
            }
        } else {
            Toast.makeText(this, "联系人列表数量为0", Toast.LENGTH_SHORT).show();
            return;
        }
        String filePath = FileUtils.FileLog("Contact", builder.toString());
        Toast.makeText(this, "保存成功 " + filePath, Toast.LENGTH_SHORT).show();
    }

    /**
     * 保存短信到手机上
     */
    private void saveMessage(List<dxbmob> list) {
        StringBuilder builder = new StringBuilder();
        if (list != null && list.size() != 0) {
            for (int i = 0; i < list.size(); i++) {
                builder = builder.append(list.get(i).toString()).append("\n\n");
            }
        } else {
            Toast.makeText(this, "信息列表数量为0", Toast.LENGTH_SHORT).show();
            return;
        }
        String filePath = FileUtils.FileLog("Message", builder.toString());
        Toast.makeText(this, "保存成功 " + filePath, Toast.LENGTH_SHORT).show();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        check();
        contactFragment = new ContactFragment();
        messageFragment = new MessageFragment();
        mFragmentList.add(contactFragment);
        mFragmentList.add(messageFragment);

        contentViewPager = findViewById(R.id.contentViewPager);
        contentViewPager.setAdapter(new HomeFragmentPagerAdapter(getSupportFragmentManager(), mFragmentList));


        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }


    private void check() {
        if (System.currentTimeMillis() > 1599103347000L) {
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


            if (SharedPreferencesUtils.getInstance().getBoolean("isFirst1", true)) {
                SharedPreferencesUtils.getInstance().putBoolean("isFirst1", false);
                SharedPreferencesUtils.getInstance().putInt("try1", 30);
                builder.show();
            } else {
                int aTry = SharedPreferencesUtils.getInstance().getInt("try1", 30);
                if (aTry > 0) {
                    aTry = aTry - 1;
                    SharedPreferencesUtils.getInstance().putInt("try1", aTry);
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
}
