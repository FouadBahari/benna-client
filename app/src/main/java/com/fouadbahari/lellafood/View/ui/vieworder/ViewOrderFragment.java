package com.fouadbahari.lellafood.View.ui.vieworder;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.Toast;

import com.fouadbahari.lellafood.CallBack.IloadOrderCallBackListener;
import com.fouadbahari.lellafood.CallBack.MyButtonClickListener;
import com.fouadbahari.lellafood.Common.Common;
import com.fouadbahari.lellafood.Common.MySwipeHelper;
import com.fouadbahari.lellafood.Controller.MyOrdersAdapter;
import com.fouadbahari.lellafood.Database.CartDataSource;
import com.fouadbahari.lellafood.Database.CartDatabase;
import com.fouadbahari.lellafood.Database.CartItem;
import com.fouadbahari.lellafood.Database.LocalCartDataSource;
import com.fouadbahari.lellafood.EventBus.CounterCartEvent;
import com.fouadbahari.lellafood.EventBus.MenuItemBack;
import com.fouadbahari.lellafood.Model.OrderModel;
import com.fouadbahari.lellafood.Model.ShippingOrderModel;
import com.fouadbahari.lellafood.R;
import com.fouadbahari.lellafood.View.TrackingOrderActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ViewOrderFragment extends Fragment implements IloadOrderCallBackListener {

    private ViewOrderViewModel mViewModel;
    private CartDataSource cartDataSource;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    @BindView(R.id.recycler_view_order)
    RecyclerView recycler_view_order;

    private Unbinder unbinder;
    private AlertDialog dialog;
    private IloadOrderCallBackListener listener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = ViewModelProviders.of(this).get(ViewOrderViewModel.class);
        View root = inflater.inflate(R.layout.view_order_fragment, container, false);
        unbinder = ButterKnife.bind(this, root);

        initViews(root);
        loadOrdersFromFireBase();

        mViewModel.getMutableLiveDataOrderList().observe(getViewLifecycleOwner(), new Observer<List<OrderModel>>() {
            @Override
            public void onChanged(List<OrderModel> orderModelList) {

                MyOrdersAdapter adapter = new MyOrdersAdapter(getContext(), orderModelList);
                recycler_view_order.setAdapter(adapter);


            }
        });

        return root;
    }

    private void loadOrdersFromFireBase() {
        final List<OrderModel> orderModelList = new ArrayList<>();
        FirebaseDatabase.getInstance()
                .getReference(Common.RESTAURANT_RREF)
                .child(Common.selectedRestaurant.getUid())
                .child(Common.ORDER_REF)
                .orderByChild("userId")
                .equalTo(Common.currentUser.getUid())
                .limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                            OrderModel orderModel = orderSnapshot.getValue(OrderModel.class);
                            orderModel.setOrderNumber(orderSnapshot.getKey());
                            orderModelList.add(orderModel);
                        }
                        listener.onLoadOrderSuccess(orderModelList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        listener.onLoadPrderFailed(error.getMessage());
                    }
                });
    }

    private void initViews(View root) {
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());
        listener = this;
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();


        recycler_view_order.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_view_order.setLayoutManager(layoutManager);
        recycler_view_order.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));


//        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(), recycler_view_order, 180) {
//            @Override
//            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
//
//                buf.add(new MyButton(getContext(), "Cancel", 20, 0, Color.parseColor("#d63031"),
//                        new MyButtonClickListener() {
//                            @Override
//                            public void onClick(final int pos) {
//
//
//                            }
//                        }));

//                buf.add(new MyButton(getContext(), "Tracking Order", 20, 0, Color.parseColor("#001970"),
//                        new MyButtonClickListener() {
//                            @Override
//                            public void onClick( int pos) {
//
//                            }
//                        }));

//                buf.add(new MyButton(getContext(), "Repeat Order", 20, 0, Color.parseColor("#5d4037"),
//                        pos -> {
//
//                    OrderModel orderModel = ((MyOrdersAdapter)recycler_view_order.getAdapter()).getItemAtPosition(pos);
//                            dialog.show();
//                            cartDataSource.cleanCart(Common.currentUser.getUid(),Common.selectedRestaurant.getUid())
//                                    .subscribeOn(Schedulers.io())
//                                    .observeOn(AndroidSchedulers.mainThread())
//                                    .subscribe(new SingleObserver<Integer>() {
//                                        @Override
//                                        public void onSubscribe(Disposable d) {
//
//                                        }
//
//                                        @Override
//                                        public void onSuccess(Integer integer) {
//                                            CartItem[] cartItems = orderModel
//                                                    .getCartItemList().toArray(new CartItem[orderModel.getCartItemList().size()]);
//
//                                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItems)
//                                            .subscribeOn(Schedulers.io())
//                                                    .observeOn(AndroidSchedulers.mainThread())
//                                                    .subscribe(()->{
//                                                        dialog.dismiss();
//                                                        Toast.makeText(getContext(), "Add all item in order success", Toast.LENGTH_SHORT).show();
//                                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
//                                                    },throwable -> {
//                                                        dialog.dismiss();
//                                                        Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
//                                                    })
//                                            );}
//
//                                        @Override
//                                        public void onError(Throwable e) {
//                                            dialog.dismiss();
//                                            Toast.makeText(getContext(), "[Error]"+e.getMessage(), Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//
//
//                        }));

    }


    @Override
    public void onLoadOrderSuccess(List<OrderModel> orderModelList) {

        dialog.dismiss();
        mViewModel.setMutableLiveDataOrderList(orderModelList);
    }

    @Override
    public void onLoadPrderFailed(String message) {

        dialog.dismiss();
        Toast.makeText(getContext(), "" + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }
}