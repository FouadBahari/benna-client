package com.fouadbahari.lellafood.View.ui.menu;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fouadbahari.lellafood.CallBack.ICategoryCallBackLitener;
import com.fouadbahari.lellafood.Common.Common;
import com.fouadbahari.lellafood.Model.CategoryModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MenuViewModel extends ViewModel implements ICategoryCallBackLitener {


    private MutableLiveData<List<CategoryModel>> categoryListMutable;
    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private ICategoryCallBackLitener categoryCallBackLitener;

    public MenuViewModel() {
        categoryCallBackLitener = this;
    }

    public MutableLiveData<List<CategoryModel>> getCategoryListMutable() {

        if (categoryListMutable == null) {
            categoryListMutable = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadCategory();
        }
        return categoryListMutable;

    }

    public void loadCategory() {
        final List<CategoryModel> tempList = new ArrayList<>();
        DatabaseReference categoryRef = FirebaseDatabase.
                getInstance().getReference(Common.RESTAURANT_RREF)
                .child(Common.selectedRestaurant.getUid())
                .child(Common.CATEGORY_REF);
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    CategoryModel categoryModel = itemSnapshot.getValue(CategoryModel.class);
                    categoryModel.setMenu_id(itemSnapshot.getKey());
                    if (categoryModel.isActive()) {
                        tempList.add(categoryModel);
                    }

                }
                categoryCallBackLitener.onCategoryLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                categoryCallBackLitener.onCategoryLoadFailed(error.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onCategoryLoadSuccess(List<CategoryModel> categoryModels) {

        categoryListMutable.setValue(categoryModels);
    }

    @Override
    public void onCategoryLoadFailed(String message) {

        messageError.setValue(message);
    }
}