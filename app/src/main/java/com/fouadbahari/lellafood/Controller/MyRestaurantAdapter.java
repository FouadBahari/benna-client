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
import com.fouadbahari.lellafood.CallBack.IRecyclerClickListener;

import com.fouadbahari.lellafood.Common.Common;
import com.fouadbahari.lellafood.EventBus.MenuItemEvent;
import com.fouadbahari.lellafood.Model.RestaurantModel;
import com.fouadbahari.lellafood.R;


import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyRestaurantAdapter extends RecyclerView.Adapter<MyRestaurantAdapter.MyViewHolder>{

    Context context;
    List<RestaurantModel> restaurantModelList;

    public MyRestaurantAdapter(Context context, List<RestaurantModel> restaurantModelList) {
        this.context = context;
        this.restaurantModelList = restaurantModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).
                inflate(R.layout.layout_restaurant_item,parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context)
                .load(restaurantModelList.get(position).getImageUrl())
                .into(holder.img_restaurant);
        Glide.with(context)
                .load(restaurantModelList.get(position).getProfileUrl())
                .into(holder.img_profile_id);
        holder.txt_restaurant_name.setText(restaurantModelList.get(position).getName());
        holder.txt_restaurant_address.setText(restaurantModelList.get(position).getAddress());

        // Event
        holder.setListener((view, pos) -> {
            Common.selectedRestaurant = restaurantModelList.get(pos);
            EventBus.getDefault().postSticky(new MenuItemEvent(true,restaurantModelList.get(pos)));

        });

    }

    @Override
    public int getItemCount() {
        return restaurantModelList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Unbinder unbinder;
        @BindView(R.id.img_restaurant)
        ImageView img_restaurant;

        @BindView(R.id.img_profile_id)
        ImageView img_profile_id;

        @BindView(R.id.txt_restaurant_name)
        TextView txt_restaurant_name;

        @BindView(R.id.txt_restaurant_address)
        TextView txt_restaurant_address;

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
