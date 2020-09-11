package com.gabo.ryoko.Common;

import com.gabo.ryoko.Model.RiderModel;

public class Common {
    public static final String RIDER_INFO_REFERENCE = "Riders";

    public static RiderModel currentRider;

    public static String buildWelcomeMessage() {
        if (Common.currentRider != null)
        {
            return new StringBuilder("Hola ")
                    .append(Common.currentRider.getFirstName())
                    .append(" ")
                    .append(Common.currentRider.getLastName()).toString();
        }
        else
            return "";
    }
}
