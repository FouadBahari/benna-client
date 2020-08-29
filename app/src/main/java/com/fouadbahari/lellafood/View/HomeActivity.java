package com.fouadbahari.lellafood.View;

import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.fouadbahari.lellafood.Common.Common;
import com.fouadbahari.lellafood.Database.CartDataSource;
import com.fouadbahari.lellafood.Database.CartDatabase;
import com.fouadbahari.lellafood.Database.LocalCartDataSource;
import com.fouadbahari.lellafood.EventBus.BestDealItemClick;
import com.fouadbahari.lellafood.EventBus.CategoryClick;
import com.fouadbahari.lellafood.EventBus.CounterCartEvent;
import com.fouadbahari.lellafood.EventBus.FoodItemClick;
import com.fouadbahari.lellafood.EventBus.HideFABCart;
import com.fouadbahari.lellafood.EventBus.MenuItemBack;
import com.fouadbahari.lellafood.EventBus.PopularCategoryClick;
import com.fouadbahari.lellafood.MainActivity;
import com.fouadbahari.lellafood.Model.CategoryModel;
import com.fouadbahari.lellafood.Model.FoodModel;
import com.fouadbahari.lellafood.Model.User;
import com.fouadbahari.lellafood.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, FirebaseAuth.AuthStateListener {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavController navController;
    private NavigationView navigationView;

    android.app.AlertDialog dialog;

    private CartDataSource cartDataSource;

    int menuClickId=-1;

    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference users;
    private FirebaseUser mUser;
    private String lastUid;
    String token = FirebaseInstanceId.getInstance().getToken();


    @BindView(R.id.fab)
    CounterFab counterFab;


    @Override
    protected void onResume() {
        navController.navigate(R.id.nav_home);
        super.onResume();
        countCartItem();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        Toolbar toolbar = findViewById(R.id.toolbar);
        ButterKnife.bind(this);
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());

        dialog=new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        auth=FirebaseAuth.getInstance();
        setSupportActionBar(toolbar);
        counterFab = findViewById(R.id.fab);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_menu, R.id.nav_cart,R.id.nav_sign_out,R.id.nav_view_order)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

        getUserSetUI();



        counterFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.nav_cart);

            }
        });

        countCartItem();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        drawer.closeDrawers();
        switch (item.getItemId()) {
            case R.id.nav_home:
                if (item.getItemId()!=menuClickId)
                    navController.navigate(R.id.nav_home);
                break;

            case R.id.nav_menu:
                if (item.getItemId()!=menuClickId)
                    navController.navigate(R.id.nav_menu);
                break;

            case R.id.nav_cart:
                if (item.getItemId()!=menuClickId)
                    navController.navigate(R.id.nav_cart);
                break;

            case R.id.nav_sign_out:
                signOut();
                break;

            case R.id.nav_view_order:
                if (item.getItemId()!=menuClickId)
                    navController.navigate(R.id.nav_view_order);
                break;
        }
        menuClickId=item.getItemId();

        return true;
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Signout")
                .setMessage("Do you really want to sign out?")
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Common.selectedFood = null;
                Common.categorySelected = null;
                Common.currentUser = null;
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    //EventBus


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseAuth.getInstance().addAuthStateListener(this);
        EventBus.getDefault().register(this);


        mUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.USER_REFERENCES);
        users.child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshot) {



                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                User user = snapshot.getValue(User.class);
                                Common.currentUser = user;
                                Common.userCurrent = user;
                                Common.currentToken = token;
                                View headerView = navigationView.getHeaderView(0);
                                TextView txt_user = (TextView) headerView.findViewById(R.id.txt_user);
                                Common.setSpanString("Hey, ", Common.userCurrent.getName(), txt_user);
                            }
                        }).addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        User user = snapshot.getValue(User.class);
                        Common.currentUser = user;
                        Common.userCurrent = user;
                        Common.currentToken = token;
                        Common.updateToken(HomeActivity.this,task.getResult().getToken());
                        View headerView = navigationView.getHeaderView(0);
                        TextView txt_user = (TextView) headerView.findViewById(R.id.txt_user);
                        Common.setSpanString("Hey, ", Common.userCurrent.getName(), txt_user);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(HomeActivity.this, "" + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onStop() {

        EventBus.getDefault().unregister(this);
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(this);

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCategorySelected(CategoryClick event) {
        if (event.isSuccess()) {
            navController.navigate(R.id.nav_foodList);
        }
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onFoodSelected(FoodItemClick event) {
        if (event.isSuccess()) {
            navController.navigate(R.id.nav_fooddetails);
        }
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onHideFABEvent(HideFABCart event) {
        if (event.isHidden()) {

            counterFab.hide();
        } else counterFab.show();
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPopularCategoryClick(final PopularCategoryClick event) {
        if (event.getPopularCategoryModel() != null) {

            dialog.show();
            FirebaseDatabase.getInstance()
                    .getReference("Category")
                    .child(event.getPopularCategoryModel().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists())
                            {

                                Common.categorySelected=snapshot.getValue(CategoryModel.class);
                                Common.categorySelected.setMenu_id(snapshot.getKey());

                                FirebaseDatabase.getInstance()
                                        .getReference("Category")
                                        .child(event.getPopularCategoryModel().getMenu_id())
                                        .child("foods")
                                        .orderByChild("id")
                                        .equalTo(event.getPopularCategoryModel().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                if (snapshot.exists())
                                                {
                                                    for (DataSnapshot snapshotItem:snapshot.getChildren())
                                                    {
                                                        Common.selectedFood=snapshotItem.getValue(FoodModel.class);
                                                        Common.selectedFood.setKey(snapshotItem.getKey());
                                                    }
                                                    navController.navigate(R.id.nav_fooddetails);

                                                }else {
                                                    Toast.makeText(HomeActivity.this, "Item doesn't exist!", Toast.LENGTH_SHORT).show();
                                                }
                                                dialog.dismiss();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                                dialog.dismiss();
                                                Toast.makeText(HomeActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }else {
                                dialog.dismiss();
                                Toast.makeText(HomeActivity.this, "Item doesn't exist!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                            dialog.dismiss();
                            Toast.makeText(HomeActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }



    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onBestDealClick(final BestDealItemClick event) {
        if (event.getBestDealModel() != null) {

            dialog.show();
            FirebaseDatabase.getInstance()
                    .getReference("Category")
                    .child(event.getBestDealModel().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists())
                            {

                                Common.categorySelected=snapshot.getValue(CategoryModel.class);
                                Common.categorySelected.setMenu_id(snapshot.getKey());

                                FirebaseDatabase.getInstance()
                                        .getReference("Category")
                                        .child(event.getBestDealModel().getMenu_id())
                                        .child("foods")
                                        .orderByChild("id")
                                        .equalTo(event.getBestDealModel().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                if (snapshot.exists())
                                                {
                                                    for (DataSnapshot snapshotItem:snapshot.getChildren())
                                                    {
                                                        Common.selectedFood=snapshotItem.getValue(FoodModel.class);
                                                        Common.selectedFood.setKey(snapshotItem.getKey());
                                                    }
                                                    navController.navigate(R.id.nav_fooddetails);

                                                }else {
                                                    Toast.makeText(HomeActivity.this, "Item doesn't exist!", Toast.LENGTH_SHORT).show();
                                                }
                                                dialog.dismiss();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                                dialog.dismiss();
                                                Toast.makeText(HomeActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }else {
                                dialog.dismiss();
                                Toast.makeText(HomeActivity.this, "Item doesn't exist!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                            dialog.dismiss();
                            Toast.makeText(HomeActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCartCounter(CounterCartEvent event) {
        if (event.isSuccess()) {
            countCartItem();
        }
    }

    private void countCartItem() {
        cartDataSource.countItemInCart(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {

                        counterFab.setCount(integer);
                    }

                    @Override
                    public void onError(Throwable e) {

                        if (!e.getMessage().contains("Query returned empty")) {
                            Toast.makeText(HomeActivity.this, "[Count Cart]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        } else counterFab.setCount(0);
                    }
                });


    }

    private void getUserSetUI(){
        lastUid = (auth == null || auth.getCurrentUser() == null) ?
                null : auth.getUid();
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth auth){
        String uid = auth.getUid(); // could be null
        if( (uid == null && lastUid != null) || // loggedout
                (uid != null && lastUid == null) || // loggedin
                (uid != null && lastUid != null && // switched accounts (unlikely)
                        !uid.equals(lastUid))){
            getUserSetUI();
        }
    }
    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onMenuItemBack(MenuItemBack event)
    {
        menuClickId=-1;
        if (getSupportFragmentManager().getBackStackEntryCount()>0)
            getSupportFragmentManager().popBackStack();

    }
}