package com.fouadbahari.lellafood.View.ui.cart;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fouadbahari.lellafood.Common.Common;
import com.fouadbahari.lellafood.Database.CartDataSource;
import com.fouadbahari.lellafood.Database.CartDatabase;
import com.fouadbahari.lellafood.Database.CartItem;
import com.fouadbahari.lellafood.Database.LocalCartDataSource;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class CartViewModel extends ViewModel {

    private CompositeDisposable compositeDisposable ;
    private CartDataSource cartDataSource;
    private MutableLiveData<List<CartItem>> mutableLiveDataCartItem;

    public CartViewModel() {
        compositeDisposable=new CompositeDisposable();
    }

    public void initCartDataSource(Context context){
        cartDataSource=new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());

    }
    public MutableLiveData<List<CartItem>> getMutableLiveDataCartItem() {
        if (mutableLiveDataCartItem == null)
            mutableLiveDataCartItem =new MutableLiveData<>();
        getAllCartItem();
        return mutableLiveDataCartItem;
    }

    private void getAllCartItem() {

        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid(),Common.selectedRestaurant.getUid())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<List<CartItem>>() {
            @Override
            public void accept(List<CartItem> cartItems) throws Exception {
                mutableLiveDataCartItem.setValue(cartItems);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                mutableLiveDataCartItem.setValue(null);
            }
        }));

    }

    public void onStop(){
        compositeDisposable.clear();
    }
}