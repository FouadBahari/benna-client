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
import com.fouadbahari.lellafood.Model.AddonModel;
import com.fouadbahari.lellafood.Model.CommentModel;
import com.fouadbahari.lellafood.Model.FoodModel;
import com.fouadbahari.lellafood.Model.SizeModel;
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
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class FoodDtailsFragment extends Fragment implements TextWatcher {

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
    @BindView(R.id.rdi_groupe_size)
    RadioGroup radioGroup;
    @BindView(R.id.chip_group_user_selected)
    ChipGroup chip_group_user_selected;
    @BindView(R.id.img_addon)
    ImageView img_addon;








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

        img_addon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.selectedFood.getAddon()!=null)
                {
                    displayAddonList();
                    addonBottomSheetDialog.show();
                }
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

                final CartItem cartItem =new CartItem();
                cartItem.setUid(Common.currentUser.getUid());
                cartItem.setUserPhone(Common.currentUser.getPhone());

                cartItem.setFoodId(Common.selectedFood.getId());
                cartItem.setFoodName(Common.selectedFood.getName());
                cartItem.setFoodImage(Common.selectedFood.getImage());


                cartItem.setFoodPrice(Double.valueOf(String.valueOf(Common.selectedFood.getPrice())));
                cartItem.setFoodQuantity(Integer.valueOf(number_button.getNumber()));
                cartItem.setFoodExtraPrice(Common.calculateExtraPrice(Common.selectedFood.getUserSelectedSize(),Common.selectedFood.getUserSelectedAddon()));
                if (Common.selectedFood.getUserSelectedAddon() != null)
                    cartItem.setFoodAddon(new Gson().toJson(Common.selectedFood.getUserSelectedAddon()));
                else
                    cartItem.setFoodAddon("Default");

                if (Common.selectedFood.getUserSelectedSize() != null)
                    cartItem.setFoodSize(new Gson().toJson(Common.selectedFood.getUserSelectedSize()));
                else
                    cartItem.setFoodSize("Default");


                cartDataSource.getItemWhithAllOptionsInCart(Common.currentUser.getUid(),
                        cartItem.getFoodId(),
                        cartItem.getFoodSize(),
                        cartItem.getFoodAddon())
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

                                    cartItemFromDb.setFoodExtraPrice(cartItem.getFoodExtraPrice());
                                    cartItemFromDb.setFoodAddon(cartItem.getFoodAddon());
                                    cartItemFromDb.setFoodSize(cartItem.getFoodSize());
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

    private void displayAddonList() {
        if (Common.selectedFood.getAddon().size()>0)
        {
            addonChipGroup.clearCheck();
            addonChipGroup.removeAllViews();

            edtSearch.addTextChangedListener(this);

            for (final AddonModel addonModel :Common.selectedFood.getAddon())
            {
                    Chip chip=(Chip)getLayoutInflater().inflate(R.layout.layout_addon_item,null);
                    chip.setText(new StringBuilder(addonModel.getName()).append("(+$").
                            append(addonModel.getPrice()).append(")"));
                    chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {

                                    if (Common.selectedFood.getUserSelectedAddon() == null)
                                        Common.selectedFood.setUserSelectedAddon(new ArrayList<AddonModel>());
                                    Common.selectedFood.getUserSelectedAddon().add(addonModel);


                            }
                        }
                    });
                    addonChipGroup.addView(chip);


            }

        }
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
        addonBottomSheetDialog =new BottomSheetDialog(getContext(),R.style.DialogStyle);
        View layou_addon_display=getLayoutInflater().inflate(R.layout.layout_addon_display,null);
        addonChipGroup=(ChipGroup)layou_addon_display.findViewById(R.id.chip_group_addon);
        edtSearch=(EditText)layou_addon_display.findViewById(R.id.edt_search);
        addonBottomSheetDialog.setContentView(layou_addon_display);

        addonBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                displayUserSelectedAddon();
                calculateTotalPrice();
            }
        });



    }

    private void displayUserSelectedAddon() {
        if (Common.selectedFood.getUserSelectedAddon() != null
        && Common.selectedFood.getUserSelectedAddon().size() >0 )
        {

            chip_group_user_selected.clearCheck();
            chip_group_user_selected.removeAllViews();
            for (final AddonModel addonModel:Common.selectedFood.getUserSelectedAddon())
            {
                Chip chip=(Chip)getLayoutInflater().inflate(R.layout.layout_chip_with_delete,null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                .append(addonModel.getPrice()).append(")"));
                chip.setClickable(false);
                chip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        chip_group_user_selected.removeView(v);
                        Common.selectedFood.getUserSelectedAddon().remove(addonModel);
                        calculateTotalPrice();
                    }
                });
                chip_group_user_selected.addView(chip);
            }
        }else //if (Common.selectedFood.getUserSelectedAddon().size() == 0)
        {
            chip_group_user_selected.removeAllViews();
        }
    }

    private void submitRatingToFirebase(final CommentModel commentModel) {
        waitingDialog.show();
        FirebaseDatabase.getInstance()
                .getReference(Common.COMMENT_REF)
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
                .getReference(Common.CATEGORY_REF)
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
                            double result=sumRating/ratingCount;


                            Map<String,Object>  updateData =new HashMap<>();
                            updateData.put("ratingValue",result);
                            updateData.put("ratingCount",ratingCount);

                            foodModel.setRatingValue(result);
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
        ratingBar.setRating(foodModel.getRatingValue().floatValue());

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(Common.selectedFood.getName());

        for (final SizeModel sizeModel:Common.selectedFood.getSize())
        {

            RadioButton radioButton=new RadioButton(getContext());
            radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                    {
                        Common.selectedFood.setUserSelectedSize(sizeModel);
                        calculateTotalPrice();
                    }
                }
            });
            LinearLayout.LayoutParams params =new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.MATCH_PARENT,1.0f);
            radioButton.setLayoutParams(params);
            radioButton.setText(sizeModel.getName());
            radioButton.setTag(sizeModel.getPrice());

            radioGroup.addView(radioButton);
            radioButton.setChecked(true);
        }

        if (radioGroup.getChildCount()>0)
        {
            RadioButton mradioButton=(RadioButton)radioGroup.getChildAt(0);
            mradioButton.setChecked(true);

        }


        calculateTotalPrice();

    }

    private void calculateTotalPrice() {
        double totalPrice=Double.parseDouble(Common.selectedFood.getPrice().toString()),displayPrice=0.0;

        if (Common.selectedFood.getUserSelectedAddon() != null &&
                Common.selectedFood.getUserSelectedAddon().size() >0)
            for (AddonModel addonModel :Common.selectedFood.getUserSelectedAddon())
                totalPrice += Double.parseDouble(addonModel.getPrice().toString());






        totalPrice += Double.parseDouble(Common.selectedFood.getUserSelectedSize().getPrice().toString());
        displayPrice=totalPrice*(Integer.parseInt(number_button.getNumber()));
        displayPrice=Math.round(displayPrice*100.0/100.0);

        food_price.setText(new StringBuilder("").append(Common.formatPrice(displayPrice)).toString());

    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {


    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, final int count) {

        addonChipGroup.clearCheck();
        addonChipGroup.removeAllViews();
        for (final AddonModel addonModel :Common.selectedFood.getAddon())
        {
            if (addonModel.getName().toLowerCase().contains(s.toString().toLowerCase()))
            {
                Chip chip=(Chip)getLayoutInflater().inflate(R.layout.layout_addon_item,null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$").
                        append(addonModel.getPrice()).append(")"));
                chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked)
                        {
                            if (Common.selectedFood.getUserSelectedAddon() == null)
                                Common.selectedFood.setUserSelectedAddon(new ArrayList<AddonModel>());
                            Common.selectedFood.getUserSelectedAddon().add(addonModel);

                        }
                    }
                });
                addonChipGroup.addView(chip);

            }
        }

    }


    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }
}