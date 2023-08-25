package com.kissspace.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kissspace.android.R;
import com.kissspace.common.model.HomeUserListBean;
import com.kissspace.common.widget.planetview.adapter.PlanetAdapter;
import com.kissspace.util.ImageViewKt;

import java.util.ArrayList;
import java.util.List;

public class ViewTagsAdapter extends PlanetAdapter {

    public List<HomeUserListBean> mList = new ArrayList<HomeUserListBean>();
    public ViewTagsAdapter(List<HomeUserListBean> list){
        mList.addAll(list);
    }
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public View getView(Context context, int position, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.tag_item_view, parent, false);
        ImageView imageView = view.findViewById(R.id.iv);
        HomeUserListBean homeUserListBean = mList.get(position);
        ImageViewKt.loadImageCircle(imageView,homeUserListBean.getProfilePath(),null);
        return view;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public int getPopularity(int position) {
        return position % 10;
    }

    @Override
    public void onThemeColorChanged(View view, int themeColor) {

    }
}
