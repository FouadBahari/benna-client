package com.fouadbahari.lellafood.View.ui.activategps;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.fouadbahari.lellafood.EventBus.ActivateGPSEvent;
import com.fouadbahari.lellafood.EventBus.CategoryClick;
import com.fouadbahari.lellafood.R;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class GPSFragment extends Fragment {

    private GViewModel mViewModel;
    private Unbinder unbinder;

    @BindView(R.id.btn_gps)
    Button btn_gps;

    @OnClick(R.id.btn_gps)
    void onGPSClick(){
        EventBus.getDefault().postSticky(new ActivateGPSEvent());
    }



    public static GPSFragment newInstance() {
        return new GPSFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root  =  inflater.inflate(R.layout.g_fragment, container, false);
        unbinder= ButterKnife.bind(this,root);



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


        EventBus.getDefault().postSticky(new ActivateGPSEvent());
        return root;
    }


}