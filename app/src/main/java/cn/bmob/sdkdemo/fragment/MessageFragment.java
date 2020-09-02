package cn.bmob.sdkdemo.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.github.nukc.stateview.StateView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.bmob.sdkdemo.R;
import cn.bmob.sdkdemo.adapter.MessageAdapter;
import cn.bmob.sdkdemo.adapter.MessageBaseMultiItemQuickAdapter;
import cn.bmob.sdkdemo.bean.dxbmobHeader;
import cn.bmob.sdkdemo.dxbmob;
import cn.bmob.sdkdemo.utils.FileUtils;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobQueryResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SQLQueryListener;
import cn.bmob.v3.listener.UpdateListener;
import io.reactivex.functions.Consumer;

public class MessageFragment extends Fragment implements OnRefreshLoadMoreListener {

    private RecyclerView recyclerView;
    private StateView stateView;
    private MessageAdapter adapter;
    private SmartRefreshLayout refresh;
    private int messageCount = 0;
    private int skipCount = 0;
    private int limitCount = 20;
    private int page = 0;
    private MessageBaseMultiItemQuickAdapter baseMultiItemQuickAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        stateView = StateView.inject(view.findViewById(R.id.recyclerView));
        stateView.showLoading();
        stateView.setOnRetryClickListener(new StateView.OnRetryClickListener() {
            @Override
            public void onRetryClick() {
                doCheck();
            }
        });
        initView(view);
//        checkCount();
        doCheck();
        return view;
    }

    private void doCheck() {
        BmobQuery<dxbmob> query = new BmobQuery<>();
        query.setLimit(500)
                .setSkip(0)
                .order("person,phone")
                .findObjects(new FindListener<dxbmob>() {
                    @Override
                    public void done(List<dxbmob> list, BmobException e) {
                        if (e == null) {
                            if (list != null && list.size() != 0) {
                                baseMultiItemQuickAdapter.setNewData(setData(list));
                                stateView.showContent();
                            } else {
                                baseMultiItemQuickAdapter.setNewData(null);
                                stateView.showEmpty();
                            }
                        } else {
                            stateView.showRetry();
                        }
                        refresh.finishRefresh();
                    }
                });
    }

    private ArrayList<MultiItemEntity> setData(List<dxbmob> list) {
        List<String> a = new ArrayList<>();
        dxbmobHeader header = null;
        ArrayList<MultiItemEntity> res = new ArrayList<>();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                if (!a.contains(list.get(i).getPerson())) {

                    if (i != 0) {
                        res.add(header);
                    }
                    a.add(list.get(i).getPerson());
                    header = new dxbmobHeader();
                    header.setCode(list.get(i).getCode());
                    header.setPerson(list.get(i).getPerson());

                }

                dxbmob dx = new dxbmob();
                dx.setnr(list.get(i).getnr());
                dx.setphone(list.get(i).getphone());
                dx.setObjectId(list.get(i).getObjectId());
                header.addSubItem(dx);

                if (i == list.size() - 1) {
                    res.add(header);
                }

            }
        }
        return res;
    }

    private void checkCount() {

        String bql = "select count(*),* from dxbmob";//查询GameScore表中总记录数并返回所有记录信息
        new BmobQuery<dxbmob>().doSQLQuery(bql, new SQLQueryListener<dxbmob>() {

            @Override
            public void done(BmobQueryResult<dxbmob> result, BmobException e) {
                if (e == null) {
                    messageCount = result.getCount();//这里得到符合条件的记录数
                    List<dxbmob> list = (List<dxbmob>) result.getResults();
                    if (list.size() > 0) {
                        Log.i("smile", "查询成功，数据量：" + list.size());
                        getData(1);
                    } else {
                        Log.i("smile", "查询成功，无数据");
                        adapter.setNewData(null);
                        stateView.showEmpty();
                        refresh.finishRefresh();
                    }
                } else {
                    Log.i("smile", "错误码：" + e.getErrorCode() + "，错误描述：" + e.getMessage());
                    stateView.showRetry();
                    refresh.finishRefresh();
                }
            }
        });


//        BmobQuery<dxbmob> bmobQuery2 = new BmobQuery<dxbmob>();
//        bmobQuery2
//                .count(dxbmob.class, new CountListener() {
//                    @Override
//                    public void done(Integer integer, BmobException e) {
//                        if (e == null) {
//                            Log.e("短信_查询数量成功", "数量：" + integer);
//                        } else {
//                            Log.e("短信_查询数量失败", e.getMessage());
//                        }
//                    }
//                });


    }

    /**
     * 1是首次加载（下拉刷新），2是上拉加载更多
     *
     * @param type
     */
    private void getData(final int type) {
        BmobQuery<dxbmob> bmobQuery = new BmobQuery<dxbmob>();
        if (type == 1) {
            page = 0;
            bmobQuery.setSkip(page * limitCount);
        } else if (type == 2) {
            bmobQuery.setSkip(page * limitCount);
        }
        bmobQuery.setLimit(limitCount)
                .findObjects(new FindListener<dxbmob>() {
                    @Override
                    public void done(List<dxbmob> list, BmobException e) {
                        if (e == null) {
                            Log.e("查询短信", "成功");
                            if (list == null || list.size() == 0) {
                                if (type == 1) {
                                    stateView.showEmpty();
                                } else if (type == 2) {
                                    Toast.makeText(getContext(), "没有更多信息了", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                if (type == 1) {
                                    adapter.setNewData(list);
                                    stateView.showContent();
                                } else if (type == 2) {
                                    adapter.addData(list);
                                }
                            }
                        } else {
                            Log.e("查询短信", "失败 " + e.getMessage() + "    " + e.getErrorCode());
                            stateView.showRetry();
                        }
                        refresh.finishRefresh();
                        refresh.finishLoadMore();
                    }
                });
    }

    private void initView(View view) {
        refresh = view.findViewById(R.id.refresh);
        refresh.setOnRefreshLoadMoreListener(this);
        refresh.setEnableLoadMore(false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        adapter = new MessageAdapter(R.layout.item_message, null);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (adapter.getData() != null && adapter.getData().size() > position) {
                    dxbmob o = (dxbmob) adapter.getData().get(position);
                    if (o != null) {
                        showMessage(o);
                    }
                }
            }
        });
        adapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                if (adapter.getData() != null && adapter.getData().size() > position) {
                    dxbmob o = (dxbmob) adapter.getData().get(position);
                    if (o != null) {
                        deleteMessage(o, position);
                    }
                }
                return true;
            }
        });
//        recyclerView.setAdapter(adapter);
        baseMultiItemQuickAdapter = new MessageBaseMultiItemQuickAdapter(getContext(), null);
        baseMultiItemQuickAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (adapter.getData() != null && adapter.getData().size() > position) {
                    MultiItemEntity o = (MultiItemEntity) adapter.getData().get(position);

                    if (o != null) {
                        if (o.getItemType() == MessageBaseMultiItemQuickAdapter.TYPE_LEVEL_0) {
                            final dxbmobHeader lv0 = (dxbmobHeader) o;
                            if (!lv0.isExpanded()) {
                                adapter.expand(position);
                            }
                        }
                        switch (view.getId()) {
                            case R.id.btn_delete:
                                deleteMessage(adapter, position);
                                break;
                            case R.id.btn_download:
                                checkPermission(adapter, position);
                                break;
                        }
                    }
                }
            }
        });
        recyclerView.setAdapter(baseMultiItemQuickAdapter);
    }

    private void checkPermission(final BaseQuickAdapter adapter, final int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            RxPermissions rxPermission = new RxPermissions(getActivity());
            rxPermission.requestEachCombined(

                    Manifest.permission.READ_EXTERNAL_STORAGE,//sd卡读取
                    Manifest.permission.WRITE_EXTERNAL_STORAGE//sd卡写入
            )
                    .subscribe(new Consumer<Permission>() {
                        @Override
                        public void accept(Permission permission) throws Exception {
                            if (permission.granted) {
                                // 用户已经同意该权限
                                downloadMessage(adapter, position);
                            } else if (permission.shouldShowRequestPermissionRationale) {
                                // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
//                                                ToastUtils.show(getString(R.string.tips_request_storage_permission));

                                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
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
                                Uri packageURI = Uri.parse("package:" + getContext().getPackageName());
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                                startActivity(intent);
                            }
                        }
                    });

        } else {
            downloadMessage(adapter, position);
        }
    }

    private void downloadMessage(BaseQuickAdapter adapter, int position) {
        if (adapter != null) {
            if (adapter.getData() != null && adapter.getData().size() > position) {
                String fileName = null;
                StringBuilder builder = new StringBuilder();

                for (int i = position; i < adapter.getData().size(); i++) {
                    MultiItemEntity o = (MultiItemEntity) adapter.getData().get(i);
                    if (i == position) {
                        final dxbmobHeader lv0 = (dxbmobHeader) o;
                        fileName = lv0.getPerson() + "--" + lv0.getCode();
                        continue;
                    }
                    if (o.getItemType() == MessageBaseMultiItemQuickAdapter.TYPE_LEVEL_0 || i == adapter.getData().size() - 1) {
                        Toast.makeText(getContext(), "下载到 " + FileUtils.FileLog("message_" + fileName, builder.toString()), Toast.LENGTH_SHORT).show();
                        break;
                    }
                    final dxbmob dxbmob = (dxbmob) o;
                    builder = builder.append(dxbmob.toString()).append("\n\n");
                }

            }
        }
    }

    private void showMessage(dxbmob o) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("短信详情")
                .setMessage("号码：" + o.getphone() + "\n\n"
                        + "内容：" + o.getnr())
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.show();
    }

    private void deleteMessage(final dxbmob o, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("注意")
                .setMessage("请确定是否需要删除此条信息")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, int i) {
                        final dxbmob p2 = new dxbmob();
                        p2.setObjectId(o.getObjectId());
                        p2.delete(new UpdateListener() {

                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    Toast.makeText(getContext(), "删除成功", Toast.LENGTH_SHORT).show();
                                    adapter.remove(position);
                                    dialogInterface.dismiss();
                                } else {
                                    Toast.makeText(getContext(), "删除失败", Toast.LENGTH_SHORT).show();
                                }
                            }

                        });
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.show();
    }

    private void deleteMessage(BaseQuickAdapter adapter, int position) {
        if (adapter != null) {
            if (adapter.getData() != null && adapter.getData().size() > position) {
                for (int i = position + 1; i < adapter.getData().size(); i++) {
                    MultiItemEntity o = (MultiItemEntity) adapter.getData().get(i);
                    if (o.getItemType() == MessageBaseMultiItemQuickAdapter.TYPE_LEVEL_0) {
                        break;
                    }
                    final dxbmob dxbmob1 = (dxbmob) adapter.getData().get(i);
                    Log.e("sfsdrrfgger", i + "   " + dxbmob1.getObjectId());
                    deleteMessage(adapter, dxbmob1.getObjectId(), i);
                }
            }
            refresh.autoRefresh(1000, 1000, 1, false);
        }
    }

    private void deleteMessage(final BaseQuickAdapter adapter, String objectId, final int position) {
        final dxbmob p2 = new dxbmob();
        p2.setObjectId(objectId);
        p2.delete(new UpdateListener() {

            @Override
            public void done(BmobException e) {
                if (e == null) {
                    Log.e("fafasfsdqqda", "删除成功");
                } else {
                    Log.e("fafasfsdqqda", e.getMessage());
                }
            }

        });
    }

    public void saveMessage() {

    }

    /**
     * 删除所有信息
     */
    public void deleteMessage() {
        if (adapter != null) {
            if (adapter.getData() != null && adapter.getData().size() != 0) {
                for (int i = 0; i < adapter.getData().size(); i++) {
                    dxbmob o = adapter.getData().get(i);
                    if (o != null) {
                        final dxbmob p2 = new dxbmob();
                        p2.setObjectId(o.getObjectId());
                        p2.delete(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {

                                } else {

                                }
                            }
                        });
                    }
                }
                checkCount();

            }
        }
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        page++;
        getData(2);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
//        page = 0;
        doCheck();
    }
}
