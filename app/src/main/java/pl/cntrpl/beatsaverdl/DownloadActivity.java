package pl.cntrpl.beatsaverdl;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import pl.cntrpl.beatsaverdl.uris.BSURIHandler;
import pl.cntrpl.beatsaverdl.utils.BSZipUtil;
import pl.cntrpl.beatsaverdl.utils.HTTPUtil;

public class DownloadActivity extends Activity {

    static final String customLevelPath = "/sdcard/ModData/com.beatgames.beatsaber/Mods/SongCore/CustomLevels/";

    public static String tempDownloadsDir = null;

    class DownloadAndInstallTask extends Thread {
        private Uri uri;
        private Activity context;

        public DownloadAndInstallTask(Activity caller, Uri uri) {
            this.uri = uri;
            this.context = caller;
        }

        @Override
        public void run() {
            Looper.prepare();
            BSURIHandler uriHandler = BSURIHandler.getUriHandlerForUri(uri);
            if (uriHandler != null) {
                try {
                    ZipFile zip = uriHandler.download();
                    if (zip != null) {
                        processZip(context, zip);
                        zip.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            context.finish();
            Looper.loop();
            Looper.myLooper().quitSafely();

        }
    }
    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view for the download activity
        setContentView(R.layout.activity_download);
        createNotifChannel();

        tempDownloadsDir = getExternalCacheDir().getAbsolutePath();

        if (!Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } else {

            Intent intent = getIntent();
            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                Uri uri = intent.getData();
                Toast.makeText(this, "Download started: " + uri.getHost(), Toast.LENGTH_LONG).show();

                new DownloadAndInstallTask(this, uri).start();
            }
        }
    }

    public void createNotifChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "BeatSaverDL notifications";
            String description = "";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("bsdl_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void postNotification(int notif_id, Context caller, String title, String message) {
        Notification.Builder builder = new Notification.Builder(caller, "bsdl_channel")
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher_round)   //doesn't matter because the quest doesn't show the icon anyway
                .setAutoCancel(true);
        NotificationManager notificationManager = (NotificationManager)caller.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(notif_id, builder.build());
        }
    }

    public static boolean processZip(Context caller, ZipFile zip) {
        File f = new File(zip.getName());
        try {
            if (zip != null) {
                String filename = f.getName();
                String subDirName = f.getName().substring(0, f.getName().lastIndexOf('.'));

                JSONObject infoDat = BSZipUtil.getInfoDat(zip);
                if (infoDat != null) {
                    try {
                        subDirName = "bsdl-"
                                + subDirName.substring(0,6) + " "
                                + infoDat.getString("_songAuthorName") + " - "
                                + infoDat.getString("_songName")
                                + " ["+infoDat.getString("_levelAuthorName")+"]";
                    } catch (Exception e) { e.printStackTrace(); }
                }

                subDirName = subDirName.replaceAll("[\\\\/:*?\"<>|]", "_"); //sanitize folder name
                String newDir = customLevelPath + "/" + subDirName;
                //unpack zip to this dir
                try {
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();

                        String entryPath = newDir + "/" + entry.getName();

                        if (entry.isDirectory()) {
                            new File(entryPath).mkdirs();
                        } else {
                            File file = new File(entryPath);
                            if (!file.getParentFile().exists()) {
                                file.getParentFile().mkdirs();
                            }
                            try (InputStream in = zip.getInputStream(entry);
                                 OutputStream out = new FileOutputStream(file)) {
                                out.write(in.readAllBytes());
                            }
                        }
                    }

                    //optionally: parse info.dat as a json and rename the folder
                    System.out.println("Unpack completed to: " + newDir);
                    postNotification(1, caller, "Download completed", "Unpacked: " + subDirName);
                    Toast.makeText(caller, "Download completed: " + newDir, Toast.LENGTH_LONG).show();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error extracting zip");
                    postNotification(2, caller, "Error extracting zip", e.getMessage());
                    Toast.makeText(caller, "Error extracting zip: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    return false;
                }
            }
            return false;
        }
        finally {
            if (f.exists()) {
                f.delete();
            }
        }
    }
}
