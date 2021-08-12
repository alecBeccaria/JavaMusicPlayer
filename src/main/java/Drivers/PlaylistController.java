package Drivers;

import java.io.File;
import java.util.ArrayList;

public class PlaylistController {
    private static ArrayList<Playlist> rootPlaylist = new ArrayList<>();

    //Creates a playlist and adds it to rootPlaylist Array
    public static void createNewPlaylist(String playlistName) {
        Playlist p = new Playlist(playlistName);
        rootPlaylist.add(p);
    }

    public static void importPlaylist(String playlistPath) {
        File newPlaylistFile = new File(playlistPath);
        Playlist newPlaylist = new Playlist(newPlaylistFile);
        rootPlaylist.add(newPlaylist);
    }

    //Returns rootPlaylist as ArrayList
    public static ArrayList<Playlist> getRootPlaylist() {
        return rootPlaylist;
    }

    //Loads target serialized playlist file
    public static void loadRootPlaylist(String playlistPath) {
        rootPlaylist =  readWrite.deserializePlaylist(playlistPath);
    }

    //Saves rootPlaylist as a serialized file to target directory
    public static void saveRootPlaylist(String playlistPath) {
        readWrite.writeToDirectory(rootPlaylist, playlistPath);
    }

    //Lists all of the playlists in the rootPlaylist Array
    public static String readAndListPlayLists() {
        StringBuilder sb = new StringBuilder();
        for(Playlist p: rootPlaylist){
            sb.append(p.getPlaylistName());
            sb.append("\n");
        }
        return sb.toString();
    }

    //returns a single Playlist by passing in a playlist name
    public static Playlist readSinglePlaylist(String nameOfPlayList){

        for (Playlist p: rootPlaylist){
            if (p.getPlaylistName().equals(nameOfPlayList)){
                return p;
            }else {
                throw new IllegalArgumentException("Playlist " + nameOfPlayList + " does not exist");
            }
        }
        return null;
    }

    //deletes a playlist, no matter how many levels of sub-folders it contains
    public static void deletePlaylist(String nameOfPlayList){
        for (Playlist p: rootPlaylist){
            if (p.getPlaylistName().equals(nameOfPlayList)){
                rootPlaylist.remove(p);
                break;
            }
        }
    }
}
