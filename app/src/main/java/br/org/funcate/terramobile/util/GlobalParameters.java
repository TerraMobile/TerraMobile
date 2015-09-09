package br.org.funcate.terramobile.util;

/**
 * Created by Andre Carvalho on 03/09/15.
 * Commonly used to store the keys used on process to swap messages between activities.
 */
public class GlobalParameters {

    /**
     * Used to send and receive a broadcast message between MainActivity and SettingActivity
     */
    public static final String ACTION_BROADCAST_MAIN_ACTIVITY = "SETTING_ACTION";

    /**
     * Key to inform the selection state of the GPSTracker configuration on menu settings.
     */
    public static final String STATE_GPS_LOCATION = "STATE_GPS_LOCATION";
    public static final String STATE_GPS_CENTER = "STATE_GPS_CENTER";
}
