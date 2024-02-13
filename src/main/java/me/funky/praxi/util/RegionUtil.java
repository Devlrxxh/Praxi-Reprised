package me.funky.praxi.util;

import lombok.experimental.UtilityClass;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@UtilityClass
public class RegionUtil {

    private static final List<String> EU_COUNTRY_CODES = Arrays.asList(
            "AT", "BE", "BG", "CY", "CZ", "DE", "DK", "EE", "ES", "FI", "FR", "GR", "HR",
            "HU", "IE", "IT", "LT", "LU", "LV", "MT", "NL", "PL", "PT", "RO", "SE", "SI", "SK"
    );

    public static boolean isFromEU(String ipAddress) {
        try {
            URL url = new URL("https://ipinfo.io/" + ipAddress + "/json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            String countryCode = getString(conn);
            return EU_COUNTRY_CODES.contains(countryCode);

        } catch (IOException e) {
            return false;
        }
    }

    private static String getString(HttpURLConnection conn) throws IOException {
        if (conn.getResponseCode() != 200) {
            throw new IOException("HTTP error code: " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        br.close();

        JSONObject jsonObject = new JSONObject(response.toString());
        return jsonObject.getString("country");
    }
}
