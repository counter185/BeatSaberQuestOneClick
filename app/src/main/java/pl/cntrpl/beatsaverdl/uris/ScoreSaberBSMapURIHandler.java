package pl.cntrpl.beatsaverdl.uris;

import android.net.Uri;

import java.util.zip.ZipFile;

import pl.cntrpl.beatsaverdl.utils.HTTPUtil;

public class ScoreSaberBSMapURIHandler extends BSURIHandler {

    final String beatSaverAPIMapByHashURL = "https://api.beatsaver.com/maps/hash/";

    ScoreSaberBSMapURIHandler(Uri uri) {
        this.uri = uri;
    }

    public static ScoreSaberBSMapURIHandler create(Uri uri) { return new ScoreSaberBSMapURIHandler(uri); }

    @Override
    public ZipFile download() {
        try {
            String hash = uri.getHost();
            String fullUrl = beatSaverAPIMapByHashURL + hash;
            String responseJson = HTTPUtil.readText(fullUrl);
            return BeatSaverURIHandler.evalBeatSaverJson(responseJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
