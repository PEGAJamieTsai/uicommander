package com.slamtec.android.uicommander.views.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.slamtec.android.uicommander.R;

/**
 * Created by Alan on 9/29/15.
 */
public class RPControlBar extends FrameLayout {

    private IOnButtonClickListener clickListener;

    public RPControlBar(Context context) {
        super(context);
        initView();
    }

    public RPControlBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public RPControlBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.control_rp_control_bar, this, true);

        ImageView buttonAddWall = (ImageView) rootView.findViewById(R.id.button_add_wall);
        ImageView buttonClearWall = (ImageView) rootView.findViewById(R.id.button_clear_map);
        ImageView buttonSweep = (ImageView) rootView.findViewById(R.id.button_sweep);
        ImageView buttonHome = (ImageView) rootView.findViewById(R.id.button_home);
        ImageView buttonClearTrack = (ImageView) rootView.findViewById(R.id.button_clear_track);
        ImageView buttonQuitToConnect = (ImageView) rootView.findViewById(R.id.button_quit_to_connect);
        ImageView buttonController = (ImageView) rootView.findViewById(R.id.button_controller);
        ImageView buttonSweepSpot = (ImageView) rootView.findViewById(R.id.button_sweep_spot);

        rootView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // intentionally empty
            }
        });
        buttonAddWall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onButtonEditWallClicked();
            }
        });
        buttonClearWall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onButtonClearWallClicked();
            }
        });
        buttonSweep.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onButtonSweepClicked();
            }
        });
        buttonHome.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onButtonHomeClicked();
            }
        });
        buttonClearTrack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onButtonClearTrackClicked();
            }
        });
        buttonQuitToConnect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onButtonQuitToConnectClicked();
            }
        });
        buttonController.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onButtonControllerClicked();
            }
        });
        buttonSweepSpot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onButtonSweepSpotClicked();
            }
        });
    }

    public void setClickListener(IOnButtonClickListener listener) {
        clickListener = listener;
    }

    public interface IOnButtonClickListener {
        void onButtonEditWallClicked();
        void onButtonClearWallClicked();
        void onButtonSweepClicked();
        void onButtonHomeClicked();
        void onButtonClearTrackClicked();
        void onButtonQuitToConnectClicked();
        void onButtonControllerClicked();
        void onButtonSweepSpotClicked();
    }
}
