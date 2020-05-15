package stickermaker.wastickerapps.stickersforwhatsapp.add;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import stickermaker.wastickerapps.stickersforwhatsapp.R;

public class ItemChooseAdapter extends RecyclerView.Adapter {

    private static final String TAG = "ItemChooseAdapter";
    private List<String> mSubItem;
    private OnClickListener onClickListener;

    public ItemChooseAdapter(List mSubItem) {
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
        String uriImage = (String) mSubItem.get(position);
        ViewHolder holder1 = (ViewHolder) holder;
        //holder1.imgSub.setImageURI(uriImage);
        Glide.with(holder1.itemView)
                .load(mSubItem.get(position))
                .apply(new RequestOptions().centerCrop())
                .into(holder1.imgSub);
        Log.e(TAG, "onBindViewHolder: "+uriImage );
        holder1.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.onClickItem(position);
            }
        });

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
