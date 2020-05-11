package com.example.stickerwhatsapp.add;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.stickerwhatsapp.R;
import com.example.stickerwhatsapp.utils.pictureFacer;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AddSubItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "ItemAdapter";
    static final int TYPE_ADD_FACE = 23;
    static final int TYPE_NORMAL = 232;
    private final int CODE_IMG_GALLERY = 1;
    private List<pictureFacer> mSubItem;
    private OnClickListener onClickListener;

    public AddSubItemAdapter(List<pictureFacer> mSubItem) {
        this.mSubItem = mSubItem;
    }

    public void setOnClickListener(OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
//        View subItemView = inflater.inflate(R.layout.item_sub_main, parent, false);
//        ViewHolder viewHolder = new ViewHolder(subItemView);
        View view = null;
        if (viewType == TYPE_NORMAL) {
            view = inflater.inflate(R.layout.item_sub_main, parent, false);
            GalleryHolder viewHolder = new GalleryHolder(view);
            return viewHolder;
        } else {
            view = inflater.inflate(R.layout.item_add_gallery, parent, false);
            FaceLibraryHolderAddFace faceGallery= new FaceLibraryHolderAddFace(view);
            return faceGallery;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_NORMAL) {
            final GalleryHolder galleryHolder = (GalleryHolder) holder;
            //Uri imgFace = mSubItem.get(position);
            Glide.with(galleryHolder.itemView)
                    .load(mSubItem.get(position).getPicturePath())
                    .apply(new RequestOptions().centerCrop())
                    .into(galleryHolder.imgSub);
            galleryHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickListener.onClickItem(position);
                }
            });
        } else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onClickItem(position);
                }
            });
        }
    }
    public class GalleryHolder extends RecyclerView.ViewHolder{
        private ImageView imgSub;
        public GalleryHolder(@NonNull View itemView) {
            super(itemView);
            imgSub = itemView.findViewById(R.id.imgSubMain);
        }
    }
    public class FaceLibraryHolderAddFace extends RecyclerView.ViewHolder {
        public FaceLibraryHolderAddFace(@NonNull View itemView) {
            super(itemView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ?  TYPE_ADD_FACE : TYPE_NORMAL;
    }

    @Override
    public int getItemCount() {
        return mSubItem.size();
    }
    public interface OnClickListener{
        void onClickItem(int position);
    }
}
