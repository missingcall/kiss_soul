package com.kissspace.android.widget.bottombar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kissspace.android.R;


public class BottomBarTab extends RelativeLayout {
    private ImageView mIvIcon;
    private TextView mTextView;
    private Context mContext;
    private int mTabPosition = -1;
    private BottomTabBean mBottomTabBean;

    public BottomBarTab(Context context, BottomTabBean bottomTabBean) {
        super(context);
        init(context,bottomTabBean);
    }

    public BottomBarTab(Context context, AttributeSet attrs, BottomTabBean bottomTabBean) {
        super(context, attrs, 0);
        init(context, bottomTabBean);
    }

    public BottomBarTab(Context context, AttributeSet attrs, int defStyleAttr, BottomTabBean bottomTabBean) {
        super(context, attrs, defStyleAttr);
        init(context, bottomTabBean);
    }

    private void init(Context context, BottomTabBean bottomTabBean) {
        mContext = context;
        mBottomTabBean = bottomTabBean;
        View.inflate(context, R.layout.custom_layout_bottom_item, this);
//        mIvIcon = findViewById(R.id.image);
//        mTextView = findViewById(R.id.text);
//        if (bottomTabBean.isSelected()) {
//            mIvIcon.setImageResource(bottomTabBean.getSelectedIconRes());
//        } else {
//            mIvIcon.setImageResource(bottomTabBean.getNormalIconRes());
//        }
//        mTextView.setText(bottomTabBean.getText());
//        LogUtils.e("时空的吗"+this);
//        LayoutParams lp = (LayoutParams) getLayoutParams();
//        lp.setMargins(0, SizeUtils.dp2px(8f),0,0);
//        setLayoutParams(lp);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
//        mBottomTabBean.setSelected(selected);
//        if (selected) {
//            mIvIcon.setImageResource(mBottomTabBean.getSelectedIconRes());
//            mTextView.setTextColor(ContextCompat.getColor(mContext, com.kissspace.module_common.R.color.color_6C74FB));
////            MarginLayoutParams lp = (MarginLayoutParams)getLayoutParams();
////            lp.setMargins(0, SizeUtils.dp2px(-12f),0,0);
////            setLayoutParams(lp);
//        } else {
//            mIvIcon.setImageResource(mBottomTabBean.getNormalIconRes());
//            mTextView.setTextColor(ContextCompat.getColor(mContext, com.kissspace.module_common.R.color.color_A8A8B3));
////            MarginLayoutParams lp = (MarginLayoutParams)getLayoutParams();
////            lp.setMargins(0, SizeUtils.dp2px(8f),0,0);
////            setLayoutParams(lp);
//        }
    }


    public void setTabPosition(int position) {
        mTabPosition = position;
        if (position == 0) {
            setSelected(true);
        }
    }

    public int getTabPosition() {
        return mTabPosition;
    }


    @SuppressLint("CheckResult")
    protected void updateTagText(String text) {

    }

    public ImageView getIcon() {
        return mIvIcon;
    }
}
