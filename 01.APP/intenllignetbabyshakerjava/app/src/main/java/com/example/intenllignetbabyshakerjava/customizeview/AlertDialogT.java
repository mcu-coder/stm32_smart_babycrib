package com.example.intenllignetbabyshakerjava.customizeview;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;

import com.example.intenllignetbabyshakerjava.databinding.UpdateAlertdialogViewBinding;
import com.example.intenllignetbabyshakerjava.utils.HandlerAction;

public class AlertDialogT implements HandlerAction {
    private Context context;
    private AlertDialog.Builder builder;
    private AlertDialog alert;
    private UpdateAlertdialogViewBinding binding;
    public AlertDialogT(Context context) {
        this.context = context;
        builder = new AlertDialog.Builder(context);

    }
    /**
     *  显示
     */
    public void showDialog(){
        binding = UpdateAlertdialogViewBinding.inflate(LayoutInflater.from(context));
        builder.setTitle("Dialog").setView(binding.getRoot());
        alert = builder.create();

        alert.show();
    }

    /***
     * 关闭
     */
    public void dismiss(){
        alert.dismiss();
    }
}
