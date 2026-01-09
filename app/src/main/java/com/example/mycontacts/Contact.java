package com.example.mycontacts;

import android.os.Parcel;
import android.os.Parcelable;


public class Contact implements Parcelable {
    private String id;
    private String name;
    private String phoneNumber;
    private String email;
    private byte[] photo; // 头像数据

    // 构造函数
    public Contact(String id, String name, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }
    public Contact copy() {
        Contact copy = new Contact(null, this.name, this.phoneNumber);
        copy.setEmail(this.email);
        copy.setPhoto(this.photo);
        return copy;
    }
    public Contact copyWithSuffix(String suffix) {
        Contact copy = new Contact(null, this.name + suffix, this.phoneNumber);
        copy.setEmail(this.email);
        copy.setPhoto(this.photo);
        return copy;
    }

    protected Contact(Parcel in) {
        id = in.readString();
        name = in.readString();
        phoneNumber = in.readString();
        email = in.readString();
        photo = in.createByteArray();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(phoneNumber);
        dest.writeString(email);
        dest.writeByteArray(photo);
    }

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "Contact{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    // Getter和Setter方法
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public byte[] getPhoto() { return photo; }
    public void setPhoto(byte[] photo) { this.photo = photo; }




}