/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.eonsound.esm;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 * see https://btprodspecificationrefs.blob.core.windows.net/assigned-values/16-bit%20UUID%20Numbers%20Document.pdf
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    //public static String HEART_RATE_SERVICE = "0000180d-0000-1000-8000-00805f9b34fb";

    public static String DEVICE_INFO_UUID = "0000180a-0000-1000-8000-00805f9b34fb";
    public static String GATT_OBJ_MODEL_NUMBER_STR_UUID = "00002a24-0000-1000-8000-00805f9b34fb";
    public static String GATT_OBJ_SERIAL_NUMBER_STR_UUID = "00002a25-0000-1000-8000-00805f9b34fb";
    public static String GATT_OBJ_FIRMWARE_REV_STR_UUID	= "00002a26-0000-1000-8000-00805f9b34fb";
    public static String GATT_OBJ_HARDWARE_REV_STR_UUID = "00002a27-0000-1000-8000-00805f9b34fb";
    public static String GATT_OBJ_SOFTWARE_REV_STR_UUID = "00002a28-0000-1000-8000-00805f9b34fb";
    public static String GATT_OBJ_MANUFACTURER_NAME_STR_UUID = "00002a29-0000-1000-8000-00805f9b34fb";

    public static String strGattUuidService_Battery = "0000180f-0000-1000-8000-00805f9b34fb";
    public static String strGattUuidCharacteristic_Battery = "00002a19-0000-1000-8000-00805f9b34fb";

    public static String BAGPIPE_MANOMETER_SERVICE = "25112096-1bf5-1aa5-4841-2ee3960eb110";
    public static String CHARACTERISTIC_PRESSURE = "00002a6d-0000-1000-8000-00805f9b34fb";
    public static String GATT_CHR_UUID_MEASUREMENT_INTERVAL = "00002a21-0000-1000-8000-00805f9b34fb";
/*
    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put(DEVICE_INFO_UUID, "Device Info");
        attributes.put(BAGPIPE_MANOMETER_SERVICE, "Bagpipe manometer");

        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put(CHARACTERISTIC_PRESSURE, "Pressure");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }

    public static boolean bIsHeartRate(String uuid) {
        String name = attributes.get(uuid);
        return name.equals("Heart Rate Service");
    }

    public static boolean bIsHeartRateMeasurement(String uuid) {
        String name = attributes.get(uuid);
        return name.equals("Heart Rate Measurement");
    }
    */

}
