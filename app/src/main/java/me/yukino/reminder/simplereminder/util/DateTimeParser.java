package me.yukino.reminder.simplereminder.util;

import android.annotation.TargetApi;
import android.os.Build;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a utility for parse the date and time information in a string.
 * The implementation is about Regex Expression.
 *
 * @author Yukino Yukinoshita
 */

@TargetApi(Build.VERSION_CODES.O)
public class DateTimeParser {

    /**
     * A regex expression string for date.
     */
    private static String regex_date;

    /**
     * A regex expression string for time.
     */
    private static String regex_time;

    /**
     * Get the LocalDateTime object after parsing input
     *
     * @param input the input string with date and time information
     * @return a LocalDateTime object indicate the date and time after parsing
     */
    public static LocalDateTime getDateTime(String input) {
        return getDateTime(input, 1);
    }

    /**
     * Get the LocalDateTime object after parsing input
     *
     * @param input the input string with date and time information
     * @param currentWeek a int value indicate the week now
     * @return a LocalDateTime object indicate the date and time after parsing
     */
    @TargetApi(Build.VERSION_CODES.O)
    public static LocalDateTime getDateTime(String input, int currentWeek) {
        String[] dateStringAry = getDateTimeString(regex_date, input);
        String[] timeStringAry = getDateTimeString(regex_time, input);
        LocalDateTime parseDateTime = LocalDateTime.now().withHour(0).withMinute(0);

        parseDateTime = parseDate(dateStringAry, parseDateTime, currentWeek);
        parseDateTime = parseTime(timeStringAry, parseDateTime);

        return parseDateTime;
    }

    /**
     * Get the LocalDateTime object after parsing input with allow day different
     *
     * @param input the input string with date and time information
     * @param currentWeek a int value indicate the week now
     * @param allowDayDifferent a int value for day different allowed
     * @return a LocalDateTime object indicate the date and time after parsing
     */
    @TargetApi(Build.VERSION_CODES.O)
    public static LocalDateTime getDateTime(String input, int currentWeek, int allowDayDifferent) {
        LocalDateTime resultDateTime = getDateTime(input, currentWeek);
        if (allowDayDifferent > 0) {
            LocalDateTime currentDateTime = LocalDateTime.now();
            long dayDiff = Math.abs(resultDateTime.toLocalDate().toEpochDay() - currentDateTime.toLocalDate().toEpochDay());
            if (dayDiff > allowDayDifferent) {
                return currentDateTime;
            }
        }
        return resultDateTime;
    }

    /**
     * Get the parsed strings
     *
     * @param input the input string with date and time information
     * @return a string array with date and time information
     */
    public static String[] getOriginStrings(String input) {
        String[] dateStringAry = getDateTimeString(regex_date, input);
        String[] timeStringAry = getDateTimeString(regex_time, input);
        ArrayList<String> outputStringAryList = new ArrayList<>(Arrays.asList(dateStringAry));
        outputStringAryList.addAll(Arrays.asList(timeStringAry));

        String[] outputStringAry = new String[outputStringAryList.size()];
        outputStringAry = outputStringAryList.toArray(outputStringAry);
        return outputStringAry;
    }

    /**
     * Parse date information
     *
     * @param dateStringAry a string array about date information
     * @param currentDateTime a LocalDateTime object indicate the date and time in the parsing phase
     * @param currentWeek a int value indicate the week now
     * @return a LocalDateTime object indicate the date and time after parsing date
     */
    @TargetApi(Build.VERSION_CODES.O)
    private static LocalDateTime parseDate(String[] dateStringAry, LocalDateTime currentDateTime, int currentWeek) {

        LocalDateTime resultDateTime = LocalDateTime.from(currentDateTime);

        for (String dateString : dateStringAry) {
            int monthNow = resultDateTime.getMonthValue();
            int monthNeed = parseMonth(dateString, resultDateTime);
            int monthDiff = monthNeed - monthNow;
            resultDateTime = resultDateTime.plusMonths(monthDiff);


            int dayNow = resultDateTime.getDayOfMonth();
            int dayNeed = parseDay(dateString, resultDateTime);
            int dayDiff = dayNeed - dayNow;
            resultDateTime = resultDateTime.plusDays(dayDiff);


            int weekNeed = parseWeek(dateString, currentWeek);
            int weekDiff = weekNeed - currentWeek;
            resultDateTime = resultDateTime.plusWeeks(weekDiff);

            int dayOfWeekNeed = parseDayOfWeek(dateString, resultDateTime);
            int dayOfWeekDiff = dayOfWeekNeed - resultDateTime.getDayOfWeek().getValue();
            resultDateTime = resultDateTime.plusDays(dayOfWeekDiff);
        }

        return resultDateTime;
    }

    /**
     * Parse time information
     *
     * @param timeStringAry a string array about time information
     * @param currentDateTime a LocalDateTime object indicate the date and time in the parsing phase
     * @return a LocalDateTime object indicate the date and time after parsing time
     */
    @TargetApi(Build.VERSION_CODES.O)
    private static LocalDateTime parseTime(String[] timeStringAry, LocalDateTime currentDateTime) {
        if (timeStringAry.length == 0) {
            return currentDateTime;
        }

        // only parse the first string
        String timeString = timeStringAry[0];
        LocalDateTime resultDateTime = LocalDateTime.from(currentDateTime);

        int hourNeed = parseHour(timeString, resultDateTime);
        int hourDiff = hourNeed - resultDateTime.getHour();
        resultDateTime = resultDateTime.plusHours(hourDiff);

        int minuteNeed = parseMinute(timeString, resultDateTime);
        int minuteDiff = minuteNeed - resultDateTime.getMinute();
        resultDateTime = resultDateTime.plusMinutes(minuteDiff);

        return resultDateTime;
    }

    /**
     * Parse month information from a date string
     *
     * @param dateString a string about date
     * @param currentDateTime a LocalDateTime object indicate the date and time in the parsing phase
     * @return a int value indicate the month needed.
     */
    @TargetApi(Build.VERSION_CODES.O)
    private static int parseMonth(String dateString, LocalDateTime currentDateTime) {
        if (Pattern.matches(".*((下个?月)|(一个?月后)).*", dateString)) {
            return currentDateTime.getMonthValue() + 1;
        } else if (Pattern.matches(".*(下下个?月|[^十]([两二]月后?)|([两二]个月后?)).*", dateString)) {
            return currentDateTime.getMonthValue() + 2;
        } else if (Pattern.matches(".*三个?月后.*", dateString)) {
            return currentDateTime.getMonthValue() + 3;
        }

        final String C09 = "([0123456789十一二三四五六七八九])";
        final String C0 = "[0十]";
        final String C1 = "[1一]";
        final String C01 = "[0十1一]";
        final String C12 = "[12一二]";
        final String patternMonth = ".*" + orRegex(C09, C1 + C0, C01 + C12) + "月.*";

        int monthNeed = currentDateTime.getMonthValue();
        if (Pattern.matches(patternMonth, dateString)) {
            int monthNow = currentDateTime.getMonthValue();
            Matcher matcher = Pattern.compile(orRegex(C09, C1 + C0, C01 + C12) + "月").matcher(dateString);
            matcher.find();
            int monthIndex = matcher.start();

            if (Pattern.matches(orRegex(C1, C0), String.valueOf(dateString.charAt(monthIndex)))) {
                // 1月 一月 10月 十月 11~12月 十一~十二月
                if (Pattern.matches("月", String.valueOf(dateString.charAt(monthIndex + 1)))) {
                    // 1月 一月 十月
                    switch (dateString.charAt(monthIndex)) {
                        case '1':
                        case '一':
                            monthNeed = 1;
                            break;
                        case '十':
                            monthNeed = 10;
                            break;
                        default:
                            monthNeed = monthNow;
                    }
                } else {
                    // 10月 11~12月 十一~十二月
                    if (Pattern.matches("0", String.valueOf(dateString.charAt(monthIndex + 1)))) {
                        // 10月
                        monthNeed = 10;
                    } else {
                        // 11~12月 十一~十二月
                        switch (dateString.charAt(monthIndex + 1)) {
                            case '1':
                            case '一':
                                monthNeed = 11;
                                break;
                            case '2':
                            case '二':
                                monthNeed = 12;
                                break;
                            default:
                                monthNeed = monthNow;
                        }
                    }
                }
            } else {
                // 2~9月 二~九月
                switch (dateString.charAt(monthIndex)) {
                    case '2':
                    case '二':
                        monthNeed = 2;
                        break;
                    case '3':
                    case '三':
                        monthNeed = 3;
                        break;
                    case '4':
                    case '四':
                        monthNeed = 4;
                        break;
                    case '5':
                    case '五':
                        monthNeed = 5;
                        break;
                    case '6':
                    case '六':
                        monthNeed = 6;
                        break;
                    case '7':
                    case '七':
                        monthNeed = 7;
                        break;
                    case '8':
                    case '八':
                        monthNeed = 8;
                        break;
                    case '9':
                    case '九':
                        monthNeed = 9;
                        break;
                    default:
                        monthNeed = monthNow;
                }
            }
        }

        return monthNeed;
    }

    /**
     * Parse day information from a date string
     *
     * @param dateString a string about date
     * @param currentDateTime a LocalDateTime object indicate the date and time in the parsing phase
     * @return a int value indicate the day needed.
     */
    @TargetApi(Build.VERSION_CODES.O)
    private static int parseDay(String dateString, LocalDateTime currentDateTime) {
        if (Pattern.matches(".*明[天日早晚].*", dateString)) {
            return currentDateTime.getDayOfMonth() + 1;
        } else if (Pattern.matches(".*(后[两二]?[天日]|([两二][天日]后)).*", dateString)) {
            return currentDateTime.getDayOfMonth() + 2;
        } else if (Pattern.matches(".*(((大后|后三)[天日])|(三[天日]后)).*", dateString)) {
            return currentDateTime.getDayOfMonth() + 3;
        }

        final String C03 = "[0123十一二三]";
        final String C09 = "([0123456789十一二三四五六七八九])";
        final String C0 = "[0十]";
        final String C1 = "[1一]";
        final String patternDay = ".*" + orRegex(C09, orRegex(C0, C1) + C09, orRegex(C03, C03 + C0) + C09) + "([号日]).*";

        final char[] lowerNumber = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        final char[] upperNumber = {'十', '一', '二', '三', '四', '五', '六', '七', '八', '九'};

        int dayNeed = currentDateTime.getDayOfMonth();
        if (Pattern.matches(patternDay, dateString)) {
            int dayNow = dayNeed;
            Matcher matcher = Pattern.compile(orRegex(C09, orRegex(C0, C1) + C09, orRegex(C03, C03 + C0) + C09) + "([号日])").matcher(dateString);
            matcher.find();
            int dayIndex = matcher.start();

            if (Pattern.matches(orRegex(C0, C1), String.valueOf(dateString.charAt(dayIndex)))) {
                // 1号 一号 10~19号 十号 十一~十九号
                if (Pattern.matches("([号日])", String.valueOf(dateString.charAt(dayIndex + 1)))) {
                    // 1号 一号 十号
                    switch (dateString.charAt(dayIndex)) {
                        case '1':
                        case '一':
                            dayNeed = 1;
                            break;
                        case '十':
                            dayNeed = 10;
                            break;
                        default:
                            dayNeed = dayNow;
                    }
                } else {
                    // 10~19号 十一~十九号
                    if (dateString.charAt(dayIndex + 1) == lowerNumber[0]) {
                        dayNeed = 10;
                    } else {
                        for (int i = 1; i < 10; ++i) {
                            if (dateString.charAt(dayIndex + 1) == lowerNumber[i]
                                    || dateString.charAt(dayIndex + 1) == upperNumber[i]) {
                                dayNeed = 10 + i;
                                break;
                            }
                        }
                    }
                }
            } else if (Pattern.matches("([号日])", String.valueOf(dateString.charAt(dayIndex + 1)))) {
                // 2~9号 二~九号
                for (int i = 2; i < 10; ++i) {
                    if (dateString.charAt(dayIndex) == lowerNumber[i]
                            || dateString.charAt(dayIndex) == upperNumber[i]) {
                        dayNeed = i;
                        break;
                    }
                }
            } else {
                // 20~29号 30~31号 二十号~二十九号 三十号 三十一号
                if (Pattern.matches("([2二])", String.valueOf(dateString.charAt(dayIndex)))) {
                    // 20~29号 二十号~二十九号
                    if (Pattern.matches("([号日])", String.valueOf(dateString.charAt(dayIndex + 2)))) {
                        // 20~29号 二十号
                        for (int i = 0; i < 10; ++i) {
                            if (dateString.charAt(dayIndex + 1) == lowerNumber[i]
                                    || dateString.charAt(dayIndex + 1) == upperNumber[i]) {
                                dayNeed = 20 + i;
                                break;
                            }
                        }
                    } else {
                        // 二十一号~二十九号
                        for (int i = 1; i < 10; ++i) {
                            if (dateString.charAt(dayIndex + 2) == upperNumber[i]) {
                                dayNeed = 20 + i;
                                break;
                            }
                        }
                    }
                } else {
                    // 30~31号 三十号 三十一号
                    if (Pattern.matches("([号日])", String.valueOf(dateString.charAt(dayIndex + 2)))) {
                        // 30~31号 三十号
                        for (int i = 0; i < 10; ++i) {
                            if (dateString.charAt(dayIndex + 1) == lowerNumber[i]
                                    || dateString.charAt(dayIndex + 1) == upperNumber[i]) {
                                dayNeed = 30 + i;
                                break;
                            }
                        }
                    } else {
                        // 三十一号
                        for (int i = 1; i < 10; ++i) {
                            if (dateString.charAt(dayIndex + 2) == upperNumber[i]) {
                                dayNeed = 30 + i;
                                break;
                            }
                        }
                    }
                }
            }
        }

        return dayNeed;
    }

    /**
     * Parse week information from a date string
     *
     * @param dateString a string about date
     * @param currentWeek a int value indicate the current week
     * @return a int value indicate the week needed.
     */
    private static int parseWeek(String dateString, int currentWeek) {
        final String patternNextTwoWeeks = ".*下下个?(周|星期|礼拜).*";
        final String patternNextWeek = ".*下个?(周|星期|礼拜).*";

        // 下下周 下周
        if (Pattern.matches(patternNextTwoWeeks, dateString)) {
            return currentWeek + 2;
        } else if (Pattern.matches(patternNextWeek, dateString)) {
            return currentWeek + 1;
        }

        final String C03 = "[0123十一二三]";
        final String C09 = "([0123456789十一二三四五六七八九])";
        final String C0 = "[0十]";
        final String C1 = "[1一]";
        final String patternWeek = ".*" + orRegex(C09, orRegex(C0, C1) + C09, orRegex(C03, C03 + C0) + C09) + "周.*";

        final char[] lowerNumber = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        final char[] upperNumber = {'十', '一', '二', '三', '四', '五', '六', '七', '八', '九'};

        int weekNeed = currentWeek;
        if (Pattern.matches(patternWeek, dateString)) {
            Matcher matcher = Pattern.compile(orRegex(C09, orRegex(C0, C1) + C09, orRegex(C03, C03 + C0) + C09) + "周").matcher(dateString);
            matcher.find();
            int weekIndex = matcher.start();

            if (Pattern.matches(orRegex(C0, C1), String.valueOf(dateString.charAt(weekIndex)))) {
                // 1周 一周 10~19周 十周 十一~十九周
                if (Pattern.matches("周", String.valueOf(dateString.charAt(weekIndex + 1)))) {
                    // 1周 一周 十周
                    switch (dateString.charAt(weekIndex)) {
                        case '1':
                        case '一':
                            weekNeed = 1;
                            break;
                        case '十':
                            weekNeed = 10;
                            break;
                        default:
                            weekNeed = currentWeek;
                    }
                } else {
                    // 10~19周 十一~十九周
                    if (dateString.charAt(weekIndex + 1) == lowerNumber[0]) {
                        weekNeed = 10;
                    } else {
                        for (int i = 1; i < 10; ++i) {
                            if (dateString.charAt(weekIndex + 1) == lowerNumber[i]
                                    || dateString.charAt(weekIndex + 1) == upperNumber[i]) {
                                weekNeed = 10 + i;
                                break;
                            }
                        }
                    }
                }
            } else if (Pattern.matches("周", String.valueOf(dateString.charAt(weekIndex + 1)))) {
                // 2~9周 二~九周
                for (int i = 2; i < 10; ++i) {
                    if (dateString.charAt(weekIndex) == lowerNumber[i]
                            || dateString.charAt(weekIndex) == upperNumber[i]) {
                        weekNeed = i;
                        break;
                    }
                }
            } else {
                // duplicate
                if (Pattern.matches("(2|二)", String.valueOf(dateString.charAt(weekIndex)))) {
                    if (Pattern.matches("周", String.valueOf(dateString.charAt(weekIndex + 2)))) {
                        for (int i = 0; i < 10; ++i) {
                            if (dateString.charAt(weekIndex + 1) == lowerNumber[i]
                                    || dateString.charAt(weekIndex + 1) == upperNumber[i]) {
                                weekNeed = 20 + i;
                                break;
                            }
                        }
                    } else {
                        for (int i = 1; i < 10; ++i) {
                            if (dateString.charAt(weekIndex + 2) == upperNumber[i]) {
                                weekNeed = 20 + i;
                                break;
                            }
                        }
                    }
                } else {
                    if (Pattern.matches("周", String.valueOf(dateString.charAt(weekIndex + 2)))) {
                        for (int i = 0; i < 10; ++i) {
                            if (dateString.charAt(weekIndex + 1) == lowerNumber[i]
                                    || dateString.charAt(weekIndex + 1) == upperNumber[i]) {
                                weekNeed = 30 + i;
                                break;
                            }
                        }
                    } else {
                        for (int i = 1; i < 10; ++i) {
                            if (dateString.charAt(weekIndex + 2) == upperNumber[i]) {
                                weekNeed = 30 + i;
                                break;
                            }
                        }
                    }
                }
            }
        }

        return weekNeed;
    }

    /**
     * Parse the day of a week information from a date string
     *
     * @param dateString a string about date
     * @param currentDateTime a LocalDateTime object indicate the date and time in the parsing phase
     * @return a int value indicate the day of week needed.
     */
    @TargetApi(Build.VERSION_CODES.O)
    private static int parseDayOfWeek(String dateString, LocalDateTime currentDateTime) {
        final String patternDayOfWeek = ".*(周|星期|礼拜)([1234567一二三四五六日天七]).*";

        int dayOfWeekNeed = currentDateTime.getDayOfWeek().getValue();
        if (Pattern.matches(patternDayOfWeek, dateString)) {
            int dayOfWeekNow = currentDateTime.getDayOfWeek().getValue();
            Matcher matcher = Pattern.compile("(周|星期|礼拜)([1234567一二三四五六日天七])").matcher(dateString);
            matcher.find();
            int dayIndex = matcher.start() + 1;
            if (!Pattern.matches("[1234567一二三四五六七日天]", String.valueOf(dateString.charAt(dayIndex)))) {
                dayIndex = dayIndex + 1;
            }

            switch (dateString.charAt(dayIndex)) {
                case '一':
                case '1':
                    dayOfWeekNeed = 1;
                    break;
                case '二':
                case '2':
                    dayOfWeekNeed = 2;
                    break;
                case '三':
                case '3':
                    dayOfWeekNeed = 3;
                    break;
                case '四':
                case '4':
                    dayOfWeekNeed = 4;
                    break;
                case '五':
                case '5':
                    dayOfWeekNeed = 5;
                    break;
                case '六':
                case '6':
                    dayOfWeekNeed = 6;
                    break;
                case '日':
                case '天':
                case '七':
                case '7':
                    dayOfWeekNeed = 7;
                    break;
                default:
                    dayOfWeekNeed = dayOfWeekNow;
            }
        }
        return dayOfWeekNeed;
    }

    /**
     * Parse hour information from a time string
     *
     * @param timeString a string about time
     * @param currentDateTime a LocalDateTime object indicate the date and time in the parsing phase
     * @return a int value indicate the hour needed.
     */
    @TargetApi(Build.VERSION_CODES.O)
    private static int parseHour(String timeString, LocalDateTime currentDateTime) {
        if (Pattern.matches(".*上午", timeString)) {
            return 10;
        } else if (Pattern.matches(".*中午", timeString)) {
            return 12;
        } else if (Pattern.matches(".*下午", timeString)) {
            return 17;
        } else if (Pattern.matches(".*晚上?", timeString)) {
            return 22;
        }
        final String patternHour = ".*[时点:].*";
        if (!Pattern.matches(patternHour, timeString)) {
            return currentDateTime.getHour();
        }

        final String C09 = "([0,1,2,3,4,5,6,7,8,9,十,一,二,三,四,五,六,七,八,九])";
        final String C09l = "[0123456789]";
        final char[] lowerNumber = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        final char[] upperNumber = {'十', '一', '二', '三', '四', '五', '六', '七', '八', '九'};

        int hourNeed = currentDateTime.getHour();
        Matcher matcher = Pattern.compile(orRegex(C09, C09 + C09, C09 + C09 + C09) + "([时点:])").matcher(timeString);
        matcher.find();
        int hourIndex = matcher.start();

        if (Pattern.matches(C09l, String.valueOf(timeString.charAt(hourIndex)))) {
            if (Pattern.matches("[12]", String.valueOf(timeString.charAt(hourIndex))) && !Pattern.matches("[时点:]", String.valueOf(timeString.charAt(hourIndex + 1)))) {
                if (timeString.charAt(hourIndex) == '1') {
                    for (int i = 0; i < 10; ++i) {
                        if (timeString.charAt(hourIndex + 1) == lowerNumber[i]) {
                            hourNeed = 10 + i;
                            break;
                        }
                    }
                } else {
                    for (int i = 0; i < 10; ++i) {
                        if (timeString.charAt(hourIndex + 1) == lowerNumber[i]) {
                            hourNeed = 20 + i;
                            break;
                        }
                    }
                }
            } else {
                if (timeString.charAt(hourIndex) == '0' && Pattern.matches(C09l, String.valueOf(timeString.charAt(hourIndex + 1)))) {
                    for (int i = 0; i < 10; ++i) {
                        if (timeString.charAt(hourIndex + 1) == lowerNumber[i]) {
                            hourNeed = i;
                            break;
                        }
                    }
                } else {
                    for (int i = 0; i < 10; ++i) {
                        if (timeString.charAt(hourIndex) == lowerNumber[i]) {
                            hourNeed = i;
                            break;
                        }
                    }
                }
            }
        } else {
            if (Pattern.matches("[一二三四五六七八九十]", String.valueOf(timeString.charAt(hourIndex))) && Pattern.matches("[时点:]", String.valueOf(timeString.charAt(hourIndex + 1)))) {
                if (timeString.charAt(hourIndex) == '十') {
                    hourNeed = 10;
                } else {
                    for (int i = 1; i < 10; ++i) {
                        if (timeString.charAt(hourIndex) == upperNumber[i]) {
                            hourNeed = i;
                            break;
                        }
                    }
                }
            } else if (timeString.charAt(hourIndex) == '十' && Pattern.matches("[时点:]", String.valueOf(timeString.charAt(hourIndex + 2)))) {
                for (int i = 1; i < 10; ++i) {
                    if (timeString.charAt(hourIndex + 1) == upperNumber[i]) {
                        hourNeed = 10 + i;
                        break;
                    }
                }
            } else if (Pattern.matches("[时点:]", String.valueOf(timeString.charAt(hourIndex + 3)))) {
                for (int i = 1; i < 5; ++i) {
                    if (timeString.charAt(hourIndex + 2) == upperNumber[i]) {
                        hourNeed = 20 + i;
                        break;
                    }
                }
            } else {
                hourNeed = 20;
            }
        }

        if (Pattern.matches(".*(下午|pm).*", timeString)) {
            hourNeed = hourNeed < 12 ? hourNeed + 12 : hourNeed;
        } else if (Pattern.matches(".*(晚上|晚).*", timeString)) {
            if (hourNeed >= 5) {
                hourNeed = hourNeed < 13 ? hourNeed + 12 : hourNeed;
            }
        }
        return hourNeed;
    }

    /**
     * Parse minute information from a time string
     *
     * @param timeString a string about time
     * @param currentDateTime a LocalDateTime object indicate the date and time in the parsing phase
     * @return a int value indicate the minute needed.
     */
    @TargetApi(Build.VERSION_CODES.O)
    private static int parseMinute(String timeString, LocalDateTime currentDateTime) {
        if (Pattern.matches(".*[时点]半.*", timeString)) {
            return 30;
        }
        Matcher matcherNotMinute = Pattern.compile("[时点]").matcher(timeString);
        if (matcherNotMinute.find()) {
            if (matcherNotMinute.start() + 1 == timeString.length()) {
                return 0;
            }
        }

        final String C09l = "[0123456789]";
        final String C05l = "[012345]";
        final String patternMinute = ".*[时点:]" + orRegex(C09l, C09l + C09l) + "分?.*";
        if (!Pattern.matches(patternMinute, timeString)) {
            return currentDateTime.getMinute();
        }

        final char[] lowerNumber = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

        int minuteNeed = currentDateTime.getMinute();
        Matcher matcher = Pattern.compile("[时点:]" + orRegex(C09l, C05l + C09l)).matcher(timeString);
        matcher.find();
        int minuteIndex = matcher.start() + 1;

        if (minuteIndex + 1 == timeString.length()) {
            for (int i = 0; i < 10; ++i) {
                if (timeString.charAt(minuteIndex) == lowerNumber[i]) {
                    minuteNeed = i;
                }
            }
        } else {
            if (Pattern.matches(C05l, String.valueOf(timeString.charAt(minuteIndex))) && Pattern.matches(C09l, String.valueOf(timeString.charAt(minuteIndex + 1)))) {
                for (int i = 0; i < 6; ++i) {
                    if (timeString.charAt(minuteIndex) == lowerNumber[i]) {
                        minuteNeed = i;
                        break;
                    }
                    minuteNeed = 0;
                }
                for (int i = 0; i < 10; ++i) {
                    if (timeString.charAt(minuteIndex + 1) == lowerNumber[i]) {
                        minuteNeed = minuteNeed * 10 + i;
                    }
                }
            }
        }

        return minuteNeed;

    }

    /**
     * Parse date and time from input string according to regex pattern
     *
     * @param regex A regex expression pattern to match
     * @param input The input string to parse
     * @return An String array includes date and time in the input
     */
    private static String[] getDateTimeString(String regex, String input) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        String[] s = new String[10];
        int len = 0;
        while (matcher.find()) {
            s[len] = matcher.group();
            len++;
        }
        return Arrays.copyOf(s, len);
    }

    /**
     * Perform as OR for regex expression
     *
     * @param args each regex expression
     * @return a regex expression with OR between each input regex expression
     */
    private static String orRegex(String... args) {
        String regex = "";
        for (int i = 0; i < args.length; i++) {
            regex = regex + "|" + "(" + args[i] + ")";
        }
        return "(" + regex.substring(1) + ")";
    }

    static {
        createRegexDate();
        createRegexTime();
    }

    /**
     * Define regex expression for time
     */
    private static void createRegexTime() {
        String nh = "((1[0-9])|(2[0-4])|((0|)[0-9]))";
        String duan = "(上午|中午|下午|晚|晚上|凌晨|早上|am|pm)";
        String ch = "(((二|)(十|)[零,一,二,三,四,五,六,七,八,九,十]))";
        String ce = "(时|点|:)";
        String cme = "(分|)";
        String t1 = "((一刻)|(半)|([0-5][0-9]))" + cme;
        String t2 = orRegex(nh, ch) + ce + orRegex(t1, "");
        String t2duan = duan + t2;
        String t3 = orRegex(t2duan, t2);
        regex_time = orRegex(t3, duan);
    }

    /**
     * Define regex expression for date
     */
    private static void createRegexDate() {
        final String C09 = "([0,1,2,3,4,5,6,7,8,9,十,一,二,三,四,五,六,七,八,九])";
        final String C19 = "([1,2,3,4,5,6,7,8,9,一,二,三,四,五,六,七,八,九])";
        final String C0 = "[0,十]";
        final String C1 = "[1,一]";
        final String C01 = "[0,十,1,一]";
        final String C02 = "[0,1,2,十,一,二]";
        final String C3 = "[3,三]";
        final String C12 = "[1,2,一,二]";
        final String C17 = "[1,2,3,4,5,6,7,一,二,三,四,五,六,日]";
        final String Y = C02 + C09 + C09 + C09;
        final String M = orRegex(C09, orRegex(C1, C0) + C02);
        final String D = orRegex(C12 + C09, C3 + C01, orRegex(C0, "") + C19, "[二三]十" + C19);
        final String W = "((周|下个?周|本周)(" + C17 + "|末))|((下个?周星期|下个?礼拜|礼拜|下个?星期|星期)(" + C17 + "|天))";
        final String W2 = orRegex(C09, C09 + C09, C09 + C09 + C09) + "周";
        final String M_END1 = "月";
        final String D_END1 = "(日|号)";
        final String Y_END1 = "年";
        final String E = "(-|\\.)";
        String year = orRegex(Y + Y_END1, "");
        String year_month = year + M + M_END1;
        String day = D + D_END1;
        String day_week = orRegex(day, "") + orRegex(W, W2);
        String month_day = orRegex(orRegex(M + M_END1, "") + day, M + M_END1 + D);
        String year_month_day = year_month + day;
        regex_date = orRegex(year_month_day, month_day, day_week, year_month);
        regex_date = orRegex(regex_date, Y + E + M + E + D, Y + E + M, M + E + D);
        regex_date = orRegex(regex_date, "(明|大?后).?[日天早晚]", ".?下?个?月后?");
    }

}
