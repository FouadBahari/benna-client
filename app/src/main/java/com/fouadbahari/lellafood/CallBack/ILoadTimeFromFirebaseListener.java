package com.fouadbahari.lellafood.CallBack;

import com.fouadbahari.lellafood.Model.OrderModel;

public interface ILoadTimeFromFirebaseListener {
    void onLoadTimeSuccess(OrderModel orderModel, long estimationTimeInMs);
    void onLoadTimeFailed(String message);


}
