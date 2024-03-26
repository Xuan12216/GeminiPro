package com.example.geminipro.Database;

import android.content.Context;

import androidx.lifecycle.Lifecycle;
import androidx.room.Room;

import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class UserRepository {
    private final UserDao userDao;
    private final Lifecycle lifecycle;

    public UserRepository(Context context, Lifecycle lifecycle) {
        AppDatabase appDatabase = Room.databaseBuilder(context, AppDatabase.class, "my-database").build();
        userDao = appDatabase.userDao();
        this.lifecycle = lifecycle;
    }

    private Completable insertUser(User user) {
        return Completable.fromAction(() -> userDao.insertUser(user))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Completable updateUser(User user) {
        return Completable.fromAction(() -> userDao.updateUser(user))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void updateOrInsertUserByPause(String type, User user, String text) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(()-> {
            if (type.equals("update")) userDao.updateUser(user);
            else if (type.equals("insert")) userDao.insertUser(user);
        });
        System.out.println("TestXuan: "+text);
    }

    public void getUserByTitle_onPause(String title, onDoneGetTitleCallback callback){
        synchronized (this){
            ExecutorService service = Executors.newSingleThreadExecutor();
            service.execute(() -> {
                User user = userDao.getUserByTitle_onPause(title);
                if (null != callback) callback.onDone(user);
            });
        }
    }

    private Completable deleteUser(User user) {
        return Completable.fromAction(() -> userDao.deleteUser(user))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<User> getUserByTitle(String title) {
        return userDao.getUserByTitle(title)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Flowable<List<User>> getAllUsersDesc() {
        return userDao.getAllUsersDesc()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public void saveDatabase(String type, User user, String printText, onDoneCallback callback){
        switch (type){
            case "insert":
                insertUser(user)
                        .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycle)))
                        .subscribe(() -> {
                            System.out.println("TestXuan: "+printText);
                            callback.onDone();
                        }, Throwable::printStackTrace);
                break;
            case "update":
                updateUser(user)
                        .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycle)))
                        .subscribe(() -> {
                            System.out.println("TestXuan: "+printText);
                            callback.onDone();
                        }, Throwable::printStackTrace);
                break;
            case "delete":
                deleteUser(user)
                        .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycle)))
                        .subscribe(() -> {
                            System.out.println("TestXuan: "+printText);
                            callback.onDone();
                        }, Throwable::printStackTrace);
                break;
        }
    }

    public void getSaveData(onDoneGetDataCallback callback) {
        getAllUsersDesc()
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycle)))
                .subscribe(userList -> {
                    System.out.println("TestXuan: getSaveData");
                    callback.onDone(userList);
                }, Throwable::printStackTrace);
    }

    public interface onDoneCallback {
        void onDone();
    }

    public interface onDoneGetDataCallback {
        void onDone(List<User> userList);
    }

    public interface onDoneGetTitleCallback{
        void onDone(User user);
    }
}
