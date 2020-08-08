package com.fouadbahari.lellafood.CallBack;

import com.fouadbahari.lellafood.Model.BestDealModel;

import java.util.List;

public interface IBestDealCallbackListener {

    void onBestDealLoadSuccess(List<BestDealModel> bestDealModels);
    void onBestDealLoadFailed(String message);

}
