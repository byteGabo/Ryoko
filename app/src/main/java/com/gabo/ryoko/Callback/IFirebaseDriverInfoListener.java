package com.gabo.ryoko.Callback;

import com.gabo.ryoko.Model.DriverGeoModel;

public interface IFirebaseDriverInfoListener {
    void OnDriverInfoLoadSuccess(DriverGeoModel driverGeoModel);
}
