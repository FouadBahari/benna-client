package com.fouadbahari.lellafood.View.ui.fooddetails;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.fouadbahari.lellafood.Common.Common;
import com.fouadbahari.lellafood.Database.CartDataSource;
import com.fouadbahari.lellafood.Database.CartDatabase;
import com.fouadbahari.lellafood.Database.CartItem;
import com.fouadbahari.lellafood.Database.LocalCartDataSource;
import com.fouadbahari.lellafood.EventBus.CounterCartEvent;
import com.fouadbahari.lellafood.EventBus.MenuItemBack;
import com.fouadbahari.lellafood.Model.CommentModel;
import com.fouadbahari.lellafood.Model.FoodModel;
import com.fouadbahari.lellafood.R;
import com.fouadbahari.lellafood.View.ui.comments.CommentsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class FoodDtailsFragment extends Fragment {

    private FoodDtailsViewModel foodDtailsViewModel;

    private Unbinder unbinder;

    private android.app.AlertDialog waitingDialog;
    private BottomSheetDialog addonBottomSheetDialog;

    private ChipGroup addonChipGroup;
    private EditText edtSearch;

    private CartDataSource cartDataSource;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();



    @BindView(R.id.img_food)
    ImageView imgFood;
    @BindView(R.id.btnCartId)
    CounterFab btnCart;
    @BindView(R.id.btn_rating)
    FloatingActionButton btn_rating;
    @BindView(R.id.food_name_id)
    TextView food_name;
    @BindView(R.id.food_description)
    TextView food_description;
    @BindView(R.id.food_price)
    TextView food_price;
    @BindView(R.id.number_button)
    ElegantNumberButton number_button;
    @BindView(R.id.ratingBar)
    RatingBar ratingBar;
    @BindView(R.id.btnShowComments)
    Button btnShowComments;









    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        foodDtailsViewModel=
                ViewModelProviders.of(this).get(FoodDtailsViewModel.class);

        View root = inflater.inflate(R.layout.food_dtails_fragment, container, false);
        unbinder= ButterKnife.bind(this,root);
        initViews();
        btn_rating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogBar();
            }
        });

        btnShowComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentsFragment commentsFragment=CommentsFragment.getInstance();
                commentsFragment.show(getActivity().getSupportFragmentManager(),"CommentsFragment");


            }
        });


        number_button.setOnClickListener(new ElegantNumberButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculateTotalPrice();
            }
        });

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CartItem cartItem =new CartItem();
                cartItem.setRestaurantId(Common.selectedRestaurant.getUid());
                cartItem.setUid(Common.currentUser.getUid());
                cartItem.setUserPhone(Common.currentUser.getPhone());

                cartItem.setCategoryId(Common.categorySelected.getMenu_id());
                cartItem.setFoodId(Common.selectedFood.getId());
                cartItem.setFoodName(Common.selectedFood.getName());
                cartItem.setFoodImage(Common.selectedFood.getImage());
                cartItem.setFoodPrice(Double.valueOf(String.valueOf(Common.selectedFood.getPrice())));
                cartItem.setFoodQuantity(Integer.valueOf(number_button.getNumber()));

                cartDataSource.getItemWhithAllOptionsInCart(Common.currentUser.getUid(),
                        Common.categorySelected.getMenu_id(),
                        cartItem.getFoodId(),
                        Common.selectedRestaurant.getUid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<CartItem>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onSuccess(CartItem cartItemFromDb) {

                                if (cartItemFromDb.equals(cartItem))
                                {

                                    cartItemFromDb.setFoodQuantity(cartItem.getFoodQuantity() +cartItem.getFoodQuantity());

                                    cartDataSource.updateCartItems(cartItemFromDb)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new SingleObserver<Integer>() {
                                                @Override
                                                public void onSubscribe(Disposable d) {

                                                }

                                                @Override
                                                public void onSuccess(Integer integer) {

                                                    Toast.makeText(getContext(), "Update Cart success", Toast.LENGTH_SHORT).show();
                                                    EventBus.getDefault().postSticky(new CounterCartEvent(true));


                                                }

                                                @Override
                                                public void onError(Throwable e) {

                                                    Toast.makeText(getContext(), "[UPDATE CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });


                                }else
                                {
                                    compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Action() {
                                                @Override
                                                public void run() throws Exception {

                                                    Toast.makeText(getContext(), "Add to cart success", Toast.LENGTH_SHORT).show();
                                                    EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                                }
                                            }, new Consumer<Throwable>() {
                                                @Override
                                                public void accept(Throwable throwable) throws Exception {
                                                    Toast.makeText(getContext(), "[Cart Error]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }));
                                }
                            }

                            @Override
                            public void onError(Throwable e) {

                                if (e.getMessage().contains("empty"))
                                {
                                    compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Action() {
                                                @Override
                                                public void run() throws Exception {

                                                    Toast.makeText(getContext(), "Add to cart success", Toast.LENGTH_SHORT).show();
                                                    EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                                }
                                            }, new Consumer<Throwable>() {
                                                @Override
                                                public void accept(Throwable throwable) throws Exception {
                                                    Toast.makeText(getContext(), "[Cart Error]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }));


                                }else
                                    Toast.makeText(getContext(), "[CART]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });
        foodDtailsViewModel.getFoodModelMutableLiveData().observe(getViewLifecycleOwner(), new Observer<FoodModel>() {
            @Override
            public void onChanged(FoodModel foodModel) {

                displayInfo(foodModel);
            }
        });

        foodDtailsViewModel.getCommentModelMutableLiveData().observe(getViewLifecycleOwner(), new Observer<CommentModel>() {
            @Override
            public void onChanged(CommentModel commentModel) {

                submitRatingToFirebase(commentModel);
            }
        });

        return root;
    }


    private void showDialogBar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Rating Food");
        builder.setMessage("Please fill infos");

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View rating_layout = inflater.inflate(R.layout.layout_rating, null);
        final RatingBar ratingBar=(RatingBar)rating_layout.findViewById(R.id.rating_bar);
        final EditText edt_comment=(EditText)rating_layout.findViewById(R.id.edt_comment);


        builder.setView(rating_layout);
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });
        builder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                CommentModel commentModel=new CommentModel();
                commentModel.setName(Common.currentUser.getName());
                commentModel.setUid(Common.currentUser.getUid());
                commentModel.setComment(edt_comment.getText().toString());
                commentModel.setRatingValue(ratingBar.getRating());
                Map<String ,Object> serverTimepStump=new HashMap<>();
                serverTimepStump.put("timeStamp", ServerValue.TIMESTAMP);
                commentModel.setCommentTimeStamp(serverTimepStump);

                foodDtailsViewModel.setCommentModel(commentModel);

            }
        });

        AlertDialog dialog=builder.create();
        dialog.show();
    }

    private void initViews() {
        cartDataSource =new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

        waitingDialog= new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();


    }


    private void submitRatingToFirebase(final CommentModel commentModel) {
        waitingDialog.show();
        FirebaseDatabase.getInstance()
                .getReference(Common.RESTAURANT_RREF)
                .child(Common.selectedRestaurant.getUid())
                .child(Common.COMMENT_REF)
                .child(Common.selectedFood.getId())
                .push()
                .setValue(commentModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful())
                                {

                                    addRatingToFood(commentModel.getRatingValue());
                                }

                                waitingDialog.dismiss();
                            }
                        });
    }

    private void addRatingToFood(final float ratingValue) {

        FirebaseDatabase.getInstance()
                .getReference(Common.RESTAURANT_RREF)
                .child(Common.selectedRestaurant.getUid())
                .child(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
                .child("foods")
                .child(Common.selectedFood.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists())
                        {

                             final FoodModel foodModel=snapshot.getValue(FoodModel.class);
                            foodModel.setKey(Common.selectedFood.getKey());


                            if (foodModel.getRatingValue()==null)
                                foodModel.setRatingValue(0d);
                            if (foodModel.getRatingCount()==null)
                                foodModel.setRatingCount(0l);
                            double sumRating=foodModel.getRatingValue()+ratingValue;
                            long ratingCount=foodModel.getRatingCount()+1;


                            Map<String,Object>  updateData =new HashMap<>();
                            updateData.put("ratingValue",sumRating);
                            updateData.put("ratingCount",ratingCount);

                            foodModel.setRatingValue(sumRating);
                            foodModel.setRatingCount(ratingCount);


                            snapshot.getRef()
                                    .updateChildren(updateData)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            waitingDialog.dismiss();
                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(getContext(), "Thank You!",
                                                        Toast.LENGTH_SHORT).show();
                                                Common.selectedFood=foodModel;
                                                foodDtailsViewModel.setFoodModel(foodModel);
                                            }
                                        }
                                    });
                        }
                        else waitingDialog.dismiss();
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        waitingDialog.dismiss();
                        Toast.makeText(getContext(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayInfo(FoodModel foodModel) {

        Glide.with(getContext()).load(foodModel.getImage()).into(imgFood);
        food_name.setText(new StringBuilder(foodModel.getName()));
        food_description.setText(new StringBuilder(foodModel.getDescription()));
        food_price.setText(new StringBuilder(foodModel.getPrice().toString()));

        if (foodModel.getRatingValue() !=null)
              ratingBar.setRating(foodModel.getRatingValue().floatValue() /foodModel.getRatingCount());

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(Common.selectedFood.getName());


        calculateTotalPrice();

    }

    private void calculateTotalPrice() {
        double totalPrice=Double.parseDouble(Common.selectedFood.getPrice().toString()),displayPrice=0.0;


        displayPrice=totalPrice*(Integer.parseInt(number_button.getNumber()));
        displayPrice=Math.round(displayPrice*100.0/100.0);

        food_price.setText(new StringBuilder("").append(Common.formatPrice(displayPrice))
                .append(" DZD").toString());

    }




    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }
    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}