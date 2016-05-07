package com.adisoftwares.bookreader.drive;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.adisoftwares.bookreader.BusStation;
import com.adisoftwares.bookreader.R;
import com.google.api.services.drive.model.File;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by adityathanekar on 18/03/16.
 */
public class DriveAdapter extends RecyclerView.Adapter<DriveAdapter.DriveViewHolder> {

    private LayoutInflater inflater;
    private ArrayList<File> filesList;
    private Context context;

    private DriveItemSelected driveItemSelected;

    public void setDriveItemSelectedListener(DriveItemSelected driveItemSelected) {
        this.driveItemSelected = driveItemSelected;
    }

    public DriveAdapter(Context context, ArrayList<File> filesList) {
        this.filesList = filesList;
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public DriveViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        DriveViewHolder holder = new DriveViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(DriveViewHolder holder, int position) {
        File data = filesList.get(position);
        holder.filename.setText(data.getName());
    }

    @Override
    public int getItemCount() {
        return filesList.size();
    }

    class DriveViewHolder extends RecyclerView.ViewHolder {

        @BindView(android.R.id.text1)
        TextView filename;

        public DriveViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            filename.setTextColor(context.getResources().getColor(android.R.color.black));
            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(driveItemSelected != null)
                        driveItemSelected.onDriveItemSelected(getAdapterPosition());
                }
            });
        }

    }

    public interface DriveItemSelected {
        void onDriveItemSelected(int position);
    }
}
