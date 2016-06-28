package com.imaginat.androidtodolist.data;

/**
 * Created by nat on 5/10/16.
 */
public class DbSchema {

    //=========================================================================
    public static final class lists_table{
        public static final String NAME = "listOfLists";
        public static final String[] ALL_COLUMNS = new String[]{cols.LIST_ID,cols.LIST_TITLE,cols.LIST_ICON};//
        public static final String createCommand =
                "CREATE TABLE "+NAME+"("+
                        cols.LIST_ID+" INTEGER PRIMARY KEY, "+
                        cols.LIST_TITLE+" TEXT, "+
                        cols.LIST_ICON+" INTEGER DEFAULT 0 "+
                        ")";
        public static final class cols{
            public static final String LIST_ID="list_id";
            public static final String LIST_TITLE="list_title";
            public static final String LIST_ICON="list_icon";
        }
    }

    //=========================================================================
    public static final class reminders_table{
        public static final String NAME = "reminders";
        public static final String[] ALL_COLUMNS = new String[]{cols.REMINDER_ID,cols.LIST_ID,cols.REMINDER_TEXT,cols.IS_COMPLETED};
        public static final String createCommand=
                "CREATE TABLE "+NAME+"("+
                        cols.REMINDER_ID+" INTEGER PRIMARY KEY, "+
                        cols.LIST_ID+" INTEGER NOT NULL, "+
                        cols.REMINDER_TEXT+" TEXT NOT NULL, "+
                        cols.IS_COMPLETED+" INTEGER DEFAULT 0, "+
                        "FOREIGN KEY("+cols.LIST_ID+") REFERENCES "+lists_table.NAME+"("+lists_table.cols.LIST_ID+")"+
                        ")";

        public static final class cols{
            public static final String REMINDER_ID="reminder_id";
            public static final String LIST_ID="list_id";
            public static final String REMINDER_TEXT="reminderText";
            public static final String IS_COMPLETED="isCompleted";
        }
    }
    //=============================================================================
    public static final class calendarAlarm_table{
        public static final String NAME="calendarAlarms";
        public static final String[] ALL_COLUMNS = new String[]{cols.CALENDAR_ALARM_ID,cols.REMINDER_ID,cols.DAY,cols.MONTH,cols.YEAR,cols.HOUR,cols.MINUTES,cols.IS_ACTIVE};

        public static final String createCommand=
                "CREATE TABLE "+NAME+"("+
                        cols.CALENDAR_ALARM_ID+" STRING PRIMARY KEY, "+
                        cols.REMINDER_ID+" INTEGER NOT NULL, "+
                        cols.DAY+" INTEGER NOT NULL, "+
                        cols.MONTH+" INTEGER NOT NULL, "+
                        cols.YEAR+" INTEGER NOT NULL, "+
                        cols.HOUR+" INTEGER NOT NULL, "+
                        cols.MINUTES+" INTEGER NOT NULL, "+
                        cols.IS_ACTIVE+" INTEGER DEFAULT 0,"+
                        "FOREIGN KEY("+cols.REMINDER_ID+") REFERENCES "+reminders_table.NAME+"("+reminders_table.cols.REMINDER_ID+")"+
                        ")";


        public static final class cols{
            public static final String CALENDAR_ALARM_ID="calendarAlarm_id";
            public static final String REMINDER_ID="reminder_id";
            public static final String DAY ="alarmDay";
            public static final String MONTH="alarmMonth";
            public static final String YEAR="alarmYear";
            public static final String HOUR="alarmHour";
            public static final String MINUTES="alarmMinutes";
            public static final String IS_ACTIVE="isAlarmActive";

        }
    }
    //========================================================================
    public static final class geoFenceAlarm_table{
        public static final String NAME="geoFenceAlarm";
        public static final String[] ALL_COLUMNS=new String[]{cols.GEOFENCE_ALARM_ID,cols.REMINDER_ID,cols.STREET,cols.CITY,cols.STATE,cols.ZIPCODE,cols.LONGITUDE,
            cols.LATITUDE,cols.ALARM_TAG,cols.RADIUS,cols.IS_ACTIVE};

        public static final String createCommand =
                "CREATE TABLE "+NAME+"("+
                        cols.GEOFENCE_ALARM_ID+" INTEGER PRIMARY KEY, "+
                        cols.REMINDER_ID+" INTEGER NOT NULL, "+
                        cols.STREET+" STRING, "+
                        cols.CITY+" STRING, "+
                        cols.STATE+" STRING, "+
                        cols.ZIPCODE+" STRING, "+
                        cols.LONGITUDE+" REAL, "+
                        cols.LATITUDE+" REAL, "+
                        cols.ALARM_TAG+" TEXT NOT NULL, "+
                        cols.RADIUS+" INTEGER NOT NULL, "+
                        cols.IS_ACTIVE+" INTEGER DEFAULT 0,"+
                        "FOREIGN KEY("+cols.REMINDER_ID+") REFERENCES "+reminders_table.NAME+"("+reminders_table.cols.REMINDER_ID+")"+
                        ")";

        public static final class cols{
            public static final String GEOFENCE_ALARM_ID="geoFenceAlarm_id";
            public static final String REMINDER_ID="reminder_id";
            public static final String STREET="street";
            public static final String CITY ="city";
            public static final String STATE="state";
            public static final String ZIPCODE="zipcode";
            public static final String LONGITUDE="longitude";
            public static final String LATITUDE="latitude";
            public static final String ALARM_TAG="alarmTag";
            public static final String RADIUS="meterRadius";
            public static final String IS_ACTIVE="isAlarmActive";
        }
    }
    //===========================================================================
    public static final class locationsOfAlarm_table{
        public static final String NAME="locationsOfAlarm";
        public static final String[] ALL_COLUMNS=new String[]{};



        public static final class cols{
            public static final String LOCATION_FENCE_ID="location_id";
            
        }
    }
}
