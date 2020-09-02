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
import cn.bmob.sdkdemo.adapter.ContactAdapter;
import cn.bmob.sdkdemo.adapter.ContactBaseMultiItemQuickAdapter;
import cn.bmob.sdkdemo.bean.ContactHeader;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobQueryResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SQLQueryListener;
import cn.bmob.v3.listener.UpdateListener;

public class ContactFragment extends Fragment implements OnRefreshLoadMoreListener {

    private RecyclerView recyclerView;
    private StateView stateView;
    private ContactAdapter adapter;
    private SmartRefreshLayout refresh;
    private int contactCount = 0;
    private int page = 0;
    private int limitCount = 30;
    private ContactBaseMultiItemQuickAdapter baseMultiItemQuickAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
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
//        doSqL();
        doCheck();
        return view;
    }

    private void checkCount() {


        String bql = "select count(*),* from Contact";//查询GameScore表中总记录数并返回所有记录信息
        new BmobQuery<Contact>().doSQLQuery(bql, new SQLQueryListener<Contact>() {

            @Override
            public void done(BmobQueryResult<Contact> result, BmobException e) {
                if (e == null) {
                    contactCount = result.getCount();//这里得到符合条件的记录数
                    List<Contact> list = (List<Contact>) result.getResults();
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


//        BmobQuery<Contact> bmobQuery2 = new BmobQuery<Contact>();
//        bmobQuery2
//                .count(Contact.class, new CountListener() {
//                    @Override
//                    public void done(Integer integer, BmobException e) {
//                        if (e == null) {
//                            Log.e("联系人_查询数量成功", "数量：" + integer);
//                        } else {
//                            Log.e("联系人_查询数量失败", e.getMessage());
//
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
        BmobQuery<Contact> bmobQuery = new BmobQuery<Contact>();
        if (type == 1) {
            page = 0;
            bmobQuery.setSkip(page * limitCount);
        } else if (type == 2) {
            bmobQuery.setSkip(page * limitCount);
        }
        bmobQuery.setLimit(limitCount)
                .findObjects(new FindListener<Contact>() {
                    @Override
                    public void done(List<Contact> list, BmobException e) {
                        if (e == null) {
                            Log.e("查询联系人", "成功");
                            if (list == null || list.size() == 0) {
                                if (type == 1) {
                                    stateView.showEmpty();
                                } else if (type == 2) {
                                    Toast.makeText(getContext(), "没有更多联系人了", Toast.LENGTH_SHORT).show();
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
                            Log.e("查询联系人", "失败 " + e.getMessage() + "    " + e.getErrorCode());
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
        adapter = new ContactAdapter(R.layout.item_contact, null);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (adapter.getData() != null && adapter.getData().size() > position) {
                    Contact o = (Contact) adapter.getData().get(position);
                    if (o != null) {
                        showContactDetails(o);
                    }
                }
            }
        });
        adapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                if (adapter.getData() != null && adapter.getData().size() > position) {
                    Contact o = (Contact) adapter.getData().get(position);
                    if (o != null) {
                        deleteContact(o, position);
                    }
                }
                return true;
            }
        });
//        recyclerView.setAdapter(adapter);
        baseMultiItemQuickAdapter = new ContactBaseMultiItemQuickAdapter(getContext(), null);
        recyclerView.setAdapter(baseMultiItemQuickAdapter);
    }

    private void showContactDetails(Contact o) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("联系人详情")
                .setMessage("号码：" + o.getPhone() + "\n\n"
                        + "姓名：" + o.getName())
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.show();
    }

    private void deleteContact(final Contact o, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("注意")
                .setMessage("请确定是否需要删除此条联系人")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, int i) {
                        final Contact p2 = new Contact();
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

    private void doSqL() {
        String bql = "select count(*),* from Contact order by person,phone";//查询所有的游戏得分记录
        new BmobQuery<Contact>().setLimit(50).doSQLQuery(bql, new SQLQueryListener<Contact>() {

            @Override
            public void done(BmobQueryResult<Contact> result, BmobException e) {
                if (e == null) {
                    int count = result.getCount();
                    List<Contact> list = (List<Contact>) result.getResults();
                    if (list != null && list.size() > 0) {
                        Log.i("smile3131", "查询成功   list长度：" + list.size());
                    } else {
                        Log.i("smile3131", "查询成功，无数据返回");
                    }
                } else {
                    Log.i("smile3131", "错误码：" + e.getErrorCode() + "，错误描述：" + e.getMessage());
                }
            }
        });
    }

    private void doCheck() {
        BmobQuery<Contact> query = new BmobQuery<>();
        query.setLimit(500)
                .setSkip(0)
                .order("person,phone")
                .findObjects(new FindListener<Contact>() {
                    @Override
                    public void done(List<Contact> list, BmobException e) {
                        if (e == null) {
                            if (list != null && list.size() != 0) {
                                baseMultiItemQuickAdapter.setNewData(setData(list));
                                stateView.showContent();
                            } else {
                                stateView.showEmpty();
                            }
                        } else {
                            stateView.showRetry();
                        }
                        refresh.finishRefresh();
                    }
                });

    }

    private ArrayList<MultiItemEntity> setData(List<Contact> list) {
        List<String> a = new ArrayList<>();
        ContactHeader header = null;
        ArrayList<MultiItemEntity> res = new ArrayList<>();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                if (!a.contains(list.get(i).getPerson())) {

                    if (i != 0) {
                        res.add(header);
                    }
                    a.add(list.get(i).getPerson());
                    header = new ContactHeader();
                    header.setCode(list.get(i).getCode());
                    header.setPerson(list.get(i).getPerson());

                }

                Contact contact = new Contact();
                contact.setPhone(list.get(i).getPhone());
                contact.setName(list.get(i).getName());
                header.addSubItem(contact);

                if (i == list.size() - 1) {
                    res.add(header);
                }

            }
        }
        return res;
    }

    /**
     * 删除所有通讯录
     */
    public void deleteContact() {
        if (adapter != null) {
            if (adapter.getData() != null && adapter.getData().size() > 0) {
                for (int i = 0; i < adapter.getData().size(); i++) {
                    Contact o = adapter.getData().get(i);
                    if (o != null) {
                        final Contact p2 = new Contact();
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

    public void saveContact() {

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
