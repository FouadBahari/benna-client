package com.fouadbahari.lellafood.View.ui.vieworder;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fouadbahari.lellafood.Model.OrderModel;

import java.util.List;

public class ViewOrderViewModel extends ViewModel {


    private MutableLiveData<List<OrderModel>> mutableLiveDataOrderList;

    public ViewOrderViewModel() {
        mutableLiveDataOrderList = new MutableLiveData<>();
    }

    public MutableLiveData<List<OrderModel>> getMutableLiveDataOrderList() {
        return mutableLiveDataOrderList;
    }

    public void setMutableLiveDataOrderList(List<OrderModel> orderModelList) {

        mutableLiveDataOrderList.setValue(orderModelList);
    }

}