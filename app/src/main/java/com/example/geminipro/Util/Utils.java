package com.example.geminipro.Util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

import androidx.lifecycle.LifecycleOwner;

import com.example.geminipro.Database.User;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.security.auth.callback.Callback;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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

    public static void parallelSearch(Context context, List<User> usersList, String keyword, onFilterDoneCallback callback) {
        Observable.fromCallable(() -> filterList(usersList, keyword))//goto Func filterList
                .subscribeOn(Schedulers.io())//在io訂閱
                .observeOn(AndroidSchedulers.mainThread())//轉換主線程
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from((LifecycleOwner) context)))//auto dispose
                .subscribe(callback::onDone);//訂閱成功使用callback回傳
    }

    private static List<User> filterList(List<User> usersList, String keyword) {
        String lowercaseKeyword = keyword.toLowerCase();
        return usersList.stream()
                .filter(user -> {
                    String userTitle = user.getTitle().toLowerCase();
                    if (userTitle.contains(lowercaseKeyword)) return true;
                    for (String uri : user.getStringUris()) {
                        if (uri.toLowerCase().contains(lowercaseKeyword)) return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }


    public interface onFilterDoneCallback{
        void onDone(List<User> filteredList);
    }
}
