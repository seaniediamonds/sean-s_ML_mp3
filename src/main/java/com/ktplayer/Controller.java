package com.ktplayer;

import com.jfoenix.controls.JFXSlider;
import com.mpatric.mp3agic.*;
import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.text.DecimalFormat;


public class Controller {

    @FXML
    private AnchorPane window;

    @FXML
    private AnchorPane playlistNode;

    @FXML
    private Pane showPlaylist;
    @FXML
    private Pane exit;
    @FXML
    private Pane minimize;
    @FXML
    private Pane imagePane;

    @FXML
    private TableView<Song> songTable;
    @FXML
    private TableColumn<Song, String> idColumn;
    @FXML
    private TableColumn<Song, String> artistNameColumn;
    @FXML
    private TableColumn<Song, String> songNameColumn;
    @FXML
    private TableColumn<Song, String> sizeColumn;
    @FXML
    private TableColumn<Song, String> durationColumn;
    @FXML
    private TableColumn<Song, String> albumColumn;


    @FXML
    private Label artistName;
    @FXML
    private Label albumName;
    @FXML
    private Label songName;
    @FXML
    private Label totalDuration;
    @FXML
    private Label currentDuration;
    @FXML
    private Label volumeValue;
    @FXML
    private Label songsCounter;

    @FXML
    private JFXSlider songSlider;
    @FXML
    private Slider volumeSlider;

    @FXML
    private ImageView folderChooser;

    @FXML
    private ImageView playButton;
    @FXML
    private ImageView pauseButton;
    @FXML
    private ImageView nextSongButton;
    @FXML
    private ImageView previousSongButton;
    @FXML
    private ImageView muteIcon;
    @FXML
    private ImageView volumeIcon;
    @FXML
    private ToggleButton autoPlayIcon;


    private Stage stage;

    private Main main;

    private ObservableList<Song> songs = FXCollections.observableArrayList();
    private MediaPlayer mediaPlayer;
    private int currentIndex = -1;

    private boolean isRepeat;
    private double volume = 0.1;

    private double xOffset = 0;
    private double yOffset = 0;

    private FadeTransition fadeIn = new FadeTransition();
    private FadeTransition fadeOut = new FadeTransition();


    public Controller() {
        isRepeat = false;
        stage = Main.getStage();
        stage.getIcons().add(new Image(ClassLoader.getSystemResource("images/logo.png").toExternalForm()));
    }

    @FXML
    private void initialize() {

        window.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            xOffset = stage.getX() - event.getScreenX();
            yOffset = stage.getY() - event.getScreenY();
        });

        window.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            stage.setX(event.getScreenX() + xOffset);
            stage.setY(event.getScreenY() + yOffset);
        });

        autoPlayIcon.selectedProperty().addListener((observable, oldValue, newValue) -> isRepeat = newValue);

        showPlaylist.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if(playlistNode.isVisible()) {
                hideTransition(playlistNode);
            }
            else {
                showTransition(playlistNode);
            }
        });

        minimize.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> stage.setIconified(true));

        exit.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> System.exit(0));

        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        artistNameColumn.setCellValueFactory(cellData -> cellData.getValue().artistNameProperty());
        songNameColumn.setCellValueFactory(cellData -> cellData.getValue().songNameProperty());
        sizeColumn.setCellValueFactory(cellData -> cellData.getValue().sizeProperty());
        durationColumn.setCellValueFactory(cellData -> cellData.getValue().durationProperty());
        albumColumn.setCellValueFactory(cellData -> cellData.getValue().albumProperty());

        songTable.setItems(songs);
        songTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showSongInfo(newValue));

        // Clicking a row loads that song; clicks on empty space or the header do nothing
        songTable.setRowFactory(table -> {
            TableRow<Song> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if(!row.isEmpty() && e.getClickCount() == 1) {
                    loadSong(row.getIndex(), false);
                }
            });
            return row;
        });

        showSongInfo(null);
        setImage();

        folderChooser.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            DirectoryChooser chooser = new DirectoryChooser();
            File selectedDirectory = chooser.showDialog(stage);
            if(selectedDirectory == null) {
                System.out.println("No directory selected!");
            }
            else {
                openFolder(selectedDirectory);
            }
        });

        playButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> play());

        pauseButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if((mediaPlayer != null) && (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING)) {
                mediaPlayer.pause();
            }
            showPlayButton();
        });

        nextSongButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if(mediaPlayer != null) {
                boolean wasPlaying = mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
                loadSong((currentIndex + 1) % songs.size(), wasPlaying);
            }
        });

        previousSongButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if(mediaPlayer != null) {
                mediaPlayer.seek(Duration.ZERO);
            }
        });

        // Seek when the user lets go of the song slider (after a click on the track or a thumb drag)
        songSlider.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if(mediaPlayer != null) {
                Duration total = mediaPlayer.getTotalDuration();
                if((total != null) && !total.isUnknown() && !total.isIndefinite()) {
                    mediaPlayer.seek(total.multiply(songSlider.getValue() / 100));
                }
            }
        });
        songSlider.setValueFactory(slider ->
                Bindings.createStringBinding(
                        () -> secToMin((long) sliderTime().toSeconds()),
                        songSlider.valueProperty()
                )
        );
        songSlider.setValue(0);

        volumeSlider.setValue(volume * 100);
        volumeValue.setText(String.valueOf((int) volumeSlider.getValue()));
        volumeIconChanger();
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            volume = newValue.doubleValue() / 100;
            if(mediaPlayer != null) {
                mediaPlayer.setVolume(volume);
            }
            volumeValue.setText(String.valueOf(newValue.intValue()));
            volumeIconChanger();
        });
    }

    public void showSongInfo(Song song) {
        if(song != null) {
            artistName.setText(song.getArtistName());
            songName.setText(song.getSongName());
            albumName.setText(song.getAlbum());
        }
        else {
            artistName.setText("-");
            songName.setText("-");
            albumName.setText("-");
        }

    }

    private void openFolder(File dir) {
        disposePlayer();
        currentIndex = -1;
        songs.setAll(readSongs(dir));
        showPlayButton();
        showSongInfo(null);
        currentDuration.setText(secToMin(0));
        totalDuration.setText(secToMin(0));
        songSlider.setValue(0);
        songsCounter.setText("Songs: " + songs.size());
    }

    public ObservableList<Song> readSongs(File dir) {
        ObservableList<Song> songData = FXCollections.observableArrayList();
        File[] files = dir.listFiles();
        if(files == null) {
            System.out.println("Cannot read directory " + dir);
            return songData;
        }
        int i = 0;
        for(File file : files) {
            String name = file.getName().toLowerCase();
            if(file.isFile() && (name.endsWith(".mp3") || name.endsWith(".wav"))) {
                i++;
                songData.add(readSong(file, i));
            }
        }
        return songData;
    }

    private Song readSong(File file, int id) {
        String artist = null;
        String title = null;
        String album = null;
        String duration = "";

        if(file.getName().toLowerCase().endsWith(".mp3")) {
            try {
                Mp3File mp3 = new Mp3File(file.getPath());
                ID3v1 tag = mp3.hasId3v2Tag() ? mp3.getId3v2Tag() : mp3.getId3v1Tag();
                if(tag != null) {
                    artist = tag.getArtist();
                    title = tag.getTitle();
                    album = tag.getAlbum();
                }
                duration = secToMin(mp3.getLengthInSeconds());
            }
            catch(Exception e) {
                System.out.println("Cannot read tags of " + file + ": " + e);
            }
        }
        else {
            try {
                AudioFileFormat format = AudioSystem.getAudioFileFormat(file);
                float frameRate = format.getFormat().getFrameRate();
                if((format.getFrameLength() > 0) && (frameRate > 0)) {
                    duration = secToMin((long) (format.getFrameLength() / frameRate));
                }
            }
            catch(Exception e) {
                System.out.println("Cannot read format of " + file + ": " + e);
            }
        }

        if((title == null) || title.trim().isEmpty()) {
            title = file.getName().substring(0, file.getName().lastIndexOf('.'));
        }
        return new Song(String.valueOf(id), artist, title, kbToMb(file.length()), duration, album, file.getAbsolutePath());
    }

    private void loadSong(int index, boolean autoPlay) {
        if((index < 0) || (index >= songs.size())) {
            return;
        }
        disposePlayer();

        Song song = songs.get(index);
        currentIndex = index;
        songTable.getSelectionModel().select(index);
        showSongInfo(song);
        showPlayButton();
        currentDuration.setText(secToMin(0));
        totalDuration.setText(song.getDuration().isEmpty() ? secToMin(0) : song.getDuration());
        songSlider.setValue(0);

        MediaPlayer player;
        try {
            player = new MediaPlayer(new Media(new File(song.getUrl()).toURI().toString()));
        }
        catch(MediaException e) {
            System.out.println("Cannot play " + song.getUrl() + ": " + e.getMessage());
            return;
        }
        mediaPlayer = player;
        player.setVolume(volume);

        // Every handler ignores events still queued for a player that has since been replaced
        player.setOnReady(() -> {
            if(player == mediaPlayer) {
                totalDuration.setText(secToMin((long) player.getTotalDuration().toSeconds()));
            }
        });

        player.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            if(player != mediaPlayer) {
                return;
            }
            currentDuration.setText(secToMin((long) newValue.toSeconds()));
            if(!songSlider.isValueChanging() && !songSlider.isPressed()) {
                updateSliderPosition(newValue);
            }
        });

        player.setOnEndOfMedia(() -> {
            if(player != mediaPlayer) {
                return;
            }
            if(isRepeat) {
                player.seek(Duration.ZERO);
                player.play();
            }
            else {
                loadSong((currentIndex + 1) % songs.size(), true);
            }
        });

        player.setOnError(() -> {
            System.out.println("Cannot play " + song.getUrl() + ": " + player.getError());
            if(player == mediaPlayer) {
                showPlayButton();
            }
        });

        if(autoPlay) {
            play();
        }
    }

    private void play() {
        if(mediaPlayer == null) {
            int selected = songTable.getSelectionModel().getSelectedIndex();
            loadSong(selected >= 0 ? selected : 0, false);
        }
        if(mediaPlayer != null) {
            mediaPlayer.play();
            showPauseButton();
        }
    }

    private void disposePlayer() {
        if(mediaPlayer != null) {
            // dispose() also stops playback; calling stop() first would queue a late stop event
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    public void setMain(Main main) {
        this.main = main;
    }


    public String kbToMb(long length) {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(((double) length / 1024) / 1024) + "Mb";
    }

    public String secToMin(long sec) {
        String time;
        if((sec % 60) < 10) {
            time = sec / 60 + ":0" + sec % 60;
        }
        else {
            time = sec / 60 + ":" + sec % 60;
        }
        return time;
    }

    public void showPauseButton() {
        playButton.setVisible(false);
        playButton.setDisable(true);
        pauseButton.setVisible(true);
        pauseButton.setDisable(false);
    }

    public void showPlayButton() {
        pauseButton.setVisible(false);
        pauseButton.setDisable(true);
        playButton.setVisible(true);
        playButton.setDisable(false);
    }

    private Duration sliderTime() {
        if(mediaPlayer == null) {
            return Duration.ZERO;
        }
        Duration total = mediaPlayer.getTotalDuration();
        if((total == null) || total.isUnknown() || total.isIndefinite()) {
            return Duration.ZERO;
        }
        return total.multiply(songSlider.getValue() / 100);
    }

    private void updateSliderPosition(Duration currentTime) {
        final Duration total = mediaPlayer.getTotalDuration();
        if((total == null) || (currentTime == null) || total.isUnknown() || total.isIndefinite() || (total.toMillis() <= 0)) {
            songSlider.setValue(0);
        }
        else {
            songSlider.setValue((currentTime.toMillis() / total.toMillis()) * 100);
        }
    }

    private void volumeIconChanger() {
        if(volumeSlider.getValue() == 0) {
            muteIcon.setVisible(true);
            volumeIcon.setVisible(false);
        }
        else {
            muteIcon.setVisible(false);
            volumeIcon.setVisible(true);
        }
    }

    private void transitionOperation(AnchorPane anchorPane, FadeTransition fadeTransition, boolean isShowing) {
    	fadeTransition.setNode(anchorPane);
        fadeTransition.setDuration(Duration.millis(1000));
        fadeTransition.setFromValue(isShowing ? 0.0 : 1.0);
        fadeTransition.setToValue(isShowing ? 1.0 : 0.0);
        anchorPane.setVisible(isShowing);
        fadeTransition.play();
    }

    private void showTransition(AnchorPane anchorPane) {
        transitionOperation(anchorPane, fadeIn, true);
    }

    private void hideTransition(AnchorPane anchorPane) {
        transitionOperation(anchorPane, fadeOut, false);
    }

    private void setImage() {
        String path = ClassLoader.getSystemResource("images/Question.PNG").toExternalForm();

        imagePane.setStyle("-fx-background-image: url(\"" + path + "\"); " +
                "-fx-background-position: center center; " +
                "-fx-background-repeat: stretch;");

    }
}
