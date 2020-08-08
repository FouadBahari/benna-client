package com.fouadbahari.lellafood.Controller;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.asksira.loopingviewpager.LoopingPagerAdapter;
import com.bumptech.glide.Glide;
import com.fouadbahari.lellafood.Model.BestDealModel;
import com.fouadbahari.lellafood.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyBestDealAdapter extends LoopingPagerAdapter<BestDealModel> {


    @BindView(R.id.imageBestDealId)
    ImageView imageBestDeal;

    @BindView(R.id.textBestDealId)
    TextView textBestDeal;

    Unbinder unbinder;

    public MyBestDealAdapter(Context context, List<BestDealModel> itemList, boolean isInfinite) {
        super(context, itemList, isInfinite);
    }

    @Override
    protected View inflateView(int viewType, ViewGroup container, int listPosition) {
        return LayoutInflater.from(context).inflate(R.layout.layout_best_deals_item,container,false);

    }

    @Override
    protected void bindView(View convertView, int listPosition, int viewType) {

        unbinder = ButterKnife.bind(this,convertView);
        Glide.with(convertView).load(itemList.get(listPosition).getImage()).into(imageBestDeal);
        textBestDeal.setText(itemList.get(listPosition).getName());
    }
}
