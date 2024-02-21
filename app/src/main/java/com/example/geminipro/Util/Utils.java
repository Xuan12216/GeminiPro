package com.example.geminipro.Util;

import android.content.Context;
import android.util.DisplayMetrics;

import com.example.geminipro.Database.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Utils {

    public static int convertDpToPixel(float dp, Context context){
        int px = (int) (dp * getDensity(context));
        return px;
    }

    private static float getDensity(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.density;
    }

    public static int dp2px(Context mContext, float dp) {
        return (int) (mContext.getResources().getDisplayMetrics().density * dp + 0.5f);
    }

    public static List<User> parallelSearch(List<User> usersList, String keyword) {
        List<User> filteredUsers = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(2);

        // 线程1: 搜索标题
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (User user : usersList.subList(0, usersList.size() / 2)){
                    String userTitle = user.getTitle();
                    if (userTitle.toLowerCase().contains(keyword)) filteredUsers.add(user);
                    else {
                        for (String uri : user.getStringUris()) {
                            if (uri.toLowerCase().contains(keyword)) {
                                filteredUsers.add(user);
                                break;
                            }
                        }
                    }
                }
                latch.countDown();
            }
        }).start();

        // 线程2: 搜索URI
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (User user : usersList.subList(usersList.size() / 2, usersList.size())){
                    String userTitle = user.getTitle();
                    if (userTitle.toLowerCase().contains(keyword)) filteredUsers.add(user);
                    else {
                        for (String uri : user.getStringUris()) {
                            if (uri.toLowerCase().contains(keyword)) {
                                filteredUsers.add(user);
                                break;
                            }
                        }
                    }
                }
                latch.countDown();
            }
        }).start();

        try {
            // 等待两个线程执行完毕
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return filteredUsers;
    }
}
