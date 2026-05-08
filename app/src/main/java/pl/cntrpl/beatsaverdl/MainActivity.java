package pl.cntrpl.beatsaverdl;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Looper;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.compose.runtime.internal.ThreadMap;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class MainActivity extends Activity {

    class ThreadMapList extends Thread {

        MainActivity caller = null;
        public ThreadMapList(MainActivity caller) {
            this.caller = caller;
        }

        @Override
        public void run() {
            super.run();
            Looper.prepare();

            try {
                Stream<Path> dirList = Files.list(Paths.get(DownloadActivity.customLevelPath));
                dirList.forEach((Path p) -> {
                    File f = p.toFile();
                    Path infoPath = Paths.get(p.toString(), "info.dat");
                    if (f.isDirectory()
                            && infoPath.toFile().exists()) {

                        //textArtist.setText("");
                        //textLevelAuthors.setText("");
                        //textName.setText(f.getName());
                        try {
                            String jsonData = String.join("", Files.readAllLines(infoPath));
                            JSONObject jsonObject = new JSONObject(jsonData);
                            String songAuthor = jsonObject.getString("_songAuthorName");
                            String songName = jsonObject.getString("_songName");
                            String levelAuthor = jsonObject.getString("_levelAuthorName");
                            String coverName = jsonObject.getString("_coverImageFilename");
                            Bitmap bmp = BitmapFactory.decodeFile(Paths.get(p.toString(), coverName).toString());

                            caller.mapListPanel.post(()-> {
                                LayoutInflater l = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
                                View elementRoot = l.inflate(R.layout.item_mapentry, caller.mapListPanel, false);
                                caller.mapListPanel.addView(elementRoot);
                                ImageView img = elementRoot.findViewById(R.id.img_cover);
                                TextView textArtistName = elementRoot.findViewById(R.id.text_artistname);
                                TextView textLevelAuthors = elementRoot.findViewById(R.id.text_levelauthor);
                                Button b = elementRoot.findViewById(R.id.btn_delete);
                                b.setOnClickListener((a) -> {
                                    caller.promptDelete(songName, p, elementRoot);
                                });

                                textArtistName.setText(songAuthor + " " + songName, TextView.BufferType.SPANNABLE);
                                Spannable s = (Spannable)textArtistName.getText();
                                s.setSpan(new ForegroundColorSpan(0x80FFFFFF), 0, songAuthor.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                textLevelAuthors.setText(levelAuthor);
                                if (bmp != null) {
                                    img.setImageBitmap(bmp);
                                }
                            });
                        } catch (Exception e) {
                            Log.e("BSDL", "error processing file: " + f.getName());
                            e.printStackTrace();
                        }
                    }
                });
                dirList.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Looper.loop();
            Looper.myLooper().quitSafely();
        }
    }

    CheckBox openLanCheckbox = null;
    public LinearLayout mapListPanel = null;

    final String PREF_OPEN_TO_LAN = "bsdl.open_lan";
    ThreadMapList mapListThread = null;


    public void promptDelete(String displayName, Path directory, View ui) {
        Context ctx = this;
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        try (Stream<Path> paths = Files.walk(directory)) {
                            paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
                            mapListPanel.removeView(ui);
                        } catch (Exception e) {
                            Toast.makeText(ctx, "Delete failed", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete: " + displayName + "?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view for the activity
        setContentView(R.layout.activity_main);
        final SharedPreferences prefs = getSharedPreferences(getPackageName() + ".prefs", Context.MODE_PRIVATE);

        openLanCheckbox = findViewById(R.id.chkbox_openlan);
        openLanCheckbox.setChecked(prefs.getBoolean(PREF_OPEN_TO_LAN, false));

        openLanCheckbox.setOnCheckedChangeListener((a,b) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(PREF_OPEN_TO_LAN, b);
            editor.apply();
        });

        mapListPanel = findViewById(R.id.panel_maplist);
        if (mapListThread == null) {
            mapListThread = new ThreadMapList(this);
            mapListThread.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapListThread != null && mapListThread.isAlive()) {
            mapListThread.interrupt();
        }
    }
}
