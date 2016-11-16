package io.anyway.galaxy.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtil {
 
	private static final long SECOND = 1000;
	
	private static final long MINUTE = 60*SECOND;
	
	private static final long HOUR = 60*MINUTE;
	
	private static final long DAY = 24*HOUR;
	
	private DateUtil(){
		
	}
	
	public static java.sql.Date getPrevDate(int interval){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis() - DAY*interval);
		cal.set(Calendar.HOUR, -12);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return new java.sql.Date(cal.getTimeInMillis());
	}

	public static java.sql.Date getPrevSecDate(int interval){
		return new java.sql.Date(System.currentTimeMillis() - SECOND*interval);
	}

	public static Timestamp getPrevSecTimestamp(int interval){
		return new Timestamp(System.currentTimeMillis() - SECOND*interval);
	}

	public static void main(String args[]){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");		
		System.out.println(sdf.format(new Date(System.currentTimeMillis())));
		System.out.println(sdf.format(DateUtil.getPrevDate(5)));
		
		
	}
}
