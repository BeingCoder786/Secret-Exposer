package com.mnnit.secretexposer.ui.groups;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mnnit.secretexposer.R;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.GroupContentHolder> {
    @NonNull
    String []data;
    public RecyclerViewAdapter(String[] data){
        this.data=data;
    }
    @Override
    public GroupContentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        View view=inflater.inflate(R.layout.user_list_view,parent,false);
        return new GroupContentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupContentHolder holder, int position) {
        String title=data[position];
        holder.groupName.setText(title);

    }
    @Override
    public int getItemCount() {
        return data.length;
    }

    public class GroupContentHolder extends RecyclerView.ViewHolder{
        ImageView groupImageView;
        TextView groupName;
        public GroupContentHolder(@NonNull View itemView) {
            super(itemView);
            groupImageView=(ImageView)itemView.findViewById(R.id.group_image);
            groupName=(TextView)itemView.findViewById(R.id.group_name);
        }
    }
}
