package com.fouadbahari.lellafood.View.ui.restaurant;


import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.KeyEvent;
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
import android.widget.Toast;

import com.fouadbahari.lellafood.Common.Common;
import com.fouadbahari.lellafood.Controller.MyRestaurantAdapter;
import com.fouadbahari.lellafood.EventBus.CounterCartEvent;
import com.fouadbahari.lellafood.EventBus.HideFABCart;
import com.fouadbahari.lellafood.EventBus.MenuInflateEvent;
import com.fouadbahari.lellafood.Model.FoodModel;
import com.fouadbahari.lellafood.Model.RestaurantModel;
import com.fouadbahari.lellafood.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class RestaurantFragment extends Fragment {

    private RestaurantViewModel mViewModel;

    Unbinder unbinder;

    @BindView(R.id.recycler_restaurant)
    RecyclerView recycler_restaurant;

    LayoutAnimationController layoutAnimationController;
    MyRestaurantAdapter adapter;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(RestaurantViewModel.class);
        View root = inflater.inflate(R.layout.restaurant_fragment, container, false);



        root.setFocusableInTouchMode(true);
        root.requestFocus();
        root.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {

                        return true;
                    }
                }
                return false;
            }
        });





        unbinder = ButterKnife.bind(this, root);
        initViews();

        mViewModel.getMessageError().observe(getViewLifecycleOwner(), s -> {
            Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
        });

        mViewModel.getRestautantlistMutable().observe(getViewLifecycleOwner(), restaurantModels -> {
            adapter = new MyRestaurantAdapter(getContext(), restaurantModels);
            recycler_restaurant.setAdapter(adapter);
            recycler_restaurant.setLayoutAnimation(layoutAnimationController);
        });
        return root;
    }

    private void initViews() {
        EventBus.getDefault().postSticky(new HideFABCart(true));
        setHasOptionsMenu(true);
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recycler_restaurant.setLayoutManager(linearLayoutManager);
        recycler_restaurant.addItemDecoration(new DividerItemDecoration(getContext(), linearLayoutManager.getOrientation()));
    }


    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().postSticky(new CounterCartEvent(true));
        EventBus.getDefault().postSticky(new MenuInflateEvent(false));

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        final MenuItem menuItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                startSearchChef(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        ImageView closeButton = (ImageView) searchView.findViewById(R.id.search_close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText ed = (EditText) searchView.findViewById(R.id.search_src_text);
                ed.setText("");
                searchView.setQuery("", false);
                searchView.onActionViewCollapsed();
                menuItem.collapseActionView();
                mViewModel.getRestautantlistMutable();


            }
        });
    }

    private void startSearchChef(String s) {
        
        List<RestaurantModel> restaurantModels = new ArrayList<>();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_RREF);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot restaurantSnapshot : snapshot.getChildren()) {

                        RestaurantModel restaurantModel = restaurantSnapshot.getValue(RestaurantModel.class);
                        if (restaurantModel.isActive()) {
                            if (restaurantModel.getName().toLowerCase().contains(s.toLowerCase())) {
                                restaurantModel.setUid(restaurantSnapshot.getKey());
                                restaurantModels.add(restaurantModel);
                            }
                        }
                    }
                    if (restaurantModels.size() > 0)
                    {
                        mViewModel.getRestautantlistMutable().setValue(restaurantModels);
                    }
                    else
                        Toast.makeText(getContext(), "Restaurant List empty", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Restaurants list does not exists", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

}