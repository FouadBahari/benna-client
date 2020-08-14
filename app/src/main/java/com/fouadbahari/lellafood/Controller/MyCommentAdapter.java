package com.fouadbahari.lellafood.Controller;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fouadbahari.lellafood.Model.CommentModel;
import com.fouadbahari.lellafood.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyCommentAdapter extends RecyclerView.Adapter<MyCommentAdapter.MyViewHolder> {

    Context context;
    List<CommentModel> commentModelList;

    public MyCommentAdapter(Context context, List<CommentModel> commentModelList) {
        this.context = context;
        this.commentModelList = commentModelList;
    }

    @NonNull
    @Override
    public MyCommentAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).
                inflate(R.layout.layout_comment_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyCommentAdapter.MyViewHolder holder, int position) {

        Long timeStamp=Long.valueOf(commentModelList.get(position).getCommentTimeStamp().get("timeStamp").toString());
        holder.commentDate.setText(DateUtils.getRelativeTimeSpanString(timeStamp));
        holder.comment.setText(commentModelList.get(position).getComment());
        holder.commentName.setText(commentModelList.get(position).getName());
        holder.ratingBar.setRating(commentModelList.get(position).getRatingValue());


    }

    @Override
    public int getItemCount() {
        return commentModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private Unbinder unbinder;

        @BindView(R.id.txt_comment_name)
        TextView commentName;

        @BindView(R.id.txt_comment_date)
        TextView commentDate;

        @BindView(R.id.txt_comment)
        TextView comment;

        @BindView(R.id.rating_bar)
        RatingBar ratingBar;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            unbinder= ButterKnife.bind(this,itemView);
        }
    }
}
