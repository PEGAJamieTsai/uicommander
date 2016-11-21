package com.slamtec.android.uicommander.views.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.slamtec.android.uicommander.R;

/**
 * Created by Alan on 12/31/15.
 */
public class RPVirtualWallEditPanel extends RelativeLayout implements View.OnClickListener {

    private IOnVirtualWallPanelClickListener clickListener;

    public RPVirtualWallEditPanel(Context context) {
        super(context);
        init();
    }

    public RPVirtualWallEditPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RPVirtualWallEditPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View rootView = LayoutInflater.from(getContext()).inflate(
                R.layout.control_rp_virtual_wall_panel, this, true);

        ImageView buttonExitWallEdit = (ImageView) rootView.findViewById(R.id.button_exit_wall_edit);
        ImageView buttonAddWall = (ImageView) rootView.findViewById(R.id.button_add_wall);
        ImageView buttonRemoveWall = (ImageView) rootView.findViewById(R.id.button_remove_wall);
        ImageView buttonClearWalls = (ImageView) rootView.findViewById(R.id.button_clear_walls);

        rootView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // intentionally empty
            }
        });

        buttonExitWallEdit.setOnClickListener(this);
        buttonAddWall.setOnClickListener(this);
        buttonRemoveWall.setOnClickListener(this);
        buttonClearWalls.setOnClickListener(this);
    }

    public void setClickListener(IOnVirtualWallPanelClickListener listener) {
        clickListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (clickListener == null) {
            return;
        }

        switch (v.getId()) {
            case R.id.button_exit_wall_edit:
                clickListener.onButtonExitWallEditClicked();
                break;
            case R.id.button_add_wall:
                clickListener.onButtonAddWallClicked();
                break;
            case R.id.button_remove_wall:
                clickListener.onButtonRemoveWallClicked();
                break;
            case R.id.button_clear_walls:
                clickListener.onButtonClearWallsClicked();
                break;
            default:
                break;
        }
    }

    public interface IOnVirtualWallPanelClickListener {
        void onButtonExitWallEditClicked();
        void onButtonAddWallClicked();
        void onButtonRemoveWallClicked();
        void onButtonClearWallsClicked();
    }
}
