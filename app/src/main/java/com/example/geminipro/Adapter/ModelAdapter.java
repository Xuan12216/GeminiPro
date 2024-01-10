package com.example.geminipro.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.geminipro.R;

import java.util.ArrayList;
import java.util.List;

public class ModelAdapter extends RecyclerView.Adapter<ModelAdapter.ModelViewHolder> {

    private List<String> StringUris = new ArrayList<>();
    private List<String> userOrGemini = new ArrayList<>();
    private final Context context;

    public ModelAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new ModelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModelViewHolder holder, int position) {
        String who = userOrGemini.get(position);
        String text = StringUris.get(position);
        holder.messageTextView.setText(text);
        holder.avatarCardView.setCardBackgroundColor(("User").equals(who) ? context.getResources().getColor(R.color.navy_blue,null) : context.getResources().getColor(R.color.black,null));
        holder.usernameTextView.setText(("User").equals(who) ? "You" : "Gemini Pro");
        holder.avatarImageView.setImageResource(("User").equals(who) ? R.drawable.baseline_person_24 : R.drawable.baseline_person_robot_24);

    }

    @Override
    public int getItemCount() {
        return StringUris.size();
    }

    public void addData(String resultText, String who) {
        StringUris.add(resultText);
        userOrGemini.add(who);
        notifyDataSetChanged();
    }

    public void refreshData(String text, int index){
        StringUris.set(index, text);
        notifyDataSetChanged();
    }

    public static class ModelViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView,usernameTextView;
        public ImageView avatarImageView;
        public CardView avatarCardView;

        public ModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
            avatarCardView = itemView.findViewById(R.id.avatarCardView);
        }
    }
}
