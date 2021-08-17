package Drivers;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.media.Media;
import javafx.util.Duration;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.net.URL;

public class Song implements Serializable {
    @Serial
    private static final long serialVersionUID = 4456821371902977809L;
    File song;
    String filePath;
    String title;

    public Song(File song) {
        setSong(song);
        setFilePath(song.getAbsolutePath());
        setTitle(song.getName());
    }


    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {

        this.title = title;
    }

    public File getSong() {
        return song;

    }

    public String getFilePath() {
        return filePath;
    }


    public void setSong(File song) {
        if(song != null){
            this.song = song;

        }else {
            throw new IllegalArgumentException("Song file cannot be null");
        }

    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
