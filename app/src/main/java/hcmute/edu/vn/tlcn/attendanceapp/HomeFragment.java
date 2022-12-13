package hcmute.edu.vn.tlcn.attendanceapp;

import static android.Manifest.permission.CAMERA;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DigitalClock;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.squareup.picasso.Picasso;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import hcmute.edu.vn.tlcn.attendanceapp.model.Record;
import hcmute.edu.vn.tlcn.attendanceapp.model.Statistic;
import hcmute.edu.vn.tlcn.attendanceapp.model.User;
import hcmute.edu.vn.tlcn.attendanceapp.pattern.User_singeton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
    TextView txtNameUser, txtTimeCheckIn, txtTimeCheckOut, txtDayNow, notifiDone;
    CircleImageView avatarUser;
    DigitalClock digitalClock;
    Button btnTimeIn, btnTimeOut;
    User_singeton user_singeton = User_singeton.getInstance();
    User user;
    String status;
    String type;
    String absent1 = "absent with permission";
    String absent2 = "absent without permission";
    boolean absent = false;

    Interpreter tflite;
    Bitmap bitmapOrigin;
    int[] intValues;
    int inputSize = 112;
    int OUTPUT_SIZE =192;
    float [][] embeedings;
    private static final float IMAGE_MEAN = 128.0f;
    private static final float IMAGE_STD = 128.0f;

    float[][] ori_embedding = new float[1][192];
    float[][] checkIn_embedding = new float[1][192];
    Date currentTime;
    SimpleDateFormat dateFormat;
    SimpleDateFormat timeFormat;
    SimpleDateFormat dayFormat;
    SimpleDateFormat monthFormat;
    SimpleDateFormat yearFormat;
    int count;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_home, container, false);

        mapping();
        currentTime = Calendar.getInstance().getTime();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dayFormat = new SimpleDateFormat("dd");
        timeFormat = new SimpleDateFormat("HH:mm");
        monthFormat = new SimpleDateFormat("MM");
        yearFormat = new SimpleDateFormat("yyyy");

        try {
            tflite = new Interpreter(loadModelFile(getActivity()));
        }catch (Exception e){
            e.printStackTrace();
            Log.v("tflite",e.getMessage());
        }
        user = user_singeton.getUser();
        checkPreviousAttendance();

        putDataToView();

        btnTimeIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "checkIn";

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(currentTime);
                if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    dialog.setMessage("Today is sunday are you sure you want to take attendance ? ");
                    dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            takeAttendance();
                        }
                    });
                    dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            btnTimeIn.setVisibility(View.INVISIBLE);
                            notifiDone.setText("Have a nice weekend!!");
                            notifiDone.setVisibility(View.VISIBLE);
                            return;
                        }
                    });
                    dialog.show();
                }
                else {
                    checkIsTimeCheckIn();
                    if (absent) {
                        btnTimeIn.setVisibility(View.INVISIBLE);
                        notifiDone.setText("Check in time has passed!!");
                        notifiDone.setTextColor(Color.parseColor("#ff0000"));
                        notifiDone.setVisibility(View.VISIBLE);

                        currentTime = Calendar.getInstance().getTime();
                        String absentDate = dateFormat.format(currentTime);

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference recordRef = database.getReference("record")
                                .child(user.getPhone()).child(absentDate).child("absent");

                        Record absentRecord = new Record(user.getPhone(), absentDate, "", absent2, "absent");
                        recordRef.setValue(absentRecord);
                        updateStatistic(absent2);
                        Toast.makeText(getActivity(), "Attendance time has passed!!", Toast.LENGTH_SHORT).show();
                    } else {
                        takeAttendance();
                    }
                }

            }
        });

        btnTimeOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "checkOut";
                if(CheckPermissions()) {
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);
                }else {
                    RequestPermissions();
                }

            }
        });


        return view;
    }

    private void updateStatistic(String status){
        String currentMonth = monthFormat.format(currentTime);
        String currentYear = yearFormat.format(currentTime);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference statisticRef = database.getReference("statistic");
        statisticRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot dataSnapshot = snapshot.child(currentYear).child(currentMonth);
                Statistic monthStatistic = dataSnapshot.getValue(Statistic.class);

                DataSnapshot dataSnapshot2 = snapshot.child(user.getPhone()).child(currentYear).child(currentMonth);
                Statistic empStatistic = dataSnapshot2.getValue(Statistic.class);

                int countOnTime;
                int countLate;
                int countAbsentWithoutPer;
                //month statistic
                if(monthStatistic == null){
                    countOnTime = 0;
                    countLate = 0;
                    countAbsentWithoutPer = 0;
                    if(status.equals("on time")){
                        System.out.println("run");
                        countOnTime = 1;
                    }
                    else if(status.equals("late")){
                        countLate = 1;
                    }
                    else if(status.equals(absent2)){
                        countAbsentWithoutPer = 1;
                    }

                    //month statistic
                    Statistic newStatistic = new Statistic(countOnTime,countLate,0,countAbsentWithoutPer,currentMonth,currentYear,"");
                    statisticRef.child(currentYear).child(currentMonth).setValue(newStatistic);

                }
                else{
                    countOnTime = monthStatistic.getOnTime();
                    countLate = monthStatistic.getLate();
                    countAbsentWithoutPer = monthStatistic.getAbsentWithoutPer();
                    if(status.equals("on time")){
                        countOnTime++;
                        monthStatistic.setOnTime(countOnTime);
                    }
                    else if(status.equals("late")){
                        countLate++;
                        monthStatistic.setLate(countLate);
                    }
                    else if(status.equals(absent2)){
                        countAbsentWithoutPer++;
                        monthStatistic.setAbsentWithoutPer(countAbsentWithoutPer);
                    }
                    statisticRef.child(currentYear).child(currentMonth).setValue(monthStatistic);
                }
                //emp statistic
                if(empStatistic == null){
                    countOnTime = 0;
                    countLate = 0;
                    countAbsentWithoutPer = 0;
                    if(status.equals("on time")){
                        System.out.println("run");
                        countOnTime = 1;
                    }
                    else if(status.equals("late")){
                        countLate = 1;
                    }
                    else if(status.equals(absent2)){
                        countAbsentWithoutPer = 1;
                    }

                    Statistic newStatistic = new Statistic(countOnTime,countLate,0,countAbsentWithoutPer,currentMonth,currentYear,user.getPhone());
                    statisticRef.child(user.getPhone()).child(currentYear).child(currentMonth).setValue(newStatistic);
                }
                else{
                    countOnTime = empStatistic.getOnTime();
                    countLate = empStatistic.getLate();
                    countAbsentWithoutPer = empStatistic.getAbsentWithoutPer();
                    if(status.equals("on time")){
                        countOnTime+= 1;
                        empStatistic.setOnTime(countOnTime);
                    }
                    else if(status.equals("late")){
                        countLate+= 1;
                        empStatistic.setLate(countLate);
                    }
                    else if(status.equals(absent2)){
                        countAbsentWithoutPer+= 1;
                        empStatistic.setAbsentWithoutPer(countAbsentWithoutPer);
                    }
                    statisticRef.child(user.getPhone()).child(currentYear).child(currentMonth).setValue(empStatistic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkPreviousAttendance(){
        Date today = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("yyyy-MM");
        String dayCurr = dayFormat.format(today.getTime());
        String YMCurr = monthYearFormat.format(today.getTime());
        Calendar calendar = Calendar.getInstance();

        int n = Integer.parseInt(dayCurr);
        for(int i = 1; i < n; i++){
            count = 0;
            String dateAttend;
            if(String.valueOf(i).length()==1){
                dateAttend = YMCurr + "-0" + (i);
            }
            else {
                dateAttend = YMCurr + "-" + (i);
            }
            Date getDate = null;
            try {
                getDate = dateFormat.parse(dateAttend);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            calendar.setTime(getDate);
            
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference recordRef = database.getReference("record");
            if(calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                recordRef.child(user.getPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //check-in
                        DataSnapshot dataSnapshot = snapshot.child(dateAttend).child("checkIn");
                        Record checkInRecord = dataSnapshot.getValue(Record.class);
                        DataSnapshot dataSnapshot2 = snapshot.child(dateAttend).child("absent");
                        Record absentRecord = dataSnapshot2.getValue(Record.class);
                        DataSnapshot dataSnapshot3 = snapshot.child(dateAttend).child("checkOut");
                        Record checkOutRecord = dataSnapshot3.getValue(Record.class);

                        if (checkInRecord == null) {
                            if (absentRecord == null) {
                                Record absent = new Record(user.getPhone(), dateAttend, "", absent2, "absent");
                                recordRef.child(user.getPhone()).child(dateAttend).child("absent").setValue(absent);

                                count += 1;

                                //updateStatistic
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference statisticRef = database.getReference("statistic");
                                statisticRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String currentYear = dateAttend.substring(0, 4);
                                        String currentMonth = dateAttend.substring(5, 7);
                                        DataSnapshot dataSnapshot = snapshot.child(currentYear).child(currentMonth);
                                        Statistic monthStatistic = dataSnapshot.getValue(Statistic.class);
                                        int countAbsentWithoutPer;

                                        DataSnapshot dataSnapshot2 = snapshot.child(user.getPhone()).child(currentYear).child(currentMonth);
                                        Statistic empStatistic = dataSnapshot2.getValue(Statistic.class);

                                        if (monthStatistic == null) {
                                            Statistic newStatistic = new Statistic(0, 0, 0, count, currentMonth, currentYear, "");
                                            statisticRef.child(currentYear).child(currentMonth).setValue(newStatistic);
                                        } else {
                                            countAbsentWithoutPer = monthStatistic.getAbsentWithoutPer();
                                            countAbsentWithoutPer += count;
                                            statisticRef.child(currentYear).child(currentMonth).child("absentWithoutPer").setValue(countAbsentWithoutPer);
                                        }

                                        if (empStatistic == null) {
                                            Statistic newStatistic = new Statistic(0, 0, 0, count, currentMonth, currentYear, user.getPhone());
                                            statisticRef.child(user.getPhone()).child(currentYear).child(currentMonth).setValue(newStatistic);
                                        } else {
                                            countAbsentWithoutPer = empStatistic.getAbsentWithoutPer();
                                            countAbsentWithoutPer += count;
                                            statisticRef.child(user.getPhone()).child(currentYear).child(currentMonth).child("absentWithoutPer").setValue(countAbsentWithoutPer);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        } else {
                            if (checkOutRecord == null) {
                                Record checkOut = new Record(user.getPhone(), dateAttend, "17:00", "", "checkOut");

                                recordRef.child(user.getPhone()).child(dateAttend).child("checkOut").setValue(checkOut);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }
    }

    private void checkIsTimeCheckIn(){
        try {
            Date nine = timeFormat.parse("09:00");
            Date haftNine = timeFormat.parse("09:30");

            currentTime = Calendar.getInstance().getTime();
            String strCurTime = timeFormat.format(currentTime);
            Date dateCurTime = timeFormat.parse(strCurTime);

            if (dateCurTime != null) {
                if (dateCurTime.before(nine)) {
                    status = "on time";
                } else if (dateCurTime.after(nine) && dateCurTime.before(haftNine)) {
                    status = "late";
                } else if (dateCurTime.after(haftNine)) {
                    absent = true;
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
            Log.v("convertTimeErr", e.getMessage());
        }
    }

    private void takeAttendance(){
        if (CheckPermissions()) {
            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePicture, 0);
        } else {
            RequestPermissions();
        }
    }

    private void putDataToView() {

        if(user == null) {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }

        txtNameUser.setText(user.getFullName());

        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        txtDayNow.setText(format.format(today.getTime()));

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference(user.getAvatar());
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).fit().centerCrop().into(avatarUser);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG", "onFailure: " + e.getMessage());
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference recordRef = database.getReference("record").child(user.getPhone());

        String currentDate = dateFormat.format(currentTime);
        recordRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot dataSnapshot1 = snapshot.child(currentDate).child("checkIn");
                Record checkInRecord = dataSnapshot1.getValue(Record.class);
                if (checkInRecord != null && !checkInRecord.getTime().equals("")) {
                    txtTimeCheckIn.setText(checkInRecord.getTime());
                    btnTimeIn.setVisibility(View.INVISIBLE);
                    btnTimeOut.setVisibility(View.VISIBLE);
                }

                DataSnapshot dataSnapshot2 = snapshot.child(currentDate).child("checkOut");
                Record checkOutRecord = dataSnapshot2.getValue(Record.class);
                if (checkOutRecord != null && !checkOutRecord.getTime().equals("")) {
                    txtTimeCheckOut.setText(checkOutRecord.getTime());
                    btnTimeOut.setVisibility(View.INVISIBLE);
                    notifiDone.setVisibility(View.VISIBLE);
                }

                DataSnapshot dataSnapshot3 = snapshot.child(currentDate).child("absent");
                Record absentRecord = dataSnapshot3.getValue(Record.class);
                if (absentRecord != null) {
                    if(absentRecord.getStatus().equals(absent1)){
                        notifiDone.setText("You have the day off!!");
                    }
                    else{
                        notifiDone.setText("You are absent today!!");
                    }
                    btnTimeIn.setVisibility(View.INVISIBLE);
                    notifiDone.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void mapping() {
        txtNameUser = (TextView) view.findViewById(R.id.txtNameUser);
        txtTimeCheckIn = (TextView) view.findViewById(R.id.txtTimeCheckIn);
        txtTimeCheckOut = (TextView) view.findViewById(R.id.txtTimeCheckOut);
        txtDayNow = (TextView) view.findViewById(R.id.txtDayNow);
        avatarUser = (CircleImageView) view.findViewById(R.id.avatarUser);
        digitalClock = (DigitalClock) view.findViewById(R.id.digitalClock);
        btnTimeIn = (Button) view.findViewById(R.id.btnTimeIn);
        btnTimeOut = (Button) view.findViewById(R.id.btnTimeOut);
        notifiDone = (TextView) view.findViewById(R.id.notifiDone);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == getActivity().RESULT_OK
                && data != null){
            Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.PNG,100,stream);

            if(bitmapOrigin == null){
                BitmapDrawable drawable = (BitmapDrawable) avatarUser.getDrawable();
                bitmapOrigin = Bitmap.createBitmap( drawable.getBitmap());
                face_detector(bitmapOrigin,"origin");
            }

            face_detector(selectedImage,"checkin");


//            byte[] byteArray = stream.toByteArray();
//
//            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(stream.toByteArray());
            //imgAvatarProfile.setImageBitmap(bitmap);
        }
    }

    private float cal_distance(float[] ori_embedding, float[] checkIn_embedding ){
        float distance = 0;
        for(int i=0; i < ori_embedding.length;i++){
            float diff = checkIn_embedding[i] - ori_embedding[i];
            distance += diff*diff;
        }
        return (float) Math.sqrt(distance);
    }

    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd("mobile_face_net.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffSet = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffSet,declaredLength);
    }

    public void face_detector(Bitmap bitmap, String name){
        InputImage image = InputImage.fromBitmap(bitmap,0);
        FaceDetector detector = FaceDetection.getClient();
        detector.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
            @Override
            public void onSuccess(List<Face> faces) {
                for (Face face :faces){
                    RectF bounds = new RectF(face.getBoundingBox());
                    Bitmap cropped_face = getCropBitmapByCPU(bitmap, bounds);

                    Bitmap scaled = getResizedBitmap(cropped_face, inputSize, inputSize);
                    //Bitmap cropped = Bitmap.createBitmap(bitmap,bounds.left,bounds.top,bounds.width(),bounds.height());
                    get_embaddings(scaled,name);
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private static Bitmap getCropBitmapByCPU(Bitmap source, RectF cropRectF) {
        Bitmap resultBitmap = Bitmap.createBitmap((int) cropRectF.width(),
                (int) cropRectF.height(), Bitmap.Config.ARGB_8888);
        Canvas cavas = new Canvas(resultBitmap);

        // draw background
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setColor(Color.WHITE);
        cavas.drawRect(
                new RectF(0, 0, cropRectF.width(), cropRectF.height()),
                paint);

        Matrix matrix = new Matrix();
        matrix.postTranslate(-cropRectF.left, -cropRectF.top);

        cavas.drawBitmap(source, matrix, paint);

        if (source != null && !source.isRecycled()) {
            source.recycle();
        }
        return resultBitmap;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    public void get_embaddings(Bitmap bitmap, String name){

        ByteBuffer imgData = ByteBuffer.allocateDirect(inputSize * inputSize * 3 * 4);
        imgData.order(ByteOrder.nativeOrder());
        intValues = new int[inputSize * inputSize];

        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixelValue = intValues[i * inputSize + j];
                 // Float model
                imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
            }
        }

        //imgData is input to our model
        Object[] inputArray = {imgData};

        Map<Integer, Object> outputMap = new HashMap<>();

        outputMap.put(0, embeedings);

        embeedings = new float[1][OUTPUT_SIZE]; //output of model will be stored in this variable

        outputMap.put(0, embeedings);

        tflite.runForMultipleInputsOutputs(inputArray, outputMap);

        if(name.equals("checkin")){
            checkIn_embedding = embeedings;
            System.out.println("checkIn_embedding "+ Arrays.toString(checkIn_embedding[0]));
        }else{
            ori_embedding = embeedings;
            System.out.println("ori_embedding "+ Arrays.toString(ori_embedding[0]));
        }

        if(checkIn_embedding != null && ori_embedding != null){
            recordToDatabase(checkIn_embedding,ori_embedding);
            checkIn_embedding = null;
        }
    }

    private void recordToDatabase(float[][] checkIn_embedding, float[][] ori_embedding) {
        float dis = cal_distance(ori_embedding[0],checkIn_embedding[0]);
        System.out.println("Distance: "+dis);
        if(dis < 1.0f){
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference recordRef = database.getReference("record").child(user.getPhone());
            if(type.equals("checkIn")) {
                Toast.makeText(getActivity(), "Check In Successfully", Toast.LENGTH_SHORT).show();

                currentTime = Calendar.getInstance().getTime();
                String checkInDate = dateFormat.format(currentTime);
                String checkInTime = timeFormat.format(currentTime);

                Record record = new Record(user.getPhone(),checkInDate,checkInTime,status,type);

                recordRef.child(checkInDate).child(type).setValue(record);
                updateStatistic(status);

                txtTimeCheckIn.setText(digitalClock.getText());
                btnTimeIn.setVisibility(View.INVISIBLE);
                btnTimeOut.setVisibility(View.VISIBLE);
            }
            else{
                Toast.makeText(getActivity(), "Check Out Successfully", Toast.LENGTH_SHORT).show();

                currentTime = Calendar.getInstance().getTime();
                String checkOutDate = dateFormat.format(currentTime);
                String checkOutTime = timeFormat.format(currentTime);

                Record record = new Record(user.getPhone(),checkOutDate,checkOutTime,"",type);

                recordRef.child(checkOutDate).child(type).setValue(record);

                txtTimeCheckOut.setText(digitalClock.getText());
                notifiDone.setVisibility(View.VISIBLE);
                btnTimeOut.setVisibility(View.INVISIBLE);
            }
        }else {
            bitmapOrigin = null;
            Toast.makeText(getActivity(),"Can't recognize face",Toast.LENGTH_SHORT).show();
        }
    }

    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    public boolean CheckPermissions() {
        int result = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestPermissions() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{CAMERA}, REQUEST_AUDIO_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0) {
                boolean permissionToCamera = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (permissionToCamera) {
                    Toast.makeText(getActivity().getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}