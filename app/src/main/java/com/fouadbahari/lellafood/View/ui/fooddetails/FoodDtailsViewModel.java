package com.fouadbahari.lellafood.View.ui.fooddetails;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fouadbahari.lellafood.Common.Common;
import com.fouadbahari.lellafood.Model.CommentModel;
import com.fouadbahari.lellafood.Model.FoodModel;

public class FoodDtailsViewModel extends ViewModel {


    private MutableLiveData<FoodModel> foodModelMutableLiveData;
    private MutableLiveData<CommentModel> commentModelMutableLiveData;



    public void setCommentModel(CommentModel commentModel){
        if (commentModelMutableLiveData!=null)
            commentModelMutableLiveData.setValue(commentModel);

    }

    public MutableLiveData<CommentModel> getCommentModelMutableLiveData() {
        return commentModelMutableLiveData;
    }

    public FoodDtailsViewModel(){

        commentModelMutableLiveData=new MutableLiveData<>();

    }

    public MutableLiveData<FoodModel> getFoodModelMutableLiveData() {
        if (foodModelMutableLiveData==null)
            foodModelMutableLiveData=new MutableLiveData<>();
        foodModelMutableLiveData.setValue(Common.selectedFood);
        return foodModelMutableLiveData;
    }

    public void setFoodModel(FoodModel foodModel) {
        if (foodModelMutableLiveData!=null)
        foodModelMutableLiveData.setValue(foodModel);
    }
}