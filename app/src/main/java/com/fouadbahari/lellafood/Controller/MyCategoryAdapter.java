package com.fouadbahari.lellafood.Controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fouadbahari.lellafood.CallBack.IRecyclerClickListener;
import com.fouadbahari.lellafood.Common.Common;
import com.fouadbahari.lellafood.Database.CartDataSource;
import com.fouadbahari.lellafood.Database.CartDatabase;
import com.fouadbahari.lellafood.Database.CartItem;
import com.fouadbahari.lellafood.Database.LocalCartDataSource;
import com.fouadbahari.lellafood.EventBus.CategoryClick;
import com.fouadbahari.lellafood.EventBus.CounterCartEvent;
import com.fouadbahari.lellafood.Model.CategoryModel;
import com.fouadbahari.lellafood.Model.FoodModel;
import com.fouadbahari.lellafood.R;

import org.greenrobot.eventbus.EventBus;

import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MyCategoryAdapter extends RecyclerView.Adapter<MyCategoryAdapter.MyViewHolder> {

    Context context;
    List<CategoryModel> categoryModelList;
    private CompositeDisposable compositeDisposable;
    private CartDataSource cartDataSource;

    public MyCategoryAdapter(Context context, List<CategoryModel> categoryModelList) {
        this.context = context;
        this.categoryModelList = categoryModelList;
        this.compositeDisposable=new CompositeDisposable();
        this.cartDataSource=new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());

    }

    @NonNull
    @Override
    public MyCategoryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).
                inflate(R.layout.layout_pack_item,parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull MyCategoryAdapter.MyViewHolder holder, int position) {

        Glide.with(context).load(categoryModelList.get(position).getImage()).into(holder.img_pack);
        holder.text_pack_name.setText(new StringBuilder(categoryModelList.get(position).getName()));
        holder.text_pack_price.setText(new StringBuilder(categoryModelList.get(position).getPrice())
                              .append(" DZD"));


        holder.setListener(new IRecyclerClickListener() {
            @Override
            public void onItemClickListener(View view, int position) {

                Common.categorySelected=categoryModelList.get(position);
                EventBus.getDefault().postSticky(new CategoryClick(true,categoryModelList.get(position)));
            }
        });

        holder.img_add_pack_to_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Common.categorySelected=categoryModelList.get(position);


                CartItem[] cartItems = new CartItem[Common.categorySelected.getFoods().size()];
                for (FoodModel foodModel : Common.categorySelected.getFoods()){

                    CartItem cartItem =new CartItem();
                    cartItem.setUid(Common.currentUser.getUid());
                    cartItem.setUserPhone(Common.currentUser.getPhone());
                    cartItem.setRestaurantId(Common.selectedRestaurant.getUid());
                    cartItem.setCategoryId(Common.categorySelected.getMenu_id());

                    cartItem.setFoodId(foodModel.getId());
                    cartItem.setFoodName(foodModel.getName());
                    cartItem.setFoodImage(foodModel.getImage());
                    cartItem.setFoodPrice(Double.valueOf(String.valueOf(foodModel.getPrice())));
                    cartItem.setFoodQuantity(1);

                    cartDataSource.getItemWhithAllOptionsInCart(Common.currentUser.getUid(),
                            Common.categorySelected.getMenu_id(),
                            cartItem.getFoodId(),
                            Common.selectedRestaurant.getUid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<CartItem>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onSuccess(CartItem cartItemFromDb) {

                                    if (cartItemFromDb.equals(cartItem))
                                    {

                                        cartItemFromDb.setFoodQuantity(cartItem.getFoodQuantity() +cartItem.getFoodQuantity());

                                        cartDataSource.updateCartItems(cartItemFromDb)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new SingleObserver<Integer>() {
                                                    @Override
                                                    public void onSubscribe(Disposable d) {

                                                    }

                                                    @Override
                                                    public void onSuccess(Integer integer) {

                                                        Toast.makeText(context, "Update Cart success", Toast.LENGTH_SHORT).show();
                                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));


                                                    }

                                                    @Override
                                                    public void onError(Throwable e) {

                                                        Toast.makeText(context, "[UPDATE CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });


                                    }else
                                    {
                                        compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Action() {
                                                    @Override
                                                    public void run() throws Exception {

                                                        Toast.makeText(context, "Add to cart success", Toast.LENGTH_SHORT).show();
                                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                                    }
                                                }, new Consumer<Throwable>() {
                                                    @Override
                                                    public void accept(Throwable throwable) throws Exception {
                                                        Toast.makeText(context, "[Cart Error]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }));
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {

                                    if (e.getMessage().contains("empty"))
                                    {
                                        compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Action() {
                                                    @Override
                                                    public void run() throws Exception {

                                                        Toast.makeText(context, "Add to cart success", Toast.LENGTH_SHORT).show();
                                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                                    }
                                                }, new Consumer<Throwable>() {
                                                    @Override
                                                    public void accept(Throwable throwable) throws Exception {
                                                        Toast.makeText(context, "[Cart Error]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }));


                                    }else
                                        Toast.makeText(context, "[CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryModelList.size();
    }

    public List<CategoryModel> getListCategory() {
        return categoryModelList;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Unbinder unbinder;
        @BindView(R.id.img_pack)
        ImageView img_pack;

        @BindView(R.id.img_add_pack_to_cart)
        ImageView img_add_pack_to_cart;

        @BindView(R.id.text_pack_name)
        TextView text_pack_name;

        @BindView(R.id.text_pack_price)
        TextView text_pack_price;



        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder= ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClickListener(v,getAdapterPosition());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (categoryModelList.size()==1)
            return Common.DEFAULT_COLUMN_COUNT;
        else
        {
            if (categoryModelList.size()%2==0)
                return Common.DEFAULT_COLUMN_COUNT;
            else
                return (position>1 && position ==categoryModelList.size()-1) ? Common.FULL_WIDTH_COLUMN:Common.DEFAULT_COLUMN_COUNT;
        }
    }
}
