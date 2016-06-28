package com.imaginat.androidtodolist.customlayouts;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.imaginat.androidtodolist.R;
import com.imaginat.androidtodolist.customlayouts.alarm.DatePickerFragment;
import com.imaginat.androidtodolist.customlayouts.alarm.DialogMapFragment;
import com.imaginat.androidtodolist.customlayouts.alarm.TimePickerFragment;
import com.imaginat.androidtodolist.managers.AlarmReceiver;
import com.imaginat.androidtodolist.models.ToDoListItem;
import com.imaginat.androidtodolist.managers.ToDoListItemManager;
import com.imaginat.androidtodolist.data.DbSchema;
import com.imaginat.androidtodolist.google.Constants;
import com.imaginat.androidtodolist.google.location.CoordinatesResultReceiver;
import com.imaginat.androidtodolist.google.location.FenceData;
import com.imaginat.androidtodolist.google.location.GeoCoder;
import com.imaginat.androidtodolist.google.LocationUpdateService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by nat on 5/1/16.
 */
public class ToDoListOptionsFragment extends Fragment
        implements CoordinatesResultReceiver.ICoordinateReceiver {


    private ToDoListItem mToDoListItem;

    private static final String CALENDAR = "CAL";
    private static final String GEOFENCE = "GEO";

    private CoordinatesResultReceiver mCoordinatesResultReceiver;
    Location mLocation;

    //===================INTERFACE USED TO COMMNICATE TO HOSTING ACTIVITY ===========================================
    public interface IGeoOptions {

        public LocationUpdateService getServiceReference();

        public void requestStartOfLocationUpdateService();

        public void requestStopOfLocationUpdateService();
    }


    private static final String TAG = ToDoListOptionsFragment.class.getSimpleName();
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;

    private String mListID, mItemID;


    AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    // private TimePicker alarmTimePicker;
    private EditText mEditTextOfListItem;
    private TextView alarmTextView;
    private TextView displayAlarmDateTextView, displayAlarmTimeTextView;
    //private Date mAlarmDate,mAlarmTime;
    private Calendar mAlarmCalendar;
    private Button mCoordinatesToAddressButton;
    private Button mRemoveFenceButton;
    private IGeoOptions mIGeoOptions;
    private EditText mStreetAddress_EditText, mCity_EditText, mState_EditText, mZip_EditText;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.todos_more_options, container, false);

        //I. GET REFERENCE TO VIEWS
        mEditTextOfListItem = (EditText) view.findViewById(R.id.theItemText_EditText);
        displayAlarmTimeTextView = (TextView) view.findViewById(R.id.displayTime_textView);
        displayAlarmDateTextView = (TextView) view.findViewById(R.id.displayDate_textView);

        mStreetAddress_EditText = (EditText) view.findViewById(R.id.streetAddress_editText);
        mCity_EditText = (EditText) view.findViewById(R.id.city_editText);
        mState_EditText = (EditText) view.findViewById(R.id.state_editText);
        mZip_EditText = (EditText) view.findViewById(R.id.zipCode_editText);

        // alarmTimePicker = (TimePicker) view.findViewById(R.id.alarmTimePicker);
        alarmTextView = (TextView) view.findViewById(R.id.alarmText);
        Switch alarmToggle = (Switch) view.findViewById(R.id.alarmToggle);
        alarmManager = (AlarmManager) getActivity().getSystemService(getContext().ALARM_SERVICE);

        //II. GET INFO BASED ON TO THE listID,itemID
        ToDoListItemManager itemManager = ToDoListItemManager.getInstance(getContext());
        mToDoListItem = itemManager.getSingleListItem(mListID, mItemID);
        Log.d(TAG, mToDoListItem.getText());

        mAlarmCalendar = Calendar.getInstance();

        //III. POPULATE PAGE CONTROLS WITH INFO (from database)
        //A.set the text
        mEditTextOfListItem.setText(mToDoListItem.getText());

        //B. set calendar alarm if set
        SimpleDateFormat sf;
        if (mToDoListItem.isCalendarAlarm()) {
            //get the date
            sf = new SimpleDateFormat("MM.dd.yy H:m");
            String theDate = mToDoListItem.getAlarmMonth() + "." + mToDoListItem.getAlarmDay() + "." + (mToDoListItem.getAlarmYear() - 2000);
            //get the time
            String theTime = mToDoListItem.getAlarmHour() + ":" + mToDoListItem.getAlarmMin();
            Date date = null;

            try {
                date = sf.parse(theDate + " " + theTime);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            setAlarmDate(date);
            setAlarmCalendarTime(date);
            boolean isActive = mToDoListItem.isCalendarAlarmActive();
            if (isActive) {
                alarmToggle.setChecked(true);

            }
            setCalendarAlarm(isActive);


        }
        //3. set geofence alarm if set
        if (mToDoListItem.isGeoFenceAlarm()) {

            String streetAddress = mToDoListItem.getStreet();
            String city = mToDoListItem.getCity();
            String state = mToDoListItem.getState();
            String zip = mToDoListItem.getZip();
            boolean isActive = mToDoListItem.isGeoAlarmActive();

            mStreetAddress_EditText.setText(streetAddress);
            mCity_EditText.setText(city);
            mState_EditText.setText(state);
            mZip_EditText.setText(zip);

            Switch alarmGEOToggle = (Switch) view.findViewById(R.id.alarmGEOToggle);
            alarmGEOToggle.setChecked(isActive);

            //setGeoFenceAlarm(isActive);


        }


        Button selectDateButton = (Button) view.findViewById(R.id.selectDate_button);
        selectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(new Date(System.currentTimeMillis()));
                //DatePickerFragment dialog = new DatePickerFragment();
                dialog.setTargetFragment(ToDoListOptionsFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        Button selectTimeButton = (Button) view.findViewById(R.id.selectTime_button);
        selectTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(new Date(System.currentTimeMillis()));
                //TimePickerFragment dialog = new TimePickerFragment();
                dialog.setTargetFragment(ToDoListOptionsFragment.this, REQUEST_TIME);
                dialog.show(manager, DIALOG_TIME);

            }
        });


        alarmToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                setCalendarAlarm(isChecked);
            }
        });


        Switch alarmGEOToggle = (Switch) view.findViewById(R.id.alarmGEOToggle);
        alarmGEOToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setGeoFenceAlarm(isChecked);
            }
        });


        Button testButton = (Button) view.findViewById(R.id.test_button);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "DialogMa testbutton");
                DialogMapFragment newFragment = new DialogMapFragment();
                newFragment.setCurrentLocation(mLocation);
                newFragment.show(getActivity().getSupportFragmentManager(), "options");

//                SupportMapFragment supportMapFragment = newFragment.getFragment();
//
//
//                supportMapFragment.getMapAsync(new OnMapReadyCallback() {
//                    @Override
//                    public void onMapReady(GoogleMap googleMap) {
//                        Log.d(TAG,"onMapReady");
//                        Toast.makeText(getContext(), "onMapReady", Toast.LENGTH_SHORT).show();
//                        googleMap.addMarker(new MarkerOptions().position(new LatLng(mLocation.getLatitude(), mLocation.getLongitude())).title("marker title").icon(BitmapDescriptorFactory.fromResource(R.drawable.alarm_clock_white)));
//                        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//                            @Override
//                            public void onMapClick(LatLng latLng) {
//                                Toast.makeText(getContext(), "onMapClick", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
//                });
//                Intent myIntent = new Intent(getContext(), GeofenceTransitionsIntentService.class);
//                myIntent.setAction("com.imaginat.androidtodolist.LOCATiON_RECEIVED");
//                pendingIntent = PendingIntent.getBroadcast(getContext(), ToDoListOptionsFragment.this.createAlarmTag(GEOFENCE),
//                        myIntent, 0);
//                mIGeoOptions.testButton(pendingIntent);
            }
        });
        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            setAlarmDate(date);

        } else if (requestCode == REQUEST_TIME) {
            Date date = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            setAlarmCalendarTime(date);

        }


    }

    public void setIGeoOptions(IGeoOptions IGeoOptions) {
        mIGeoOptions = IGeoOptions;
    }

    public void setAlarmText(String alarmText) {
        alarmTextView.setText(alarmText);
    }

    public String getListID() {
        return mListID;
    }

    public void setListID(String listID) {
        mListID = listID;
    }

    public String getItemID() {
        return mItemID;
    }

    public void setItemID(String itemID) {
        mItemID = itemID;
    }

    //=====================CALENDAR ALARM RELATED HELPERS========================================
    private void setAlarmDate(Date date) {
        Calendar result = Calendar.getInstance();
        result.setTime(date);
        // mAlarmDate=date;
        SimpleDateFormat sf = new SimpleDateFormat("MM.dd.yy");
        displayAlarmDateTextView.setText(sf.format(date));
        mAlarmCalendar.set(Calendar.MONTH, result.get(Calendar.MONTH));
        mAlarmCalendar.set(Calendar.DAY_OF_MONTH, result.get(Calendar.DAY_OF_MONTH));
        mAlarmCalendar.set(Calendar.YEAR, result.get(Calendar.YEAR));
    }

    private void setAlarmCalendarTime(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        SimpleDateFormat sf = new SimpleDateFormat("hh:mm:ss a");
        displayAlarmTimeTextView.setText(sf.format(date));
        mAlarmCalendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
        mAlarmCalendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
    }

    private String getAlarmID(String type) {

        return "_L" + mListID + "I" + mItemID + type;
    }

    private int createAlarmTag(String type) {
        String result = getAlarmID(type);
        int strlen = result.length();
        int hash = 7;
        for (int i = 0; i < strlen; i++) {
            hash = hash * 31 + result.charAt(i);
        }
        return hash;
    }


    private void setCalendarAlarm(boolean onOff) {
        //SETTING THE CALENDAR ALARM
        ToDoListItemManager listItemManager = ToDoListItemManager.getInstance(getContext());

        //I. CREATING THE INTENT (using a custom tag). Intent can be used to start or cancel alarm
        Intent myIntent = new Intent(getContext(), AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(getContext(), ToDoListOptionsFragment.this.createAlarmTag(CALENDAR),
                myIntent, 0);

        //II. IF THE switch is set to ON, update info, save it, and
        if (onOff) {
            alarmManager.set(AlarmManager.RTC, mAlarmCalendar.getTimeInMillis(), pendingIntent);
            //now save current setting in database
            DateFormat df = new SimpleDateFormat("MM.dd:yy:HH:mm:ss");
            ToDoListItemManager itemManager = ToDoListItemManager.getInstance(getContext());
            itemManager.saveCalendarAlarm(getAlarmID(CALENDAR), mItemID, 1 + mAlarmCalendar.get(Calendar.MONTH), mAlarmCalendar.get(Calendar.DATE), mAlarmCalendar.get(Calendar.YEAR),
                    mAlarmCalendar.get(Calendar.HOUR_OF_DAY), mAlarmCalendar.get(Calendar.MINUTE), true);
        } else {
            //cancel the pending intent here  (with alarmManager and in database)
            alarmManager.cancel(pendingIntent);
            ToDoListItemManager itemManager = ToDoListItemManager.getInstance(getContext());
            itemManager.toggleCalendarAlarm(getAlarmID(CALENDAR), 0);
        }
    }

    //=========================GEO FENCE RELATED====================================================
    private void setGeoFenceAlarm(boolean onOff) {
        Log.d(TAG, "alarmGEOToggle set");
        if (onOff) {
            String streetAddress = mStreetAddress_EditText.getText().toString();
            String cityAddress = mCity_EditText.getText().toString();
            String stateAddress = mState_EditText.getText().toString();
            String zipAddress = mZip_EditText.getText().toString();
            setGeoFenceAddress(streetAddress, cityAddress, stateAddress, zipAddress, ToDoListOptionsFragment.this.getAlarmID(GEOFENCE), mItemID, mListID);

            //mIGeoOptions.setGeoFenceAddress(streetAddress, cityAddress, stateAddress, zipAddress);
            //mIGeoOptions.setGeoFenceAddress(streetAddress, cityAddress, stateAddress, zipAddress, ToDoListOptionsFragment.this.getCalendarAlarmID(GEOFENCE),mItemID,mListID);
        } else {
            //mIGeoOptions.removeGeoFence(getCalendarAlarmID(GEOFENCE));
            removeGeoFence();


        }
    }

    public void setGeoFenceAddress(String street, String city, String state, String zipCode, String alarmTag, String reminderID, String listID) {
        Log.d(TAG, "Inside setGeoFenceAddress " + street + " " + city + " " + state + " " + zipCode);
        if (mCoordinatesResultReceiver == null) {
            mCoordinatesResultReceiver = new CoordinatesResultReceiver(new Handler());
        }
        HashMap<String, String> data = new HashMap<>();
        data.put(DbSchema.geoFenceAlarm_table.cols.STREET, street);
        data.put(DbSchema.geoFenceAlarm_table.cols.CITY, city);
        data.put(DbSchema.geoFenceAlarm_table.cols.STATE, state);
        data.put(DbSchema.geoFenceAlarm_table.cols.ZIPCODE, zipCode);
        data.put(DbSchema.geoFenceAlarm_table.cols.REMINDER_ID, reminderID);
        data.put(DbSchema.geoFenceAlarm_table.cols.ALARM_TAG, alarmTag);
        ToDoListItemManager listItemManager = ToDoListItemManager.getInstance(getContext());
        listItemManager.saveGeoFenceAlarm(alarmTag, reminderID, data);

        GeoCoder.getLocationFromAddress(getContext(), street + " " + city + "," + state + " " + zipCode, alarmTag, reminderID, listID, mCoordinatesResultReceiver);
        //ToDoListOptionsFragment currentFragment =(ToDoListOptionsFragment) MainActivity.this.getSupportFragmentManager().findFragmentById(R.id.my_frame);
        mCoordinatesResultReceiver.setResult(this);


    }

    private void removeGeoFence() {
        ToDoListItemManager listItemManager = ToDoListItemManager.getInstance(getContext());
        HashMap<String, String> data = new HashMap<>();
        data.put(DbSchema.geoFenceAlarm_table.cols.IS_ACTIVE, "0");
        listItemManager.saveGeoFenceAlarm(getAlarmID(GEOFENCE), mItemID, data);
        mIGeoOptions.requestStopOfLocationUpdateService();

        //now get refence
        mIGeoOptions.requestStartOfLocationUpdateService();
        mIGeoOptions.getServiceReference();
        LocationUpdateService locationUpdateService = mIGeoOptions.getServiceReference();

        //Get reference to the item (in order to get the text & any other info)
        //ToDoListItem toDoItem = listItemManager.getSingleListItem(mListID,mItemID);


        locationUpdateService.removeGeofencesByTag(mToDoListItem.getAlarmTag());

    }

    /**
     * @param resultCode
     * @param resultData
     */

    @Override
    public void onReceiveCoordinatesResult(int resultCode, Bundle resultData) {

        if (Constants.SUCCESS_RESULT == resultCode) {
            //NOW ADD FENCE
            Location lastLocation = resultData.getParcelable(Constants.RESULT_DATA_KEY);
            String requestID = resultData.getString(Constants.ALARM_TAG);
            String reminderID = resultData.getString(Constants.REMINDER_ID);
            String listID = resultData.getString(Constants.LIST_ID);
            //mLocationServices.addToGeoFenceList(requestID, lastLocation.getLatitude(), lastLocation.getLongitude());
            Log.d(TAG, "ABOUT TO SAVE SOME INFO");

            //save info to local databasae
            HashMap<String, String> data = new HashMap<>();
            data.put(DbSchema.geoFenceAlarm_table.cols.ALARM_TAG, requestID);
            data.put(DbSchema.geoFenceAlarm_table.cols.LATITUDE, Double.toString(lastLocation.getLatitude()));
            data.put(DbSchema.geoFenceAlarm_table.cols.LONGITUDE, Double.toString(lastLocation.getLongitude()));
            data.put(DbSchema.geoFenceAlarm_table.cols.IS_ACTIVE, "1");

            mLocation = lastLocation;
            ToDoListItemManager listItemManager = ToDoListItemManager.getInstance(getContext());
            listItemManager.saveGeoFenceAlarm(requestID, reminderID, data);

            //Get reference to the item (in order to get the text & any other info)
            ToDoListItem toDoItem = listItemManager.getSingleListItem(listID, reminderID);

            //now get refence
            mIGeoOptions.requestStartOfLocationUpdateService();
            mIGeoOptions.getServiceReference();
            LocationUpdateService locationUpdateService = mIGeoOptions.getServiceReference();


            //locationUpdateService.addToGeoFenceList(theText,lastLocation.getLatitude(),lastLocation.getLongitude());
            ArrayList<FenceData> datas = listItemManager.getActiveFenceData();
            locationUpdateService.populateGeofenceList(datas);
            locationUpdateService.addGeofences(createAlarmTag(GEOFENCE));
        }


    }
}
