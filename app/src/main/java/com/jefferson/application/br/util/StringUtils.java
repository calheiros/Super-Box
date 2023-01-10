/*
 * Copyright (C) 2023 Jefferson Calheiros


 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.jefferson.application.br.util;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Patterns;

import java.text.DecimalFormat;
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

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (operators.containsKey(c)) {
                c = operators.get(c);
            }
            builder.append(c);
        }
        return builder.toString();
    }

    public static String getRandomString(int length) {
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
        }
        return links.toArray(new String[links.size()]);
    }

    public static String getFormattedVideoDuration(String millis) {
        int duration = 0;
        try {
            duration = Integer.parseInt(millis);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        long hours = TimeUnit.MINUTES.toHours(minutes);
        minutes = minutes - (hours * 60);
        //long secunds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));

        return hours > 0 ? String.format("%d:%02d:%02d", hours, minutes, seconds) : String.format("%d:%02d", minutes, seconds);
    }

    public static String getFormattedDate() {
        return getFormattedDate("dd-MM-yyyy_HH-mm-ss");
    }

    public static String getFormattedDate(String format) {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        SimpleDateFormat simpleDate = new SimpleDateFormat(format);
        return simpleDate.format(now);
    }

    public static String getFormattedFileSize(long fileSize) {
        // Use StringBuilder to build the string instead of multiple string concatenations
        StringBuilder sb = new StringBuilder();
        if (fileSize < 1024) {
            // File size is in bytes
            sb.append(fileSize).append(" Bytes");
        } else if (fileSize < 1024 * 1024) {
            // File size is in kilobytes
            sb.append(fileSize / 1024).append(" Kilobytes");
        } else if (fileSize < 1024 * 1024 * 1024) {
            // File size is in megabytes
            sb.append(fileSize / (1024 * 1024)).append(" Megabytes");
        } else if (fileSize < 1024L * 1024 * 1024 * 1024) {
            // File size is in gigabytes
            sb.append(fileSize / (1024 * 1024 * 1024)).append(" Gigabytes");
        } else {
            // File size is in terabytes
            sb.append(fileSize / (1024L * 1024 * 1024 * 1024)).append(" Terabytes");
        }
        return sb.toString();
    }

}
