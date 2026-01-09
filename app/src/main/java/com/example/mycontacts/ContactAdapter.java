// ContactAdapter.java
package com.example.mycontacts;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private final List<Contact> contactList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Contact contact);
        void onItemLongClick(Contact contact);
    }

    public ContactAdapter(List<Contact> contactList, OnItemClickListener listener) {
        this.contactList = contactList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = contactList.get(position);

        holder.tvName.setText(contact.getName());
        holder.tvPhone.setText(contact.getPhoneNumber());

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            Log.d("ADAPTER_DEBUG", "Item clicked at position: " + position);
            if (listener != null) {
                listener.onItemClick(contact);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            Log.d("ADAPTER_DEBUG", "Item long clicked at position: " + position);
            if (listener != null) {
                listener.onItemLongClick(contact);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvPhone;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPhone = itemView.findViewById(R.id.tv_phone);
        }
    }
}
