package com.fouadbahari.lellafood.View.ui.menu;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fouadbahari.lellafood.Common.Common;
import com.fouadbahari.lellafood.Common.SpacesItemDecoraction;
import com.fouadbahari.lellafood.Controller.MyCategoryAdapter;
import com.fouadbahari.lellafood.EventBus.MenuItemBack;
import com.fouadbahari.lellafood.Model.CategoryModel;
import com.fouadbahari.lellafood.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
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

    @BindView(R.id.img_restaurant)
    ImageView img_restaurant;

    @BindView(R.id.img_profile_id)
    ImageView img_profile_id;

    @BindView(R.id.txt_restaurant_name)
    TextView txt_restaurant_name;

    @BindView(R.id.txt_restaurant_address)
    TextView txt_restaurant_address;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        menuViewModel =
                new ViewModelProvider(this).get(MenuViewModel.class);
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
        setHasOptionsMenu(true);

        Glide.with(getContext()).load(Common.selectedRestaurant.getImageUrl()).into(img_restaurant);
        Glide.with(getContext()).load(Common.selectedRestaurant.getProfileUrl()).into(img_profile_id);
        txt_restaurant_name.setText(Common.selectedRestaurant.getName());
        txt_restaurant_address.setText(Common.selectedRestaurant.getAddress());


        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        dialog.show();
        layoutAnimationController= AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
//        GridLayoutManager layoutManager= new GridLayoutManager(getContext(),2);
//        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
//            @Override
//            public int getSpanSize(int position) {
//                if (adapter!=null)
//                {
//
//                    switch (adapter.getItemViewType(position))
//                    {
//                        case Common.DEFAULT_COLUMN_COUNT: return 1;
//                        case Common.FULL_WIDTH_COLUMN: return 2;
//                        default:return -1;
//
//                    }
//                }
//                return -1;
//            }
//        });
        recyclerCagtegory.setLayoutManager(new LinearLayoutManager(getContext(),
                RecyclerView.VERTICAL,false));
        recyclerCagtegory.addItemDecoration(new SpacesItemDecoraction(8));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu,menu);
         final MenuItem menuItem=menu.findItem(R.id.action_search);

        SearchManager searchManager=(SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView=(SearchView)menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                startSearchFood(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        ImageView closeButton=(ImageView)searchView.findViewById(R.id.search_close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText ed=(EditText)searchView.findViewById(R.id.search_src_text);
                ed.setText("");
                searchView.setQuery("",false);
                searchView.onActionViewCollapsed();
                menuItem.collapseActionView();
                menuViewModel.loadCategory();


            }
        });
    }

    private void startSearchFood(String s) {

        List<CategoryModel> resultList=new ArrayList<>();
        for (int i=0;i<adapter.getListCategory().size();i++)
        {

            CategoryModel categoryModel=adapter.getListCategory().get(i);
            if (categoryModel.getName().toLowerCase().contains(s.toLowerCase()))
                resultList.add(categoryModel);
        }
        menuViewModel.getCategoryListMutable().setValue(resultList);

    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}