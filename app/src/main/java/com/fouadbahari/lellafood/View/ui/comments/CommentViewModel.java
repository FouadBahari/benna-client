package com.fouadbahari.lellafood.View.ui.comments;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fouadbahari.lellafood.Model.CommentModel;

import java.util.List;

public class CommentViewModel extends ViewModel {
    private MutableLiveData<List<CommentModel>> mutableLiveDataFoodList;

    public CommentViewModel(){
        mutableLiveDataFoodList=new MutableLiveData<>();
    }

    public MutableLiveData<List<CommentModel>> getMutableLiveDataFoodList(){
        return mutableLiveDataFoodList;
    }

    public void setCommentList(List<CommentModel> commentList){
        mutableLiveDataFoodList.setValue(commentList);

    }

}
