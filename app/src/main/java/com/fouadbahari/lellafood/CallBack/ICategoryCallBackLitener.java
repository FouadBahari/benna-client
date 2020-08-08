package com.fouadbahari.lellafood.CallBack;

import com.fouadbahari.lellafood.Model.CategoryModel;

import java.util.List;

public interface ICategoryCallBackLitener {
    void onCategoryLoadSuccess(List<CategoryModel> categoryModels);
    void onCategoryLoadFailed(String message);

}
