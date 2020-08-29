package com.fouadbahari.lellafood.View.ui.comments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fouadbahari.lellafood.CallBack.ICommentsCallBackListener;
import com.fouadbahari.lellafood.Common.Common;
import com.fouadbahari.lellafood.Controller.MyCommentAdapter;
import com.fouadbahari.lellafood.Model.CommentModel;
import com.fouadbahari.lellafood.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class CommentsFragment extends BottomSheetDialogFragment implements ICommentsCallBackListener {

    private CommentViewModel commentViewModel;
    Unbinder unbinder;

    @BindView(R.id.recyclerComment)
    RecyclerView recyclerComments;

    AlertDialog dialog;
    ICommentsCallBackListener listener;


    public CommentsFragment() {
        listener=this;
    }


    private static CommentsFragment instance;

    public  static CommentsFragment getInstance(){
        if(instance==null)
           instance=new CommentsFragment();
        return instance;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View itemView =LayoutInflater.from(getContext()).
                inflate(R.layout.bottom_sheet_comment_fragment,container,false);
        unbinder= ButterKnife.bind(this,itemView);
        initViews();
        loadCommentsFromFirebase();
        commentViewModel.getMutableLiveDataFoodList().observe(this, new Observer<List<CommentModel>>() {
            @Override
            public void onChanged(List<CommentModel> commentModels) {

                MyCommentAdapter adapter=new MyCommentAdapter(getContext(),commentModels);
                recyclerComments.setAdapter(adapter);
            }
        });

        return itemView;
    }

    private void loadCommentsFromFirebase() {
        dialog.show();
        final List<CommentModel> commentModels=new ArrayList<>();
        FirebaseDatabase.getInstance().getReference(Common.COMMENT_REF)
                .child(Common.selectedFood.getId())
                .orderByChild("commentTimeStamp")
                .limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot commentSnapshot:snapshot.getChildren())
                        {
                            if(commentSnapshot.child("comment").toString() != null) {
                                CommentModel commentModel = commentSnapshot.getValue(CommentModel.class);
                                commentModels.add(commentModel);

                            }
                        }
                        listener.onCommentsLoadSuccess(commentModels);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        listener.onCommentsLoadFailed(error.getMessage());
                    }
                });

    }

    private void initViews() {
        commentViewModel= ViewModelProviders.of(this).get(CommentViewModel.class);
        dialog=new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();

        recyclerComments.setHasFixedSize(true);
        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext(),RecyclerView.VERTICAL,true);
        recyclerComments.setLayoutManager(layoutManager);
        recyclerComments.addItemDecoration(new DividerItemDecoration(getContext(),layoutManager.getOrientation()));
    }

    @Override
    public void onCommentsLoadSuccess(List<CommentModel> commentModels) {

        dialog.dismiss();
        commentViewModel.setCommentList(commentModels);

    }

    @Override
    public void onCommentsLoadFailed(String message) {

        dialog.dismiss();
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
