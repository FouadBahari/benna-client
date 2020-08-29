package com.fouadbahari.lellafood.View.ui.vieworder;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.fouadbahari.lellafood.CallBack.IloadOrderCallBackListener;
import com.fouadbahari.lellafood.Common.Common;
import com.fouadbahari.lellafood.Controller.MyOrdersAdapter;
import com.fouadbahari.lellafood.EventBus.MenuItemBack;
import com.fouadbahari.lellafood.Model.Order;
import com.fouadbahari.lellafood.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class ViewOrderFragment extends Fragment implements IloadOrderCallBackListener {

    private ViewOrderViewModel mViewModel;

    @BindView(R.id.recycler_view_order)
    RecyclerView recycler_view_order;

    private Unbinder unbinder;
    private AlertDialog dialog;
    private IloadOrderCallBackListener listener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel=ViewModelProviders.of(this).get(ViewOrderViewModel.class);
        View root= inflater.inflate(R.layout.view_order_fragment, container, false);
        unbinder= ButterKnife.bind(this,root);

        initViewa(root);
        loadOrdersFromFireBase();

        mViewModel.getMutableLiveDataOrderList().observe(getViewLifecycleOwner(), new Observer<List<Order>>() {
            @Override
            public void onChanged(List<Order> orderList) {

                MyOrdersAdapter adapter =new MyOrdersAdapter(getContext(),orderList);
                recycler_view_order.setAdapter(adapter);


            }
        });

        return root;
    }

    private void loadOrdersFromFireBase() {
        final List<Order> orderList=new ArrayList<>();
        FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
                .orderByChild("userId")
                .equalTo(Common.currentUser.getUid())
                .limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot orderSnapshot:snapshot.getChildren())
                        {
                            Order order=orderSnapshot.getValue(Order.class);
                            order.setOrderNumber(orderSnapshot.getKey());
                            orderList.add(order);
                        }
                        listener.onLoadOrderSuccess(orderList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        listener.onLoadPrderFailed(error.getMessage());
                    }
                });
    }

    private void initViewa(View root) {

        listener=this;
        dialog=new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();

        recycler_view_order.setHasFixedSize(true);
        LinearLayoutManager layoutManager =new LinearLayoutManager(getContext());
        recycler_view_order.setLayoutManager(layoutManager);
        recycler_view_order.addItemDecoration(new DividerItemDecoration(getContext(),layoutManager.getOrientation()));



    }


    @Override
    public void onLoadOrderSuccess(List<Order> orderList) {

        dialog.dismiss();
        mViewModel.setMutableLiveDataOrderList(orderList);
    }

    @Override
    public void onLoadPrderFailed(String message) {

        dialog.dismiss();
        Toast.makeText(getContext(), ""+message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}