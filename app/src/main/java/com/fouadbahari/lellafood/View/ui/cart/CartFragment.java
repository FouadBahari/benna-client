package com.fouadbahari.lellafood.View.ui.cart;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fouadbahari.lellafood.CallBack.ILoadTimeFromFirebaseListener;
import com.fouadbahari.lellafood.CallBack.ISearchCategoryCallbackListener;
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
import com.fouadbahari.lellafood.EventBus.SumAllItemInCartEvent;
import com.fouadbahari.lellafood.EventBus.UpdateItemCart;
import com.fouadbahari.lellafood.EventBus.ViewOrderEventClick;
import com.fouadbahari.lellafood.Model.CategoryModel;
import com.fouadbahari.lellafood.Model.FCMResponse;
import com.fouadbahari.lellafood.Model.FCMSendData;
import com.fouadbahari.lellafood.Model.FoodModel;
import com.fouadbahari.lellafood.Model.OrderModel;
import com.fouadbahari.lellafood.Model.ShipperModel;
import com.fouadbahari.lellafood.Model.ShippingOrderModel;
import com.fouadbahari.lellafood.Model.TokenModel;
import com.fouadbahari.lellafood.R;
import com.fouadbahari.lellafood.Remote.IFCMService;
import com.fouadbahari.lellafood.Remote.RetrofitFCMClient;
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
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class CartFragment extends Fragment implements ILoadTimeFromFirebaseListener, ISearchCategoryCallbackListener {

    private Place placeSelected;
    private AutocompleteSupportFragment places_fragment;
    private PlacesClient placesClient;
    private List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG);

    private BottomSheetDialog bottomSheetDialog;
    private ChipGroup chip_group_addon, chip_group_user_selected_addon;
    private EditText edt_search;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private CompositeDisposable disposable = new CompositeDisposable();

    private ISearchCategoryCallbackListener iSearchCategoryCallbackListener;

    private CartViewModel mViewModel;
    private Parcelable recyclerViewState;
    private CartDataSource cartDataSource;
    private MyCartAdapter adapter;
    IFCMService ifcmService;


    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;
    private static final int MY_PERMISSSION_REQUEST_CODE = 1;

    private String MY_ORDER_KEY ;


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


    @BindView(R.id.btn_view_order)
    Button btn_view_order;

    @OnClick(R.id.btn_view_order)
    public void onViewOrdersClicks(){
        EventBus.getDefault().postSticky(new ViewOrderEventClick());
    }


    private Unbinder unbinder;

    public static CartFragment newInstance() {
        return new CartFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = ViewModelProviders.of(this).get(CartViewModel.class);
        View root = inflater.inflate(R.layout.cart_fragment, container, false);
        ifcmService = RetrofitFCMClient.getClient().create(IFCMService.class);

        listener = this;

        mViewModel.initCartDataSource(getContext());
        mViewModel.getMutableLiveDataCartItem().observe(getViewLifecycleOwner(), new Observer<List<CartItem>>() {
            @Override
            public void onChanged(List<CartItem> cartItems) {

                btn_place_holder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("One more step!");
                        View viewlayout = LayoutInflater.from(getActivity()).inflate(R.layout.layout_place_order, null);


                        EditText edt_commet_place_order = (EditText) viewlayout.findViewById(R.id.edt_commet_place_order);
                        TextView txt_address_details = (TextView) viewlayout.findViewById(R.id.txt_address_details);
                        RadioButton rdi_other_address = (RadioButton) viewlayout.findViewById(R.id.rdi_other_address);
                        RadioButton rdi_ship_this_address = (RadioButton) viewlayout.findViewById(R.id.rdi_ship_this_address);
                        RadioButton rdi_cash_delivery = (RadioButton) viewlayout.findViewById(R.id.rdi_cash_delivery);




                        rdi_ship_this_address.setChecked(true);


                        placeSelected = null;
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
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


                                Single<String> stringSingle = Single
                                        .just(getAddressFromLngLst(task.getResult().getLatitude(),
                                                task.getResult().getLongitude()));

                                Disposable disposable = stringSingle.subscribeWith(new DisposableSingleObserver<String>() {

                                    @Override
                                    public void onSuccess(String s) {

                                        txt_address_details.setText(s);
                                        txt_address_details.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                        txt_address_details.setText(e.getMessage());
                                        txt_address_details.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        });


                        places_fragment = (AutocompleteSupportFragment) getActivity().getSupportFragmentManager()
                                .findFragmentById(R.id.places_autocomplete_fragment);
                        places_fragment.setPlaceFields(placeFields);
                        places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                            @Override
                            public void onPlaceSelected(@NonNull Place place) {
                                placeSelected = place;
                                txt_address_details.setText(placeSelected.getAddress());
                                txt_address_details.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onError(@NonNull Status status) {
                                Toast.makeText(getContext(), "" + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        txt_address_details.setText(Common.currentUser.getAddress());

                        rdi_other_address.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                if (b) {
                                    txt_address_details.setVisibility(View.VISIBLE);
                                }
                            }
                        });

                        rdi_ship_this_address.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                if (b) {
                                    placeSelected = null;
                                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        return;
                                    }
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


                                            Single<String> stringSingle = Single
                                                    .just(getAddressFromLngLst(task.getResult().getLatitude(),
                                                            task.getResult().getLongitude()));

                                            Disposable disposable = stringSingle.subscribeWith(new DisposableSingleObserver<String>() {

                                                @Override
                                                public void onSuccess(String s) {

                                                    txt_address_details.setText(s);
                                                    txt_address_details.setVisibility(View.VISIBLE);
                                                }

                                                @Override
                                                public void onError(Throwable e) {

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
                        builder.setNegativeButton("NO", (dialogInterface, i) -> {

                            dialogInterface.dismiss();
                        }).setPositiveButton("Yes", (dialogInterface, i) ->
                        {

                            if (rdi_cash_delivery.isChecked()) {
                                paymentCOD(txt_address_details.getText().toString(), edt_commet_place_order.getText().toString(), placeSelected);

                            }

                        });
                        AlertDialog dialog = builder.create();
                        dialog.setOnDismissListener(dialogInterface -> {
                            if (places_fragment != null)
                            {
                                //Fix crash duplicate order
                                getActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .remove(places_fragment)
                                        .commit();
                            }

                        });
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



    private void paymentCOD(final String address, final String comment, final Place placeSelected) {

        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid(),Common.selectedRestaurant.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<CartItem>>() {
                    @Override
                    public void accept(final List<CartItem> cartItems) throws Exception {

                        cartDataSource.sumPriceInCart(Common.currentUser.getUid(),Common.selectedRestaurant.getUid())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new SingleObserver<Double>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {

                                    }

                                    @Override
                                    public void onSuccess(Double totalPrice) {

                                        double finalePrice = totalPrice;
                                        OrderModel orderModel = new OrderModel();
                                        orderModel.setUserId(Common.currentUser.getUid());
                                        orderModel.setUserPhone(Common.currentUser.getPhone());
                                        orderModel.setShippingAddress(address);
                                        orderModel.setComment(comment);

                                        if (placeSelected != null) {
                                            orderModel.setLat(placeSelected.getLatLng().latitude);
                                            orderModel.setLng(placeSelected.getLatLng().longitude);
                                        } else if (currentLocation != null) {
                                            orderModel.setLat(currentLocation.getLatitude());
                                            orderModel.setLng(currentLocation.getLongitude());

                                        } else {
                                            orderModel.setLat(-0.1f);
                                            orderModel.setLng(-0.1f);

                                        }
                                        orderModel.setCartItemList(cartItems);
                                        orderModel.setTotalPayment(totalPrice);
                                        orderModel.setDiscount(0);
                                        orderModel.setFinalPayment(finalePrice);
                                        orderModel.setCod(true);
                                        orderModel.setTransactionId("Cash On Delivery");

                                        syncLocalTimeWithGlobalTime(orderModel);

                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        if (!e.getMessage().contains("Query returned empty result set"))
                                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });


                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                        Toast.makeText(getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }));

    }

    private void syncLocalTimeWithGlobalTime(final OrderModel orderModel) {
        final DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                long offset = snapshot.getValue(Long.class);
                long estimatedServerTimeMs = System.currentTimeMillis() + offset;
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
                Date resultDate = new Date(estimatedServerTimeMs);
                Log.d("TEST_DATE", "" + sdf.format(resultDate));

                listener.onLoadTimeSuccess(orderModel, estimatedServerTimeMs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                listener.onLoadTimeFailed(error.getMessage());
            }
        });
    }

    private void writeOrderToFirebase(OrderModel orderModel) {
        MY_ORDER_KEY = Common.createOrderNumber();
        orderModel.setKey(MY_ORDER_KEY);
        FirebaseDatabase.getInstance()
                .getReference(Common.RESTAURANT_RREF)
                .child(Common.selectedRestaurant.getUid())
                .child(Common.ORDER_REF)
                .child(MY_ORDER_KEY)
                .setValue(orderModel)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnCompleteListener(task ->

                cartDataSource.cleanCart(Common.currentUser.getUid(),Common.selectedRestaurant.getUid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Integer>() {
                            @Override
                            public void onSubscribe(Disposable d) {


                            }

                            @Override
                            public void onSuccess(Integer integer) {

                                Map<String, String> notiData = new HashMap<>();
                                notiData.put(Common.NOTIF_TITLE, "New Order");
                                notiData.put(Common.NOTIF_CONTENT, "You have new order from: " + Common.currentUser.getPhone());
                                FCMSendData sendData = new FCMSendData(Common.createTopicOrder(), notiData);


                                compositeDisposable.add(ifcmService.sendNotification(sendData)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Consumer<FCMResponse>() {
                                            @Override
                                            public void accept(FCMResponse fcmResponse) throws Exception {

                                                Toast.makeText(getContext(), "Order placed successfully!", Toast.LENGTH_SHORT).show();
                                                EventBus.getDefault().postSticky(new CounterCartEvent(true));

                                                createShipperOrder(MY_ORDER_KEY,orderModel);
                                            }
                                        }, new Consumer<Throwable>() {
                                            @Override
                                            public void accept(Throwable throwable) throws Exception {
                                                Toast.makeText(getContext(), "Order was sent but failure to send notification", Toast.LENGTH_SHORT).show();
                                                EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                            }
                                        }));

                            }

                            @Override
                            public void onError(Throwable e) {

                                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }));
    }

    private void createShipperOrder(String key,OrderModel orderModel) {
        ShippingOrderModel shippingOrderModel =new ShippingOrderModel();
        shippingOrderModel.setOrderModel(orderModel);
        shippingOrderModel.setRestaurantUid(Common.selectedRestaurant.getUid());
        shippingOrderModel.setStartTrip(false);
        shippingOrderModel.setOrderStatus(0);

        //  TO EDIT

        shippingOrderModel.setCurrentLat(-1.0);
        shippingOrderModel.setCurrentLng(-1.0);

        FirebaseDatabase.getInstance()
                .getReference(Common.SHIPPING_ORDER_REF)
                .child(key)
                .setValue(shippingOrderModel)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnCompleteListener(task -> {
            if (task.isSuccessful()){

                Map<String, String> notiData = new HashMap<>();
                notiData.put(Common.NOTIF_TITLE, "Your have new order need ship");
                notiData.put(Common.NOTIF_CONTENT, new StringBuilder("Order to :  ")
                        .append(orderModel.getUserPhone()).toString());

                FCMSendData sendData = new FCMSendData(Common.createShippingOrderTopic(), notiData);

                compositeDisposable.add(ifcmService.sendNotification(sendData)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(fcmResponse -> {
                            Toast.makeText(getContext(), "Notification sent", Toast.LENGTH_SHORT).show();
                        }, throwable -> {
                            Toast.makeText(getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }));
            }
        });
    }

//    public void updateOrder(OrderModel orderModel, int status) {
//        if (!TextUtils.isEmpty(orderModel.getKey())) {
//            Map<String, Object> updateData = new HashMap<>();
//            updateData.put("orderStatus", status);
//
//            FirebaseDatabase.getInstance()
//                    .getReference(Common.RESTAURANT_RREF)
//                    .child(Common.selectedRestaurant.getUid())
//                    .child(Common.ORDER_REF)
//                    .child(orderModel.getKey())
//                    .updateChildren(updateData)
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//
//                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    }).addOnSuccessListener(aVoid -> {
//                        Toast.makeText(getContext(), "Update order success", Toast.LENGTH_SHORT).show();
//
////
////                android.app.AlertDialog dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
////                dialog.show();
////
////                FirebaseDatabase.getInstance()
////                        .getReference(Common.TOKEN_REF)
////                        .child(orderModel.getUserId())
////                        .addListenerForSingleValueEvent(new ValueEventListener() {
////                            @Override
////                            public void onDataChange(@NonNull DataSnapshot snapshot) {
////
////                                if (snapshot.exists()) {
////                                    TokenModel tokenModel = snapshot.getValue(TokenModel.class);
////                                    Map<String, String> notiData = new HashMap<>();
////                                    notiData.put(Common.NOTIF_TITLE, "Your order was updated");
////                                    notiData.put(Common.NOTIF_CONTENT, new StringBuilder("Your order: ")
////                                            .append(orderModel.getKey())
////                                            .append(" was update to ")
////                                            .append(Common.convertStatusToString(status)).toString());
////
////                                    FCMSendData sendData = new FCMSendData(tokenModel.getToken(), notiData);
////                                    compositeDisposable.add(ifcmService.sendNotification(sendData)
////                                            .subscribeOn(Schedulers.io())
////                                            .observeOn(AndroidSchedulers.mainThread())
////                                            .subscribe(fcmResponse -> {
////                                                dialog.dismiss();
////                                                if (fcmResponse.getSuccess() == 1) {
////
////                                                    Toast.makeText(getContext(), "Update order success", Toast.LENGTH_SHORT).show();
////
////                                                } else {
////                                                    Toast.makeText(getContext(), "Update order success but failed to send notification!", Toast.LENGTH_SHORT).show();
////                                                }
////                                            }, throwable -> {
////                                                dialog.dismiss();
////                                                Toast.makeText(getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
////                                            }));
////
////                                } else {
////                                    dialog.dismiss();
////                                    Toast.makeText(getContext(), "Token not found", Toast.LENGTH_SHORT).show();
////
////                                }
////                            }
////
////                            @Override
////                            public void onCancelled(@NonNull DatabaseError error) {
////                                dialog.dismiss();
////                                Toast.makeText(getContext(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
////                            }
////                        });
//            });
//
//        } else {
//            Toast.makeText(getContext(), "Order number must not be null  or empty!", Toast.LENGTH_SHORT).show();
//        }
//    }

    private String getAddressFromLngLst(double latitude, double longitude) {

        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
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


    private void initLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ){//Can add more as per requirement

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }
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

        iSearchCategoryCallbackListener = this;
        Places.initialize(getContext(), getString(R.string.google_maps_key));
        placesClient = Places.createClient(getContext());

        setHasOptionsMenu(true);
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

        EventBus.getDefault().postSticky(new HideFABCart(true));
        recycle_cart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycle_cart.setLayoutManager(layoutManager);
        recycle_cart.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));


        sumAllItemInCart();
        calculateTotalPrice();

    }


    private void sumAllItemInCart() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid(),Common.selectedRestaurant.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {


                    }

                    @Override
                    public void onSuccess(Double aDouble) {

                        txt_total_price.setText(new StringBuilder("Total: ").append(aDouble).append(" DZD"));
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
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().postSticky(new HideFABCart(false));
        EventBus.getDefault().postSticky(new CounterCartEvent(false));

        mViewModel.onStop();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        compositeDisposable.clear();
        disposable.clear();
        super.onStop();

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        calculateTotalPrice();
        super.onResume();
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUpdateItemInCartEvent(UpdateItemCart event) {
        if (event.getCartItem() != null) {

            recyclerViewState = recycle_cart.getLayoutManager().onSaveInstanceState();
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
                            recycle_cart.getLayoutManager().onRestoreInstanceState(recyclerViewState);

                        }

                        @Override
                        public void onError(Throwable e) {

                            Toast.makeText(getContext(), "[CART ERROR]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSumAllItemIncart(SumAllItemInCartEvent event) {
        if (event.isSuccess()){
            sumAllItemInCart();
        }

    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.cart_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.delete_cart) {
            cartDataSource.cleanCart(Common.currentUser.getUid(),Common.selectedRestaurant.getUid())
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

                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        return super.onOptionsItemSelected(item);
    }

    private void calculateTotalPrice() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid(),Common.selectedRestaurant.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double price) {

                        Double total_final_price = price+200.00;
                        txt_total_price.setText(new StringBuilder("Order: ").append(price)
                                .append(" DZD")
                                .append("\n")
                                .append("Delivery: ").append("200,00 DZD")
                                .append("\n")
                                .append("Total: ").append(total_final_price)
                                .append(" DZD"));

                    }

                    @Override
                    public void onError(Throwable e) {

                        if (!e.getMessage().contains("Query returned empty result set"))
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    @Override
    public void onLoadTimeSuccess(OrderModel orderModel, long estimationTimeInMs) {

        orderModel.setCreateDate(estimationTimeInMs);
        orderModel.setOrderStatus(0);
        writeOrderToFirebase(orderModel);

    }

    @Override
    public void onLoadTimeFailed(String message) {

        Toast.makeText(getContext(), "" + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }


    @Override
    public void onSearchCategoryFound(CategoryModel categoryModel, CartItem cartItem) {
        FoodModel foodModel = Common.findFoodInListById(categoryModel, cartItem.getFoodId());
        if (foodModel != null) {

        } else {
            Toast.makeText(getContext(), "Food id not found", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onSearchCategoryNotFound(String message) {
        Toast.makeText(getContext(), "" + message, Toast.LENGTH_SHORT).show();
    }


}