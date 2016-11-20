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
		_name = String.valueOf(saveddata[0]);
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
		//There is definitely a better solution to this, I'll revisit this soon.
		//TODO: make this more efficient.
		/*
		 * math logic: to get how many days are remaining, first find the difference between the amount of time that has
		 * passed between _date and current time. make that a fraction, times it by a hundred to make it a percentage, then
		 * times that percentage by _days to find out how many days have been consumed.
		 * then, take _days and subtract the above answer from days to find out how many days are remaining.
		 * 
		 * wait, can't i just divide the amount of time that has passed between _date and current time and then subtract
		 * that from _days?  commenting code = finding solutions. i'll implement that soon.
		 */
		return  (int) (_days - ((((((System.currentTimeMillis() / 1000L) - _date)) / (86400L * _days)) *  100) * _days));
	}
	
	public String toString() {
		return String.valueOf(this._name) + "," + String.valueOf(this._date) + "," + String.valueOf(this._days);
	}
	
	public boolean isDonor() {
		/* 
		 * if their days is 0, that means permanently donor (e.g. they bought donor status)
		 * 
		 * math logic: take the current unix time, subtract the original purchase date unix time, 
		 * and see if that's less than the amount of whole-digit days, times the amount of seconds
		 * in a day.
		*/
	    if (_days == 0 || ((System.currentTimeMillis() / 1000L) - _date) < (86400L * _days)) {
	    	return true;
	    }
	    return false;
	}
}
