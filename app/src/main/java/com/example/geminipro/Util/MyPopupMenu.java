package com.example.geminipro.Util;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.PopupMenu;

import com.example.geminipro.Activity.InfoActivity;
import com.example.geminipro.Activity.SettingsActivity;
import com.example.geminipro.Adapter.HistoryAdapter;
import com.example.geminipro.Database.User;
import com.example.geminipro.R;

import java.util.List;

public class MyPopupMenu {

    private Context context;
    private int menuRes;
    private View v;
    private List<User> title;
    private HistoryAdapter.HistoryAdapterListener listener;
    private User user;

    public MyPopupMenu(Context context, int menuRes, View view){
        this.context = context;
        this.menuRes = menuRes;
        this.v = view;
    }

    public MyPopupMenu(Context context, int menuRes, View view, List<User> title, HistoryAdapter.HistoryAdapterListener listener, User user){
        this.context = context;
        this.menuRes = menuRes;
        this.v = view;
        this.title = title;
        this.listener = listener;
        this.user = user;
    }

    public void startPopUp(){
        String oriName = "";
        PopupMenu popupMenu = new PopupMenu(context, v, Gravity.END, 0, R.style.MyPopupMenuStyle);
        popupMenu.setForceShowIcon(true);
        popupMenu.inflate(menuRes);

        if (null != user){
            oriName = user.getTitle();
            MenuItem pinMenuItem = popupMenu.getMenu().findItem(R.id.pin);
            pinMenuItem.setTitle(user.isPin() ? R.string.menu_uppin : R.string.menu_pin);
        }

        String finalOriName = oriName;
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.Info) {
                context.startActivity(new Intent(context, InfoActivity.class));
                return true;
            }
            else if (item.getItemId() == R.id.settings){
                context.startActivity(new Intent(context, SettingsActivity.class));
                return true;
            }
            else if (item.getItemId() == R.id.pin) {
                if (null != user && null != listener) {
                    user.setPin(!user.isPin());
                    listener.onChooseHistoryStatus(user, "pin", "");
                }
                return true;
            }
            else if (item.getItemId() == R.id.rename){
                CustomDialog dialog = new CustomDialog(context, title, false, new CustomDialog.onEditSuccess() {
                    @Override
                    public void onSuccess(String rename) {
                        if (null != user && null != listener) {
                            user.setTitle(rename);
                            listener.onChooseHistoryStatus(user, "rename", finalOriName);
                        }
                    }
                });
                dialog.show();
                return true;
            }
            else if (item.getItemId() == R.id.delete){
                CustomDialog dialog = new CustomDialog(context, title, true,new CustomDialog.onEditSuccess() {
                    @Override
                    public void onSuccess(String status) {
                        if (null != user && null != listener && status.equals("true")) listener.onChooseHistoryStatus(user, "delete", "");
                    }
                });
                dialog.show();
                return  true;
            }
            return false;
        });
        popupMenu.show();
    }
}
