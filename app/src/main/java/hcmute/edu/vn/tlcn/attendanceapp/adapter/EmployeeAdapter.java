package hcmute.edu.vn.tlcn.attendanceapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hcmute.edu.vn.tlcn.attendanceapp.R;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;

public class EmployeeAdapter extends BaseAdapter {
    private Context context;
    private int layout;
    private List<User> lstEmp;

    public EmployeeAdapter(Context context, int layout, List<User> lstEmp) {
        this.context = context;
        this.layout = layout;
        this.lstEmp = lstEmp;
    }

    @Override
    public int getCount() {
        return lstEmp.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class ViewHolder{
        CircleImageView empAvatar;
        TextView empName;
        ImageView btnMore;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout,null);
            //ánh xạ các view
            holder.empAvatar = (CircleImageView) convertView.findViewById(R.id.empAvatar);
            holder.empName = (TextView) convertView.findViewById(R.id.empName);
            holder.btnMore = (ImageView) convertView.findViewById(R.id.btnMore);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        User user = lstEmp.get(position);
        holder.empName.setText(user.getFullName());
        if(!user.getAvatar().equals("")){
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference(user.getAvatar());
            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).fit().centerCrop().into(holder.empAvatar);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("TAG", "onFailure: " + e.getMessage());
                }
            });
        }
        else{
            holder.empAvatar.setImageResource(R.drawable.man_placeholder);
        }

        holder.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context,v );
                popupMenu.getMenuInflater().inflate(R.menu.menu_mng_emp,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.btnAddEmp:
                                return true;
                            case R.id.btnDeleteEmp:
                                Toast.makeText(context,"Success!!",Toast.LENGTH_SHORT).show();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });

        return convertView;
    }
}
