package com.example.notetakingapplication;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NoteDetailActivity extends AppCompatActivity {
    private EditText titleEditText, descEditText;
    private ImageButton deleteButton;
    private Note selectedNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);
        initWidget();
        checkForEditNote();
    }

    private void checkForEditNote() {
        Intent previousIntent = getIntent();
        int passedNoteID = previousIntent.getIntExtra(Note.NOTE_EDIT_EXTRA, -1);
        selectedNote = Note.getNoteForID(passedNoteID);

        if (selectedNote != null) {
            titleEditText.setText(selectedNote.getTitle());
            descEditText.setText(selectedNote.getDescription());
        } else {
            deleteButton.setVisibility(View.INVISIBLE);
        }
    }

    private void initWidget() {
        titleEditText = findViewById(R.id.titleEditText);
        descEditText = findViewById(R.id.descriptionEditText);
        deleteButton = findViewById(R.id.deleteNoteButton);
    }

    public void saveNote(View view) {
        SQLiteManager sqLiteManager = SQLiteManager.instanceOfDatabase(this);
        String title = titleEditText.getText().toString();
        String description = descEditText.getText().toString();
        LocalDateTime created = LocalDateTime.now(); // Update the LocalDateTime creation
         // Set formatted date to your EditText
        if (selectedNote == null) {
            // Get the last used ID or use the size of the array + 1 as a fallback
            int id;
            if (Note.noteArrayList.isEmpty()) {
                id = 1; // Start from ID 1 if the array is empty
            } else {
                id = Note.noteArrayList.get(Note.noteArrayList.size() - 1).getId() + 1;
            }

            Note newNote = new Note(id, title, description, created);
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

    public void setAlarm(View view) {
        Intent intent = getIntent();
        intent.putExtra("title", titleEditText.toString());
        intent.putExtra("description", descEditText.toString());
        // Get the current date and time
        Calendar calendar = Calendar.getInstance();

        // Show DatePickerDialog to select the date
        DatePickerDialog.OnDateSetListener dateSetListener = (view1, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // Show TimePickerDialog to select the time
            TimePickerDialog.OnTimeSetListener timeSetListener = (view2, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                // Set the alarm
                setAlarm(calendar);
            };

            // Show the TimePickerDialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    timeSetListener,
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        };

        // Show the DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void setAlarm(Calendar calendar) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        // You can put extra data in the intent if needed

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Set the alarm
        if (alarmManager != null) {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }

        // Notify the user that the alarm is set
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String alarmTime = sdf.format(calendar.getTime());
        Toast.makeText(
                this,
                "Alarm set for " + alarmTime,
                Toast.LENGTH_SHORT
        ).show();
    }
    public void addImageButton(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        //imagePickerLauncher.launch(String.valueOf(intent));
    }

}