package com.bluelinelabs.conductor.support.util;

import android.util.SparseArray;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.support.RouterPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class FakePager {

    private ViewGroup container;
    private int offscreenPageLimit;
    private final SparseArray<Object> pages = new SparseArray<>();

    private RouterPagerAdapter adapter;

    public FakePager(ViewGroup container) {
        this.container = container;
    }

    public void setAdapter(RouterPagerAdapter adapter) {
        this.adapter = adapter;
    }

    public void pageTo(int page) {
        int firstPage = Math.max(0, page - offscreenPageLimit);
        int lastPage = Math.min(adapter.getCount() - 1, page + offscreenPageLimit);

        List<Integer> pagesI = new ArrayList<>();
        for (int i = 0; i < pages.size(); i++) {
            pagesI.add(pages.keyAt(i));
        }

        for (int i = pages.size() - 1; i >= 0; i--) {
            int key = pages.keyAt(i);

            if (key < firstPage || key > lastPage) {
                adapter.destroyItem(container, key, pages.get(key));
                pages.remove(key);
            }
        }

        for (int key = firstPage; key <= lastPage; key++) {
            if (pages.get(key) == null) {
                pages.put(key, adapter.instantiateItem(container, key));
            }
        }

        adapter.setPrimaryItem(container, page, pages.get(page));
    }

    public int getOffscreenPageLimit() {
        return offscreenPageLimit;
    }

    public void setOffscreenPageLimit(int offscreenPageLimit) {
        this.offscreenPageLimit = offscreenPageLimit;
    }
}
