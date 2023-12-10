package com.example.notetakingapplication;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.notetakingapplication.databinding.ActivityMainBinding;
import com.example.notetakingapplication.databinding.ActivityNoteDetailBinding;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NoteDetailActivity extends AppCompatActivity {
    private EditText titleEditText, descEditText;
    private TextView selectTime;
    private ImageButton deleteButton;
    private ImageView image;
    private Note selectedNote;
    private @NonNull ActivityNoteDetailBinding binding;
    private MaterialTimePicker timePicker;
    private Calendar calendar;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoteDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initWidget();
        checkForEditNote();
        new Thread(() -> createNotificationChannel()).start();

        binding.selectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_12H)
                        .setHour(12)
                        .setMinute(0)
                        .setTitleText("Select Alarm Time")
                        .build();

                timePicker.show(getSupportFragmentManager(), "androidknowledge");
                timePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("đang set thời gian");
                        if(timePicker.getHour() > 12) {
                            binding.selectTime.setText(
                                    String.format("%02d", (timePicker.getHour() - 12)) + ":" +
                                            String.format("%02d", (timePicker.getMinute())) + "PM"
                            );
                        } else{
                            binding.selectTime.setText(timePicker.getHour() + ":" + timePicker.getMinute() + "AM");
                        }

                        calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                        calendar.set(Calendar.MINUTE, timePicker.getMinute());
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                    }
                });
            }
        });

        binding.setAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(NoteDetailActivity.this, AlarmReceiver.class);
                pendingIntent = PendingIntent.getBroadcast(NoteDetailActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                Toast.makeText(NoteDetailActivity.this, "Alarm Set", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void checkForEditNote() {
        Intent previousIntent = getIntent();
        int passedNoteID = previousIntent.getIntExtra(Note.NOTE_EDIT_EXTRA, -1);
        selectedNote = Note.getNoteForID(passedNoteID);

        if (selectedNote != null) {
            titleEditText.setText(selectedNote.getTitle());
            descEditText.setText(selectedNote.getDescription());
            // Get the image byte array from the selected note
            byte[] img = selectedNote.getImage();

            // Check if the image is null
            if (img != null) {
                // Set the image directly from the byte array
                image.setImageBitmap(BitmapFactory.decodeByteArray(img, 0, img.length));
            }
        } else {
            deleteButton.setVisibility(View.INVISIBLE);
        }
    }

    private void initWidget() {
        titleEditText = findViewById(R.id.titleEditText);
        descEditText = findViewById(R.id.descriptionEditText);
        deleteButton = findViewById(R.id.deleteNoteButton);
        selectTime = findViewById(R.id.selectTime);
        image = findViewById(R.id.image);
    }

    public void saveNote(View view) {
        SQLiteManager sqLiteManager = SQLiteManager.instanceOfDatabase(this);
        String title = titleEditText.getText().toString();
        String description = descEditText.getText().toString();
        LocalDateTime created = LocalDateTime.now(); // Update the LocalDateTime creation
        byte[] img = null;
        Drawable drawable_img = image.getDrawable();
        if (drawable_img != null) {
            Bitmap bitmap = ((BitmapDrawable) drawable_img).getBitmap();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            img = byteArrayOutputStream.toByteArray();
        }

        // Set formatted date to your EditText
        if (selectedNote == null) {
            // Get the last used ID or use the size of the array + 1 as a fallback
            int id;
            if (Note.noteArrayList.isEmpty()) {
                id = 1; // Start from ID 1 if the array is empty
            } else {
                id = Note.noteArrayList.get(Note.noteArrayList.size() - 1).getId() + 1;
            }
            Note newNote = new Note(id, title, description, created, img);
            Note.noteArrayList.add(newNote);
            sqLiteManager.addNoteToDatabase(newNote);
        } else {
            selectedNote.setTitle(title);
            selectedNote.setDescription(description);
            selectedNote.setCreated(created);
            sqLiteManager.updateNoteInDB(selectedNote);
        }
        finish();
    }

    public void deleteNote(View view) {
        selectedNote.setDeleted(new Date());
        SQLiteManager sqLiteManager = SQLiteManager.instanceOfDatabase(this);
        sqLiteManager.updateNoteInDB(selectedNote);
        finish();
    }


    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            CharSequence name = "akchannel";
            String desc = "Channel for Alarm";
            int imp = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel("Notify", name, imp);
            channel.setDescription(desc);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            // Kiểm tra xem ứng dụng đã được cấp quyền SEND_NOTIFICATION chưa
            if (!notificationManager.areNotificationsEnabled()) {
                // Nếu chưa, chuyển người dùng đến màn hình cài đặt để cấp quyền
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
            }
        }
    }



    public void addImageButton(View view) {
//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setType("image/*");
        //imagePickerLauncher.launch(String.valueOf(intent));
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        ActivityCompat.startActivityForResult(this, intent, PICK_IMAGE_REQUEST, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SQLiteManager sqLiteManager = SQLiteManager.instanceOfDatabase(this);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            // Get the URI of the selected image
            Uri selectedImageUri = data.getData();

            // Set the selected image to ImageView
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                image.setImageBitmap(bitmap);

                // Convert Bitmap to byte array
//                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArray);
//                byte[] img = byteArray.toByteArray();
//
//                // Save image to database
//                boolean insert = sqLiteManager.addImageToNote(img);
//                if (insert) {
//                    Toast.makeText(NoteDetailActivity.this, "Data Save", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(NoteDetailActivity.this, "Data Not Save", Toast.LENGTH_SHORT).show();
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}