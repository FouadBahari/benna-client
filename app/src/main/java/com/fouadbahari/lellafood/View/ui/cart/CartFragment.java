package com.fouadbahari.lellafood.View.ui.cart;

import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fouadbahari.lellafood.CallBack.MyButtonClickListener;
import com.fouadbahari.lellafood.Common.Common;
import com.fouadbahari.lellafood.Common.MySwipeHelper;
import com.fouadbahari.lellafood.Controller.MyCartAdapter;
import com.fouadbahari.lellafood.Database.CartDataSource;
import com.fouadbahari.lellafood.Database.CartDatabase;
import com.fouadbahari.lellafood.Database.CartItem;
import com.fouadbahari.lellafood.Database.LocalCartDataSource;
import com.fouadbahari.lellafood.EventBus.CounterCartEvent;
import com.fouadbahari.lellafood.EventBus.HideFABCart;
import com.fouadbahari.lellafood.EventBus.UpdateItemCart;
import com.fouadbahari.lellafood.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CartFragment extends Fragment {

    private CartViewModel mViewModel;
    private Parcelable recyclerViewState;
    private CartDataSource cartDataSource;
    private  MyCartAdapter adapter;

    @BindView(R.id.recycle_cart)
    RecyclerView recycle_cart;
    @BindView(R.id.txt_total_price)
    TextView txt_total_price;
    @BindView(R.id.txt_empty_cart)
    TextView txt_empty_cart;
    @BindView(R.id.groupe_place_holder)
    CardView groupe_place_holder;


    private Unbinder unbinder;

    public static CartFragment newInstance() {
        return new CartFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel= ViewModelProviders.of(this).get(CartViewModel.class);
       View root= inflater.inflate(R.layout.cart_fragment, container, false);
       mViewModel.initCartDataSource(getContext());
       mViewModel.getMutableLiveDataCartItem().observe(getViewLifecycleOwner(), new Observer<List<CartItem>>() {
           @Override
           public void onChanged(List<CartItem> cartItems) {

               if (cartItems==null || cartItems.isEmpty()  )
               {

                   recycle_cart.setVisibility(View.GONE);
                   groupe_place_holder.setVisibility(View.GONE);
                   txt_empty_cart.setVisibility(View.VISIBLE);

               }else {
                   recycle_cart.setVisibility(View.VISIBLE);
                   groupe_place_holder.setVisibility(View.VISIBLE);
                   txt_empty_cart.setVisibility(View.GONE);

                   adapter=new MyCartAdapter(getContext(),cartItems);
                   recycle_cart.setAdapter(adapter);

               }

           }
       });
       unbinder= ButterKnife.bind(this,root);
       initView();
       return root;
    }

    private void initView() {

        setHasOptionsMenu(true);
        cartDataSource= new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

        EventBus.getDefault().postSticky(new HideFABCart(true));
        recycle_cart.setHasFixedSize(true);
        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());
        recycle_cart.setLayoutManager(layoutManager);
        recycle_cart.addItemDecoration(new DividerItemDecoration(getContext(),layoutManager.getOrientation()));

        MySwipeHelper mySwipeHelper=new MySwipeHelper(getContext(),recycle_cart,200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {

          buf.add(new MyButton(getContext(),"Delete",30,0, Color.parseColor("#d63031"),
                  new MyButtonClickListener() {
                      @Override
                      public void onClick(final int pos) {

                          CartItem cartItem=adapter.getItemAtPosition(pos);
                          cartDataSource.deleteCartItem(cartItem)
                                  .subscribeOn(Schedulers.io())
                                  .observeOn(AndroidSchedulers.mainThread())
                                  .subscribe(new SingleObserver<Integer>() {
                                      @Override
                                      public void onSubscribe(Disposable d) {


                                      }

                                      @Override
                                      public void onSuccess(Integer integer) {

                                          adapter.notifyItemRemoved(pos);
                                          sumAllItemInCart();
                                          EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                          Toast.makeText(getContext(), "Delete item from cart successful", Toast.LENGTH_SHORT).show();


                                      }

                                      @Override
                                      public void onError(Throwable e) {

                                          Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                      }
                                  });

                      }
                  }));
            }
        };

        sumAllItemInCart();
    }

    private void sumAllItemInCart() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {


                    }

                    @Override
                    public void onSuccess(Double aDouble) {

                        txt_total_price.setText(new StringBuilder().append(aDouble));
                    }

                    @Override
                    public void onError(Throwable e) {

                        if (!e.getMessage().contains("Query returned empty"))
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();


                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

    }

    @Override
    public void onStop() {
        EventBus.getDefault().postSticky(new HideFABCart(false));
        mViewModel.onStop();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onUpdateItemInCartEvent(UpdateItemCart event)
    {
        if (event.getCartItem()!=null)
        {

            recyclerViewState =recycle_cart.getLayoutManager().onSaveInstanceState();
            cartDataSource.updateCartItems(event.getCartItem())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {


                            calculateTotalPrice();
                            recycle_cart.getLayoutManager().onRestoreInstanceState(recyclerViewState ) ;

                        }

                        @Override
                        public void onError(Throwable e) {

                            Toast.makeText(getContext(), "[CART ERROR]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.action_settings).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.cart_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.delete_cart)
        {
            cartDataSource.cleanCart(Common.currentUser.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {


                        }

                        @Override
                        public void onSuccess(Integer integer) {

                            Toast.makeText(getContext(), "Clean cart success", Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                        }

                        @Override
                        public void onError(Throwable e) {

                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        return super.onOptionsItemSelected(item);
    }

    private void calculateTotalPrice() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double price) {

                        txt_total_price.setText(new StringBuilder("Total: ")
                        .append(Common.formatPrice(price)));
                    }

                    @Override
                    public void onError(Throwable e) {

                        Toast.makeText(getContext(), "[SUM ERROR]"+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }
}