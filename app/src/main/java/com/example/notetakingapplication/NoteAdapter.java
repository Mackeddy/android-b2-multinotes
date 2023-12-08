package com.example.notetakingapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class NoteAdapter extends ArrayAdapter<Note> {
    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
    private LayoutInflater inflater;

    public NoteAdapter(Context context, List<Note> notes) {
        super(context, 0, notes);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Note note = getItem(position);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.note_cell, parent, false);
        }

        TextView title = convertView.findViewById(R.id.cellTitle);
        TextView description = convertView.findViewById(R.id.cellDesc);
        TextView createdDate = convertView.findViewById(R.id.cellCreatedDate);

        title.setText(note.getTitle());
        description.setText(note.getDescription());

        // Format LocalDateTime to String before setting to TextView
        String formattedDate = note.getCreated().format(dateTimeFormatter);
        createdDate.setText(formattedDate);

        return convertView;
    }
}
