package com.fouadbahari.lellafood.CallBack;

import com.fouadbahari.lellafood.Model.Order;

public interface ILoadTimeFromFirebaseListener {
    void onLoadTimeSuccess(Order order ,long estimationTimeInMs);
    void onLoadTimeFailed(String message);


}
