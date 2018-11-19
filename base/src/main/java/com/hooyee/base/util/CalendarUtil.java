/*
 * Copyright (c) 2018.
 * last modified : 18-9-30 下午6:16
 * author                             bug                             date                            comment
 * maohui----------------------------------------------------------2018-09-30--------------------------init
 */

package com.hooyee.base.util;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.TextUtils;

import com.hooyee.base.R;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author maohui
 * @date Created on 2018/9/30.
 * @description
 * @added
 */

public class CalendarUtil {
    private static String CALENDARS_NAME = "nykj";
    private static String CALENDARS_ACCOUNT_NAME = "nykj@91160.com";
    private static String CALENDARS_ACCOUNT_TYPE = "cn.kidyn.qdmedical160";
    private static String CALENDARS_DISPLAY_NAME = "健康160";

    /**
     * 检查是否已经添加了日历账户，如果没有添加先添加一个日历账户再次进行查询
     * 获取账户成功返回账户id，否则返回-1
     */
    private static int checkAndAddCalendarAccount(Context context) {
        int oldId = checkCalendarAccount(context);
        if (oldId >= 0) {
            return oldId;
        } else {
            long addId = addCalendarAccount(context);
            if (addId >= 0) {
                return checkCalendarAccount(context);
            } else {
                return -1;
            }
        }
    }

    @SuppressLint("MissingPermission")
    private static int checkCalendarAccount(Context context) {
        Cursor userCursor = context.getContentResolver().query(CalendarContract.Calendars.CONTENT_URI,
                null, null, null, null);
        try {
            if (userCursor == null) { // 查询返回空值
                return -1;
            }
            int count = userCursor.getCount();
            if (count > 0) { // 存在现有账户，取第一个账户的id返回
                userCursor.moveToFirst();
                return userCursor.getInt(userCursor.getColumnIndex(CalendarContract.Calendars._ID));
            } else {
                return -1;
            }
        } finally {
            if (userCursor != null) {
                userCursor.close();
            }
        }
    }

    private static long addCalendarAccount(Context context) {
        TimeZone timeZone = TimeZone.getDefault();
        ContentValues value = new ContentValues();
        value.put(CalendarContract.Calendars.NAME, CALENDARS_NAME);
        value.put(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME);
        value.put(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE);
        value.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CALENDARS_DISPLAY_NAME);
        value.put(CalendarContract.Calendars.VISIBLE, 1);
        value.put(CalendarContract.Calendars.CALENDAR_COLOR, Color.BLUE);
        value.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
                CalendarContract.Calendars.CAL_ACCESS_OWNER);
        value.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        value.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.getID());
        value.put(CalendarContract.Calendars.OWNER_ACCOUNT, CALENDARS_ACCOUNT_NAME);
        value.put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0);

        Uri calendarUri = CalendarContract.Calendars.CONTENT_URI;
        calendarUri = calendarUri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME,
                        CALENDARS_ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE,
                        CALENDARS_ACCOUNT_TYPE)
                .build();

        Uri result = context.getContentResolver().insert(calendarUri, value);
        long id = result == null ? -1 : ContentUris.parseId(result);
        return id;
    }


    @SuppressLint("MissingPermission")
    public static long insertCalendarEvent(Context context, String title, String description, long tipTimeMills, long beginTimeMillis, long endTimeMillis) {

        if (context == null || TextUtils.isEmpty(title) || TextUtils.isEmpty(description)) {
            return -1;
        }

        int calId = checkAndAddCalendarAccount(context); // 获取日历账户的id
        if (calId < 0) { // 获取账户id失败直接返回，添加日历事件失败
            return -1;
        }

        // 如果起始时间为零，使用当前时间
        if (beginTimeMillis == 0) {
            Calendar beginCalendar = Calendar.getInstance();
            beginTimeMillis = beginCalendar.getTimeInMillis();
        }
        // 如果结束时间为零，使用起始时间+30分钟
        if (endTimeMillis == 0) {
            endTimeMillis = beginTimeMillis + 30 * 60 * 1000;
        }
        long eventId = -1;
        try {
            /** 插入日程 */
            ContentValues eventValues = new ContentValues();
            eventValues.put(CalendarContract.Events.DTSTART, beginTimeMillis);
            eventValues.put(CalendarContract.Events.DTEND, endTimeMillis);
            eventValues.put(CalendarContract.Events.TITLE, title);
            eventValues.put(CalendarContract.Events.DESCRIPTION, description);
            eventValues.put(CalendarContract.Events.CALENDAR_ID, 1);
            eventValues.put(CalendarContract.Events.EVENT_LOCATION, context.getString(R.string.app_name));

            TimeZone tz = TimeZone.getDefault(); // 获取默认时区
            eventValues.put(CalendarContract.Events.EVENT_TIMEZONE, tz.getID());
            Uri eUri = context.getContentResolver().insert(CalendarContract.Events.CONTENT_URI, eventValues);
            eventId = ContentUris.parseId(eUri);
            if (eventId == 0) { // 插入失败
                return -1;
            }

            /** 插入提醒 - 依赖插入日程成功 */
            ContentValues reminderValues = new ContentValues();
            // uri.getLastPathSegment();
            reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventId);
            reminderValues.put(CalendarContract.Reminders.MINUTES, tipTimeMills); // 提前tipTimeMills分钟提醒
            reminderValues.put(CalendarContract.Reminders.METHOD,
                    CalendarContract.Reminders.METHOD_ALERT);
            Uri rUri = context.getContentResolver().insert(CalendarContract.Reminders.CONTENT_URI,
                    reminderValues);
            if (rUri == null || ContentUris.parseId(rUri) == 0) {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        return eventId;
    }

    @SuppressLint("MissingPermission")
    public static void deleteCalendarEvent(Context context, String title) {
        if (context == null) {
            return;
        }

        Cursor eventCursor = context.getContentResolver().query(CalendarContract.Events.CONTENT_URI,
                null, null, null, null);
        try {
            if (eventCursor == null) { // 查询返回空值
                return;
            }
            if (eventCursor.getCount() > 0) {
                // 遍历所有事件，找到title跟需要查询的title一样的项
                for (eventCursor.moveToFirst(); !eventCursor.isAfterLast(); eventCursor.moveToNext()) {
                    String eventTitle = eventCursor.getString(eventCursor.getColumnIndex("title"));
                    if (!TextUtils.isEmpty(title) && title.equals(eventTitle)) {
                        int id = eventCursor.getInt(eventCursor
                                .getColumnIndex(CalendarContract.Calendars._ID)); // 取得id
                        Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Reminders.CONTENT_URI, id);
                        int rows = context.getContentResolver().delete(deleteUri, null, null);
                        if (rows == -1) { // 事件删除失败
                            return;
                        }
                    }
                }
            }
        } finally {
            if (eventCursor != null) {
                eventCursor.close();
            }
        }
    }

    public static boolean deleteEventById(Context context, long envId) {
        if (envId <= 0) {
            return false;
        }
        Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, envId);
        int rows = context.getContentResolver().delete(deleteUri, null, null);
        if (rows == -1) { // 事件删除失败
            return false;
        }
        return true;
    }
}

