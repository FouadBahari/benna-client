package com.fouadbahari.lellafood.View.ui.restaurant;

import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fouadbahari.lellafood.CallBack.IRecyclerClickListener;
import com.fouadbahari.lellafood.CallBack.IRestaurtantClickListener;
import com.fouadbahari.lellafood.Common.Common;
import com.fouadbahari.lellafood.Model.RestaurantModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RestaurantViewModel extends ViewModel implements IRestaurtantClickListener {
    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private MutableLiveData<List<RestaurantModel>> restautantlistMutable;

    private IRestaurtantClickListener listener;

    public RestaurantViewModel() {
        listener = this;
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    public MutableLiveData<List<RestaurantModel>> getRestautantlistMutable() {
        if (restautantlistMutable == null) {
            restautantlistMutable = new MutableLiveData<>();
            loadRestaurant();
        }
        return restautantlistMutable;
    }

    private void loadRestaurant() {
        List<RestaurantModel> restaurantModels = new ArrayList<>();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_RREF);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot restaurantSnapshot : snapshot.getChildren()) {

                        RestaurantModel restaurantModel = restaurantSnapshot.getValue(RestaurantModel.class);
                        if (restaurantModel.isActive()){
                            restaurantModel.setUid(restaurantSnapshot.getKey());
                            restaurantModels.add(restaurantModel);
                        }
                    }
                    if (restaurantModels.size()>0)
                        listener.onLoadRestaurantSuccess(restaurantModels);
                    else
                        listener.onLoadRestaurantFailed("Restaurant List empty");

                } else {
                    listener.onLoadRestaurantFailed("Restaurants list does not exists");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onLoadRestaurantFailed(error.getMessage());
            }
        });
    }

    @Override
    public void onLoadRestaurantSuccess(List<RestaurantModel> restaurantModels) {
        restautantlistMutable.setValue(restaurantModels);
    }

    @Override
    public void onLoadRestaurantFailed(String message) {
        messageError.setValue(message);
    }
}