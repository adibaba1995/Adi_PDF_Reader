package com.adisoftwares.bookreader;

import com.squareup.otto.Bus;

/**
 * Created by adityathanekar on 19/03/16.
 */
public class BusStation {
    private static Bus bus;

    private BusStation() {
    }

    public static Bus getBus() {
        if(bus == null)
            bus = new Bus();
        return bus;
    }
}
