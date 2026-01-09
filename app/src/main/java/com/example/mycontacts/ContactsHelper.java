// ContactsHelper.java
package com.example.mycontacts;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.content.ContentProviderOperation;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ContactsHelper {
    private static final String TAG = "ContactsHelper";

    /**
     * 获取所有联系人
     */
    public static List<Contact> getAllContacts(Context context) {
        List<Contact> contacts = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();

        // 查询联系人
        Cursor cursor = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, // 查询所有列
                null, // 没有WHERE条件
                null, // 没有WHERE参数
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC" // 按名字排序
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // 获取联系人ID
                String contactId = cursor.getString(cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID));

                // 获取联系人姓名
                String name = cursor.getString(cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                // 获取联系人电话号码
                String phoneNumber = cursor.getString(cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER));

                // 跳过没有号码的联系人
                if (TextUtils.isEmpty(phoneNumber)) {
                    continue;
                }

                // 创建联系人对象
                Contact contact = new Contact(contactId, name, phoneNumber);

                // 获取头像（可选）
                String photoId = cursor.getString(cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.PHOTO_ID));
                if (photoId != null) {
                    contact.setPhoto(getContactPhoto(context, contactId));
                }

                contacts.add(contact);

            } while (cursor.moveToNext());

            cursor.close();
        }

        return contacts;
    }

    /**
     * 获取联系人头像
     */
    private static byte[] getContactPhoto(Context context, String contactId) {
        try {
            Uri contactUri = ContentUris.withAppendedId(
                    ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactId));

            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(
                    context.getContentResolver(), contactUri);

            if (input != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                // 将Bitmap转换为byte数组（这里简化处理，实际项目中需要压缩）
                // 注意：这里返回null，实际使用时需要实现Bitmap转byte[]
                input.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "获取头像失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 添加新联系人
     */
    public static boolean addContact(Context context, Contact contact) {
        try {
            ArrayList<ContentValues> data = new ArrayList<>();

            // 添加姓名
            ContentValues name = new ContentValues();
            name.put(ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
            name.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                    contact.getName());
            data.add(name);

            // 添加电话号码
            if (!TextUtils.isEmpty(contact.getPhoneNumber())) {
                ContentValues phone = new ContentValues();
                phone.put(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                phone.put(ContactsContract.CommonDataKinds.Phone.NUMBER,
                        contact.getPhoneNumber());
                phone.put(ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
                data.add(phone);
            }

            // 添加邮箱（如果有）
            if (!TextUtils.isEmpty(contact.getEmail())) {
                ContentValues email = new ContentValues();
                email.put(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
                email.put(ContactsContract.CommonDataKinds.Email.DATA,
                        contact.getEmail());
                email.put(ContactsContract.CommonDataKinds.Email.TYPE,
                        ContactsContract.CommonDataKinds.Email.TYPE_WORK);
                data.add(email);
            }

            // 批量插入数据
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY,
                    createBatchOperations(data));

            return true;
        } catch (Exception e) {
            Log.e(TAG, "添加联系人失败: " + e.getMessage());
            return false;
        }
    }

    private static ArrayList<ContentProviderOperation> createBatchOperations(
            ArrayList<ContentValues> data) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        int rawContactInsertIndex = ops.size();
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        for (ContentValues values : data) {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
                            rawContactInsertIndex)
                    .withValues(values)
                    .build());
        }

        return ops;
    }

    /**
     * 删除联系人
     */
    public static boolean deleteContact(Context context, String contactId) {
        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactId);
            int rowsDeleted = context.getContentResolver().delete(uri, null, null);
            return rowsDeleted > 0;
        } catch (Exception e) {
            Log.e(TAG, "删除联系人失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 更新联系人
     */
    public static boolean updateContact(Context context, Contact contact) {
        try {
            ContentValues values = new ContentValues();
            values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                    contact.getName());

            // 更新电话号码
            if (!TextUtils.isEmpty(contact.getPhoneNumber())) {
                values.put(ContactsContract.CommonDataKinds.Phone.NUMBER,
                        contact.getPhoneNumber());
            }

            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI,
                    contact.getId());
            int rowsUpdated = context.getContentResolver().update(uri, values, null, null);

            return rowsUpdated > 0;
        } catch (Exception e) {
            Log.e(TAG, "更新联系人失败: " + e.getMessage());
            return false;
        }
    }
}