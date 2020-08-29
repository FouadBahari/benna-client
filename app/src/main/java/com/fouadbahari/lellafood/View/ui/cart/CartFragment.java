package com.fouadbahari.lellafood.View.ui.cart;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.fouadbahari.lellafood.CallBack.ILoadTimeFromFirebaseListener;
import com.fouadbahari.lellafood.CallBack.MyButtonClickListener;
import com.fouadbahari.lellafood.Common.Common;
import com.fouadbahari.lellafood.Common.MySwipeHelper;
import com.fouadbahari.lellafood.Controller.MyCartAdapter;
import com.fouadbahari.lellafood.Database.CartDataSource;
import com.fouadbahari.lellafood.Database.CartDatabase;
import com.fouadbahari.lellafood.Database.CartItem;
import com.fouadbahari.lellafood.Database.LocalCartDataSource;
import com.fouadbahari.lellafood.EventBus.CounterCartEvent;
import com.fouadbahari.lellafood.EventBus.HideFABCart;
import com.fouadbahari.lellafood.EventBus.MenuItemBack;
import com.fouadbahari.lellafood.EventBus.UpdateItemCart;
import com.fouadbahari.lellafood.Model.Order;
import com.fouadbahari.lellafood.R;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class CartFragment extends Fragment implements ILoadTimeFromFirebaseListener {

    private CompositeDisposable compositeDisposable=new CompositeDisposable();

    private CartViewModel mViewModel;
    private Parcelable recyclerViewState;
    private CartDataSource cartDataSource;
    private MyCartAdapter adapter;

    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;
    private static final int MY_PERMISSSION_REQUEST_CODE=1;


    ILoadTimeFromFirebaseListener listener;

    @BindView(R.id.recycle_cart)
    RecyclerView recycle_cart;
    @BindView(R.id.txt_total_price)
    TextView txt_total_price;
    @BindView(R.id.txt_empty_cart)
    TextView txt_empty_cart;
    @BindView(R.id.groupe_place_holder)
    CardView groupe_place_holder;
    @BindView(R.id.btn_place_holder)
    Button btn_place_holder;


    private Unbinder unbinder;

    public static CartFragment newInstance() {
        return new CartFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = ViewModelProviders.of(this).get(CartViewModel.class);
        View root = inflater.inflate(R.layout.cart_fragment, container, false);
        listener= this;

        mViewModel.initCartDataSource(getContext());
        mViewModel.getMutableLiveDataCartItem().observe(getViewLifecycleOwner(), new Observer<List<CartItem>>() {
            @Override
            public void onChanged(List<CartItem> cartItems) {

                btn_place_holder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("One more step!");
                        View viewlayout = LayoutInflater.from(getContext()).inflate(R.layout.layout_place_order, null);

                        final EditText edt_address = (EditText) viewlayout.findViewById(R.id.edt_address);
                        final EditText edt_commet_place_order = (EditText) viewlayout.findViewById(R.id.edt_commet_place_order);
                        final TextView txt_address_details = (TextView) viewlayout.findViewById(R.id.txt_address_details);
                        RadioButton rdi_home = (RadioButton) viewlayout.findViewById(R.id.rdi_home_address);
                        RadioButton rdi_other_address = (RadioButton) viewlayout.findViewById(R.id.rdi_other_address);
                        RadioButton rdi_ship_this_address = (RadioButton) viewlayout.findViewById(R.id.rdi_ship_this_address);
                        final RadioButton rdi_cash_delivery = (RadioButton) viewlayout.findViewById(R.id.rdi_cash_delivery);
                        RadioButton rdi_Braintree = (RadioButton) viewlayout.findViewById(R.id.rdi_Braintree);


                        edt_address.setText(Common.currentUser.getAddress());

                        rdi_home.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                if (b) {
                                    edt_address.setText(Common.currentUser.getAddress());
                                    txt_address_details.setVisibility(View.GONE);

                                }
                            }
                        });

                        rdi_other_address.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                if (b) {
                                    edt_address.setText(null);
                                    txt_address_details.setVisibility(View.GONE);

                                }
                            }
                        });

                        rdi_ship_this_address.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @SuppressLint("MissingPermission")
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                if (b) {

                                    fusedLocationProviderClient.getLastLocation()
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    txt_address_details.setVisibility(View.GONE);
                                                }
                                            }).addOnCompleteListener(new OnCompleteListener<Location>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Location> task) {
                                            final String coordinates = new StringBuilder()
                                                    .append(task.getResult().getLatitude())
                                                    .append("/")
                                                    .append(task.getResult().getLongitude()).toString();


                                            Single<String> stringSingle=Single
                                                    .just(getAddressFromLngLst(task.getResult().getLatitude(),
                                                            task.getResult().getLongitude()));

                                            Disposable disposable= stringSingle.subscribeWith(new DisposableSingleObserver<String>(){

                                                @Override
                                                public void onSuccess(String s) {

                                                    edt_address.setText(coordinates);
                                                    txt_address_details.setText(s);
                                                    txt_address_details.setVisibility(View.VISIBLE);
                                                }

                                                @Override
                                                public void onError(Throwable e) {

                                                    edt_address.setText(coordinates);
                                                    txt_address_details.setText(e.getMessage());
                                                    txt_address_details.setVisibility(View.VISIBLE);
                                                }
                                            });


                                        }
                                    });
                                }
                            }
                        });


                        builder.setView(viewlayout);
                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                dialogInterface.dismiss();
                            }
                        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if (rdi_cash_delivery.isChecked())
                                {
                                    paymentCOD(edt_address.getText().toString(),edt_commet_place_order.getText().toString());

                                }

                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
                if (cartItems == null || cartItems.isEmpty()) {

                    recycle_cart.setVisibility(View.GONE);
                    groupe_place_holder.setVisibility(View.GONE);
                    txt_empty_cart.setVisibility(View.VISIBLE);

                } else {
                    recycle_cart.setVisibility(View.VISIBLE);
                    groupe_place_holder.setVisibility(View.VISIBLE);
                    txt_empty_cart.setVisibility(View.GONE);

                    adapter = new MyCartAdapter(getContext(), cartItems);
                    recycle_cart.setAdapter(adapter);

                }

            }
        });
        unbinder = ButterKnife.bind(this, root);
        initView();
        initLocation();
        return root;
    }

    private void paymentCOD(final String address, final String comment) {

        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<List<CartItem>>() {
            @Override
            public void accept(final List<CartItem> cartItems) throws Exception {

                cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Double>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onSuccess(Double totalPrice) {

                                double finalePrice=totalPrice;
                                Order order=new Order();
                                order.setUserId(Common.currentUser.getUid());
                                order.setUserName(Common.currentUser.getName());
                                order.setUserPhone(Common.currentUser.getPhone());
                                order.setShippingAddress(address);
                                order.setComment(comment);

                                if (currentLocation != null)
                                {
                                    order.setLat(currentLocation.getLatitude());
                                    order.setLng(currentLocation.getLongitude());

                                }else
                                {
                                    order.setLat(-0.1f);
                                    order.setLng(-0.1f);

                                }
                                order.setCartItemList(cartItems);
                                order.setTotalPayment(totalPrice);
                                order.setDiscount(0);
                                order.setFinalPayment(finalePrice);
                                order.setCod(true);
                                order.setTransactionId("Cash On Delivery");

                                syncLocalTimeWithGlobalTime(order);

                            }

                            @Override
                            public void onError(Throwable e) {

                                Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });


            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {

                Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));

    }

    private void syncLocalTimeWithGlobalTime(final Order order) {
        final DatabaseReference offsetRef=FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                long offset =snapshot.getValue(Long.class);
                long estimatedServerTimeMs =System.currentTimeMillis()+offset;
                SimpleDateFormat sdf=new SimpleDateFormat("MMM dd,yyyy HH:mm");
                Date resultDate=new Date(estimatedServerTimeMs);
                Log.d("TEST_DATE",""+sdf.format(resultDate));

                listener.onLoadTimeSuccess(order,estimatedServerTimeMs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                listener.onLoadTimeFailed(error.getMessage());
            }
        });
    }

    private void writeOrderToFirebase(Order order) {
        FirebaseDatabase.getInstance()
                .getReference(Common.ORDER_REF)
                .child(Common.createOrderNumber())
                .setValue(order)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                cartDataSource.cleanCart(Common.currentUser.getUid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Integer>() {
                            @Override
                            public void onSubscribe(Disposable d) {


                            }

                            @Override
                            public void onSuccess(Integer integer) {

                             Toast.makeText(getContext(), "Order placed successfully", Toast.LENGTH_SHORT).show();


                            }

                            @Override
                            public void onError(Throwable e) {

                                Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private String getAddressFromLngLst(double latitude, double longitude) {

        Geocoder geocoder=new Geocoder(getContext(), Locale.getDefault());
        String result="";
        try {
            List<Address> addressList =geocoder.getFromLocation(latitude,longitude,1);

            if (addressList != null && addressList.size() >0)
            {
                Address address =addressList.get(0);
                StringBuilder sb=new StringBuilder(address.getAddressLine(0));
                result=sb.toString();

            }else result="Address not found";

        }
        catch (IOException e)
        {
            e.printStackTrace();
            result=e.getMessage();
        }
        return result;
    }

    @SuppressLint("MissingPermission")
    private void initLocation() {

        buildLocationRequest();
        buildLocationCallBack();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());


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
                LocationServices.getSettingsClient(getActivity()).checkLocationSettings(builder.build());



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
                                        getActivity(),
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
        switch (requestCode) {
            case LocationRequest.PRIORITY_HIGH_ACCURACY:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        Log.i("Message", "onActivityResult: GPS Enabled by user");
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Log.i("Message", "onActivityResult: User rejected GPS request");
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    private void initView() {

        setHasOptionsMenu(true);
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

        EventBus.getDefault().postSticky(new HideFABCart(true));
        recycle_cart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycle_cart.setLayoutManager(layoutManager);
        recycle_cart.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(), recycle_cart, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {

                buf.add(new MyButton(getContext(), "Delete", 30, 0, Color.parseColor("#d63031"),
                        new MyButtonClickListener() {
                            @Override
                            public void onClick(final int pos) {

                                CartItem cartItem = adapter.getItemAtPosition(pos);
                                cartDataSource.deleteCartItem(cartItem)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new SingleObserver<Integer>() {
                                            @Override
                                            public void onSubscribe(Disposable d) {


                                            }

                                            @Override
                                            public void onSuccess(Integer integer) {

                                                adapter.notifyItemRemoved(pos);
                                                sumAllItemInCart();
                                                EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                                Toast.makeText(getContext(), "Delete item from cart successful", Toast.LENGTH_SHORT).show();


                                            }

                                            @Override
                                            public void onError(Throwable e) {

                                                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }
                        }));
            }
        };

        sumAllItemInCart();
    }

    private void sumAllItemInCart() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {


                    }

                    @Override
                    public void onSuccess(Double aDouble) {

                        txt_total_price.setText(new StringBuilder("Total: $").append(aDouble));
                    }

                    @Override
                    public void onError(Throwable e) {

                        if (!e.getMessage().contains("Query returned empty"))
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();


                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

    }

    @Override
    public void onStop() {
        EventBus.getDefault().postSticky(new HideFABCart(false));
        mViewModel.onStop();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        compositeDisposable.clear();
        super.onStop();

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onUpdateItemInCartEvent(UpdateItemCart event)
    {
        if (event.getCartItem()!=null)
        {

            recyclerViewState =recycle_cart.getLayoutManager().onSaveInstanceState();
            cartDataSource.updateCartItems(event.getCartItem())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {


                            calculateTotalPrice();
                            recycle_cart.getLayoutManager().onRestoreInstanceState(recyclerViewState ) ;

                        }

                        @Override
                        public void onError(Throwable e) {

                            Toast.makeText(getContext(), "[CART ERROR]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.action_settings).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.cart_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.delete_cart)
        {
            cartDataSource.cleanCart(Common.currentUser.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {


                        }

                        @Override
                        public void onSuccess(Integer integer) {

                            Toast.makeText(getContext(), "Clean cart success", Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                        }

                        @Override
                        public void onError(Throwable e) {

                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        return super.onOptionsItemSelected(item);
    }

    private void calculateTotalPrice() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double price) {

                        txt_total_price.setText(new StringBuilder("Total: $")
                        .append(Common.formatPrice(price)));
                    }

                    @Override
                    public void onError(Throwable e) {

                        Toast.makeText(getContext(), "[SUM ERROR]"+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    @Override
    public void onLoadTimeSuccess(Order order, long estimationTimeInMs) {

        order.setCreateDate(estimationTimeInMs);
        order.setOrderStatus(0);
        writeOrderToFirebase(order);

    }

    @Override
    public void onLoadTimeFailed(String message) {

        Toast.makeText(getContext(), ""+message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}