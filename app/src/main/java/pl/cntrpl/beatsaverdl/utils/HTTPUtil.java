package pl.cntrpl.beatsaverdl.utils;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import pl.cntrpl.beatsaverdl.DownloadActivity;

public class HTTPUtil {

    public static String textFromStream(InputStream stm) {
        if (stm != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stm));
            StringBuilder response = new StringBuilder();
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }
                reader.close();
                return response.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String readText(String urlString) {
        HttpURLConnection connection;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            if (connection.getResponseCode() == 200) {
                String response = textFromStream(connection.getInputStream());
                return response != null ? response.trim() : null;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String downloadZipFromURL(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            if (connection.getResponseCode() == 200) {
                String outputFileName = DownloadActivity.tempDownloadsDir + "/" + url.getFile().replace('/', '_');
                InputStream stm = connection.getInputStream();
                FileOutputStream fos = new FileOutputStream(outputFileName);

                fos.write(stm.readAllBytes());
                fos.close();
                stm.close();

                return outputFileName;
            } else {
                System.out.println("Failed to download file: " + connection.getResponseMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
