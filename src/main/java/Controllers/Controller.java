package Controllers;

import Drivers.Playlist;
import Drivers.PlaylistController;
import Drivers.Song;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.beans.InvalidationListener;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;
import com.java.shuffleArrayList;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private MediaView mediaView;
    private MediaPlayer mediaPlayer;
    private Media media;
    @FXML
    private Button pausePlay;
    @FXML
    private Button repeat;
    @FXML
    private Slider sliderVolume;
    @FXML
    private ListView<String> playlistView;
    @FXML
    private ListView<String> songView;
    @FXML
    private TextArea metaDisplay;
    @FXML
    private Slider sliderSeeker;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;
    @FXML
    private ImageView albumArt;
    @FXML
    private Label timeStamp;
    @FXML
    private FontAwesomeIconView pausePlayIcon;
    @FXML
    private FontAwesomeIconView repeatIcon;

    @FXML
    private Button shuffleButton;
    @FXML
    private Button unshuffleButton;
    @FXML
    private FontAwesomeIconView shuffleIcon;

    int repeatAmt = 1;
    boolean isPaused = true;
    File currentSong;
    int playListIndex = 0;
    int songIndex = 0;
    boolean changing = false;
    int changedSlider = 0;
    String saveSerialized = "playlists.txt";
    Playlist currentPlaylist;
    ArrayList<Song> originalPlaylist;
    double currentVolume = 1.0;
    Image defaultImage = new Image(getClass().getResourceAsStream("noteIMG.png"));
    boolean shuffle = false;
    File choiceFile = null;
    boolean initialize = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        File test = new File(PlaylistController.getRootPlaylistPath());
        if (!test.isDirectory()) {
            test.mkdirs();
        }

        File init = new File(saveSerialized);
        try {
            if(init.createNewFile()){
                System.out.println("Playlist storage created: " + init.getAbsolutePath());
            }else{
                System.out.println("File already exists.");
                PlaylistController.loadRootPlaylist(saveSerialized);
                if(PlaylistController.getRootPlaylist() != null){
                    updatePlaylists();
                    loadPlaylistItems();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("Starting up!");
        pausePlayIcon.setIcon(FontAwesomeIcon.PAUSE);
        albumArt.setImage(defaultImage);
    }

    public void initPlaylist(){
        ArrayList<Playlist> p = PlaylistController.getRootPlaylist();
        ArrayList<Song> s = null;
        boolean loop = true;
        while (loop){
            ArrayList<Song> temp = p.get(playListIndex).getPlayList();
            if (temp.size() != 0) {
                loop = false;
                s = temp;
            }else {
                playListIndex++;
            }
        }

        currentPlaylist = new Playlist(p.get(playListIndex).getPlaylistFile(), playListIndex);
        currentSong = s.get(songIndex).getSong();
        media = new Media (currentSong.toURI().toString());
        loadMetaData(media);
        mediaPlayer = new MediaPlayer(media);
        setOnEndOfMedia();
        mediaView.setMediaPlayer(mediaPlayer);

        sliderVolume.setValue(mediaPlayer.getVolume() * 100);
        sliderVolume.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                mediaPlayer.setVolume(sliderVolume.getValue() / 100);
                currentVolume = sliderVolume.getValue() / 100;
            }
        });

        ArrayList<Song> songs = new ArrayList<Song>();

        if(shuffle){
            shuffleCurrentPlaylist(true);
        }else{
            songs = currentPlaylist.getSongs();
            for (Song s1 : songs){
                songView.getItems().add(s1.getTitle());
            }
        }

        loadMusicSeeker();
        updatePlaylists();
    }


    @FXML
    public void onClickTableItem(MouseEvent mouse) {

        if(!initialize){
            initPlaylist();
            initialize = true;
        }

        String name = playlistView.getSelectionModel().getSelectedItem();
        if (name != null && !name.equals(currentPlaylist.getPlaylistName())){
            songView.getItems().clear();
            ArrayList<Playlist> playlist = PlaylistController.getRootPlaylist();
            ArrayList<Song> songs = new ArrayList<>();
            for (Playlist p: playlist){
                if (p.getPlaylistName().equals(name)){
                    playListIndex = p.getPlaylistIndex();
                    songIndex = 0;
                    loadMusicSeeker();
                    setOnEndOfMedia();
                    currentPlaylist = p;
                    if (shuffle){
                        shuffleCurrentPlaylist(true);
                    }else{
                        songs = p.getSongs();
                        for (Song s: songs){
                            songView.getItems().add(s.getTitle());
                        }
                    }
                }
            }
        }
    }

    @FXML
    public void onClickSongItem(MouseEvent mouse) {

        isPaused = true;
        albumArt.setImage(null);
        mediaPlayer.pause();
        String name = songView.getSelectionModel().getSelectedItem();
        if(name != null) {
            ArrayList<Song> songs = currentPlaylist.getSongs();
            for (Song s: songs){
                if(s.getTitle().equals(name)) {
                    currentSong = s.getSong();
                    media = new Media(currentSong.toURI().toString());
                    loadMetaData(media);
                    mediaPlayer = new MediaPlayer(media);
                    setOnEndOfMedia();
                    mediaPlayer.setVolume(sliderVolume.getValue() / 100);
                    mediaView.setMediaPlayer(mediaPlayer);
                    isPaused = false;
                    pausePlayIcon.setIcon(FontAwesomeIcon.PLAY);
                    mediaPlayer.play();
                    loadMusicSeeker();
                    songIndex = s.getIndex();
                }
            }
        }
    }


    public void loadMetaData(Media media) {
        albumArt.setImage(defaultImage);
        StringBuilder sb = new StringBuilder();
        media.getMetadata().addListener((MapChangeListener.Change<? extends String, ? extends Object > change) -> {
            if(change.wasAdded()){
                if("title".equals(change.getKey())){
                    sb.append("Title: ").append(change.getValueAdded().toString()).append("\n");
                }else if("artist".equals(change.getKey())){
                    sb.append("Artist: ").append(change.getValueAdded().toString()).append("\n");
                }else if("album".equals(change.getKey())){
                    sb.append("Album: ").append(change.getValueAdded().toString()).append("\n");
                }else if("image".equals(change.getKey())){
                    albumArt.setImage((Image) change.getValueAdded());
                }
                metaDisplay.setText(sb.toString());
            }
        });

    }

    public void loadPlaylistItems() {
        ArrayList<Playlist> playlists = PlaylistController.getRootPlaylist();
        ObservableList<String> list = FXCollections.observableArrayList();
        for (Playlist p : playlists){
            list.add(p.getPlaylistName());
        }
        playlistView.setItems(list);
    }

    public void updatePlaylists() {
        PlaylistController.importAllPlaylists();
        ArrayList<Playlist> temp = PlaylistController.getRootPlaylist();
        for(Playlist p: temp){
            playListIndex = p.getPlaylistIndex();
        }
        PlaylistController.saveRootPlaylist(saveSerialized);
        PlaylistController.loadRootPlaylist(saveSerialized);
        loadPlaylistItems();
    }

    @FXML
    protected void refreshPlaylistItems() {
        songView.getItems().clear();
        updatePlaylists();
        currentPlaylist = PlaylistController.getRootPlaylist().get(playListIndex);
        ArrayList<Song> songs = currentPlaylist.getSongs();
        for (Song s: songs){
            songView.getItems().add(s.getTitle());
        }
    }

    @FXML
    protected void onPauseButtonPressed() {
        isPaused = !isPaused;
        if (isPaused) {
            mediaPlayer.pause();
            pausePlayIcon.setIcon(FontAwesomeIcon.PAUSE);

        }
        else {
            mediaPlayer.play();
            pausePlayIcon.setIcon(FontAwesomeIcon.PLAY);
        }
    }

    @FXML
    protected void onFileButtonPressed() {
        File test = new File(PlaylistController.getRootPlaylistPath());
        File playlistPath;
        if (test.isDirectory()) {
            playlistPath = test;
        }else {
            test.mkdirs();
            playlistPath = test;
        }
        DirectoryChooser dc = new DirectoryChooser();
        boolean loop = true;
        while (loop) {
            if (playlistPath != null) {
                loop = false;
                dc.setInitialDirectory(playlistPath);
            }else {
                playlistPath.mkdirs();
            }

        }
        dc.setTitle("Create or Select Playlist");
        try {
            choiceFile = dc.showDialog(null);
            if (choiceFile != null){

                PlaylistController.importPlaylist(choiceFile);
                PlaylistController.saveRootPlaylist(saveSerialized);
            }
            updatePlaylists();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("No File was Selected");
        }
    }

    @FXML
    public void onRepeatButtonPressed(ActionEvent actionEvent) {
        repeatAmt++;
        switch (repeatAmt) {
            case 1:
                //Repeat Arraylist
                repeat.setText("");
                repeatIcon.setIcon(FontAwesomeIcon.REPEAT);
                break;
            case 2:
                //Repeat Song
                repeat.setText("R2");
                break;
            default:
                //No repeat
                repeat.setText("");
                repeatIcon.setIcon(FontAwesomeIcon.TIMES);
                repeatAmt = 0;
        }
    }


    @FXML
    void changeToNextSong(ActionEvent event) {
        if(!((songIndex + 1) > (currentPlaylist.getSongs().size() - 1))){
            songIndex ++;
            currentSong = currentPlaylist.getSongs().get(songIndex).getSong();
            if(currentSong.getPath().contains(" ")){
                String formattedPath = currentSong.toURI().getPath();
                formattedPath = formattedPath.replaceAll(" ", "%20");

                //Do NOT remove file:// from the beginning of this - Media expects a url to a resource, not a direct file path. The format here is required for it to
                //function.
                media = new Media("file://" + formattedPath);
            }else{
                media = new Media("file://" + currentSong.toURI().getPath());
            }
        }else{
            songIndex = 0;
            currentSong = currentPlaylist.getSongs().get(songIndex).getSong();
            if(currentSong.getPath().contains(" ")){
                String formattedPath = currentSong.toURI().getPath();
                formattedPath = formattedPath.replaceAll(" ", "%20");

                //Do NOT remove file:// from the beginning of this - Media expects a url to a resource, not a direct file path. The format here is required for it to
                //function.
                media = new Media("file://" + formattedPath);
            }else{
                media = new Media("file://" + currentSong.toURI().getPath());
            }
        }

        mediaPlayer.stop();
        mediaPlayer.dispose();
        mediaPlayer = new MediaPlayer(media);
        loadMetaData(media);
        loadMusicSeeker();
        setOnEndOfMedia();
        mediaPlayer.setVolume(sliderVolume.getValue() / 100);
        pausePlayIcon.setIcon(FontAwesomeIcon.PLAY);
        mediaPlayer.play();
        songView.getSelectionModel().select(songIndex);
    }

    @FXML
    void changeToPrevSong(ActionEvent event) {

        if(!((songIndex - 1) < 0)){
            songIndex--;
            currentSong = currentPlaylist.getSongs().get(songIndex).getSong();
            if(currentSong.getPath().contains(" ")){
                String formattedPath = currentSong.toURI().getPath();
                formattedPath = formattedPath.replaceAll(" ", "%20");

                //Do NOT remove file:// from the beginning of this - Media expects a url to a resource, not a direct file path. The format here is required for it to
                //function.
                media = new Media("file://" + formattedPath);
            }else{
                media = new Media("file://" + currentSong.toURI().getPath());
            }
        }else{
            songIndex = currentPlaylist.getSongs().size() - 1;
            currentSong = currentPlaylist.getSongs().get(songIndex).getSong();
            if(currentSong.getPath().contains(" ")){
                String formattedPath = currentSong.toURI().getPath();
                formattedPath = formattedPath.replaceAll(" ", "%20");

                //Do NOT remove file:// from the beginning of this - Media expects a url to a resource, not a direct file path. The format here is required for it to
                //function.
                media = new Media("file://" + formattedPath);
            }else{
                media = new Media("file://" + currentSong.toURI().getPath());
            }
        }
        mediaPlayer.stop();
        mediaPlayer.dispose();
        mediaPlayer = new MediaPlayer(media);
        loadMetaData(media);
        loadMusicSeeker();
        setOnEndOfMedia();
        mediaPlayer.setVolume(sliderVolume.getValue() / 100);
        pausePlayIcon.setIcon(FontAwesomeIcon.PLAY);
        mediaPlayer.play();
        songView.getSelectionModel().select(songIndex);
    }




    public void loadMusicSeeker() {
        mediaPlayer.setOnReady(() -> {
            sliderSeeker.setMax(0.0);
            sliderSeeker.setMax(mediaPlayer.getTotalDuration().toSeconds());
            // know when the slider value is actually changing
            // and only seek when it has stopped changing
            sliderSeeker.valueChangingProperty().addListener(
                    (observableValue, aBoolean, t1) -> {
                        changing = t1;
                        if (!changing) {
                            mediaPlayer.seek(Duration.seconds(changedSlider));

                        }
                    });
            // Save the slider value when changing, but don't seek yet
            // When slider not changing check the difference between
            // prev and current values.
            // For player initiated changes the delta will be small,
            // but for mouse clicks on the slider the delta will be much
            // larger, and in this case do a seek.
            sliderSeeker.valueProperty().addListener(
                    (observableValue, number, t1) -> {
                        if (changing) {
                            changedSlider = t1.intValue();
                        } else {
                            int change = Math.abs(t1.intValue() - number.intValue());
                            if (change > 1) {
                                mediaPlayer.seek(Duration.seconds(t1.intValue()));
                            }
                        }
                    });
            // set slider on player media progress,
            // but only when slider is not changing
            mediaPlayer.currentTimeProperty().addListener(
                    (observableValue, duration, t1) -> {
                        if (!changing) {
                            sliderSeeker.setValue(t1.toSeconds());
                        }
                        timeStamp.setText(formatTime(t1.toSeconds()) + "/" + formatTime(mediaPlayer.getTotalDuration().toSeconds()));
                    });
        });
    }

    //Built this because everytime we created a new musicplayer object, the listener would be nuked and we'd have no replay functionality.
    //Use whenever creating a new musicplayer object.
    public void setOnEndOfMedia(){
        mediaPlayer.setOnEndOfMedia(() -> {
            switch (repeatAmt) {
                case 1:
                    //Repeat Arraylist
                    //Maybe consider this being separate from the end of the song.
                    System.out.println("Playlist Check");
                    mediaPlayer.dispose();

                    if(!((songIndex + 1) > currentPlaylist.getSongs().size() - 1)){
                        songIndex ++;
                    }else{
                        songIndex = 0;
                    }
                    currentSong = currentPlaylist.getSongs().get(songIndex).getSong();
                    media = new Media(currentPlaylist.getSongs().get(songIndex).getSong().toURI().toString());
                    mediaPlayer = new MediaPlayer(media);
                    loadMetaData(media);
                    loadMusicSeeker();
                    mediaPlayer.setVolume(sliderVolume.getValue() / 100);
                    //pausePlayIcon.setIcon(FontAwesomeIcon.PLAY);
                    mediaPlayer.play();
                    setOnEndOfMedia();
                    songView.getSelectionModel().select(songIndex);
                    break;
                case 2:
                    //Repeat Song
                    mediaPlayer.seek(Duration.ZERO);
                    break;
                default:
                    //No repeat

                    if(!((songIndex + 1) > currentPlaylist.getSongs().size() - 1)){
                        songIndex ++;
                        currentSong = currentPlaylist.getSongs().get(songIndex).getSong();
                        media = new Media(currentPlaylist.getSongs().get(songIndex).getSong().toURI().toString());
                        mediaPlayer = new MediaPlayer(media);
                        loadMetaData(media);
                        mediaPlayer.setVolume(sliderVolume.getValue() / 100);
                        loadMusicSeeker();
                        //pausePlayIcon.setIcon(FontAwesomeIcon.PLAY);
                        mediaPlayer.play();
                        songView.getSelectionModel().select(songIndex);
                    }
                    break;
            }
        });

    }

    public String formatTime(double seconds) {
        int hours = (int) (seconds / 60 / 60);
        int mins = (int) (seconds / 60 % 60);
        int secs = (int) (seconds % 60);
        String result = formatInt(mins) + ":" + formatInt(secs);
        if (hours > 0) result = formatInt(hours) + ":" + result;
        return result;
    }
    public String formatInt(int num) {
        return String.format("%2s", num).replace(' ', '0');
    }

    @FXML
    protected void onClickShuffle(){
        shuffle = !shuffle;
        shuffleCurrentPlaylist(shuffle);
    }

    public void shuffleCurrentPlaylist(boolean shuffle){
        if (shuffle) {
            shuffleIcon.setIcon(FontAwesomeIcon.RANDOM);
            shuffleArrayList<Song> shuffler = new shuffleArrayList();
            originalPlaylist = (ArrayList<Song>) currentPlaylist.getSongs().clone();
            shuffler.shuffleList(currentPlaylist.getSongs());

            songView.getItems().clear();
            ArrayList<Song> songs = currentPlaylist.getSongs();

            for(Song s: songs){
                songView.getItems().add(s.getTitle());
            }

        } else {
            shuffleIcon.setIcon(FontAwesomeIcon.BAN);
            currentPlaylist.setSongs(originalPlaylist);
            songView.getItems().clear();
            ArrayList<Song> songs = currentPlaylist.getSongs();

            for(Song s: songs){
                songView.getItems().add(s.getTitle());
            }
        }
    }



}