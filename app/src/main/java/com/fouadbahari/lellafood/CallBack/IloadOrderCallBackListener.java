package com.fouadbahari.lellafood.CallBack;

import com.fouadbahari.lellafood.Model.OrderModel;

import java.util.List;

public interface IloadOrderCallBackListener {

    void onLoadOrderSuccess(List<OrderModel> orderModelList);
    void onLoadPrderFailed(String message);

}
