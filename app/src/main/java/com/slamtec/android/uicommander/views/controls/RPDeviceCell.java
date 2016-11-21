package com.slamtec.android.uicommander.views.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.slamtec.android.uicommander.R;

/**
 * Created by Alan on 1/14/16.
 */
public class RPDeviceCell extends RelativeLayout {

    private TextView textName;
    private TextView textModel;
    private TextView textState;

    public RPDeviceCell(Context context) {
        super(context);
        init();
    }

    public RPDeviceCell(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RPDeviceCell(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View root = LayoutInflater.from(getContext()).inflate(R.layout.control_rp_device_cell, this, true);

        textName = (TextView) root.findViewById(R.id.name);
        textModel = (TextView) root.findViewById(R.id.model);
        textState = (TextView) root.findViewById(R.id.state);
    }

    public void setDeviceName(String name) {
        textName.setText(name);
    }

    public void setDeviceModel(String model) {
        textModel.setText(model);
    }

    public void setDeviceState(String state) {
        textState.setText(state);
    }
}
