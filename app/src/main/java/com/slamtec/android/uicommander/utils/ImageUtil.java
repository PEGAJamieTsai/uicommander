package com.slamtec.android.uicommander.utils;

import android.graphics.Bitmap;
import android.graphics.Point;

/**
 * Created by Alan on 10/8/15.
 */
public class ImageUtil {
    private final static String TAG = "ImageUtil";

    public static Bitmap createImage(byte[] buffer, Point size) {
        int width = size.x;
        int height = size.y;

        int[] rawData = new int[buffer.length];

        for (int i = 0; i < buffer.length; i++) {
            int grey = 0x7f + buffer[i];
            rawData[i] = 0xff<<24 | grey<<16 | grey<<8 | grey;
        }


        return Bitmap.createBitmap(rawData, width, height, Bitmap.Config.ARGB_8888);
    }

    public static Bitmap createSweepImage(byte[] buffer, Point size) {
        int width = size.x;
        int height = size.y;

        int[] rawData = new int[buffer.length];

        for (int i = 0; i < buffer.length; i++) {
            int rawPixel = buffer[i];

            SweepCellData sweepData = new SweepCellData(rawPixel);
            if (sweepData.isBoundary()) {
                if (sweepData.isVirtualBoundary()) {
                    rawData[i] = 0x7fff0000;
                } else {
                    rawData[i] = 0x7f0000ff;
                }
            } else {
                if (sweepData.isSwept()) {
                    rawData[i] = 0x7f00ff00;
                } else if (sweepData.isVisited()) {
                    if (sweepData.isUnreachable()) {
                        rawData[i] = 0x7f101010;
                    } else {
                        rawData[i] = 0x7f99ff99;
                    }
                } else {
                    int grey = 0x7f + buffer[i];
                    rawData[i] = 0x00 << 24 | grey << 16 | grey << 8 | grey;
                }
            }
        }

        return Bitmap.createBitmap(rawData, width, height, Bitmap.Config.ARGB_8888);
    }

    private static class SweepCellData {
        private final static int BITMASK_VISITED_BIT            = 0x1<<0;
        private final static int BITMASK_BOUNDARY_BIT           = 0x1<<1;
        private final static int BITMASK_VIRTUALBOUNDARY_BIT    = 0x1<<2;
        private final static int BITMASK_UNREACHABLE_BIT        = 0x1<<3;
        private final static int BITMASK_SWEPT_BIT              = 0x1<<4;

        private final static int BITMASK_SUBGRID_SHIFT = 4;
        private final static int BITMASK_SUBGRID_BITS = 0xf0;

        /*
         bit arrangement

         7                             0
         --------------------------------
         | u[0..3]  | x | X | X | X
         |       |   |   |   |------ visited
         |       |   |   |---------- boundary
         |       |   |-------------- virtual
         |       |------------------ unreachable
         |-----------------          user defined

         */

        int data;

        public SweepCellData(int data) {
            this.data = data;
        }

        public boolean isVisited() {
            return data == BITMASK_VISITED_BIT;
        }

        public boolean isBoundary() {
            return (data & BITMASK_BOUNDARY_BIT) > 0;
        }

        public boolean isVirtualBoundary() {
            return (data & BITMASK_VIRTUALBOUNDARY_BIT) > 0;
        }

        public boolean isRealBoundary() {
            return isBoundary() && (!isVirtualBoundary());
        }

        public boolean isUnreachable() {
            return (data & BITMASK_UNREACHABLE_BIT) > 0;
        }

        public boolean isSwept() {
            return (data & BITMASK_SWEPT_BIT) > 0;
        }

        public void markOccupancy() {
            data |= BITMASK_VISITED_BIT;
        }

        public void markAsUnreachable() {
            data |= BITMASK_UNREACHABLE_BIT;
        }

        public void markAsBoundary(boolean isVirtual) {
            data |= BITMASK_BOUNDARY_BIT;
            if (!isVirtual) {
                data &= ~(BITMASK_VIRTUALBOUNDARY_BIT);
            } else {
                data |= BITMASK_VIRTUALBOUNDARY_BIT;
            }
        }
    }
}
