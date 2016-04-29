package com.sysunite.rws.deflecties;

import java.util.GregorianCalendar;

public class StationInfo {
	//TODO unclear what else is needed here
	public Integer sideOfRoad;
	public double station; // converted always to km
	public String lane;
	public GregorianCalendar dateTime;
	public String comment = "";
	public boolean isTemperatureInCelsius = true;
	public Double temperatureAsphalt;
	public Double temperatureSurface;
	public Double temperatureAir;
	
	@Override
	public String toString() {
		return Utils.f(station);
	}
}
