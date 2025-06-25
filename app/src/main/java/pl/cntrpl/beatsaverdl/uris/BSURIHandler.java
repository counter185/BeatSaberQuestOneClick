package pl.cntrpl.beatsaverdl.uris;

import static java.util.Map.entry;

import android.net.Uri;

import java.util.Map;
import java.util.function.Function;
import java.util.zip.ZipFile;

public abstract class BSURIHandler {

    protected Uri uri;

    static Map<String, Function<Uri, BSURIHandler>> creationMap = Map.ofEntries(
        entry("beatsaver", BeatSaverURIHandler::create),
        entry("web+bsmap", ScoreSaberBSMapURIHandler::create)
    );

    public static BSURIHandler getUriHandlerForUri(Uri uri) {
        String scheme = uri.getScheme();
        if (scheme == null || !creationMap.containsKey(scheme)) {
            return null;
        } else {
            return creationMap.get(scheme).apply(uri);
        }
    }

    public ZipFile download() {
        throw new RuntimeException("Not implemented");
    }
}
