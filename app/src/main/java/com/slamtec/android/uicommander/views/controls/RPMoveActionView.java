package com.slamtec.android.uicommander.views.controls;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import com.slamtec.slamware.geometry.PointF;
import android.widget.ImageView;

import com.slamtec.android.uicommander.R;
import com.slamtec.android.uicommander.agent.RPSlamwareSdpAgent;
import com.slamtec.slamware.robot.Location;
import com.slamtec.slamware.action.Path;

import java.lang.ref.WeakReference;
import java.util.Vector;

/**
 * Created by Alan on 10/13/15.
 */
public class RPMoveActionView extends RPSlamwareBaseView {

    private Path remainingMilestones;
    private Path remainingPath;
    private Vector<ImageView> milestoneImages;
    private Bitmap milestoneImage;

    private Paint paint;
    private PointF coordinate;

    private int milestoneOffsetX;
    private int milestoneOffsetY;

    public RPMoveActionView(Context context, WeakReference<RPSlamwareSdpAgent> agent) {
        super(context, agent);

        milestoneImages = new Vector<>();

        milestoneImage = BitmapFactory.decodeResource(getResources(), R.mipmap.milestone);
        milestoneOffsetX = milestoneImage.getWidth() / 2;
        milestoneOffsetY = milestoneImage.getHeight() / 2;

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.GREEN);

        coordinate = new PointF();

        setBackgroundColor(Color.TRANSPARENT);

        setWillNotDraw(false);
    }

    public void updateRemainingMilestones(Path remainingMilestones, Path remainingPath) {
        this.remainingMilestones = remainingMilestones;
        this.remainingPath = remainingPath;

        refreshMilestones();
        invalidate();
    }

    public void refreshMilestonesAfterTransition() {
        for (ImageView imageView: milestoneImages) {
            int visibility = imageView.getVisibility();
            Point position = layoutRotatedCoordinateForPhysicalCoordinate(new PointF(imageView.getX(), imageView.getY()));
            imageView.layout(position.x - milestoneOffsetX, position.y - milestoneOffsetY,
                    position.x + milestoneOffsetX, position.y + milestoneOffsetY);
            imageView.setRotation(getRotation());
            imageView.setVisibility(visibility);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (remainingMilestones != null && remainingMilestones.getPoints().size() != 0) {
            for (Location point : remainingPath.getPoints()) {
                coordinate.setX(point.getX());
                coordinate.setY(point.getY());
                Point center = layoutRotatedCoordinateForPhysicalCoordinate(coordinate, getLayoutOffset());

                canvas.drawRect(center.x - 2, center.y - 2, center.x + 1, center.y + 1, paint);
            }
        }
    }

    private void refreshMilestones() {
        if (remainingMilestones != null && remainingMilestones.getPoints().size() != 0) {
            int pointIndex = 0;

            Point offset = getLayoutOffset();

            for (Location point : remainingMilestones.getPoints()) {
                PointF coordinate = new PointF(point.getX(), point.getY());
                Point center = layoutRotatedCoordinateForPhysicalCoordinate(coordinate, offset);
                ImageView imageView;

                if (pointIndex >= milestoneImages.size()) {
                    imageView = new ImageView(getContext());
                    imageView.setImageBitmap(milestoneImage);
                    milestoneImages.add(imageView);
                    addView(imageView);
                } else {
                    imageView = milestoneImages.get(pointIndex);
                    imageView.setVisibility(VISIBLE);
                }

                imageView.layout(center.x - milestoneOffsetX, center.y - milestoneOffsetY,
                        center.x + milestoneOffsetX, center.y + milestoneOffsetY);
                pointIndex++;
            }

            for(; pointIndex < milestoneImages.size(); pointIndex++) {
                ImageView imageView = milestoneImages.get(pointIndex);
                imageView.setVisibility(INVISIBLE);
            }
        }
    }
}
