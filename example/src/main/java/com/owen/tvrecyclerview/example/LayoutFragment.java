/*
 * Copyright (C) 2014 Lucas Rocha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.owen.tvrecyclerview.example;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.owen.tvrecyclerview.example.bridge.MainUpView;
import com.owen.tvrecyclerview.example.bridge.RecyclerViewBridge;
import com.owen.tvrecyclerview.widget.TvRecyclerView;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_DRAGGING;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_SETTLING;

public class LayoutFragment extends Fragment {
    private static final String LOGTAG = LayoutFragment.class.getSimpleName();
    
    private static final String ARG_LAYOUT_ID = "layout_id";

    private TvRecyclerView mRecyclerView;
    private TextView mPositionText;
    private TextView mCountText;
    private TextView mStateText;
    private Toast mToast;

    private int mLayoutId;
    
    private LayoutAdapter mLayoutAdapter;

    MainUpView mainUpView1;
    RecyclerViewBridge mRecyclerViewBridge;
    View oldView;
    View newView;

    public static LayoutFragment newInstance(int layoutId) {
        LayoutFragment fragment = new LayoutFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_ID, layoutId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLayoutId = getArguments().getInt(ARG_LAYOUT_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(mLayoutId, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Activity activity = getActivity();

        mToast = Toast.makeText(activity, "", Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        
        View btnView = view.findViewById(R.id.btn1);
        if(null != btnView) {
            btnView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRecyclerView.smoothScrollToPosition(15);
                }
            });
        }

        mRecyclerView = (TvRecyclerView) view.findViewById(R.id.list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setInterceptKeyEvent(true);

        // 移动框
        mainUpView1 = (MainUpView) view.findViewById(R.id.mainUpView1);
        mainUpView1.setEffectBridge(new RecyclerViewBridge());
        mRecyclerViewBridge = (RecyclerViewBridge) mainUpView1.getEffectBridge();
        mRecyclerViewBridge.setUpRectResource(R.drawable.test_rectangle);
        mainUpView1.setDrawUpRectPadding(6);

        mPositionText = (TextView) view.getRootView().findViewById(R.id.position);
        mCountText = (TextView) view.getRootView().findViewById(R.id.count);

        mStateText = (TextView) view.getRootView().findViewById(R.id.state);
        updateState(SCROLL_STATE_IDLE);

        mRecyclerView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
                mRecyclerViewBridge.setUnFocusView(itemView);
//                itemView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(500).start();
//                Log.i(LOGTAG, "onItemPreSelected...1");
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                newView = itemView;
                mRecyclerViewBridge.setFocusView(itemView, 1.1f);
//                itemView.animate().scaleX(1.2f).scaleY(1.2f).setDuration(500).start();
//                Log.i(LOGTAG, "onItemSelected...2");
            }

            @Override
            public void onReviseFocusFollow(TvRecyclerView parent, View itemView, int position) {
                // 此处为了特殊情况时校正移动框
                mRecyclerViewBridge.setFocusView(itemView, 1.1f);
//                Log.i(LOGTAG, "onReviseFocusFollow...3");
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
//                mToast.setText("onItemClick::"+position);
//                mToast.show();
                mLayoutAdapter.removeItem(position);
            }
        });

        mRecyclerView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mRecyclerViewBridge.setVisibleWidget(!hasFocus);
            }
        });
        
        mRecyclerView.setOnInBorderKeyEventListener(new TvRecyclerView.OnInBorderKeyEventListener() {
            @Override
            public boolean onInBorderKeyEvent(int direction, int keyCode, KeyEvent event) {
                Log.i("zzzz", "onInBorderKeyEvent: ");
                return false;
            }
        });
        
        mRecyclerView.setOnLoadMoreListener(new TvRecyclerView.OnLoadMoreListener() {
            @Override
            public boolean onLoadMore() {
                Log.i("@@@@", "onLoadMore: ");
                mRecyclerView.setLoadingMore(true); //正在加载数据
                mLayoutAdapter.appendDatas(); //加载数据
                mRecyclerView.setLoadingMore(false); //加载数据完毕
                return true; //是否还有更多数据
            }
        });
        
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
                updateState(scrollState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                mPositionText.setText("First: " + mRecyclerView.getFirstVisiblePosition());
                mCountText.setText("Count: " + mRecyclerView.getChildCount());
            }
            
        });

        if(mLayoutId == R.layout.layout_grid2) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
            gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(gridLayoutManager);
        } else {

//      final Drawable divider = getResources().getDrawable(R.drawable.divider);
//      mRecyclerView.addItemDecoration(new DividerItemDecoration(divider));
//      mRecyclerView.addItemDecoration(new SpacingItemDecoration(20, 20));
            // 通过Margins来设置布局的横纵间距(与addItemDecoration()方法可二选一)
            // 推荐使用此方法
            mRecyclerView.setSpacingWithMargins(18, 18);
        }
        
        
        // 设置选中的Item距离开始或结束的偏移量（与setSelectedItemAtCentered()方法二选一）
//        mRecyclerView.setSelectedItemOffset(320, 220);
        // 设置选中的Item居中（与setSelectedItemOffset()方法二选一）
//        mRecyclerView.setSelectedItemAtCentered(true);


        mLayoutAdapter = new LayoutAdapter(activity, mRecyclerView, mLayoutId);
        mRecyclerView.setAdapter(mLayoutAdapter);

    }

    private void updateState(int scrollState) {
        String stateName = "Undefined";
        switch(scrollState) {
            case SCROLL_STATE_IDLE:
                stateName = "Idle";
                break;

            case SCROLL_STATE_DRAGGING:
                stateName = "Dragging";
                break;

            case SCROLL_STATE_SETTLING:
                stateName = "Flinging";
                break;
        }
        
        mStateText.setText(stateName);
    }

    public int getLayoutId() {
        return getArguments().getInt(ARG_LAYOUT_ID);
    }
    
}
