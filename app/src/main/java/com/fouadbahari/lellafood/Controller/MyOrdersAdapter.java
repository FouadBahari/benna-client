package com.fouadbahari.lellafood.Controller;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fouadbahari.lellafood.CallBack.IRecyclerClickListener;
import com.fouadbahari.lellafood.Common.Common;
import com.fouadbahari.lellafood.Database.CartItem;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyOrdersAdapter extends RecyclerView.Adapter<MyOrdersAdapter.MyViewHolder> {

    private Context context;
    private List<OrderModel> orderModelList;
    private Calendar calendar;
    private SimpleDateFormat simpleDateFormat;

    public MyOrdersAdapter(Context context, List<OrderModel> orderModelList) {
        this.context = context;
        this.orderModelList = orderModelList;
        calendar=Calendar.getInstance();
        simpleDateFormat=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    }

    public OrderModel getItemAtPosition(int pos){
        return orderModelList.get(pos);
    }

    public void setItemAtPosition(int pos , OrderModel item){
        orderModelList.set(pos,item);
    }

    @NonNull
    @Override
    public MyOrdersAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_order_item,parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyOrdersAdapter.MyViewHolder holder, int position) {

        Glide.with(context).load(orderModelList.get(position).getCartItemList().get(0).getFoodImage())
                .into(holder.img_order);
        calendar.setTimeInMillis(orderModelList.get(position).getCreateDate());
        Date date=new Date(orderModelList.get(position).getCreateDate());
        holder.txt_order_date.setText(new StringBuilder(Common.getDateOfWeek(calendar.get(Calendar.DAY_OF_WEEK)))
        .append("")
        .append(simpleDateFormat.format(date)));
        holder.txt_order_number.setText(new StringBuilder("OrderModel No: ").append(orderModelList.get(position).getOrderNumber()));
        holder.txt_order_comment.setText(new StringBuilder("Comment: ").append(orderModelList.get(position).getComment()));
        holder.txt_order_status.setText(new StringBuilder("Status: ").append(Common.convertStatusToText(orderModelList.get(position).getOrderStatus())));

        holder.setiRecyclerClickListener(new IRecyclerClickListener() {
            @Override
            public void onItemClickListener(View view, int pos) {
                showDialog(orderModelList.get(pos).getCartItemList());

            }
        });

        holder.btn_track_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OrderModel orderModel = orderModelList.get(position);

                FirebaseDatabase.getInstance()
                        .getReference(Common.SHIPPING_ORDER_REF)
                        .child(orderModel.getOrderNumber())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists())
                                {
                                    Common.currentShippingOrder = snapshot.getValue(ShippingOrderModel.class);
                                    Common.currentShippingOrder.setKey(snapshot.getKey());
                                    if (Common.currentShippingOrder.getCurrentLat() != -1 && Common.currentShippingOrder.getCurrentLng() != -1){

                                        if (orderModelList.get(position).getOrderStatus() == 1){
                                            context.startActivity(new Intent(context, TrackingOrderActivity.class));
                                        }else {
                                            Toast.makeText(context, "Your Order has been shipped!", Toast.LENGTH_SHORT).show();
                                        }
                                    }else {
                                        Toast.makeText(context, "Shipper didn't start yet, please wait!", Toast.LENGTH_SHORT).show();
                                    }

                                }else {
                                    Toast.makeText(context, "Your Order just placed, please wait!", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(context, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        holder.btn_cancel_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 OrderModel orderModel = orderModelList.get(position);
                if (orderModel.getOrderStatus() == 0){
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                    builder.setTitle("Cancel OrderModel")
                            .setMessage("Do you really want to cancel this orderModel")
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Map<String,Object> update_data = new HashMap<>();
                            update_data.put("orderStatus",-1);
                            FirebaseDatabase.getInstance()
                                    .getReference(Common.RESTAURANT_RREF)
                                    .child(Common.selectedRestaurant.getUid())
                                    .child(Common.ORDER_REF)
                                    .child(orderModel.getOrderNumber())
                                    .updateChildren(update_data)
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    orderModel.setOrderStatus(-1);
                                    setItemAtPosition(position, orderModel);
                                    notifyItemChanged(position);
                                    Toast.makeText(context, "Cancel OrderModel successfully!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                    androidx.appcompat.app.AlertDialog dialog =builder.create();
                    dialog.show();
                }else {
                    Toast.makeText(context, new StringBuilder("Your orderModel was changed to ")
                            .append(Common.convertStatusToText(orderModel.getOrderStatus()))
                            .append(" so you can't cancel it!"), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showDialog(List<CartItem> cartItemList) {
        View layout_dialog = LayoutInflater.from(context).inflate(R.layout.layout_dialog_order_detail,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(layout_dialog);
        Button btn_ok = (Button) layout_dialog.findViewById(R.id.btn_ok);
        RecyclerView recycler_order_detail = (RecyclerView)layout_dialog.findViewById(R.id.recycler_order_detail);
        recycler_order_detail.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recycler_order_detail.setLayoutManager(layoutManager);
        recycler_order_detail.addItemDecoration(new DividerItemDecoration(context,layoutManager.getOrientation()));

        MyOrderDetailsAdapter myOrderDetailsAdapter = new MyOrderDetailsAdapter(context,cartItemList );
        recycler_order_detail.setAdapter(myOrderDetailsAdapter);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });


    }


    @Override
    public int getItemCount() {
        return orderModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        @BindView(R.id.img_order)
        ImageView img_order;
        @BindView(R.id.txt_order_status)
        TextView txt_order_status;
        @BindView(R.id.txt_order_comment)
        TextView txt_order_comment;
        @BindView(R.id.txt_order_number)
        TextView txt_order_number;
        @BindView(R.id.txt_order_date)
        TextView txt_order_date;
        @BindView(R.id.btn_track_order)
        Button btn_track_order;
        @BindView(R.id.btn_cancel_order)
        Button btn_cancel_order;

        Unbinder unbinder;


        IRecyclerClickListener iRecyclerClickListener;

        public void setiRecyclerClickListener(IRecyclerClickListener iRecyclerClickListener) {
            this.iRecyclerClickListener = iRecyclerClickListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            unbinder= ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerClickListener.onItemClickListener(view,getAdapterPosition());
        }
    }
}
