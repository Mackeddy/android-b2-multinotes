package com.example.notetakingapplication;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Note {
    public static List<Note> noteArrayList = new ArrayList<>();
    public static String NOTE_EDIT_EXTRA = "noteEdit";
    private int id;
    private String title;
    private String description;
    private Date deleted;
    private LocalDateTime created;
    private byte[] image;
    public Note(int id, String title, String description,  LocalDateTime created, Date deleted, byte[] image) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.deleted = deleted;
        this.created = created;
        this.image = image;
    }

    public Note(int id, String title, String description, LocalDateTime created, byte[] image) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.created = created;
        this.deleted = null;
        this.image = image;
    }

    public Note() {
    }

    public static Note getNoteForID(int passedNoteID) {
        for (Note note : noteArrayList) {
            if (note.getId() ==  passedNoteID) {
                return note;
            }
        }
        return null;
    }

    public static List<Note> nonDeletedNotes() {
        List<Note> nonDeleted = new ArrayList<>();
        for (Note note : noteArrayList) {
            if (note.getDeleted() == null)
                nonDeleted.add(note);
        }
        return nonDeleted;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDeleted() {
        return deleted;
    }

    public void setDeleted(Date deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}