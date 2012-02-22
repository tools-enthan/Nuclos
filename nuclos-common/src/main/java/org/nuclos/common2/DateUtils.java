//Copyright (C) 2010  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.common2;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.nuclos.common.collection.Pair;

/**
 * Utility methods for <code>Date</code>s.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph Radig</a>
 * @version 01.00.00
 */
public class DateUtils {

	/**
	 * the number of milliseconds per day (where a day has 24 hours).
	 */
	public static final int MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;

	private DateUtils() {
	}

	/**
	 * @return a Date object with today's date and time set to 00:00:00.
	 * @postcondition result != null
	 * @postcondition isPure(result)
	 */
	public static Date today() {
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		final Date result = calendar.getTime();
		assert result != null;
		assert isPure(result);
		return result;
	}

	/**
	 * @return a Date object with today's date and the current time.
	 * @postcondition result != null
	 */
	public static Date now() {
		return new Date();
	}

	/**
	 * @param date is not changed by this method. May be <code>null</code>.
	 * @return the pure date (aka "day", that is the date without time of day) of the given Date object (if any).
	 * @postcondition date != null <--> result != null
	 * @postcondition date != null --> result.getHours() == 0
	 * @postcondition date != null --> result.getMinutes() == 0
	 * @postcondition date != null --> result.getSeconds() == 0
	 * @todo consider calling this method getDay(). On the other hand, that name could be confused with Date.getDay()...
	 */
	public static Date getPureDate(Date date) {
		final Date result = (date == null) ? null : org.apache.commons.lang.time.DateUtils.truncate(date, Calendar.DATE);
		assert (date != null) == (result != null);
		
		assert (date == null) || result.getHours() == 0;
		assert (date == null) || result.getMinutes() == 0;
		assert (date == null) || result.getSeconds() == 0;
		return result;
	}

	/**
	 * @param date is not altered.
	 * @return Is the given date pure?
	 * @precondition date != null
	 * @postcondition result --> date.getHours() == 0
	 * @postcondition result --> date.getMinutes() == 0
	 * @postcondition result --> date.getSeconds() == 0
	 * @see	#getPureDate(Date)
	 */
	public static boolean isPure(Date date) {
		final boolean result = date.equals(getPureDate(date));
		assert !result || date.getHours() == 0;
		assert !result || date.getMinutes() == 0;
		assert !result || date.getSeconds() == 0;
		return result;
	}

	/**
	 * @param date1
	 * @param date2
	 * @return Do the given Date objects represent the same day? The time of day (hours, minutes, seconds, millis) is ignored.
	 * @precondition date1 != null
	 * @precondition date2 != null
	 */
	public static boolean equalsPureDate(Date date1, Date date2) {
		return getPureDate(date1).equals(getPureDate(date2));
	}

	/**
	 * copies the given date (useful making for defensive copies, as described in Josh Bloch: "Effective Java", item 24).
	 * @param date may be null.
	 * @return null or a new Date object that equals <code>date</code>.
	 * @postcondition date == null --> result == null
	 * @postcondition date != null --> result != date
	 * @postcondition LangUtils.equals(result, date);
	 */
	public static Date copyDate(Date date) {
		final Date result = (date == null) ? null : new Date(date.getTime());
		assert LangUtils.implies(date == null, result == null);
		assert LangUtils.implies(date != null, result != date);
		assert LangUtils.equals(result, date);
		return result;
	}

	/**
	 * @param date may be <code>null</code>.
	 * @return a decent localized String representation of the given date, as in <code>DateFormat.getDateInstance().format(date)</code>.
	 * @postcondition result != null <--> date != null
	 */
	public static String toString(Date date) {
		return toString(date, null);
	}

	/**
	 * @param date may be <code>null</code>.
	 * @param sNullRepresentation the String representation for a <code>null</code> date.
	 * @return a decent localized String representation of the given date, as in <code>DateFormat.getDateInstance().format(date)</code>.
	 * @postcondition (date != null) --> result != null
	 * @postcondition (date == null) --> result == sNullRepresentation
	 */
	public static String toString(Date date, String sNullRepresentation) {
		final String result = (date == null) ? sNullRepresentation : SpringLocaleDelegate.getInstance().formatDate(date); // DateFormat.getDateInstance().format(date);
		assert !(date != null) || result != null;
		assert !(date == null) || result == sNullRepresentation;

		return result;
	}

	/**
	 * @param date is not changed.
	 * @param iDayCount number of days to add (may be 0 or negative).
	 * @return a new date resulting from adding the given number of days to the given date.
	 */
	public static Date addDays(Date date, int iDayCount) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, iDayCount);
		return calendar.getTime();
		//return new Date(date.getTime() + iDayCount * MILLISECONDS_PER_DAY);
	}
	

	/**
	 * @param date
	 * @param iMonthCount number of months to add (may be 0 or negative).
	 * @return a new date resulting from adding the given number of months to the given date.
	 */
	public static Date addMonths(Date date, int iMonthCount) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, iMonthCount);
		return calendar.getTime();
	}

	/**
	 * compares two "from" dates where <code>null</code> means -infinity.
	 * @param dateFrom1
	 * @param dateFrom2
	 * @return -1/0/+1 (as usual)
	 */
	public static int compareDateFrom(Date dateFrom1, Date dateFrom2) {
		final int result;

		if (dateFrom1 == null) {
			result = (dateFrom2 == null ? 0 : -1);
		}
		else if (dateFrom2 == null) {
			result = 1;
		}
		else {
			result = dateFrom1.compareTo(dateFrom2);
		}
		return result;
	}

	/**
	 * compares two "until" dates where <code>null</code> means +infinity.
	 * @param dateUntil1
	 * @param dateUntil2
	 * @return -1/0/+1 (as usual)
	 */
	public static int compareDateUntil(Date dateUntil1, Date dateUntil2) {
		final int result;

		if (dateUntil1 == null) {
			result = (dateUntil2 == null ? 0 : 1);
		}
		else if (dateUntil2 == null) {
			result = -1;
		}
		else {
			result = dateUntil1.compareTo(dateUntil2);
		}
		return result;
	}
	
	/**
	 * get actual date and time as string
	 */
	public static String getActualDateAndTime() {
		//DateFormat df = SimpleDateFormat.getDateTimeInstance();
		//return df.format(new Date());
		return SpringLocaleDelegate.getInstance().formatDateTime(now());
	}
	
	/**
	 * get the given date and time as string
	 * @param date
	 */
	public static String getDateAndTime(Date date) {
		//DateFormat df = SimpleDateFormat.getDateTimeInstance();
		//return df.format(date);
		return SpringLocaleDelegate.getInstance().formatDateTime(date);
	}
	
	static final long ONE_HOUR = 60 * 60 * 1000L;
	public static int daysBetween(Date d1, Date d2){
		return new Long((getPureDate(d2).getTime() - getPureDate(d1).getTime() + ONE_HOUR) / (ONE_HOUR * 24)).intValue();
	}
	
	private static Map<String, Integer> mapCalcStrings = new HashMap<String, Integer>();
	static {
		mapCalcStrings.put("D", Calendar.DAY_OF_YEAR);
		mapCalcStrings.put("T", Calendar.DAY_OF_YEAR);
		mapCalcStrings.put("W", Calendar.WEEK_OF_YEAR);
		mapCalcStrings.put("M", Calendar.MONTH);
		mapCalcStrings.put("Y", Calendar.YEAR);
		mapCalcStrings.put("J", Calendar.YEAR);
	}
	
	private static enum CalcFunction {
		ADD, SUBTRACT, SET;
	}
	
	private static class CalcPair extends Pair<Integer, Integer> {
	};
	
	private static CalcPair getCalcPair(String input) {
		CalcPair result = new CalcPair();
		
		String end = input.substring(input.length()-1, input.length());
		if ("[".equals(end) || "]".equals(end)) {
			// set start or end
			String field = input.substring(input.length()-2, input.length()-1);
			result.x = mapCalcStrings.get(field);
			result.y = ("[".equals(end)) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
		} else {
			String value = input.substring(0, input.length()-1);
			result.x = mapCalcStrings.get(end);
			result.y = Integer.parseInt(value);
		}
		
		return result;
	}
	
	private static void calc(GregorianCalendar result, CalcFunction cf, CalcPair cp) {
		switch (cf) {
		case ADD:
			result.add(cp.x, cp.y);
			break;
		case SUBTRACT:
			result.add(cp.x, cp.y*(-1));
			break;
		case SET:
			switch (cp.x) {
			case Calendar.YEAR:
				result.set(Calendar.DAY_OF_YEAR, cp.y==Integer.MIN_VALUE ? 
						result.getActualMinimum(Calendar.DAY_OF_YEAR) : 
						result.getActualMaximum(Calendar.DAY_OF_YEAR));
				break;
			case Calendar.MONTH:
				result.set(Calendar.DAY_OF_MONTH, cp.y==Integer.MIN_VALUE ? 
						result.getActualMinimum(Calendar.DAY_OF_MONTH) : 
						result.getActualMaximum(Calendar.DAY_OF_MONTH));
				break;
			case Calendar.WEEK_OF_YEAR:
				result.set(Calendar.DAY_OF_WEEK, cp.y==Integer.MIN_VALUE ? 
						Calendar.MONDAY : 
						Calendar.SUNDAY);
				break;
			}
			break;
		}
	}
	
	public static Date calc(String calculation) {
		calculation = calculation.toUpperCase();
		GregorianCalendar result = new GregorianCalendar();
		result.setTime(today());
		
		StringBuffer operand = new StringBuffer();
		CalcFunction cf = null;
		for (int i = 0; i < calculation.length(); i++) {
			char c = calculation.charAt(i);
			
			switch (c) {
			case '+':
			case '-':
			case '.':
				if (cf != null) {
					calc(result, cf, getCalcPair(operand.toString()));
					operand = new StringBuffer();
				}
				break;
			}
				
			switch (c) {
			case '+':
				cf = CalcFunction.ADD;
				break;
			case '-':
				cf = CalcFunction.SUBTRACT;
				break;
			case '.':
				cf = CalcFunction.SET;
				break;
			default:
				operand.append(c);
			}
		}
		
		if (operand.length() > 0 && cf != null) {
			calc(result, cf, getCalcPair(operand.toString()));
		}
		
		return result.getTime();
	}

}	// class DateUtils
