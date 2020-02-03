package es.kleiren.madclimb.util;

import java.util.HashMap;
import java.util.Map;

import static lecho.lib.hellocharts.util.ChartUtils.*;

public class InfoChartUtils {

    public static final Integer[] colors = new Integer[]{COLOR_GREEN, COLOR_BLUE, COLOR_VIOLET, COLOR_RED};
    public static final String[] labels = new String[]{"III - V+", "6a - 6c+", "7a - 7c+", "8a - 9c+"};

    public static final Map<String, Integer> map = new HashMap<String, Integer>() {{
        put("3", 1);
        put("3+", 1);
        put("IV-", 1);
        put("IV", 1);
        put("IV+", 1);
        put("V-", 1);
        put("V", 1);
        put("V+", 1);
        put("6a", 2);
        put("6a+", 2);
        put("6b", 2);
        put("6b+", 2);
        put("6c", 2);
        put("6c+", 2);
        put("7a", 3);
        put("7a+", 3);
        put("7b", 3);
        put("7b+", 2);
        put("7c", 3);
        put("7c+", 3);
        put("8a", 4);
        put("8a+", 4);
        put("8b", 4);
        put("8b+", 4);
        put("8c", 4);
        put("8c+", 4);
        put("9a", 4);
        put("9a+", 4);
        put("9b", 4);
        put("9b+", 4);
        put("9c", 4);
        put("9c+", 4);
    }};
}
