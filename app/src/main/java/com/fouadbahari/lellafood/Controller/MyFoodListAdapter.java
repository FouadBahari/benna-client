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
import com.fouadbahari.lellafood.EventBus.CounterCartEvent;
import com.fouadbahari.lellafood.EventBus.FoodItemClick;
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

public class MyFoodListAdapter extends RecyclerView.Adapter<MyFoodListAdapter.MyViewHolder> {

    private Context context;
    private List<FoodModel> foodModelList;
    private CompositeDisposable compositeDisposable;
    private CartDataSource cartDataSource;

    public MyFoodListAdapter(Context context, List<FoodModel> foodModelList) {
        this.context = context;
        this.foodModelList = foodModelList;
        this.compositeDisposable=new CompositeDisposable();
        this.cartDataSource=new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
    }

    @NonNull
    @Override
    public MyFoodListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).
                inflate(R.layout.layout_food_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyFoodListAdapter.MyViewHolder holder, final int position) {

        Glide.with(context).load(foodModelList.get(position).getImage()).into(holder.imageFoodList);
        holder.textFoodName.setText(new StringBuilder("").append(foodModelList.get(position).getName()));
        holder.textFoodPrice.setText(new StringBuilder("").append(foodModelList.get(position).getPrice())
                            .append(" DZD"));

        holder.setListener(new IRecyclerClickListener() {
            @Override
            public void onItemClickListener(View view, int pos) {
                Common.selectedFood  = foodModelList.get(pos);
                Common.selectedFood.setKey(String.valueOf(pos));
                EventBus.getDefault().postSticky(new FoodItemClick(true,foodModelList.get(pos)));

            }
        });

        holder.shoppingImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CartItem cartItem =new CartItem();
                cartItem.setUid(Common.currentUser.getUid());
                cartItem.setUserPhone(Common.currentUser.getPhone());
                cartItem.setRestaurantId(Common.selectedRestaurant.getUid());

                cartItem.setCategoryId(Common.categorySelected.getMenu_id());
                cartItem.setFoodId(foodModelList.get(position).getId());
                cartItem.setFoodName(foodModelList.get(position).getName());
                cartItem.setFoodImage(foodModelList.get(position).getImage());
                cartItem.setFoodPrice(Double.valueOf(String.valueOf(foodModelList.get(position).getPrice())));
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
        });
    }

    @Override
    public int getItemCount() {
        return foodModelList.size();
    }

    public List<FoodModel> getFoodList() {
        return foodModelList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Unbinder unbinder;

        @BindView(R.id.textFoodNameId)
        TextView textFoodName;
        @BindView(R.id.textFoodPriceId)
        TextView textFoodPrice;
        @BindView(R.id.imageFoodListId)
        ImageView imageFoodList;
        @BindView(R.id.imageShoppingId)
        ImageView shoppingImage;


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
}
