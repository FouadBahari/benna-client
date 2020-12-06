package com.fouadbahari.lellafood.Database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;


@Entity (tableName = "Cart", primaryKeys = {"uid" ,"categoryId","foodId","restaurantId"})
public class CartItem {



    @NonNull
    @ColumnInfo(name="restaurantId")
    private String  restaurantId;

   @NonNull
    @ColumnInfo(name="foodId")
    private String  foodId;

    @NonNull
    @ColumnInfo(name="categoryId")
    private String  categoryId;


    @ColumnInfo(name="foodName")
    private String  foodName;

    @ColumnInfo(name="foodImage")
    private String  foodImage;

    @ColumnInfo(name="foodPrice")
    private Double  foodPrice;

    @ColumnInfo(name="foodQuantity")
    private int  foodQuantity;

    @ColumnInfo(name="userPhone")
    private String  userPhone;



    @NonNull
    @ColumnInfo(name="uid")
    private String  uid;

    @NonNull
    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(@NonNull String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFoodImage() {
        return foodImage;
    }

    public void setFoodImage(String foodImage) {
        this.foodImage = foodImage;
    }

    public Double getFoodPrice() {
        return foodPrice;
    }

    public void setFoodPrice(Double foodPrice) {
        this.foodPrice = foodPrice;
    }

    public int getFoodQuantity() {
        return foodQuantity;
    }

    public void setFoodQuantity(int foodQuantity) {
        this.foodQuantity = foodQuantity;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }



    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


    @NonNull
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(@NonNull String categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj==this)
            return true;
        if (!(obj instanceof CartItem))
            return false;
        CartItem cartItem =(CartItem)obj;

        return cartItem.getFoodId().equals(this.foodId);
    }
}
