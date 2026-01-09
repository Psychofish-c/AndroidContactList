// MainActivity.java
package com.example.mycontacts;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private RecyclerView rvContacts;
    private EditText etSearch;
    private Button btnClearSearch, btnAdd;
    private TextView tvEmpty;
    private ContactAdapter contactAdapter;
    private List<Contact> contactList = new ArrayList<>();
    private List<Contact> filteredList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        setupSearch();
        checkPermissions();
    }

    private void initView() {
        rvContacts = findViewById(R.id.rv_contacts);
        etSearch = findViewById(R.id.et_search);
        btnClearSearch = findViewById(R.id.btn_clear_search);
        btnAdd = findViewById(R.id.btn_add);
        tvEmpty = findViewById(R.id.tv_empty);

        // 设置RecyclerView
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        contactAdapter = new ContactAdapter(filteredList, new ContactAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Contact contact) {
                // 点击联系人，跳转到详情页
                Intent intent = new Intent(MainActivity.this, ContactDetailActivity.class);
                intent.putExtra("contact", contact);
                startActivityForResult(intent, REQUEST_CONTACT_DETAIL);
            }

            @Override
            public void onItemLongClick(Contact contact) {
                // 长按删除联系人
                if (ContactsHelper.deleteContact(MainActivity.this, contact.getId())) {
                    Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                    loadContacts();
                } else {
                    Toast.makeText(MainActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
        rvContacts.setAdapter(contactAdapter);

        // 添加联系人按钮点击事件
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddContactActivity.class);
            startActivityForResult(intent, REQUEST_ADD_CONTACT);
        });

        // 清空搜索按钮
        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            filterContacts("");
        });
    }

    private void setupSearch() {
        // 搜索框文本变化监听
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContacts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 根据是否有输入显示/隐藏清空按钮
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void filterContacts(String query) {
        filteredList.clear();

        if (query == null || query.trim().isEmpty()) {
            // 如果没有搜索词，显示所有联系人
            filteredList.addAll(contactList);
        } else {
            // 过滤联系人
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Contact contact : contactList) {
                if (contact.getName() != null && contact.getName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(contact);
                }
            }
        }

        // 更新适配器
        contactAdapter.notifyDataSetChanged();

        // 显示/隐藏空状态提示
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredList.isEmpty()) {
            if (etSearch.getText().toString().trim().isEmpty()) {
                tvEmpty.setText("暂无联系人\n点击下方按钮添加");
            } else {
                tvEmpty.setText("未找到匹配的联系人");
            }
            tvEmpty.setVisibility(View.VISIBLE);
            rvContacts.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvContacts.setVisibility(View.VISIBLE);
        }
    }

    // 添加请求码常量
    private static final int REQUEST_ADD_CONTACT = 1001;
    private static final int REQUEST_CONTACT_DETAIL = 1002;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ADD_CONTACT || requestCode == REQUEST_CONTACT_DETAIL) {
                // 重新加载联系人列表
                loadContacts();
            }
        }
    }

    private void checkPermissions() {
        String[] permissions = {
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.SEND_SMS
        };

        List<String> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }

        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionList.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        } else {
            loadContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                loadContacts();
            } else {
                Toast.makeText(this, "需要所有权限才能使用通讯录功能",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadContacts() {
        contactList.clear();
        contactList.addAll(ContactsHelper.getAllContacts(this));

        // 更新过滤列表
        filterContacts(etSearch.getText().toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            loadContacts();
        }
    }
}