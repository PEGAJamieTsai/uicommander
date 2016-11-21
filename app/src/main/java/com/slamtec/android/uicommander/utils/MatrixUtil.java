package com.slamtec.android.uicommander.utils;

import android.renderscript.Matrix2f;

/**
 * Created by Alan on 10/21/15.
 */
public class MatrixUtil {
    public static Matrix2f multiply(Matrix2f lhs, Matrix2f rhs) {
        Matrix2f tmp = new Matrix2f();
        tmp.load(lhs);
        tmp.multiply(rhs);
        return tmp;
    }

    public static Matrix2f add(Matrix2f lhs, Matrix2f rhs) {
        Matrix2f tmp = new Matrix2f();
        tmp.load(lhs);
        tmp.set(0, 0, tmp.get(0, 0) + rhs.get(0, 0));
        tmp.set(0, 1, tmp.get(0, 1) + rhs.get(0, 1));
        tmp.set(1, 0, tmp.get(1, 0) + rhs.get(1, 0));
        tmp.set(1, 1, tmp.get(1, 1) + rhs.get(1, 1));
        return tmp;
    }
}
