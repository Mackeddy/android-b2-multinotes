package com.example.notetakingapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private ListView noteListview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWidgets();
        loadFromDBToMemory();
        setNoteAdapter();
        setOnClickListener();
    }

    private void setOnClickListener() {
            noteListview.setOnItemClickListener((parent, view, position, id) -> {
                Object item = noteListview.getItemAtPosition(position);
                if (item instanceof Note) {
                    Note selectedNote = (Note) item;
                    Intent editNoteIntent = new Intent(getApplicationContext(), NoteDetailActivity.class);
                    editNoteIntent.putExtra(Note.NOTE_EDIT_EXTRA, selectedNote.getId());
                    startActivity(editNoteIntent);
                }
            });
    }

    private void loadFromDBToMemory() {
        SQLiteManager sqLiteManager = SQLiteManager.instanceOfDatabase(this);
        sqLiteManager.populateNoteListArray();
    }

    private void initWidgets() {
        noteListview = findViewById(R.id.noteListView);
    }

    private void setNoteAdapter() {
        NoteAdapter noteAdapter = new NoteAdapter(getApplicationContext(), Note.nonDeletedNotes());
        noteListview.setAdapter(noteAdapter);
    }

    public void newNote(View view) {
        Intent newNoteIntent = new Intent(this, NoteDetailActivity.class);
        startActivity(newNoteIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setNoteAdapter();
    }
}
