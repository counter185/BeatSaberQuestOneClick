package pl.cntrpl.beatsaverdl.utils;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class BSZipUtil {

    public static JSONObject getInfoDat(ZipFile zip) {
        //find zip entry
        ZipEntry infoDatEntry = null;
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().toLowerCase().equals("info.dat")) {
                infoDatEntry = entry;
                break;
            }
        }

        if (infoDatEntry != null) {
            try (InputStream inputStream = zip.getInputStream(infoDatEntry)) {
                String content = HTTPUtil.textFromStream(inputStream);
                if (content != null && !content.isEmpty()) {
                    return new JSONObject(content);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("info.dat not found in zip");
        }
        return null;
    }
}
