package com.hashim.mapswithgeofencing.ViewHolders;

import androidx.recyclerview.widget.RecyclerView;

import com.hashim.mapswithgeofencing.databinding.ItemButtonsBinding;

public class ViewAllVH extends RecyclerView.ViewHolder {

    public ItemButtonsBinding hItemButtonsBinding;


    public ViewAllVH(ItemButtonsBinding itemButtonsBinding) {
        super(itemButtonsBinding.getRoot());
        hItemButtonsBinding = itemButtonsBinding;
    }
}
