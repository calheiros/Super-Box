package com.jefferson.application.br.util;
import android.util.ArrayMap;
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

    public static String replaceEach(String text, ArrayMap<Character, Character> operators) {
        // Create a buffer sufficiently large that re-allocations are minimized.
        StringBuilder builder = new StringBuilder(text.length());
        
        for (int i = 0; i < text.length(); i++ ) {
            char c = text.charAt(i);
            if (operators.containsKey(c)) {
                c = operators.get(c);
            }
            builder.append(c);
        }
        return builder.toString();
    }

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

    public static String getFormatedVideoDuration(String millis) {
        int duration = 0;
        try {
            duration = Integer.valueOf(millis);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        long hours =  TimeUnit.MINUTES.toHours(minutes);
        minutes = minutes - (hours * 60);
        //long secunds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
        final String fmt = hours > 0 ? String.format("%d:%02d:%02d", hours, minutes, seconds) : String.format("%d:%02d", minutes, seconds);

        return fmt;
    }
    public static String getFormatedDate() {

        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime(); 
        SimpleDateFormat simpleDate = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
        String timestamp = simpleDate.format(now);

        return timestamp;
    }

}
