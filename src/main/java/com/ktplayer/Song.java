package com.ktplayer;

import javafx.beans.property.*;
import javafx.scene.image.Image;

public class Song {
    private StringProperty id;
    private StringProperty artistName;
    private StringProperty songName;
    private StringProperty size;
    private StringProperty duration;
    private StringProperty album;
    private StringProperty url;
    private Image image;

    public Song() {}

    public Song(String url) {
        this.url = new SimpleStringProperty(url);
    }

    public Song(String id, String artistName, String songName, String size, String duration, String album, String url) {
        this.id = new SimpleStringProperty(id);
        this.artistName = new SimpleStringProperty(artistName);
        this.songName = new SimpleStringProperty(songName);
        this.size = new SimpleStringProperty(size);
        this.duration = new SimpleStringProperty(duration);
        this.album = new SimpleStringProperty(album);
        this.url = new SimpleStringProperty(url);
    }

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getArtistName() {
        return artistName.get();
    }

    public void setArtistName(String artistName) {
        this.artistName.set(artistName);
    }

    public StringProperty artistNameProperty() {
        return artistName;
    }

    public String getSongName() {
        return songName.get();
    }

    public void setSongName(String songName) {
        this.songName.set(songName);
    }

    public StringProperty songNameProperty() {
        return songName;
    }

    public String getSize() {
        return size.get();
    }

    public void setSize(String size) {
        this.size.set(size);
    }

    public StringProperty sizeProperty() {
        return size;
    }

    public String getDuration() {
        return duration.get();
    }

    public StringProperty durationProperty() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration.set(duration);
    }

    public String getAlbum() {
        return album.get();
    }

    public StringProperty albumProperty() {
        return album;
    }

    public void setAlbum(String album) {
        this.album.set(album);
    }

    public String getUrl() {
        return url.get();
    }

    public StringProperty urlProperty() {
        return url;
    }

    public void setUrl(String url) {
        this.url.set(url);
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
