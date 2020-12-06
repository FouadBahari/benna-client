package com.fouadbahari.lellafood.View.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fouadbahari.lellafood.CallBack.IBestDealCallbackListener;
import com.fouadbahari.lellafood.CallBack.IPopularCallbackListener;
import com.fouadbahari.lellafood.Common.Common;
import com.fouadbahari.lellafood.Model.BestDealModel;
import com.fouadbahari.lellafood.Model.PopularCategoryModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel implements IPopularCallbackListener,IBestDealCallbackListener {

    private MutableLiveData<List<PopularCategoryModel>> popularList ;
    private MutableLiveData<List<BestDealModel>> bestDealList ;
    private MutableLiveData<String> messageError ;
    private IPopularCallbackListener iPopularCallbackListener;
    private IBestDealCallbackListener bestDealCallbackListener;



    public HomeViewModel() {

        iPopularCallbackListener=this;
        bestDealCallbackListener=this;

    }

    public MutableLiveData<List<BestDealModel>> getBestDealList(String key) {
        if (bestDealList==null)
        {
            bestDealList=new MutableLiveData<>();
            messageError=new MutableLiveData<>();
            loadBestDealList(key);


        }
        return bestDealList;
    }

    private void loadBestDealList(String key) {
        final List<BestDealModel> tempList=new ArrayList<>();
        DatabaseReference bestDealRef=FirebaseDatabase
                .getInstance().getReference(Common.RESTAURANT_RREF)
                .child(Common.selectedRestaurant.getUid())
                .child(Common.BEST_DEALS_REF);
        bestDealRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot itemSnapShot:snapshot.getChildren())
                {
                    BestDealModel model =itemSnapShot.getValue(BestDealModel.class);
                    tempList.add(model);

                }
                bestDealCallbackListener.onBestDealLoadSuccess(tempList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                bestDealCallbackListener.onBestDealLoadFailed(error.getMessage());
            }
        });
    }

    public MutableLiveData<List<PopularCategoryModel>> getPopularList(String key) {
        if (popularList==null)
        {
            popularList=new MutableLiveData<>();
            messageError=new MutableLiveData<>();
            loadPopularList(key);
        }
        return popularList;
    }

    private void loadPopularList(String key) {
        final List<PopularCategoryModel> tempList = new ArrayList<>();
        DatabaseReference popularRef= FirebaseDatabase.getInstance()
                .getReference(Common.RESTAURANT_RREF)
                .child(Common.selectedRestaurant.getUid())
                .child(Common.POPULAR_CATEGORY_REF);
        popularRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot itemSnapShot:snapshot.getChildren())
                {
                    PopularCategoryModel model =itemSnapShot.getValue(PopularCategoryModel.class);
                    tempList.add(model);

                }
                iPopularCallbackListener.onPopularLoadSuccess(tempList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                iPopularCallbackListener.onPopularLoadFailed(error.getMessage());
            }
        });

    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onPopularLoadSuccess(List<PopularCategoryModel> popularCategoryModels) {

        popularList.setValue(popularCategoryModels);

    }

    @Override
    public void onPopularLoadFailed(String message) {


        messageError.setValue(message);
    }

    @Override
    public void onBestDealLoadSuccess(List<BestDealModel> bestDealModels) {
        bestDealList.setValue(bestDealModels);
    }

    @Override
    public void onBestDealLoadFailed(String message) {

        messageError.setValue(message);
    }
}