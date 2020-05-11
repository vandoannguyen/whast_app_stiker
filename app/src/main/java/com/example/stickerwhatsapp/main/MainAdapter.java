package com.example.stickerwhatsapp.main;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stickerwhatsapp.R;
import com.example.stickerwhatsapp.utils.imageFolder;

import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainHolder> {
    private static final String TAG = "MainAdapter";
    private RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
    private List<imageFolder> mMain;
    private Context context;
    private OnClickListener onClickListener;
    public MainAdapter(List mMain, Context context) {
        this.mMain = mMain;
        this.context = context;
    }
    public void setOnClickListener(OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }
    @NonNull
    @Override
    public MainHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View mainView = inflater.inflate(R.layout.item_main, parent, false);
        MainHolder mainHolder = new MainHolder(mainView);
        return mainHolder;
    }
    @Override
    public void onBindViewHolder(@NonNull MainHolder mainHolder, int position) {
        imageFolder mainModel = mMain.get(position);
        mainHolder.nameFolder.setText(mainModel.getFolderName());
        mainHolder.numSticker.setText(mainModel.getNumberOfPics() + " stickers");
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                mainHolder.rvItem.getContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        );
        layoutManager.setInitialPrefetchItemCount(mainModel.getSubItem().size());
        Log.e(TAG, "onBindViewHolder: ItemAdapter");
        ItemAdapter itemAdapter = new ItemAdapter(mainModel.getSubItem());
        mainHolder.rvItem.setLayoutManager(layoutManager);
        mainHolder.rvItem.setAdapter(itemAdapter);
        mainHolder.rvItem.setRecycledViewPool(viewPool);
        mainHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.onClickItem(position);

            }
        });

    }
    public class MainHolder extends RecyclerView.ViewHolder{
        private TextView nameFolder;
        private TextView numSticker;
        private RecyclerView rvItem;
        public MainHolder(@NonNull View itemView) {
            super(itemView);
            nameFolder = itemView.findViewById(R.id.txtFile);
            numSticker = itemView.findViewById(R.id.txtNumber);
            rvItem = itemView.findViewById(R.id.rvItem);
        }
    }
    @Override
    public int getItemCount() {
        return mMain.size();
    }
    public interface OnClickListener{
        void onClickItem(int position);
    }
}
