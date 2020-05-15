package stickermaker.wastickerapps.stickersforwhatsapp.detail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import stickermaker.wastickerapps.stickersforwhatsapp.R;
import stickermaker.wastickerapps.stickersforwhatsapp.utils.pictureFacer;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DetailGalleryAdapter extends RecyclerView.Adapter {
    private static final String TAG = "DetailGalleryAdapter";
    private List<pictureFacer> mSubItem;
    private OnClickListener onClickListener;

    public DetailGalleryAdapter(List mSubItem) {
        this.mSubItem = mSubItem;
    }

    public void setOnClickListener(OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View subItemView = inflater.inflate(R.layout.item_sub_main, parent, false);
        ViewHolder viewHolder = new ViewHolder(subItemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        pictureFacer uriImage = (pictureFacer) mSubItem.get(position);
        ViewHolder holder1 = (ViewHolder) holder;
        //holder1.imgSub.setImageURI(uriImage);
        Glide.with(holder1.itemView)
                .load(uriImage.getPicturePath())
                .apply(new RequestOptions().centerCrop())
                .into(holder1.imgSub);
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView imgSub;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSub = itemView.findViewById(R.id.imgSubMain);
        }
    }
    @Override
    public int getItemCount() {
        return mSubItem.size();
    }
    public interface OnClickListener{
        void onClickItem(int position);
    }
}
