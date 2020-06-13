package desfrene.ev3.ev360remote;

import java.util.UUID;

public final class Constants {
    public final static String GLOBAL_TAG = "desfrene.global";
    public final static String FCT_TAG = GLOBAL_TAG + ".functions";
    public final static String INFO_TAG = GLOBAL_TAG + ".info";
    public final static String DATA_TAG = GLOBAL_TAG + ".data";
    public final static String BLE_TAG = GLOBAL_TAG + ".bluetooth";

//    public final static UUID TARGET_UUID = UUID.fromString("f5b64ebf-f5e3-4a44-8c33-0e6ebfd1b2c3");

    public final static boolean LOG_ACTIVATED = false;
    public static final int CONNECTION_LOST = 2;
    public static final int CONNECTION_FAILED = 3;
    public static final int CONNECTED = 4;
    public static final int CONNECTION_CLOSED = 5;

    public static final String DEVICE_NAME = GLOBAL_TAG + ".device_name";
    public static final String TARGET_CHANNEL = GLOBAL_TAG + ".channel";
    public static final String TARGET_DEVICE = GLOBAL_TAG + ".targetDevice";
}
