package com.fouadbahari.lellafood.EventBus;

import com.fouadbahari.lellafood.Model.BestDealModel;

public class BestDealItemClick {
    BestDealModel bestDealModel;

    public BestDealItemClick(BestDealModel bestDealModel) {
        this.bestDealModel = bestDealModel;
    }

    public BestDealModel getBestDealModel() {
        return bestDealModel;
    }

    public void setBestDealModel(BestDealModel bestDealModel) {
        this.bestDealModel = bestDealModel;
    }
}
