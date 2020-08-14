package com.fouadbahari.lellafood.View;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.fouadbahari.lellafood.Common.Common;
import com.fouadbahari.lellafood.Database.CartDataSource;
import com.fouadbahari.lellafood.Database.CartDatabase;
import com.fouadbahari.lellafood.Database.LocalCartDataSource;
import com.fouadbahari.lellafood.EventBus.CategoryClick;
import com.fouadbahari.lellafood.EventBus.CounterCartEvent;
import com.fouadbahari.lellafood.EventBus.FoodItemClick;
import com.fouadbahari.lellafood.MainActivity;
import com.fouadbahari.lellafood.Model.CategoryModel;
import com.fouadbahari.lellafood.Model.User;
import com.fouadbahari.lellafood.R;
import com.google.android.gms.tasks.OnSuccessListener;
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

import androidx.annotation.NonNull;
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
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,FirebaseAuth.AuthStateListener {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavController navController;

    private CartDataSource cartDataSource;



    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference users;
    private FirebaseUser mUser;



    @BindView(R.id.fab)
    CounterFab counterFab;


    @Override
    protected void onResume() {
        super.onResume();
        countCartItem();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);



        Toolbar toolbar = findViewById(R.id.toolbar);
        ButterKnife.bind(this);
        cartDataSource=new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());


        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
         drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_menu)
                .setDrawerLayout(drawer)
                .build();
         navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

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
        switch (item.getItemId())
        {
            case R.id.nav_home:
                navController.navigate(R.id.nav_home);
                break;

            case R.id.nav_menu:
                navController.navigate(R.id.nav_menu);
                break;
        }
        return true;
    }


    //EventBus


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseAuth.getInstance().addAuthStateListener(this);
        EventBus.getDefault().register(this);

        mUser= FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.USER_REFERENCES);
        users.child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                User user = snapshot.getValue(User.class);
                Common.currentUser=user;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(HomeActivity.this, ""+error.getMessage(),
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

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onCategorySelected(CategoryClick event)
    {
        if (event.isSuccess())
        {
            navController.navigate(R.id.nav_foodList);
        }
    }


    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onFoodSelected(FoodItemClick event)
    {
        if (event.isSuccess())
        {
            navController.navigate(R.id.nav_fooddetails);
        }
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onCartCounter(CounterCartEvent event)
    {
        if (event.isSuccess())
        {
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

                        Toast.makeText(HomeActivity.this, "[Count Cart]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

        if (firebaseAuth.getCurrentUser().getUid()==null)
        {
            startActivity(new Intent(HomeActivity.this,MainActivity.class));
        }

        firebaseAuth.getCurrentUser().getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
            @Override
            public void onSuccess(GetTokenResult getTokenResult) {

                Log.d("main","ONsUCCESS"+getTokenResult.getToken());
            }
        });
    }
}