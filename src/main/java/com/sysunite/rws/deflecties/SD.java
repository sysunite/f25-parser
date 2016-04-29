package com.sysunite.rws.deflecties;

public class SD {
	public String id;
	public Integer type;
	public Double relativeGain;
	public Double absoluteGain;
	
	public SD(String id, Integer type, Double r, Double a) {
		this.id = id;
		this.type = type;
		relativeGain = r;
		absoluteGain = a;
	}
	
	@Override
	public String toString() {
		return id;
	}
}
