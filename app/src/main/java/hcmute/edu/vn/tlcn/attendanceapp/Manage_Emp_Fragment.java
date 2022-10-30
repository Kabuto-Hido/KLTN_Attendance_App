package hcmute.edu.vn.tlcn.attendanceapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.SuccessContinuation;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import at.favre.lib.crypto.bcrypt.BCrypt;
import hcmute.edu.vn.tlcn.attendanceapp.adapter.EmployeeAdapter;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;
import hcmute.edu.vn.tlcn.attendanceapp.pattern.User_singeton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Manage_Emp_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Manage_Emp_Fragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Manage_Emp_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Manage_Emp_Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Manage_Emp_Fragment newInstance(String param1, String param2) {
        Manage_Emp_Fragment fragment = new Manage_Emp_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    View view;
    ImageView btnBackPageMngEmp;
    ListView listviewEmp;
    EmployeeAdapter employeeAdapter;
    ArrayList<User> empList = new ArrayList<>();
    FloatingActionButton btnAddEmp;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_manage__emp_, container, false);

        if(User_singeton.getInstance().getUser() == null) {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }

        mapping();

        employeeAdapter = new EmployeeAdapter(getActivity(),R.layout.emp_row,empList,Manage_Emp_Fragment.this);
        listviewEmp.setAdapter(employeeAdapter);

        btnBackPageMngEmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdminSettingsFragment adminSettingsFragment = new AdminSettingsFragment();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, adminSettingsFragment).commit();
            }
        });

        btnAddEmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddEmployee addEmployee = new AddEmployee();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, addEmployee).commit();
            }
        });

        getListEmp();

        return view;
    }

    private void mapping(){
        btnBackPageMngEmp = (ImageView) view.findViewById(R.id.btnBackPageMngEmp);
        listviewEmp = (ListView) view.findViewById(R.id.listviewEmp);
        btnAddEmp = (FloatingActionButton) view.findViewById(R.id.btnAddEmp);
    }

    private void getListEmp(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users");
        ref.orderByChild("role").equalTo(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                empList.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    empList.add(user);
                }
                employeeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Get list employees failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void dialogResetPass(User user){
        AlertDialog.Builder dialogResetPass = new AlertDialog.Builder(getActivity());
        dialogResetPass.setMessage("Do you sure want to reset password for employee have phone "+user.getPhone()+" ?");
        dialogResetPass.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //reset pass emp - default: 123456
                String defaultPassword = "123456";
                String hashPass = BCrypt.withDefaults().hashToString(12, defaultPassword.toCharArray());
                user.setPassword(hashPass);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference userRef = database.getReference("users");
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChild(user.getPhone())) {
                            userRef.child(user.getPhone()).setValue(user);
                            Toast.makeText(getActivity(),"New " + user.getPhone()
                                    +"'s password is 123456",Toast.LENGTH_SHORT).show();

                            dialog.dismiss();

                        }
                        else{
                            Toast.makeText(getContext(), "User not exist !", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
            }
        });

        dialogResetPass.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialogResetPass.show();
    }

    public void DialogEmpDelete(User user){
        AlertDialog.Builder dialogDelete = new AlertDialog.Builder(getActivity());
        dialogDelete.setMessage("Do you want to delete employee have phone "+user.getPhone()+" ?");
        dialogDelete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageReference = storage.getReference(user.getAvatar());
                storageReference.delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference ref = database.getReference("users");
                                ref.child(user.getPhone()).removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                getListEmp();
                                                dialog.dismiss();
                                                Toast.makeText(getActivity(),"Delete successful!!",Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("deleteEmp",e.getMessage());
                                                dialog.dismiss();
                                                Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("deleteEmp",e.getMessage());
                                dialog.dismiss();
                                Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        dialogDelete.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogDelete.show();
    }
}