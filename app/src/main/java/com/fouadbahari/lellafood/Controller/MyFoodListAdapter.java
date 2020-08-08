package com.fouadbahari.lellafood.Controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fouadbahari.lellafood.Model.FoodModel;
import com.fouadbahari.lellafood.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyFoodListAdapter extends RecyclerView.Adapter<MyFoodListAdapter.MyViewHolder> {

    private Context context;
    private List<FoodModel> foodModelList;

    public MyFoodListAdapter(Context context, List<FoodModel> foodModelList) {
        this.context = context;
        this.foodModelList = foodModelList;
    }

    @NonNull
    @Override
    public MyFoodListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).
                inflate(R.layout.layout_food_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyFoodListAdapter.MyViewHolder holder, int position) {

        Glide.with(context).load(foodModelList.get(position).getImage()).into(holder.imageFoodList);
        holder.textFoodName.setText(new StringBuilder("").append(foodModelList.get(position).getName()));
        holder.textFoodPrice.setText(new StringBuilder("$").append(foodModelList.get(position).getPrice()));

    }

    @Override
    public int getItemCount() {
        return foodModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private Unbinder unbinder;

        @BindView(R.id.textFoodNameId)
        TextView textFoodName;
        @BindView(R.id.textFoodPriceId)
        TextView textFoodPrice;
        @BindView(R.id.imageFoodListId)
        ImageView imageFoodList;
        @BindView(R.id.imageFavoId)
        ImageView favoritImage;
        @BindView(R.id.imageShoppingId)
        ImageView shoppingImage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder= ButterKnife.bind(this,itemView);
        }
    }
}
