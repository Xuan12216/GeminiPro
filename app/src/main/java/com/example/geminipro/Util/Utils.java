package com.example.geminipro.Util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import androidx.lifecycle.LifecycleOwner;
import com.example.geminipro.Database.User;
import com.example.geminipro.enums.DBType;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class Utils {

    public static void hideKeyboard(Activity activity){
        if (activity == null) return;

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static boolean isKeyboardOpen(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm.isActive();
    }


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

    public static void parallelSearch(Context context, List<User> usersList,boolean isSearchTitleOnly, String keyword, onFilterDoneCallback callback) {
        Observable.fromCallable(() -> filterList(usersList, keyword, isSearchTitleOnly))//goto Func filterList
                .subscribeOn(Schedulers.io())//在io訂閱
                .observeOn(AndroidSchedulers.mainThread())//轉換主線程
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from((LifecycleOwner) context)))//auto dispose
                .subscribe(callback::onDone);//訂閱成功使用callback回傳
    }

    private static List<User> filterList(List<User> usersList, String keyword, boolean isSearchTitleOnly) {
        String lowercaseKeyword = keyword.toLowerCase();
        return usersList.stream()
                .filter(user -> {
                    String userTitle = user.getTitle().toLowerCase();
                    if (userTitle.contains(lowercaseKeyword)) return true;
                    if (!isSearchTitleOnly) {
                        for (String uri : user.getStringUris()) {
                            if (uri.toLowerCase().contains(lowercaseKeyword)) return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    public static void setUserData(User inputData,String funcType, onMatchDoneCallback callback) {
        if (callback == null) return;

        Date date = new Date();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);

        User user = new User(inputData.getId(), inputData.getTitle(), today, inputData.getStringUris(), inputData.getUserOrGemini(), inputData.getImageHashMap(), inputData.isPin(), funcType);

        callback.onMatchDone(DBType.insert, "Insert_setUserData", user);
    }

    //====================================================

    public interface onFilterDoneCallback{
        void onDone(List<User> filteredList);
    }

    public interface onMatchDoneCallback{
        void onMatchDone(String type, String printText, User user);
    }
}
