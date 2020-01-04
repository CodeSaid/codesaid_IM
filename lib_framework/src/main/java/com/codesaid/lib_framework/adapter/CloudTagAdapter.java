package com.codesaid.lib_framework.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codesaid.lib_framework.R;
import com.moxun.tagcloudlib.view.TagsAdapter;

import java.util.List;

/**
 * Created By codesaid
 * On :2020-01-03
 * Package Name: com.codesaid.lib_framework.adapter
 * desc : 3D 球体 adapter
 */
public class CloudTagAdapter extends TagsAdapter {

    private Context mContext;
    private List<String> mList;
    private LayoutInflater mInflater;

    public CloudTagAdapter(Context context, List<String> list) {
        mContext = context;
        mList = list;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public View getView(Context context, int position, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.layout_star_view_item, null);
        //初始化控件
        ImageView iv_star_icon = view.findViewById(R.id.iv_star_icon);
        TextView tv_star_name = view.findViewById(R.id.tv_star_name);

        tv_star_name.setText(mList.get(position));
        switch (position % 10) {
            case 0:
                iv_star_icon.setImageResource(R.drawable.icon_message);
                break;
            default:
                iv_star_icon.setImageResource(R.drawable.icon_message);
                break;
        }

        return view;
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public int getPopularity(int position) {
        return 7;
    }

    @Override
    public void onThemeColorChanged(View view, int themeColor) {

    }

}
