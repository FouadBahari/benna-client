package com.fouadbahari.lellafood.CallBack;

import com.fouadbahari.lellafood.Database.CartItem;
import com.fouadbahari.lellafood.Model.CategoryModel;
import com.fouadbahari.lellafood.Model.FoodModel;

public interface ISearchCategoryCallbackListener {
    void onSearchCategoryFound(CategoryModel categoryModel, CartItem cartItem);
    void onSearchCategoryNotFound(String message);

}
