package pl.cntrpl.beatsaverdl.uris;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.zip.ZipFile;

import pl.cntrpl.beatsaverdl.utils.HTTPUtil;

public class BeatSaverURIHandler extends BSURIHandler {
    final String beatSaverAPIURL = "https://api.beatsaver.com/maps/id/";

    BeatSaverURIHandler(Uri uri) {
        this.uri = uri;
    }

    public static BeatSaverURIHandler create(Uri uri) { return new BeatSaverURIHandler(uri); }

    public static ZipFile evalBeatSaverJson(String responseJson) throws IOException {
        try {
            if (responseJson != null) {
                JSONObject jsonObject = new JSONObject(responseJson);
                JSONArray versionsArray = jsonObject.getJSONArray("versions");
                if (versionsArray.length() > 0) {
                    JSONObject firstVersion = versionsArray.getJSONObject(0);
                    String downloadURL = firstVersion.getString("downloadURL");
                    String tempFilePath = HTTPUtil.downloadZipFromURL(downloadURL);

                    if (tempFilePath != null) {
                        return new ZipFile(tempFilePath);
                    }
                } else {
                    System.out.println("No versions found");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ZipFile download() {
        try {
            String id = uri.getHost();
            String fullUrl = beatSaverAPIURL + id;
            String responseJson = HTTPUtil.readText(fullUrl);
            return evalBeatSaverJson(responseJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}