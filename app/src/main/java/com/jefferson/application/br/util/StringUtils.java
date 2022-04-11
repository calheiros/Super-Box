package com.jefferson.application.br.util;
import android.util.Log;
import android.util.Patterns;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

public class StringUtils {
	public static String Dictionary = "abcdefghijklmnopqrstuvwxyz01234567890123456789";

    private static String TAG = "StringUtils";

	public static String  getRandomString(int length) {
		String generate = new String();
		Random random = new Random();

		for (int i = 0; i < length; i++) {
			int pos = random.nextInt(Dictionary.length());
			generate += Dictionary.charAt(pos);
		}
		return generate;
	}

    public static String[] extractLinks(String text) {
        List<String> links = new ArrayList<String>(); 
        Matcher m = Patterns.WEB_URL.matcher(text); 
        while (m.find()) { 
            String url = m.group(); 
            Log.d(TAG, "URL extracted: " + url); 
            links.add(url); 
        } return links.toArray(new String[links.size()]); 
    }

    public static String getFormatedTime(String duration) {
        int millis = 0;
        try {
            millis = Integer.valueOf(duration);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        long secunds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
        final String time = String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis), secunds);

        return time;
    }
    public static String getFormatedDate() {
        
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime(); 
        SimpleDateFormat simpleDate = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
        String timestamp = simpleDate.format(now);
        
        return timestamp;
    }

}
