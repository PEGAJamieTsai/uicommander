package com.slamtec.android.uicommander.views.controls;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.slamtec.android.uicommander.R;
import com.slamtec.android.uicommander.agent.OperateAction;

/**
 * Created by Alan on 9/30/15.
 */
public class RPMoveControlPanel extends RelativeLayout implements View.OnTouchListener {
    private final static String TAG = "RPMoveControlPanel";

    private IOnButtonClickListener clickListener;

    private Integer touchedButtonId = null;

    private boolean touching = false;

    private Handler handler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (touchedButtonId == null) {
                return;
            }

            synchronized (RPMoveControlPanel.this) {
                if (!touching) {
                    return;
                }
            }

            switch (touchedButtonId) {
                case R.id.button_move_forward:
                    clickListener.onButtonForwardClicked();
                    break;
                case R.id.button_move_backward:
                    clickListener.onButtonBackwardClicked();
                    break;
                case R.id.button_turn_left:
                    clickListener.onButtonTurnLeftClicked();
                    break;
                case R.id.button_turn_right:
                    clickListener.onButtonTurnRightClicked();
                    break;
                default:
                    return;
            }

            synchronized (RPMoveControlPanel.this) {
                if (touching) {
                    handler.postDelayed(this, 500);
                }
            }
        }
    };

    private final Rect touchViewRect = new Rect();

    public RPMoveControlPanel(Context context) {
        super(context);
        initView();
    }

    public RPMoveControlPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public RPMoveControlPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View rootView = LayoutInflater.from(getContext()).inflate(
                R.layout.control_rp_move_control_panel, this, true);

        ImageView buttonForward = (ImageView) rootView.findViewById(R.id.button_move_forward);
        ImageView buttonLeft = (ImageView) rootView.findViewById(R.id.button_turn_left);
        ImageView buttonRight = (ImageView) rootView.findViewById(R.id.button_turn_right);
        ImageView buttonBackward = (ImageView) rootView.findViewById(R.id.button_move_backward);
        ImageView buttonHide = (ImageView) rootView.findViewById(R.id.button_hide);

        rootView.setOnTouchListener(this);
        buttonForward.setOnTouchListener(this);
        buttonLeft.setOnTouchListener(this);
        buttonRight.setOnTouchListener(this);
        buttonBackward.setOnTouchListener(this);
        buttonHide.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onButtonHideClicked();
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        touchedButtonId = v.getId();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touchViewRect.set(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                keepMoveAction();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!touchViewRect.contains((int)event.getX() + v.getLeft(),
                        (int)event.getY() + v.getTop())) {
                    stopMoveAction();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                stopMoveAction();
                break;
        }
        return true;
    }

    public void setClickListener(IOnButtonClickListener listener) {
        clickListener = listener;
    }

    private void keepMoveAction() {
        OperateAction.on();
        synchronized (this) {
            touching = true;
        }
        handler.post(runnable);
    }

    private void stopMoveAction() {
        OperateAction.off();
        synchronized (this) {
            touching = false;
            touchedButtonId = null;
            handler.removeCallbacks(runnable);
        }
    }

    public interface IOnButtonClickListener {
        void onButtonForwardClicked();
        void onButtonBackwardClicked();
        void onButtonTurnLeftClicked();
        void onButtonTurnRightClicked();
        void onButtonHideClicked();
    }
}
