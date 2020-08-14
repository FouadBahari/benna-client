package com.fouadbahari.lellafood.View.ui.foodlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fouadbahari.lellafood.Common.Common;
import com.fouadbahari.lellafood.Controller.MyFoodListAdapter;
import com.fouadbahari.lellafood.Model.FoodModel;
import com.fouadbahari.lellafood.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FoodListFragment extends Fragment {

    private FoodListViewModel foodListViewModel;

    Unbinder unbinder;
    @BindView(R.id.recyclerFoodListId)
    RecyclerView recyclerFoodList;


    LayoutAnimationController layoutAnimationController;
    MyFoodListAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foodListViewModel =
                ViewModelProviders.of(this).get(FoodListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_food_list, container, false);

        unbinder= ButterKnife.bind(this,root);
        initViews();

        foodListViewModel.getMutableLiveDataFoodList().observe(getViewLifecycleOwner(), new Observer<List<FoodModel>>() {
            @Override
            public void onChanged(List<FoodModel> foodModels) {

                adapter=new MyFoodListAdapter(getContext(),foodModels);
                recyclerFoodList.setAdapter(adapter);
                recyclerFoodList.setLayoutAnimation(layoutAnimationController);

            }
        });
        return root;
    }

    private void initViews() {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(Common.categorySelected.getName());
        recyclerFoodList.setHasFixedSize(true);
        recyclerFoodList.setLayoutManager(new LinearLayoutManager(getContext()));
        layoutAnimationController= AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);

    }
}