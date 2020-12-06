package com.fouadbahari.lellafood.EventBus;

public class SumAllItemInCartEvent {
    private boolean success;

    public SumAllItemInCartEvent(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
