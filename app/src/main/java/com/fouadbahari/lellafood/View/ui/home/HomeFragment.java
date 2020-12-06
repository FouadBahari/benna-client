package com.fouadbahari.lellafood.View.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.asksira.loopingviewpager.LoopingViewPager;
import com.fouadbahari.lellafood.Controller.MyBestDealAdapter;
import com.fouadbahari.lellafood.Controller.MyPopularCategoriesAdapter;
import com.fouadbahari.lellafood.Model.BestDealModel;
import com.fouadbahari.lellafood.Model.PopularCategoryModel;
import com.fouadbahari.lellafood.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    Unbinder unbinder;



    @BindView(R.id.recyclerPopularId)
    RecyclerView recyclerView;

    @BindView(R.id.viewPagerId)
    LoopingViewPager viewPager;


    LayoutAnimationController layoutAnimationController;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        unbinder = ButterKnife.bind(this,root);

        String key = getArguments().getString("restaurant");

        init();
        homeViewModel.getPopularList(key).observe((LifecycleOwner) getContext(), new Observer<List<PopularCategoryModel>>() {
            @Override
            public void onChanged(List<PopularCategoryModel> popularCategoryModels) {

                MyPopularCategoriesAdapter adapter=new MyPopularCategoriesAdapter(getContext(),popularCategoryModels);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutAnimation(layoutAnimationController);


            }
        });

        homeViewModel.getBestDealList(key).observe((LifecycleOwner) getContext(), new Observer<List<BestDealModel>>() {
            @Override
            public void onChanged(List<BestDealModel> bestDealModels) {
                MyBestDealAdapter adapter=new MyBestDealAdapter(getContext(),bestDealModels,true);
                viewPager.setAdapter(adapter);


            }
        });
        return root;
    }

    private void init() {
        layoutAnimationController= AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                RecyclerView.HORIZONTAL,false));
    }

    @Override
    public void onResume() {
        super.onResume();
        viewPager.resumeAutoScroll();
    }

    @Override
    public void onPause() {
        viewPager.pauseAutoScroll();
        super.onPause();
    }
}