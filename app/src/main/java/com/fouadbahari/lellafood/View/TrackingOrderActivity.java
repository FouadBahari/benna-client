package com.fouadbahari.lellafood.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.fouadbahari.lellafood.Common.Common;
import com.fouadbahari.lellafood.Common.MyCustomMarkerAdapter;
import com.fouadbahari.lellafood.Model.ShippingOrderModel;
import com.fouadbahari.lellafood.R;
import com.fouadbahari.lellafood.Remote.IGoogleApi;
import com.fouadbahari.lellafood.Remote.RetrofitGoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class TrackingOrderActivity extends FragmentActivity implements OnMapReadyCallback, ValueEventListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Marker shipperMarker =  null;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IGoogleApi iGoogleApi;
    private List<LatLng> polyLineList;
    private PolylineOptions polylineOptions,blackpolylineOptions;
    private Polyline yellowPolyLine,grayPolyLine,blackPolyLine;
    private DatabaseReference shipperRef;

    private Handler handler;
    private int index , next;
    private LatLng start , end;
    private float v;
    private double lat,lng;
    private boolean isInt = false;

    @OnClick(R.id.btn_call)
    void onCallClick(){
        if (Common.currentShippingOrder != null){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                //Request permission
                Dexter.withContext(this)
                        .withPermission(Manifest.permission.CALL_PHONE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                                Toast.makeText(TrackingOrderActivity.this, "You must accept this permission To call user", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                            }
                        }).check();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse(new StringBuilder("tel:")
                    .append(Common.currentShippingOrder.getShipperPhone()).toString()));

            startActivity(intent);

        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order);

        ButterKnife.bind(this);
        iGoogleApi = RetrofitGoogleApiClient.getInstance().create(IGoogleApi.class);

        init();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        subscribeShipperMove();
    }

    private void subscribeShipperMove() {
        shipperRef = FirebaseDatabase.getInstance()
                .getReference(Common.SHIPPING_ORDER_REF)
                .child(Common.currentShippingOrder.getKey());

        shipperRef.addListenerForSingleValueEvent(this);
    }

    private void init() {

        locationRequest = new LocationRequest();
        locationRequest.setSmallestDisplacement(20f);
        locationRequest.setInterval(15000);
        locationRequest.setFastestInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new MyCustomMarkerAdapter(getLayoutInflater()));

        mMap.getUiSettings().setZoomControlsEnabled(true);
        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.
                    loadRawResourceStyle(getApplicationContext(), R.raw.uber_light_with_label));
            if (!success)
                Log.d("ERROR", "style parsing error");

        } catch (Resources.NotFoundException e) {
            Log.d("ERROR", e.getMessage());


        }

        Dexter.withContext(getApplicationContext()).
                withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {


                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                        Toast.makeText(TrackingOrderActivity.this, "Permission" +
                                        permissionDeniedResponse.getPermissionName() + "was denied"
                                , Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();
        drawRoutes();
    }

    private void drawRoutes() {
        LatLng locationOrder = new LatLng(Common.currentShippingOrder.getOrderModel().getLat()
                ,Common.currentShippingOrder.getOrderModel().getLng());
        LatLng locationShipper = new LatLng(Common.currentShippingOrder.getCurrentLat()
                ,Common.currentShippingOrder.getCurrentLng());


        mMap.addMarker(new MarkerOptions()
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.box))
        .title(Common.currentShippingOrder.getOrderModel().getUserPhone())
        .snippet(Common.currentShippingOrder.getOrderModel().getShippingAddress())
        .position(locationOrder));


        if (shipperMarker ==  null){
            int height =70 , width = 35 ;
            BitmapDrawable bitmapDrawable = (BitmapDrawable) ContextCompat
                    .getDrawable(TrackingOrderActivity.this,R.drawable.shippernew);
            Bitmap resized = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(),width,height,false);

            shipperMarker =  mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(resized))
                    .title(new StringBuilder("Shipper: ").append(Common.currentShippingOrder.getShipperName()).toString())
                    .snippet(new StringBuilder("Phone: ").append(Common.currentShippingOrder.getShipperPhone())
                    .append("\n")
                    .append("Estimate Time Delivery: ")
                    .append(Common.currentShippingOrder.getEstimateTime()).toString())
                    .position(locationShipper));

            shipperMarker.showInfoWindow();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationShipper,18));
        }
        else {
            shipperMarker.setPosition(locationShipper);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationShipper,18));

        }

        String to = new StringBuilder()
                .append(Common.currentShippingOrder.getOrderModel().getLat())
                .append(",")
                .append(Common.currentShippingOrder.getOrderModel().getLng())
                .toString();
        String from = new StringBuilder()
                .append(Common.currentShippingOrder.getCurrentLat())
                .append(",")
                .append(Common.currentShippingOrder.getCurrentLng())
                .toString();


        compositeDisposable.add(iGoogleApi.getDirecions("driving",
                "less_driving",
                from, to,
                getString(R.string.google_maps_key))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {

                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject route = jsonArray.getJSONObject(i);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyLine = poly.getString("points");
                            polyLineList = Common.decodePoly(polyLine);
                        }

                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.YELLOW);
                        polylineOptions.width(12);
                        polylineOptions.startCap(new SquareCap());
                        polylineOptions.jointType(JointType.ROUND);
                        polylineOptions.addAll(polyLineList);
                        yellowPolyLine = mMap.addPolyline(polylineOptions);


                    } catch (Exception e) {

                    }

                }, throwable -> {
                    Toast.makeText(this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().removeAllStickyEvents();
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        shipperRef.removeEventListener(this);
        isInt = false;
        super.onDestroy();
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        //save old position
        String from = new StringBuilder()
                .append(Common.currentShippingOrder.getCurrentLat())
                .append(",")
                .append(Common.currentShippingOrder.getCurrentLng())
                .toString();
        //update position
        Common.currentShippingOrder = snapshot.getValue(ShippingOrderModel.class);
        Common.currentShippingOrder.setKey(snapshot.getKey());

        String to  = new StringBuilder()
                .append(Common.currentShippingOrder.getCurrentLat())
                .append(",")
                .append(Common.currentShippingOrder.getCurrentLng())
                .toString();

        if (snapshot.exists()){
            if (isInt){
                moveMakerAnimation(shipperMarker,from,to);
            }else isInt = true;
        }
    }

    private void moveMakerAnimation(Marker shipperMarker, String from, String to) {
        compositeDisposable.add(iGoogleApi.getDirecions("driving",
                "less_driving",
                from,to,
                getString(R.string.google_maps_key))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(returnResult -> {
            try {
                JSONObject jsonObject = new JSONObject(returnResult);
                JSONArray jsonArray = jsonObject.getJSONArray("routes");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject route = jsonArray.getJSONObject(i);
                    JSONObject poly = route.getJSONObject("overview_polyline");
                    String polyLine = poly.getString("points");
                    polyLineList = Common.decodePoly(polyLine);
                }

                polylineOptions = new PolylineOptions();
                polylineOptions.color(Color.GRAY);
                polylineOptions.width(12);
                polylineOptions.startCap(new SquareCap());
                polylineOptions.jointType(JointType.ROUND);
                polylineOptions.addAll(polyLineList);
                grayPolyLine = mMap.addPolyline(polylineOptions);

                blackpolylineOptions = new PolylineOptions();
                blackpolylineOptions.color(Color.BLACK);
                blackpolylineOptions.width(6);
                blackpolylineOptions.startCap(new SquareCap());
                blackpolylineOptions.jointType(JointType.ROUND);
                blackpolylineOptions.addAll(polyLineList);
                blackPolyLine = mMap.addPolyline(blackpolylineOptions);

                ValueAnimator polylineAnimator = ValueAnimator.ofInt(0,100);
                polylineAnimator.setDuration(2000);
                polylineAnimator.setInterpolator(new LinearInterpolator());
                polylineAnimator.addUpdateListener(valueAnimator -> {
                    List<LatLng> points = grayPolyLine.getPoints();
                    int percentValue = (int)valueAnimator.getAnimatedValue();
                    int size = points.size();
                    int newPoints = (int)(size*(percentValue/100.0f));
                    List<LatLng> p = points.subList(0,newPoints);
                    blackPolyLine.setPoints(p);
                });
                polylineAnimator.start();

                //BikeMoving
                handler = new Handler();
                index = -1;
                next = 1;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (index < polyLineList.size() - 1) {
                            index++;
                            next = index + 1;
                            start = polyLineList.get(index);
                            end = polyLineList.get(next);
                        }
                        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 1);
                        valueAnimator.setDuration(1500);
                        valueAnimator.setInterpolator(new LinearInterpolator());
                        valueAnimator.addUpdateListener(valueAnimator1 -> {
                            v = valueAnimator1.getAnimatedFraction();
                            lng = v * end.longitude + (1 - v)
                                    * start.longitude;
                            lat = v * end.latitude + (1 - v)
                                    * start.latitude;
                            LatLng newPos = new LatLng(lat, lng);
                            shipperMarker.setPosition(newPos);
                            shipperMarker.setAnchor(0.5f, 0.5f);
                            shipperMarker.setRotation(Common.getBearing(start, newPos));

                            mMap.moveCamera(CameraUpdateFactory.newLatLng(newPos));
                        });
                        valueAnimator.start();
                        if (index < polyLineList.size() - 2) {
                            handler.postDelayed(this, 1500);
                        }
                    }
                },1500);
            } catch (Exception e) {

            }

        }, throwable -> {
            Toast.makeText(this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }));
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
}