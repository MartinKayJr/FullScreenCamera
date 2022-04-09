package com.example.camerademo;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;



public class SweetDialog extends Dialog{
    private TextView tv_title;
    private TextView tv_content;
    private TextView btn_yes;
    private TextView btn_no;

    private String str_title;
    private String str_content;
    private String str_yes;
    private String str_no;
    private View.OnClickListener yesClick;
    private View.OnClickListener noClick;

    public SweetDialog(Context context) {
        super(context, R.style.theme_sweet_dialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sweet_dialog);
        setCanceledOnTouchOutside(false);

        initUI();
        initData();
        addEvent();
    }

    private void initUI(){
        tv_title = findViewById(R.id.dialog_title);
        tv_content = findViewById(R.id.dialog_content);
        btn_yes = findViewById(R.id.btn_yes);
        btn_no = findViewById(R.id.btn_no);
    }
    private void initData(){
        setTitle(str_title);
        setContent(str_content);
    }
    private void addEvent(){
        setPositiveButton(str_yes, yesClick);
        setNegativeutton(str_no, noClick);
    }

    // region API
    public SweetDialog setTitle(String info){
        if(info != null)
            str_title = info;
        if(tv_title != null)
            tv_title.setText(info);

        return this;
    }
    public SweetDialog setContent(String info){
        if(info != null)
            str_content = info;
        if(tv_content != null)
            tv_content.setText(info);
        return this;
    }
    public SweetDialog setPositiveButton(String name, View.OnClickListener callback){
        if(name != null)
            str_yes = name;
        if(callback != null)
            yesClick = callback;
        if(btn_yes != null){
            btn_yes.setText(name);
            btn_yes.setOnClickListener(callback);
        }

        return this;
    }

    public SweetDialog setNegativeutton(String name, View.OnClickListener callback){
        if(name!=null)
            str_no = name;
        if(callback != null)
            noClick = callback;

        if(btn_no != null){
            btn_no.setText(name);
            btn_no.setOnClickListener(callback);
        }

        return this;
    }
    // end region API
}
