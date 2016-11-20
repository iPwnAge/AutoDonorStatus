package com.ipwnage.autodonorstatus;

public class DonorData {
	private int _date;
	private int _days;
	private String _name;
	//Yeah, I get it. 2038 problem. If Minecraft is still a thing in 2038, I'll eat my shoe.
	
	public DonorData(String name, int date, int days) {
		_date = date;
		_days = days;
		_name = name;
	}
	
	public DonorData(String loaded) {
		String[] saveddata = loaded.split(",");
		_date = Integer.valueOf(saveddata[1]);
		_days = Integer.valueOf(saveddata[2]);
	}
	
	public int getDate() {
		return _date;
	}
	
	public void setDate(int date) {
		_date = date;
	}
	
	public int getDays() {
		return _days;
	}

	public String getName() {
		return _name;
	}
	
	public void setName(String name) {
		_name = name;
	}
	
	public void setDays(int days) {
		_days = days;
	}
	
	public int getDaysRemaining() {
		return ((int) (System.currentTimeMillis() / 1000) - _date) / (86400 * _days);
	}
	
	public String toString() {
		return String.valueOf(this._date) + "," + String.valueOf(this._days);
	}
	
	public boolean isDonor() {
		/* 
		 * if their days is 0, that means permanently donor (e.g. they bought donor status)
		 * 
		 * math logic: take the current unix time, subtract the original purchase date unix time, 
		 * and see if that's less than the amount of whole-digit days, times the amount of seconds
		 * in a day.
		*/
	    if (_days == 0 || ((int) (System.currentTimeMillis() / 1000) - _date) < (86400 * _days)) {
	    	return true;
	    }
	    return false;
	}
}
