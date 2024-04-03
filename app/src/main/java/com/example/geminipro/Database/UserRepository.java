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
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class UserRepository {
    private final UserDao userDao;
    private final Lifecycle lifecycle;
    private CompositeDisposable disposable;

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
        if (null != disposable){
            disposable.dispose();
            disposable = null;
        }
        disposable = new CompositeDisposable();

        switch (type){
            case "insert":
                disposable.add(insertUser(user).subscribe(() -> {
                    System.out.println("TestXuan: "+printText);
                    callback.onDone();
                }, Throwable::printStackTrace));
                break;
            case "update":
                disposable.add(updateUser(user).subscribe(() -> {
                    System.out.println("TestXuan: "+printText);
                    callback.onDone();
                }, Throwable::printStackTrace));
                break;
            case "delete":
                disposable.add(deleteUser(user).subscribe(() -> {
                    System.out.println("TestXuan: "+printText);
                    callback.onDone();
                }, Throwable::printStackTrace));
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

    public void disposeService(){
        if (null != disposable){
            disposable.dispose();
            disposable = null;
        }
    }
    public interface onDoneCallback {
        void onDone();
    }

    public interface onDoneGetDataCallback {
        void onDone(List<User> userList);
    }
}
