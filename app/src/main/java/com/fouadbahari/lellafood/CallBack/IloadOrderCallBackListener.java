package com.fouadbahari.lellafood.CallBack;

import com.fouadbahari.lellafood.Model.Order;

import java.util.List;

public interface IloadOrderCallBackListener {

    void onLoadOrderSuccess(List<Order> orderList);
    void onLoadPrderFailed(String message);

}
