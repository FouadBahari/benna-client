package com.fouadbahari.lellafood.CallBack;

import com.fouadbahari.lellafood.Model.RestaurantModel;

import java.util.List;

public interface IRestaurtantClickListener {
    void onLoadRestaurantSuccess(List<RestaurantModel> restaurantModels);
    void onLoadRestaurantFailed(String message);
}
