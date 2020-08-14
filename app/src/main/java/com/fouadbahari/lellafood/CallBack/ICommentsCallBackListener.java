package com.fouadbahari.lellafood.CallBack;

import com.fouadbahari.lellafood.Model.CommentModel;

import java.util.List;

public interface ICommentsCallBackListener {
    void onCommentsLoadSuccess(List<CommentModel> commentModels);
    void onCommentsLoadFailed(String message);
}
