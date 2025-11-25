package com.example.intenllignetbabyshakerjava;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.intenllignetbabyshakerjava.databinding.ActivitySetBinding;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;

public class SetActivity extends AppCompatActivity {
    private ActivitySetBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
    }

    private void initView() {
        //设置共同沉浸式样式
        ImmersionBar.with(this).hideBar(BarHide.FLAG_HIDE_STATUS_BAR).init();
        binding.backBtn.setOnClickListener(view -> {
            finish();
        });
    }
}