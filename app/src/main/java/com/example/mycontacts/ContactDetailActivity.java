// 修改 ContactDetailActivity.java
package com.example.mycontacts;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ContactDetailActivity extends AppCompatActivity {

    private Contact contact;
    private TextView tvName, tvPhone, tvEmail;
    private Button btnCall, btnSms, btnSaveAsNew, btnEdit, btnDelete, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);

        // 获取 Intent 中的数据
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("contact")) {
            contact = (Contact) intent.getParcelableExtra("contact");
        }

        if (contact == null) {
            Toast.makeText(this, "无法获取联系人信息", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initView();
        setupListeners();
    }

    private void initView() {
        tvName = findViewById(R.id.tv_detail_name);
        tvPhone = findViewById(R.id.tv_detail_phone);
        tvEmail = findViewById(R.id.tv_detail_email);

        btnCall = findViewById(R.id.btn_call);
        btnSms = findViewById(R.id.btn_sms);
        btnSaveAsNew = findViewById(R.id.btn_save_as_new);
        btnEdit = findViewById(R.id.btn_edit);
        btnDelete = findViewById(R.id.btn_delete);
        btnBack = findViewById(R.id.btn_back);

        // 设置联系人信息
        if (contact != null) {
            tvName.setText(contact.getName() != null ? contact.getName() : "未知");
            tvPhone.setText(contact.getPhoneNumber() != null ? contact.getPhoneNumber() : "无");
            tvEmail.setText(contact.getEmail() != null ? contact.getEmail() : "无");
        }
    }

    private void setupListeners() {
        if (contact == null) return;

        // 返回按钮
        btnBack.setOnClickListener(v -> {
            finish();
        });

        // 打电话
        btnCall.setOnClickListener(v -> {
            String phoneNumber = contact.getPhoneNumber();
            if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:" + phoneNumber));
                startActivity(intent);
            } else {
                Toast.makeText(this, "电话号码无效", Toast.LENGTH_SHORT).show();
            }
        });

        // 发短信
        btnSms.setOnClickListener(v -> {
            String phoneNumber = contact.getPhoneNumber();
            if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_SENDTO,
                        Uri.parse("smsto:" + phoneNumber));
                startActivity(intent);
            } else {
                Toast.makeText(this, "电话号码无效", Toast.LENGTH_SHORT).show();
            }
        });

        // 保存为新联系人
        btnSaveAsNew.setOnClickListener(v -> {
            saveAsNewContact();
        });

        // 编辑联系人
        btnEdit.setOnClickListener(v -> {
            if (contact != null) {
                Intent editIntent = new Intent(ContactDetailActivity.this, EditContactActivity.class);
                editIntent.putExtra("contact", contact);
                startActivityForResult(editIntent, REQUEST_EDIT_CONTACT);
            }
        });

        // 删除联系人
        btnDelete.setOnClickListener(v -> {
            if (contact.getId() != null && !contact.getId().isEmpty()) {
                if (ContactsHelper.deleteContact(this, contact.getId())) {
                    Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "联系人ID无效", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 保存为新联系人的方法
    private void saveAsNewContact() {
        // 使用复制方法
        Contact newContact = contact.copyWithSuffix(" (副本)");

        // 保存到系统通讯录
        if (ContactsHelper.addContact(this, newContact)) {
            Toast.makeText(this, "已保存为新联系人", Toast.LENGTH_SHORT).show();

            // 可以选择询问用户是否要查看新联系人
            // showViewNewContactDialog(newContact);
        } else {
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    // 请求码
    private static final int REQUEST_EDIT_CONTACT = 1001;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_EDIT_CONTACT && resultCode == RESULT_OK) {
            // 重新加载数据
            if (data != null) {
                Contact updatedContact = data.getParcelableExtra("updated_contact");
                if (updatedContact != null) {
                    contact = updatedContact;
                    initView(); // 重新初始化视图

                    // 通知主页面刷新
                    setResult(RESULT_OK);
                }
            }
        }
    }
}