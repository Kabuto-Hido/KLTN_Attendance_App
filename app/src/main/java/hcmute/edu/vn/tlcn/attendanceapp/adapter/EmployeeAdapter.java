package hcmute.edu.vn.tlcn.attendanceapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
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
import hcmute.edu.vn.tlcn.attendanceapp.AdminMainActivity;
import hcmute.edu.vn.tlcn.attendanceapp.EditEmpFragment;
import hcmute.edu.vn.tlcn.attendanceapp.MainActivity;
import hcmute.edu.vn.tlcn.attendanceapp.Manage_Emp_Fragment;
import hcmute.edu.vn.tlcn.attendanceapp.R;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;

public class EmployeeAdapter extends BaseAdapter {
    private Context context;
    private int layout;
    private List<User> lstEmp;
    private Manage_Emp_Fragment fragment;

    public EmployeeAdapter(Context context, int layout, List<User> lstEmp, Manage_Emp_Fragment fragment) {
        this.context = context;
        this.layout = layout;
        this.lstEmp = lstEmp;
        this.fragment = fragment;
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
        TextView empName, empCode;
        ImageView btnMore;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout,null);

            holder.empAvatar = (CircleImageView) convertView.findViewById(R.id.empAvatar);
            holder.empName = (TextView) convertView.findViewById(R.id.empName);
            holder.empCode = (TextView) convertView.findViewById(R.id.empCode);
            holder.btnMore = (ImageView) convertView.findViewById(R.id.btnMore);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        User user = lstEmp.get(position);

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
        holder.empCode.setText(user.getUuid());
        holder.empName.setText(user.getFullName());

        holder.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context,v );
                popupMenu.getMenuInflater().inflate(R.menu.menu_mng_emp,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.btnEditEmp:
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("edtUser",user);
                                //bundle.putString("edtUser", user.getPhone());
                                EditEmpFragment editEmpFragment = new EditEmpFragment();
                                editEmpFragment.setArguments(bundle);
                                ((AdminMainActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.flAdminFragment, editEmpFragment).commit();
                                return true;
                            case R.id.btnDeleteEmp:
                                fragment.DialogEmpDelete(user);
                                return true;
                            case R.id.btnResetPass:
                                fragment.dialogResetPass(user);
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
