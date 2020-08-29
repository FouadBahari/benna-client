package com.fouadbahari.lellafood.View.ui.vieworder;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fouadbahari.lellafood.Model.Order;

import java.util.List;

public class ViewOrderViewModel extends ViewModel {


    private MutableLiveData<List<Order>> mutableLiveDataOrderList;

    public ViewOrderViewModel() {
        mutableLiveDataOrderList = new MutableLiveData<>();
    }

    public MutableLiveData<List<Order>> getMutableLiveDataOrderList() {
        return mutableLiveDataOrderList;
    }

    public void setMutableLiveDataOrderList(List<Order> orderList) {

        mutableLiveDataOrderList.setValue(orderList);
    }

}