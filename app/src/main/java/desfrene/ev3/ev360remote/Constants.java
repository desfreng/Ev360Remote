package desfrene.ev3.ev360remote;

import java.util.UUID;

public final class Constants {
    public final static String GLOBAL_TAG = "desfrene.global";
    public final static String FCT_TAG = GLOBAL_TAG + ".functions";
    public final static String INFO_TAG = GLOBAL_TAG + ".info";
    public final static String DATA_TAG = GLOBAL_TAG + ".data";

    public final static UUID TARGET_UUID = UUID.fromString("7a20bfdf-a797-4c50-a870-d0e49362e9fd");

    public final static boolean LOG_ACTIVATED = false;
    public static final int BYTES_READ = 1;
    public static final int CONNECTION_LOST = 2;
    public static final int CONNECTION_FAILED = 3;
    public static final int CONNECTED = 4;
    public static final String DEVICE_NAME = GLOBAL_TAG + ".device_name";
}
