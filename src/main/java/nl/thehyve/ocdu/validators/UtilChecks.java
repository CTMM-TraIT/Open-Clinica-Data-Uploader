package nl.thehyve.ocdu.validators;

import nl.thehyve.ocdu.models.OCEntities.OcEntity;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.services.InputValidationException;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoField.*;

/**
 * Common utilities for checking primitive types and dates according to OC specification.
 *
 * Created by bo on 7/1/16.
 */
public class UtilChecks {

    public final static String TEXT_DATA_TYPE = "text";
    public final static String INTEGER_DATA_TYPE = "integer";
    public final static String FLOAT_DATA_TYPE = "float";
    public final static String DATE_DATA_TYPE = "date";
    public final static String PARTIAL_DATE_DATA_TYPE = "partialDate";

    private static final String DATE_SEP = "-";
    private static DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
            .parseCaseInsensitive().appendValue(DAY_OF_MONTH, 2).appendLiteral(DATE_SEP)
            .appendValue(MONTH_OF_YEAR, 2).appendLiteral(DATE_SEP)
            .appendValue(YEAR, 4)
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT)
            .withChronology(IsoChronology.INSTANCE);


    private static DateTimeFormatter dayOfMonth = new DateTimeFormatterBuilder()
            .appendValue(DAY_OF_MONTH, 2).appendLiteral(DATE_SEP).toFormatter().withResolverStyle(ResolverStyle.STRICT);

    private static DateTimeFormatter month = new DateTimeFormatterBuilder().parseCaseSensitive()
            .appendPattern("MMM").appendLiteral(DATE_SEP).toFormatter().withResolverStyle(ResolverStyle.STRICT);

    private static DateTimeFormatter partialDateFormatter = new DateTimeFormatterBuilder()
            .parseCaseSensitive().appendOptional(dayOfMonth)
            .appendOptional(month).appendValue(YEAR, 4).toFormatter()
            .withChronology(IsoChronology.INSTANCE);



    public static String inputValidation(String input, String allowedCharacters, String inputName, int maxLength) throws InputValidationException {
        if ((! StringUtils.isEmpty(input)) && (input.length() > maxLength)) {
            throw new InputValidationException(inputName + " can be maximally " + maxLength + " characters long");
        }
        String ret = StringUtils.substring(input, 0, maxLength);
        if (StringUtils. isEmpty(ret)) {
            throw new InputValidationException("No input specified for " + inputName);
        }
        if (! StringUtils.containsOnly(input, allowedCharacters)) {
            throw new InputValidationException("Illegal characters in " + inputName +" input. Only letters, numbers, spaces, hyphens or underscores are allowed.");
        }
        return ret;
    }

    public static boolean isDate(String input) {
        try {
            LocalDate date = LocalDate.parse(input, dateTimeFormatter);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean isPDate(String input) {
        try {
            LocalDate date = LocalDate.parse(input, partialDateFormatter);
        } catch (Exception e) {
            if (isMonthAndYear(input) || isYearOnly(input)) {
                return true;
            } else
                return false;
        }
        return true;
    }

    private static boolean isYearOnly(String input) {
        return input.matches("[0-9]{4}");
    }

    private static boolean isMonthAndYear(String input) {
        return input.matches("[A-Z]{1}[a-z]{2}-[0-9]{4}") && monthMatch(input);
    }

    private static final List<String> MONTHS = Arrays.asList(new String[]{"Jan", "Feb", "Mar", "Apr", "May",
            "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"});

    private static boolean monthMatch(String input) {
        String[] split = input.split(DATE_SEP);
        if (split.length < 2) return false;
        if (MONTHS.contains(split[0])) return true;
        else
            return false;
    }


    public static boolean isInteger(String input) {
        if (containsAlphaNumeric(input)) {
            return false;
        }
        if (input.contains(".") || input.contains(",")) {
            return false;
        }
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isFloat(String input) {
        if (containsAlphaNumeric(input)) {
            return false;
        }
        if (input.contains(",")) {
            return false;
        }
        try {
            Float.parseFloat(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    public static boolean allValuesMatch(List<String> values, String expectedType) {
        for (String value : values) {
            if (!matchType(value, expectedType)) {
                return false;
            }
        }
        return true;
    }

    public static boolean matchType(String value, String expectedType) {
        if (expectedType == null || expectedType.equals(TEXT_DATA_TYPE)) {
            return true;
        } else if (expectedType.equals(INTEGER_DATA_TYPE)) {
            return UtilChecks.isInteger(value);
        } else if (expectedType.equals(FLOAT_DATA_TYPE)) {
            return UtilChecks.isFloat(value);
        } else if (expectedType.equals(DATE_DATA_TYPE)) {
            return UtilChecks.isDate(value);
        } else if (expectedType.equals(PARTIAL_DATE_DATA_TYPE)) {
            return UtilChecks.isPDate(value);
        } else {
            return true; // no expectations, no disappointment
        }
    }


    public static boolean listContainsErrorOfType(List<? extends OcEntity> entityList, ErrorClassification errorClassification) {
        for (OcEntity ocEntity : entityList) {
            if (ocEntity.hasErrorOfType(errorClassification)) {
                return true;
            }
        }
        return false;
    }

    public static void removeFromListIf(List<? extends OcEntity> entityList, Predicate<? super OcEntity>... predicates) {
        entityList.removeIf(allTrue(predicates));
    }

    @SafeVarargs
    private static <T> Predicate<T> allTrue(Predicate<? super T>... predicates) {
        return t -> {
            for (Predicate<? super T> predicate : predicates)
                if (!predicate.test(t))
                    return false;
            return true;
        };
    }

    public static String nullSafeToUpperCase(String value) {
        return (value == null) ? "" : value.toUpperCase();
    }

    public static void addErrorClassificationForSubjects(List<? extends OcEntity> entityList, Set<String> subjectIDSetWithError, ErrorClassification errorClassification) {
        List<? extends OcEntity> result = entityList.stream().filter(entityData -> subjectIDSetWithError.contains( entityData.getSsid())).collect(Collectors.toList());
        result.forEach(entityData -> entityData.addErrorClassification(errorClassification));
    }

    public static void addErrorClassificationToAll(List<? extends OcEntity> entityList, ErrorClassification errorClassification) {
        entityList.forEach(entityData -> entityData.addErrorClassification(errorClassification));
    }

    private static boolean containsAlphaNumeric(String input) {
        return input.matches(".*[A-z].*");
    }

}
