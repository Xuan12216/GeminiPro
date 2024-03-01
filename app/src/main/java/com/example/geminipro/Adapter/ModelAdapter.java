package com.example.geminipro.Adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.geminipro.Database.User;
import com.example.geminipro.R;
import com.example.geminipro.databinding.RecyclerItemBinding;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ModelAdapter extends RecyclerView.Adapter<ModelAdapter.ModelViewHolder> implements ImageAdapter.ImageAdapterListener {

    private List<String> StringUris = new ArrayList<>();
    private List<String> userOrGemini = new ArrayList<>();
    private HashMap<Integer,List<Uri>> imageHashMap = new HashMap<Integer,List<Uri>>();
    private Context context = null;
    private String geminiName, userName, storedImagePath, title = "", date = "";
    private boolean isPin = false;
    private TextToSpeech textToSpeech;
    private ModelViewHolder soundHolderTemp;

    public ModelAdapter(Context context) {
        this.context = context;
        checkSharedPreferences();
    }

    @NonNull
    @Override
    public ModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerItemBinding binding = RecyclerItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ModelViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ModelViewHolder holder, int position) {

        holder.binding.recyclerViewModel.setAdapter(null);
        holder.binding.imageViewSound.setImageResource(R.drawable.baseline_volume_up_24);

        if (imageHashMap.containsKey(position)){
            holder.binding.recyclerViewModel.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            ImageAdapter adapterDown = new ImageAdapter(context, this);
            holder.binding.recyclerViewModel.setAdapter(adapterDown);
            adapterDown.setNewImage(imageHashMap.get(position), false);
        }

        String who = userOrGemini.get(position);
        String text = StringUris.get(position);
        holder.binding.messageTextView.setText(text);
        holder.binding.avatarCardView.setCardBackgroundColor(("User").equals(who) ? context.getResources().getColor(R.color.navy_blue,null) : context.getResources().getColor(R.color.transparent,null));
        holder.binding.usernameTextView.setText(("User").equals(who) ? userName : geminiName);
        holder.binding.cardShare.setVisibility(("User").equals(who) ? View.GONE : View.VISIBLE);
        holder.binding.cardCopy.setVisibility(("User").equals(who) ? View.GONE : View.VISIBLE);
        holder.binding.cardSound.setVisibility(("User").equals(who) ? View.GONE : View.VISIBLE);
        holder.binding.cardGoogle.setVisibility(("User").equals(who) ? View.GONE : View.VISIBLE);

        holder.binding.cardShare.setOnClickListener(shareListener);
        holder.binding.cardShare.setTag(position);

        holder.binding.cardCopy.setOnClickListener(copyListener);
        holder.binding.cardCopy.setTag(position);

        holder.binding.cardSound.setOnClickListener(soundListener);
        holder.binding.cardSound.setTag(holder);

        holder.binding.cardGoogle.setOnClickListener(gotoGoogleListener);
        holder.binding.cardGoogle.setTag(position);

        Glide.with(context)
                .load((holder.getAdapterPosition() == StringUris.size() - 1 && !("User").equals(who)) ?
                        R.drawable.sparkle_resting :
                        storedImagePath.isEmpty() ?
                                (("User").equals(who) ? R.drawable.baseline_person_24 : R.mipmap.logo_single_color) :
                                (("User").equals(who) ? storedImagePath : R.mipmap.logo_single_color))
                .into(holder.binding.avatarImageView);
    }

    @Override
    public int getItemCount() {
        return StringUris.size();
    }

    private final View.OnClickListener gotoGoogleListener = v -> {
        int position = (int) v.getTag();
        if (position - 1 >= 0 && position - 1 < StringUris.size()) {
            String query = StringUris.get(position - 1);
            List<Uri> list = new ArrayList<>();

            if (imageHashMap.containsKey(position - 1)){
                list = imageHashMap.get(position - 1);
            }
            searchOnGoogle(query, list);
        }
    };

    private void searchOnGoogle(String query, List<Uri> imageUris) {
        try {
            // 构建搜索的 URL，包括查询参数
            String searchUrl = "https://www.google.com/search?q=" + Uri.encode(query);

            // 如果有图片URI，将图像URI作为搜索的一部分
            if (imageUris != null && !imageUris.isEmpty()) {
                String imageUrl = imageUris.get(0).getPath();
                //searchUrl = "https://www.google.com/searchbyimage?image_url=" + imageUrl + "&q=" + Uri.encode(query);
            }

            // 创建一个 Intent，指定 ACTION_VIEW 操作并传递搜索的 URL
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(searchUrl));

            Toast.makeText(context, R.string.google_search, Toast.LENGTH_SHORT).show();
            context.startActivity(intent);
        }
        catch (Exception e) {e.printStackTrace();}
    }

    private final View.OnClickListener shareListener = v -> {
        int position = (int) v.getTag();
        String text = StringUris.get(position);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        context.startActivity(shareIntent);
    };

    private final View.OnClickListener copyListener = v -> {
        int position = (int) v.getTag();
        String text = StringUris.get(position);
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("Gemini Generate Text", text));
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
            Toast.makeText(context, R.string.copySuccess, Toast.LENGTH_SHORT).show();
    };

    private final View.OnClickListener soundListener = v -> {
        ModelViewHolder holder = (ModelViewHolder) v.getTag();
        String text = StringUris.get(holder.getAdapterPosition());

        // 如果当前有正在播放的 TTS，则停止播放
        if (null != textToSpeech && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
            holder.binding.imageViewSound.setImageResource(R.drawable.baseline_volume_up_24);
            return;
        }

        // 否则开始播放新的 TTS
        if (!TextUtils.isEmpty(text)) {
            textToSpeech = new TextToSpeech(context, status -> {
                if (status == TextToSpeech.SUCCESS) initTTS(text, holder);
                else Toast.makeText(context, R.string.ttsError, Toast.LENGTH_SHORT).show();
            });
        }
    };

    private void initTTS(String text, ModelViewHolder holder) {
        this.soundHolderTemp = holder;
        text = filterPunctuation(text);
        int chineseCount = countChineseCharacters(text);
        int englishCount = text.length() - chineseCount;
        // 判断主要语言类型
        boolean isChinesePrimary = chineseCount > englishCount;

        int result = isChinesePrimary ? textToSpeech.setLanguage(Locale.TRADITIONAL_CHINESE) : textToSpeech.setLanguage(Locale.US);
        if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE && result != TextToSpeech.LANG_AVAILABLE) {
            Toast.makeText(context, R.string.ttsError, Toast.LENGTH_SHORT).show();
        }
        else {
            holder.binding.imageViewSound.setImageResource(R.drawable.baseline_stop_24);
            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    Toast.makeText(context, R.string.ttsSuccess, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onDone(String utteranceId) {
                    // 在朗读完成时切换图标
                    holder.binding.imageViewSound.setImageResource(R.drawable.baseline_volume_up_24);
                }

                @Override
                public void onError(String utteranceId) {
                    Toast.makeText(context, R.string.ttsError, Toast.LENGTH_SHORT).show();
                    holder.binding.imageViewSound.setImageResource(R.drawable.baseline_volume_up_24);
                }
            });

            String utteranceId = UUID.randomUUID().toString();
            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId);
        }
    }

    private String filterPunctuation(String text) {
        // 使用正则表达式去除标点符号
        return text.replaceAll("[\\p{P}]", " ");
    }

    public void ttsShutdown(){
        if (null != textToSpeech) {
            textToSpeech.stop();
            if (null != soundHolderTemp) soundHolderTemp.binding.imageViewSound.setImageResource(R.drawable.baseline_volume_up_24);
        }
    }

    private int countChineseCharacters(String text) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                    || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                    || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A) {
                count++;
            }
        }
        return count;
    }

    public void addData(String resultText,List<Uri> imageUris, String who, int index) {
        StringUris.add(resultText);
        userOrGemini.add(who);
        List<Uri> newImageUris = new ArrayList<>(imageUris);
        if (newImageUris.size() > 0 && !"Gemini".equals(who)) imageHashMap.put(index,newImageUris);
        notifyDataSetChanged();
    }

    public User saveData(){
        return new User(title, date, StringUris, userOrGemini, imageHashMap, isPin);
    }

    public void receiveDataAndShow(User user){
        resetData();
        this.StringUris = new ArrayList<>(user.getStringUris());
        this.userOrGemini = new ArrayList<>(user.getUserOrGemini());
        this.imageHashMap = new HashMap<>(user.getImageHashMap());
        this.title = user.getTitle();
        this.date = user.getDate();
        this.isPin = user.isPin();
        notifyDataSetChanged();
    }

    public void checkSharedPreferences() {
        if (context != null){
            SharedPreferences preferences = context.getSharedPreferences("gemini_private_prefs", Context.MODE_PRIVATE);
            geminiName = preferences.getString("geminiName", "");
            userName = preferences.getString("userName", "");
            storedImagePath = preferences.getString("userImage", "");
            if (geminiName.isEmpty()) geminiName = "Gemini";
            if (userName.isEmpty()) userName  = "You";
        }
    }

    private void resetData() {
        this.title = "";
        this.date = "";
        this.isPin = false;
        this.StringUris.clear();
        this.userOrGemini.clear();
        this.imageHashMap.clear();
        if (null != textToSpeech) textToSpeech.stop();
    }

    @Override
    public void onImageListUpdated(List<Uri> updatedImageUris) {}

    public static class ModelViewHolder extends RecyclerView.ViewHolder {
        public final RecyclerItemBinding binding;

        public ModelViewHolder(@NonNull RecyclerItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
