package com.fouadbahari.lellafood.View.ui.menu;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fouadbahari.lellafood.Common.Common;
import com.fouadbahari.lellafood.Common.SpacesItemDecoraction;
import com.fouadbahari.lellafood.Controller.MyCategoryAdapter;
import com.fouadbahari.lellafood.Model.CategoryModel;
import com.fouadbahari.lellafood.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class MenuFragment extends Fragment {

    private MenuViewModel menuViewModel;
    private AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyCategoryAdapter adapter;

    Unbinder unbinder;
    @BindView(R.id.recyclerCategoriesId)
    RecyclerView recyclerCagtegory;

    @SuppressLint("FragmentLiveDataObserve")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        menuViewModel =
                ViewModelProviders.of(this).get(MenuViewModel.class);
        View root = inflater.inflate(R.layout.fragment_menu, container, false);

        unbinder= ButterKnife.bind(this,root);
        initViews();
        menuViewModel.getMessageError().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {

                Toast.makeText(getContext(),""+s,Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        menuViewModel.getCategoryListMutable().observe(this, new Observer<List<CategoryModel>>() {
            @Override
            public void onChanged(List<CategoryModel> categoryModels) {
                dialog.dismiss();
                adapter= new MyCategoryAdapter(getContext(),categoryModels);
                recyclerCagtegory.setAdapter(adapter);
                recyclerCagtegory.setLayoutAnimation(layoutAnimationController);
            }
        });
        return root;
    }

    private void initViews() {
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        dialog.show();
        layoutAnimationController= AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
        GridLayoutManager layoutManager= new GridLayoutManager(getContext(),2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter!=null)
                {

                    switch (adapter.getItemViewType(position))
                    {
                        case Common.DEFAULT_COLUMN_COUNT: return 1;
                        case Common.FULL_WIDTH_COLUMN: return 2;
                        default:return -1;

                    }
                }
                return -1;
            }
        });
        recyclerCagtegory.setLayoutManager(layoutManager);
        recyclerCagtegory.addItemDecoration(new SpacesItemDecoraction(8));
    }
}