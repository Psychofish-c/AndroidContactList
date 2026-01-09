// EditContactActivity.java
package com.example.mycontacts;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class EditContactActivity extends AppCompatActivity {

    private EditText etName, etPhone, etEmail;
    private Button btnSave, btnCancel,btnBack;
    private Contact contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);

        // 获取传递的联系人数据
        contact = (Contact) getIntent().getParcelableExtra("contact");
        if (contact == null) {
            Toast.makeText(this, "无法获取联系人信息", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initView();
        setupListeners();
        loadContactData();
    }

    private void initView() {
        etName = findViewById(R.id.et_edit_name);
        etPhone = findViewById(R.id.et_edit_phone);
        etEmail = findViewById(R.id.et_edit_email);
        btnSave = findViewById(R.id.btn_save_edit);
        btnCancel = findViewById(R.id.btn_cancel_edit);
        btnBack = findViewById(R.id.btn_back);
    }

    private void loadContactData() {
        if (contact != null) {
            etName.setText(contact.getName());
            etPhone.setText(contact.getPhoneNumber());
            etEmail.setText(contact.getEmail() != null ? contact.getEmail() : "");
        }
    }
    @Override
    public void onBackPressed() {
        if (hasUnsavedChanges()) {
            showUnsavedChangesDialog();
        } else {
            super.onBackPressed();
        }
    }

    private boolean hasUnsavedChanges() {
        String currentName = etName.getText().toString().trim();
        String currentPhone = etPhone.getText().toString().trim();
        String currentEmail = etEmail.getText().toString().trim();

        return !currentName.equals(contact.getName()) ||
                !currentPhone.equals(contact.getPhoneNumber()) ||
                !currentEmail.equals(contact.getEmail() != null ? contact.getEmail() : "");
    }

    private void showUnsavedChangesDialog() {
        new AlertDialog.Builder(this)
                .setTitle("未保存的更改")
                .setMessage("您有未保存的更改，确定要离开吗？")
                .setPositiveButton("离开", (dialog, which) -> finish())
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .show();
    }


    private void setupListeners() {
        // 保存修改
        btnSave.setOnClickListener(v -> {
            if (validateInput()) {
                saveContactChanges();
            }
        });
        btnBack.setOnClickListener(v -> {
            finish();
        });
        // 取消编辑
        btnCancel.setOnClickListener(v -> {
            finish();
        });
        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    etName.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 8) {
                    etPhone.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
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

    private void saveContactChanges() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        // 创建更新后的联系人对象
        Contact updatedContact = new Contact(contact.getId(), name, phone);
        updatedContact.setEmail(email.isEmpty() ? null : email);

        // 保存修改
        boolean success = saveContactToSystem(updatedContact);

        if (success) {
            // 返回结果给详情页
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updated_contact", updatedContact);
            setResult(RESULT_OK, resultIntent);

            Toast.makeText(this, "修改保存成功", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "保存失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    // 在 EditContactActivity.java 中替换 saveContactToSystem 方法

    private boolean saveContactToSystem(Contact updatedContact) {
        try {
            // 判断联系人信息是否有变化
            if (!hasChanges(updatedContact)) {
                Toast.makeText(this, "联系人信息未修改", Toast.LENGTH_SHORT).show();
                return true;
            }

            // 尝试使用 ContentResolver 直接更新
            boolean success = updateContactViaContentResolver(updatedContact);

            if (!success) {
                // 如果直接更新失败，使用删除后重新添加的方式
                Log.d("EDIT_CONTACT", "直接更新失败，尝试重新添加");
                if (contact.getId() != null) {
                    ContactsHelper.deleteContact(this, contact.getId());
                }
                success = ContactsHelper.addContact(this, updatedContact);
            }

            return success;

        } catch (Exception e) {
            Log.e("EDIT_CONTACT", "保存联系人失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查联系人信息是否有变化
     */
    private boolean hasChanges(Contact updatedContact) {
        if (!updatedContact.getName().equals(contact.getName())) return true;
        if (!updatedContact.getPhoneNumber().equals(contact.getPhoneNumber())) return true;

        String oldEmail = contact.getEmail() != null ? contact.getEmail() : "";
        String newEmail = updatedContact.getEmail() != null ? updatedContact.getEmail() : "";
        return !oldEmail.equals(newEmail);
    }

    /**
     * 通过 ContentResolver 直接更新联系人
     */
    private boolean updateContactViaContentResolver(Contact updatedContact) {
        try {
            // 这里实现直接更新联系人数据的逻辑
            // 注意：这是一个简化的实现，实际应用中可能需要更复杂的处理

            // 删除旧的并添加新的（对于系统通讯录，这是最简单的更新方式）
            if (contact.getId() != null) {
                ContactsHelper.deleteContact(this, contact.getId());
            }
            return ContactsHelper.addContact(this, updatedContact);

        } catch (Exception e) {
            Log.e("EDIT_CONTACT", "通过ContentResolver更新失败: " + e.getMessage());
            return false;
        }
    }
}