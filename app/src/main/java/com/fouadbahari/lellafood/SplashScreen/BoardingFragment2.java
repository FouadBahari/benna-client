package com.fouadbahari.lellafood.SplashScreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fouadbahari.lellafood.MainActivity;
import com.fouadbahari.lellafood.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BoardingFragment2 extends Fragment {


    @BindView(R.id.btn_skip)
    TextView btn_skip;

    @OnClick(R.id.btn_skip)
    void onSkipClick(){
        startActivity(new Intent(getContext(), MainActivity.class));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_boarding2,container,false);
        ButterKnife.bind(this, root);

        return root;
    }
}
