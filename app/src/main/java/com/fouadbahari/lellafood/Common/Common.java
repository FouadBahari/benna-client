package com.fouadbahari.lellafood.Common;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.widget.TextView;

import com.fouadbahari.lellafood.Model.AddonModel;
import com.fouadbahari.lellafood.Model.CategoryModel;
import com.fouadbahari.lellafood.Model.FoodModel;
import com.fouadbahari.lellafood.Model.SizeModel;
import com.fouadbahari.lellafood.Model.User;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

public class Common {
    public static final String USER_REFERENCES="Users";
    public static final String POPULAR_CATEGORY_REF="MostPopular";
    public static final String BEST_DEALS_REF ="BestDeals" ;
    public static final int DEFAULT_COLUMN_COUNT = 0;
    public static final int FULL_WIDTH_COLUMN = 1;
    public static final String CATEGORY_REF ="Category";
    public static final String COMMENT_REF = "Comments";
    public static  User currentUser;


    public static CategoryModel categorySelected;
    public static FoodModel selectedFood;
    public static User userCurrent;

    public static String formatPrice(double price) {
        if (price != 0)
        {
            DecimalFormat df=new DecimalFormat("#,##0.00");
            df.setRoundingMode(RoundingMode.UP);
            String finalPrice=new StringBuilder(df.format(price)).toString();
            return finalPrice.replace(".",",");
        }else  return "0,00";
    }

    public static Double calculateExtraPrice(SizeModel userSelectedSize, List<AddonModel> userSelectedAddon) {
        Double result=0.0;
        if (userSelectedSize==null && userSelectedAddon ==null)
            return 0.0;
        else if (userSelectedSize == null)
        {
            for (AddonModel addonModel: userSelectedAddon)
                result+=addonModel.getPrice();
            return result;
        }
        else if (userSelectedAddon==null)
        {
            return userSelectedSize.getPrice()*1.0;
        }else {
            result=userSelectedSize.getPrice()*1.0;
            for (AddonModel addonModel:userSelectedAddon)
                result+=addonModel.getPrice();
            return result;
        }

    }

    public static void setSpanString(String welcome, String name, TextView txt_user) {

        SpannableStringBuilder builder=new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString  spannableString=new SpannableString(name);
        StyleSpan boldSpan=new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan,0,name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        txt_user.setText(builder,TextView.BufferType.SPANNABLE);



    }
}
