package com.fouadbahari.lellafood.View;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.fouadbahari.lellafood.Common.Common;
import com.fouadbahari.lellafood.Database.CartDataSource;
import com.fouadbahari.lellafood.Database.CartDatabase;
import com.fouadbahari.lellafood.Database.LocalCartDataSource;
import com.fouadbahari.lellafood.EventBus.ActivateGPSEvent;
import com.fouadbahari.lellafood.EventBus.BestDealItemClick;
import com.fouadbahari.lellafood.EventBus.CategoryClick;
import com.fouadbahari.lellafood.EventBus.CounterCartEvent;
import com.fouadbahari.lellafood.EventBus.FoodItemClick;
import com.fouadbahari.lellafood.EventBus.HideFABCart;
import com.fouadbahari.lellafood.EventBus.MenuInflateEvent;
import com.fouadbahari.lellafood.EventBus.MenuItemBack;
import com.fouadbahari.lellafood.EventBus.MenuItemEvent;
import com.fouadbahari.lellafood.EventBus.PopularCategoryClick;
import com.fouadbahari.lellafood.EventBus.ViewOrderEventClick;
import com.fouadbahari.lellafood.MainActivity;
import com.fouadbahari.lellafood.Model.CategoryModel;
import com.fouadbahari.lellafood.Model.FoodModel;
import com.fouadbahari.lellafood.Model.User;
import com.fouadbahari.lellafood.R;
import com.fouadbahari.lellafood.View.ui.restaurant.RestaurantFragment;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.makeramen.roundedimageview.RoundedImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import plugins.gligerglg.locusservice.LocusService;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, FirebaseAuth.AuthStateListener {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavController navController;
    private NavigationView navigationView;

    android.app.AlertDialog dialog;

    private CartDataSource cartDataSource;

    int menuClickId = -1;

    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference users;
    private FirebaseUser mUser;
    private String lastUid;
    String token = FirebaseInstanceId.getInstance().getToken();

    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;


    private Place placeSelected;
    private AutocompleteSupportFragment places_fragment;
    private PlacesClient placesClient;
    private List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG);


    LocationManager lm ;
    Location location ;

    RoundedImageView btn_benna_home;

    @BindView(R.id.fab)
    CounterFab counterFab;

    @BindView(R.id.address_layout)
    Button address_layout;

    @OnClick(R.id.address_layout)
    void onAddressClick(){
        initLocation();
    }





    @Override
    protected void onResume() {
        super.onResume();
//        countCartItem();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initPlaces();

        ButterKnife.bind(this);
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        Toolbar toolbar = findViewById(R.id.toolbar);
        auth = FirebaseAuth.getInstance();

        setSupportActionBar(toolbar);

        counterFab = findViewById(R.id.fab);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_gps,
                R.id.nav_restaurant,
                R.id.nav_menu,
                R.id.nav_cart,
                R.id.nav_sign_out,
                R.id.nav_view_order)
                .setDrawerLayout(drawer)
                .build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

        View headerView = navigationView.getHeaderView(0);
        TextView txt_user = (TextView) headerView.findViewById(R.id.txt_user_phone);
        btn_benna_home = (RoundedImageView) headerView.findViewById(R.id.btn_benna_home);

        btn_benna_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    navController.navigate(R.id.nav_restaurant);
                    EventBus.getDefault().postSticky(new MenuInflateEvent(false));
            }
        });


        txt_user.setText(Common.currentUser.getPhone());

        getUserSetUI();

//        initLocation();

        subscribeToTopic(Common.createUpdateOrderStatusTopic());


        navController.navigate(R.id.nav_gps);

        counterFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.nav_cart);

            }
        });

        EventBus.getDefault().postSticky(new HideFABCart(true));
    }


    private String getAddressFromLngLst(double latitude, double longitude) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String result = "";
        try {
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);

            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                StringBuilder sb = new StringBuilder(address.getAddressLine(0));
                result = sb.toString();

            } else result = "Address not found";

        } catch (IOException e) {
            e.printStackTrace();
            result = e.getMessage();
        }
        return result;
    }

    private void initPlaces() {
        Places.initialize(this, getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);
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

            case R.id.nav_restaurant:
                if (item.getItemId() != menuClickId)
                    navController.navigate(R.id.nav_restaurant);
                break;


            case R.id.nav_menu:
                if (item.getItemId() != menuClickId) {
                    navController.navigate(R.id.nav_menu);
                    EventBus.getDefault().postSticky(new MenuInflateEvent(true));
                }
                break;

            case R.id.nav_cart:
                if (item.getItemId() != menuClickId) {
                    navController.navigate(R.id.nav_cart);
                    EventBus.getDefault().postSticky(new MenuInflateEvent(true));
                }
                break;
//
//            case R.id.nav_update_info:
//                showUpdateInfoDialog();
//                break;

            case R.id.nav_view_order:
                if (item.getItemId() != menuClickId) {
                    navController.navigate(R.id.nav_view_order);
                    EventBus.getDefault().postSticky(new MenuInflateEvent(true));
                }
                break;

            case R.id.nav_news:
                showSubscribeNews();
                break;

            case R.id.nav_sign_out:
                signOut();
                break;
        }
        menuClickId = item.getItemId();

        return true;
    }

    private void showSubscribeNews() {
        Paper.init(this);
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Update System");
        builder.setMessage("Do you want to subscribe news from our restaurant");

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_subscribe_news, null);
        CheckBox checkBoxNews = (CheckBox) itemView.findViewById(R.id.ckb_subscribe_news);
        boolean isSubscribeNews = Paper.book().read(Common.selectedRestaurant.getUid(), false);
        if (isSubscribeNews)
            checkBoxNews.setChecked(true);
        builder.setNegativeButton("CANCEL", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        }).setPositiveButton("SEND", (dialogInterface, i) -> {
            if (checkBoxNews.isChecked()) {
                Paper.book().write(Common.selectedRestaurant.getUid(), true);
                FirebaseMessaging.getInstance()
                        .subscribeToTopic(Common.createTopicNews())
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Subscribe Successfully!", Toast.LENGTH_SHORT).show();
                });
            } else {
                Paper.book().delete(Common.selectedRestaurant.getUid());
                FirebaseMessaging.getInstance()
                        .unsubscribeFromTopic(Common.createTopicNews())
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Unsubscribe Successfully!", Toast.LENGTH_SHORT).show();
                });
            }
        });
        builder.setView(itemView);
        AlertDialog dialog = builder.create();
        dialog.show();
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
                }).
                setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Common.selectedFood = null;
                        Common.categorySelected = null;
                        Common.currentUser = null;
                        FirebaseAuth.getInstance().signOut();

                        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                        HomeActivity.this.startActivity(intent);
                        HomeActivity.this.finish();
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
    }

    private void initLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }
        buildLocationRequest();
        buildLocationCallBack();
         lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
         location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null)
                {
                    Double lat = location.getLatitude();
                    Double lng = location.getLongitude();
                    address_layout.setText(getAddressFromLngLst(lat,lng));

                }
            }
        });


    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();

            }
        };


    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);


        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        HomeActivity.this,
                                        LocationRequest.PRIORITY_HIGH_ACCURACY);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LocationRequest.PRIORITY_HIGH_ACCURACY:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        btn_benna_home.setEnabled(true);
                        navController.navigate(R.id.nav_restaurant);
                        Log.i("Message", "onActivityResult: GPS Enabled by user");
                        break;
                    case Activity.RESULT_CANCELED:

                        btn_benna_home.setEnabled(false);
                        Log.i("Message", "onActivityResult: User rejected GPS request");
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().removeAllStickyEvents();
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
                    .getReference(Common.RESTAURANT_RREF)
                    .child(Common.selectedRestaurant.getUid())
                    .child(Common.CATEGORY_REF)
                    .child(event.getPopularCategoryModel().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists()) {

                                Common.categorySelected = snapshot.getValue(CategoryModel.class);
                                Common.categorySelected.setMenu_id(snapshot.getKey());

                                FirebaseDatabase.getInstance()
                                        .getReference(Common.RESTAURANT_RREF)
                                        .child(Common.selectedRestaurant.getUid())
                                        .child(Common.CATEGORY_REF)
                                        .child(event.getPopularCategoryModel().getMenu_id())
                                        .child("foods")
                                        .orderByChild("id")
                                        .equalTo(event.getPopularCategoryModel().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                if (snapshot.exists()) {
                                                    for (DataSnapshot snapshotItem : snapshot.getChildren()) {
                                                        Common.selectedFood = snapshotItem.getValue(FoodModel.class);
                                                        Common.selectedFood.setKey(snapshotItem.getKey());
                                                    }
                                                    navController.navigate(R.id.nav_fooddetails);

                                                } else {
                                                    Toast.makeText(HomeActivity.this, "Item doesn't exist!", Toast.LENGTH_SHORT).show();
                                                }
                                                dialog.dismiss();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                                dialog.dismiss();
                                                Toast.makeText(HomeActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            } else {
                                dialog.dismiss();
                                Toast.makeText(HomeActivity.this, "Item doesn't exist!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                            dialog.dismiss();
                            Toast.makeText(HomeActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onBestDealClick(final BestDealItemClick event) {
        if (event.getBestDealModel() != null) {

            dialog.show();
            FirebaseDatabase.getInstance()
                    .getReference(Common.RESTAURANT_RREF)
                    .child(Common.selectedRestaurant.getUid())
                    .child(Common.CATEGORY_REF)
                    .child(event.getBestDealModel().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists()) {

                                Common.categorySelected = snapshot.getValue(CategoryModel.class);
                                Common.categorySelected.setMenu_id(snapshot.getKey());

                                FirebaseDatabase.getInstance()
                                        .getReference(Common.RESTAURANT_RREF)
                                        .child(Common.selectedRestaurant.getUid())
                                        .child(Common.CATEGORY_REF)
                                        .child(event.getBestDealModel().getMenu_id())
                                        .child("foods")
                                        .orderByChild("id")
                                        .equalTo(event.getBestDealModel().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                if (snapshot.exists()) {
                                                    for (DataSnapshot snapshotItem : snapshot.getChildren()) {
                                                        Common.selectedFood = snapshotItem.getValue(FoodModel.class);
                                                        Common.selectedFood.setKey(snapshotItem.getKey());
                                                    }
                                                    navController.navigate(R.id.nav_fooddetails);

                                                } else {
                                                    Toast.makeText(HomeActivity.this, "Item doesn't exist!", Toast.LENGTH_SHORT).show();
                                                }
                                                dialog.dismiss();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                                dialog.dismiss();
                                                Toast.makeText(HomeActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            } else {
                                dialog.dismiss();
                                Toast.makeText(HomeActivity.this, "Item doesn't exist!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                            dialog.dismiss();
                            Toast.makeText(HomeActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCartCounter(CounterCartEvent event) {
        if (event.isSuccess()) {
            if (Common.selectedRestaurant != null)
                countCartItem();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onViewOrderClick(ViewOrderEventClick event) {
        navController.navigate(R.id.nav_view_order);
        EventBus.getDefault().postSticky(new MenuInflateEvent(true));

    }
    private void countCartItem() {
        cartDataSource.countItemInCart(Common.currentUser.getUid(), Common.selectedRestaurant.getUid())
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

    private void getUserSetUI() {
        lastUid = (auth == null || auth.getCurrentUser() == null) ?
                null : auth.getUid();
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth auth) {
        String uid = auth.getUid(); // could be null
        if ((uid == null && lastUid != null) || // loggedout
                (uid != null && lastUid == null) || // loggedin
                (uid != null && lastUid != null && // switched accounts (unlikely)
                        !uid.equals(lastUid))) {
            getUserSetUI();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMenuItemBack(MenuItemBack event) {
        menuClickId = -1;
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onActivateGps(ActivateGPSEvent event) {

        btn_benna_home.setEnabled(false);
        navigationView.getMenu().clear();
        if (isGPSEnabled()){
            btn_benna_home.setEnabled(true);
            navController.navigate(R.id.nav_restaurant);
        }else {
            initLocation();
        }
    }


    public  boolean isGPSEnabled(){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (providerEnabled){
            return true;
        }else
            return  false;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onRestaurantClick(MenuItemEvent event) {

        Bundle bundle = new Bundle();
        bundle.putString("restaurant", event.getRestaurantModel().getUid());
        navController.navigate(R.id.action_nav_restaurant_to_nav_menu, bundle);
        EventBus.getDefault().postSticky(new MenuInflateEvent(true));
        EventBus.getDefault().postSticky(new HideFABCart(false));
        countCartItem();


    }



    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMenuInflate(MenuInflateEvent event) {
        navigationView.getMenu().clear();
        if (event.isShowDetail())
            navigationView.inflateMenu(R.menu.restaurant_details_menu);
        else
            navigationView.inflateMenu(R.menu.activity_main_drawer);

    }

        private void subscribeToTopic(String topicOrder) {
        FirebaseMessaging.getInstance()
                .subscribeToTopic(topicOrder)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {
            if (!task.isSuccessful())
                Toast.makeText(HomeActivity.this, "Failed: " + task.isSuccessful(), Toast.LENGTH_SHORT).show();
        });
    }

}