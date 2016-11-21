package com.slamtec.android.uicommander.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Alan on 7/14/16.
 */
public class ExecutorUtil {

    //
    // singleton
    //

    private static ExecutorUtil instance;

    private ExecutorUtil() {
        executorService = Executors.newFixedThreadPool(4);
    }

    public static ExecutorUtil getInstance() {
        if (instance == null) {
            synchronized (ExecutorUtil.class) {
                if (instance == null) {
                    instance = new ExecutorUtil();
                }
            }
        }
        return instance;
    }

    //
    // ExecutorService
    //
    private ExecutorService executorService;

    public void execute(Runnable runnable) {
        executorService.execute(runnable);
    }
}
