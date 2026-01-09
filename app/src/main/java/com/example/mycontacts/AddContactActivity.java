// 修改 AddContactActivity.java
package com.example.mycontacts;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddContactActivity extends AppCompatActivity {

    private EditText etName, etPhone, etEmail;
    private Button btnSave, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        initView();
        setupListeners();
    }

    private void initView() {
        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);
        btnSave = findViewById(R.id.btn_save);
        btnBack = findViewById(R.id.btn_back);
    }

    private void setupListeners() {
        // 返回按钮
        btnBack.setOnClickListener(v -> {
            finish();
        });

        // 保存按钮
        btnSave.setOnClickListener(v -> {
            if (validateInput()) {
                saveContact();
            }
        });
    }

    private boolean validateInput() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("姓名不能为空");
            etName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("电话号码不能为空");
            etPhone.requestFocus();
            return false;
        }

        // 简单的电话号码验证
        if (phone.length() < 8) {
            etPhone.setError("电话号码格式不正确");
            etPhone.requestFocus();
            return false;
        }

        return true;
    }

    private void saveContact() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        Contact contact = new Contact(null, name, phone);
        contact.setEmail(email.isEmpty() ? null : email);

        if (ContactsHelper.addContact(this, contact)) {
            Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "添加失败", Toast.LENGTH_SHORT).show();
        }
    }
}