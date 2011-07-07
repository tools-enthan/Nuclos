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
package org.nuclos.common.collect.collectable;



import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.DateTime;
import org.nuclos.common2.ExtendedRelativeDate;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.RelativeDate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common.NuclosPassword;
import org.nuclos.common.NuclosFatalException;

/**
 * Defines formatting and parsing of <code>CollectableField</code>s. This may be used to get the
 * value of a <code>CollectableField</code> into or out of a <code>CollectableTextComponent</code>.
 * This class is deliberately dependent on the default Locale.
 * @todo But, it shouldn't be! This class is to be used on a server as well as on a client.
 * @todo handle empty strings consistently!
 * Thus the Locale should be given as a parameter.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public abstract class CollectableFieldFormat {

	private static final Logger log = Logger.getLogger(CollectableFieldFormat.class);

	private static Map<Class<?>, CollectableFieldFormat> mpFormats;
	private static CollectableFieldFormat clctfformatDefault;

	/**
	 * formats the given value according to the given output format.
	 * @param sOutputFormat
	 * @param oValue
	 * @return a String representation of the given value.
	 */
	public abstract String format(String sOutputFormat, Object oValue);

	/**
	 * parses the given text according to the given input format.
	 * @param sInputFormat
	 * @param sText
	 * @return the parsed object
	 * @throws CollectableFieldFormatException
	 */
	public abstract Object parse(String sInputFormat, String sText) throws CollectableFieldFormatException;

	/**
	 * @param cls
	 * @return an appropriate instance of <code>CollectableFieldFormat</code> for the given data type.
	 */
	public static CollectableFieldFormat getInstance(Class<?> cls) {
		CollectableFieldFormat result = getFormat(cls);

		// We also allow classes derived from java.util.Date, especially java.sql.Date:
		if (result == null && java.util.Date.class.isAssignableFrom(cls)) {
			result = getFormat(Date.class);
		}
		if (result == null) {
//			throw new CommonFatalException("Unbekannter Feldtyp: " + cls.getName());
			log.warn("Default-Format erzeugt f\u00fcr Klasse " + cls.getName());
			return getDefaultFormat();
		}
		return result;
	}

	private static synchronized CollectableFieldFormat getDefaultFormat() {
		if (clctfformatDefault == null) {
			clctfformatDefault = new DefaultFormat();
		}
		return clctfformatDefault;
	}

	private static synchronized Map<Class<?>, CollectableFieldFormat> getMapOfFormats() {
		if (mpFormats == null) {
			mpFormats = new HashMap<Class<?>, CollectableFieldFormat>(6);
			mpFormats.put(String.class, new CollectableStringFormat());
			mpFormats.put(Date.class, new CollectableDateFormat());
			mpFormats.put(DateTime.class, new CollectableDateTimeFormat());
			mpFormats.put(Integer.class, new CollectableIntegerFormat());
			mpFormats.put(Double.class, new CollectableDoubleFormat());
			mpFormats.put(Boolean.class, new CollectableBooleanFormat());
			mpFormats.put(BigDecimal.class, new CollectableBigDecimalFormat());
			mpFormats.put(NuclosPassword.class, new CollectablePasswordFormat());
		}
		return mpFormats;
	}

	private static CollectableFieldFormat getFormat(Class<?> cls) {
		return getMapOfFormats().get(cls);
	}

	private static class CollectableStringFormat extends CollectableFieldFormat {
		@Override
		public String format(String sOutputFormat, Object oValue) {
			return (String) oValue;
		}

		@Override
		public Object parse(String sInputFormat, String sText) {
			return sText;
		}
	}	// class CollectableStringFormat
	
	private static class CollectablePasswordFormat extends CollectableFieldFormat {
		@Override
		public String format(String outputFormat, Object value) {
			return value == null ? "" : ((NuclosPassword) value).getValue();
		}

		@Override
		public Object parse(String inputFormat, String text) {
			return text == null ? null : new NuclosPassword(text);
		}
	}

	private static class CollectableDateTimeFormat extends CollectableFieldFormat {
		private static java.text.DateFormat getDateFormat() {
			// Note that a new DateFormat must be created each time as SimpleDateFormat is not thread safe!
			final SimpleDateFormat result = new SimpleDateFormat(DateTime.DATE_FORMAT_STRING, Locale.GERMANY);
			result.setLenient(false);
			return result;
		}
		
		@Override
		public String format(String sOutputFormat, Object oValue) {
			if (oValue == null) {
				return null;
			}
			if (!(oValue instanceof org.nuclos.common2.DateTime)) {
				throw new NuclosFatalException("Kein g\u00fcltiges Datum: " + oValue + " (" + oValue.getClass() + ").");
			}
			return getDateFormat().format(((org.nuclos.common2.DateTime)oValue).getDate());
		}

		@Override
		public org.nuclos.common2.DateTime parse(String sInputFormat, String sText) throws CollectableFieldFormatException {
			if (StringUtils.looksEmpty(sText)) {
				return null;
			}
			try {
				return new DateTime(getDateFormat().parse(sText));
			}
			catch (ParseException ex) {
				throw new CollectableFieldFormatException("Ung\u00fcltiges Datum: " + sText, ex);
			}
		}
	}	// class CollectableDateTimeFormat

	private static class CollectableDateFormat extends CollectableFieldFormat {

		@Override
		public String format(String sOutputFormat, Object oValue) {
			if (oValue == null) {
				return null;
			}
			else if (oValue.toString().equalsIgnoreCase(RelativeDate.today().toString())) {
				
				return CommonLocaleDelegate.getMessage("datechooser.today.label", "Heute");
			}
			else if (oValue instanceof ExtendedRelativeDate) {
				return ((ExtendedRelativeDate)oValue).getString(CommonLocaleDelegate.getMessage("datechooser.today.label", "Heute"));
			}
			else if(sOutputFormat != null) {
				SimpleDateFormat sdf = new SimpleDateFormat(sOutputFormat);
				return sdf.format(oValue);
			}
			else {
				return CommonLocaleDelegate.getDateFormat().format(oValue);
			}
		}

		@Override
		public java.util.Date parse(String sInputFormat, String sText) throws CollectableFieldFormatException {
			java.util.Date result = null;

			if (StringUtils.looksEmpty(sText)) {
				return null;
			}

			sText = sText.toUpperCase();

			String labelToday = CommonLocaleDelegate.getMessage("datechooser.today.label", "Heute");
			if (sText.equalsIgnoreCase(labelToday)) {
				result = RelativeDate.today();
			}
			else if (sText.startsWith(labelToday.toUpperCase())) {
				result = ExtendedRelativeDate.today();

				// remove 'LABEL_TODAY' (HEUTE)
				String sDateWithoutToday = sText.substring(labelToday.length()).trim();

				if (StringUtils.looksEmpty(sDateWithoutToday)) {
					return result;
				}

				// get operand
				if (sDateWithoutToday.startsWith(ExtendedRelativeDate.NEGATIVE_OPERAND)) {
					((ExtendedRelativeDate)result).setOperand(ExtendedRelativeDate.NEGATIVE_OPERAND);
				}
				else if (sDateWithoutToday.startsWith(ExtendedRelativeDate.POSITIVE_OPERAND)){
					((ExtendedRelativeDate)result).setOperand(ExtendedRelativeDate.POSITIVE_OPERAND);
				}
				else {
					throw new CollectableFieldFormatException(CommonLocaleDelegate.getMessage("CollectableFieldFormat.3","Invalid date: {0}", sText));
				}

				sDateWithoutToday = sDateWithoutToday.substring(1).trim();

				if (StringUtils.looksEmpty(sDateWithoutToday)) {
					return result;
				}

				// get unit
				String sUnit = sDateWithoutToday.substring(sDateWithoutToday.length()-1);

				if (Character.isLetter(sUnit.charAt(0))) {
					if (sUnit.equalsIgnoreCase(ExtendedRelativeDate.UNIT_DAY_DE) ||
							sUnit.equalsIgnoreCase(ExtendedRelativeDate.UNIT_DAY_EN)) {
						((ExtendedRelativeDate)result).setUnit(sUnit.toUpperCase());
					}
					else if (sUnit.equalsIgnoreCase(ExtendedRelativeDate.UNIT_MONTH)) {
						((ExtendedRelativeDate)result).setUnit(ExtendedRelativeDate.UNIT_MONTH);
					}
					else {
						throw new CollectableFieldFormatException(CommonLocaleDelegate.getMessage("CollectableFieldFormat.4","Invalid date: {0}", sText));
					}

					sDateWithoutToday = sDateWithoutToday.substring(0, sDateWithoutToday.length()-1).trim();
				}

				if (StringUtils.looksEmpty(sDateWithoutToday)) {
					return result;
				}

				// get quantity
				try {
					Integer iQuantity = Integer.parseInt(sDateWithoutToday);
					((ExtendedRelativeDate)result).setQuantity(iQuantity);
				}
				catch (NumberFormatException e) {
					throw new CollectableFieldFormatException(CommonLocaleDelegate.getMessage("CollectableFieldFormat.5","Invalid date: {0}", sText), e);
				}
			}
			else if(sInputFormat != null) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat(sInputFormat);
					result = sdf.parse(sText);
				}
				catch (ParseException ex) {
					throw new CollectableFieldFormatException(CommonLocaleDelegate.getMessage("CollectableFieldFormat.6","Invalid date: {0}", sText), ex);
				}
			}
			else {
				try {					
					result = CommonLocaleDelegate.parseDate(sText);
				}
				catch (ParseException ex) {
						throw new CollectableFieldFormatException(CommonLocaleDelegate.getMessage("CollectableFieldFormat.6","Invalid date: {0}", sText), ex);
				}
			}
			return result;
		}
	}	// class CollectableDateFormat

	private static class CollectableIntegerFormat extends CollectableFieldFormat {
		@Override
		public String format(String sOutputFormat, Object oValue) {
			if (oValue == null) {
				return null;
			}
			if (sOutputFormat == null) {
				return oValue.toString();
			}
			return new DecimalFormat(sOutputFormat).format(oValue);
		}

		@Override
		public Integer parse(String sInputFormat, String sText) throws CollectableFieldFormatException {
			if (StringUtils.looksEmpty(sText)) {
				return null;
			}
			try {
				final NumberFormat format = (sInputFormat == null) ?
						NumberFormat.getIntegerInstance() :
						new DecimalFormat(sInputFormat);

				if (new BigInteger(sText).compareTo(new BigInteger(new Integer(Integer.MAX_VALUE).toString())) > 0) {
					throw new CollectableFieldFormatException(CommonLocaleDelegate.getMessage("CollectableFieldFormat.13","Number too big: {0}", sText));
				}
				return format.parse(sText).intValue();
			}
			catch (ParseException ex) {
				throw new CollectableFieldFormatException(CommonLocaleDelegate.getMessage("CollectableFieldFormat.11","Invalid integer number: {0}", sText), ex);
			} catch (NumberFormatException ex) {
				throw new CollectableFieldFormatException(CommonLocaleDelegate.getMessage("CollectableFieldFormat.12","Invalid integer number: {0}", sText), ex);
			}

		}
	}	// class CollectableIntegerFormat

	private static class CollectableDoubleFormat extends CollectableFieldFormat {
		@Override
		public String format(String sOutputFormat, Object oValue) {
			if (oValue == null) {
				return null;
			}
			final NumberFormat nf;
			if (sOutputFormat == null) {
				nf = NumberFormat.getNumberInstance();
				nf.setMaximumFractionDigits(100);
				nf.setGroupingUsed(false);
			}
			else {
				nf = new DecimalFormat(sOutputFormat);
			}
			return nf.format(oValue);
		}

		@Override
		public Double parse(String sInputFormat, String sText) throws CollectableFieldFormatException {
			if (StringUtils.looksEmpty(sText)) {
				return null;
			}

			String sText1 = sText;
			try {
				final NumberFormat numberformat;
				if (sInputFormat == null) {
					numberformat = NumberFormat.getNumberInstance();
					// WORKAROUND for buggy NumberFormat: In the German Locale, "1.0" is interpreted as "10.0",
					// but should be invalid. So we check the format ourselves:
					if (Locale.getDefault().getLanguage().equals(Locale.GERMAN.getLanguage())) {
						// For the German date format, whitespace is not significant. We don't know about
						// other Locales, so we do this only for the German Locale:
						sText1 = org.apache.commons.lang.StringUtils.deleteWhitespace(sText);
						// check the desired format:
						if (!sText1.matches("-?\\d+(\\.\\d{3})*(,\\d+)?")) {   // TODO-I18N
							throw new CollectableFieldFormatException(CommonLocaleDelegate.getMessage("CollectableFieldFormat.7","Invalid decimal number: {0}", sText));
						}
					}
					else
						numberformat.setGroupingUsed(false);
				}
				else {
					numberformat = new DecimalFormat(sInputFormat);
					numberformat.setGroupingUsed(false);
				}
				return numberformat.parse(sText1).doubleValue();
			}
			catch (ParseException ex) {
				throw new CollectableFieldFormatException(CommonLocaleDelegate.getMessage("CollectableFieldFormat.8","Invalid decimal number: {0}", sText), ex);
			}
		}
	}	// class CollectableDoubleFormat

	/**
	 * @todo make this Locale dependent
	 */
	private static class CollectableBooleanFormat extends CollectableFieldFormat {
		@Override
		public String format(String sOutputFormat, Object oValue) {
			if (oValue == null) {
				return null;
			}
			else if (oValue.equals(Boolean.TRUE)) {
				return "ja";
			}
			else {
				return "nein";
			}
		}

		@Override
		public Boolean parse(String sInputFormat, String sText) throws CollectableFieldFormatException {
			if (sText == null) {
				return null;
			}
			else if (sText.equals("ja") || sText.equals("true")) { // TODO-I18N
				return Boolean.TRUE;
			}
			else if (sText.equals("nein") || sText.equals("false")) { // TODO-I18N
				return Boolean.FALSE;
			}
			else {
				throw new CollectableFieldFormatException(
					CommonLocaleDelegate.getMessage("CollectableFieldFormat.2","Invalid boolean: {0} (expected \"yes\" or \"no\").", sText));
			}
		}
	}	// class CollectableBooleanFormat

	/**
	 * @todo this is copied from DoubleFormat - refactor!
	 */
	private static class CollectableBigDecimalFormat extends CollectableFieldFormat {
		@Override
		public String format(String sOutputFormat, Object oValue) {
			if (oValue == null) {
				return null;
			}
			final NumberFormat numberformat;
			if (sOutputFormat == null) {
				numberformat = NumberFormat.getNumberInstance();
				numberformat.setGroupingUsed(false);
			}
			else {
				numberformat = new DecimalFormat(sOutputFormat);
			}
			return numberformat.format(oValue);
		}

		@Override
		public BigDecimal parse(String sInputFormat, String sText) throws CollectableFieldFormatException {
			if (StringUtils.looksEmpty(sText)) {
				return null;
			}
			String sText1 = sText;
			try {
				final NumberFormat numberformat;
				if (sInputFormat == null) {
					numberformat = NumberFormat.getNumberInstance();
					// WORKAROUND for buggy NumberFormat: In the German Locale, "1.0" is interpreted as "10.0",
					// but should be invalid. So we check the format ourselves:
					if (Locale.getDefault().getLanguage().equals(Locale.GERMAN.getLanguage())) {
						// For the German date format, whitespace is not significant. We don't know about
						// other Locales, so we do this only for the German Locale:
						sText1 = org.apache.commons.lang.StringUtils.deleteWhitespace(sText1);
						// check the desired format:
						if (!sText1.matches("-?\\d+(\\.\\d{3})*(,\\d+)?")) {  // TODO-I18N
							throw new CollectableFieldFormatException(CommonLocaleDelegate.getMessage("CollectableFieldFormat.9","Invalid decimal number: {0}", sText));
						}
					}
				}
				else {
					numberformat = new DecimalFormat(sInputFormat);
				}
				return new BigDecimal(numberformat.parse(sText1).doubleValue());
			}
			catch (ParseException ex) {
				throw new CollectableFieldFormatException(CommonLocaleDelegate.getMessage("CollectableFieldFormat.10","Invalid decimal number: {0}", sText), ex);
			}
		}
	}	// class CollectableBigDecimalFormat

	private static class DefaultFormat extends CollectableFieldFormat {
		@Override
		public String format(String sOutputFormat, Object oValue) {
			return StringUtils.emptyIfNull(LangUtils.toString(oValue));
		}

		@Override
		public Object parse(String sInputFormat, String sText) throws CollectableFieldFormatException {
			throw new UnsupportedOperationException(CommonLocaleDelegate.getMessage("CollectableFieldFormat.1","DefaultFormat.parse not implemented."));
		}
	}

}	// class CollectableFieldFormat