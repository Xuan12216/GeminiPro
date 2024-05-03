package com.example.geminipro.Database;

import android.content.Context;
import androidx.lifecycle.Lifecycle;
import androidx.room.Room;

import com.example.geminipro.enums.DBType;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;
import java.util.List;
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
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();

    public UserRepository(Context context, Lifecycle lifecycle) {
        //AppDatabase appDatabase = Room.databaseBuilder(context, AppDatabase.class, "my-database").addMigrations(AppDatabase.MIGRATION_2_3).build();
        AppDatabase appDatabase = Room.databaseBuilder(context, AppDatabase.class, "my-database")
                .build();
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
        if (type == null || user == null || printText == null || callback == null) return;
        synchronized (lock1) {
            if (null != disposable){
                disposable.dispose();
            }
            disposable = new CompositeDisposable();

            if (type.equals(DBType.insert)){
                disposable.add(insertUser(user).subscribe(() -> {
                    System.out.println("TestXuan: "+printText);
                    callback.onDone();
                }, Throwable::printStackTrace));
            }
            else if (type.equals(DBType.update)){
                disposable.add(updateUser(user).subscribe(() -> {
                    System.out.println("TestXuan: "+printText);
                    callback.onDone();
                }, Throwable::printStackTrace));
            }
            else if (type.equals(DBType.delete)){
                disposable.add(deleteUser(user).subscribe(() -> {
                    System.out.println("TestXuan: "+printText);
                    callback.onDone();
                }, Throwable::printStackTrace));
            }
        }
    }

    public void getSaveData(onDoneGetDataCallback callback) {
        synchronized (lock2) {
            getAllUsersDesc()
                    .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycle)))
                    .subscribe(userList -> {
                        System.out.println("TestXuan: getSaveData");
                        callback.onDone(userList);
                    }, Throwable::printStackTrace);
        }
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
