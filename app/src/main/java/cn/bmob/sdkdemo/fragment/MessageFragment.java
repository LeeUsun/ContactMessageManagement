package cn.bmob.sdkdemo.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.bmob.sdkdemo.Contact;
import cn.bmob.sdkdemo.R;
import cn.bmob.sdkdemo.adapter.MessageAdapter;
import cn.bmob.sdkdemo.adapter.MessageBaseMultiItemQuickAdapter;
import cn.bmob.sdkdemo.bean.ContactHeader;
import cn.bmob.sdkdemo.bean.dxbmobHeader;
import cn.bmob.sdkdemo.dxbmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobQueryResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SQLQueryListener;
import cn.bmob.v3.listener.UpdateListener;

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
                                stateView.showEmpty();
                            }
                        } else {
                            stateView.showRetry();
                            Log.e("far23fsdfsd", e.getMessage());
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
        recyclerView.setAdapter(baseMultiItemQuickAdapter);
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
