/* Copyright (c) 2018 Crown Copyright (Food Standards Agency) */

package uk.gov.food.rn;

import java.math.BigInteger;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FSA Reference Number
 *
 * A globally unique reference number that can be generated by some authority
 * (such as a UK Local Authority) to denote some entity in the food domain.
 * Typically, this will be a particular premises of a particular Food Business
 * Operator (FBO).
 *
 * The key requirements for the reference number are:
 *
 * - distributed generation, since each authority will operate independently
 * - globally unique (under some set of assumptions)
 * - easily transportable
 * - robust to some transcription and transmission errors
 * - reversible to identify at least the issuing authority so that lookups
 *   can be directed to the right place
 *
 */
public class RN implements Comparable<RN> {

    /** Denotes the issuing authority for this reference number */
    private Authority authority;

    /** Denotes the instance disambiguator for this reference number */
    private Instance instance;

    /** Denotes the type of the thing being referenced */
    private Type type;

    /** Time instant when issued (1ms precision) */
    private TimeStamp timestamp;

    /** The internal value of the reference number, as a decimal number */
    private BigInteger value;

    /** Facade class giving access to the encoded form */
    private Representation representation;

    /**
     * Construct an RN form its constituent parts.
     *
     * @param authority issuing authority
     * @param instance  issuing service instance
     * @param type      reference number type
     * @param instant   time instant when issued
     * @throws RNException if the dateTime indicated by instant is outside the range permitted for RNs.
     *
     */
    public RN(Authority authority, Instance instance, Type type, ZonedDateTime instant) {
        this.authority = authority;
        this.instance = instance;
        this.type = type;
        timestamp = new TimeStamp(instant);
        value = packElements(authority, instance, type, instant);
        representation = new Representation(this);
    }

    /**
     * Constructs a new RN from an already-decoded number.
     *
     * @param value A decimal form integer
     * @throws RNException if any of the embedded field values are outside their permitted ranges.
     */
    public RN(BigInteger value)  {
        parseDecimalForm(value);
        this.value = value;
        representation = new Representation(this);
    }

    /**
     * Construct a new RN from its encoded form (typically base-33 encoded string with check digits).
     *
     * @param  encodedForm
     * @throws RNException if an if the embedded field values are outside their permitted ranges.
     */
    public RN(String encodedForm)  {
        representation = new Representation(encodedForm);
        value = representation.getReferenceNumber().getValue();
        parseDecimalForm(value);
    }


    /**
     * @return The issuing authority
     */
    public Authority getAuthority() {
        return authority;
    }

    /**
     * @return the instance
     */
    public Instance getInstance() {
        return instance;
    }

    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * @return the instant
     */
    public TimeStamp getInstant() {
        return timestamp;
    }

    /**
     * @return The representation facade object
     */
    public Representation getRepresentation() {
        return representation;
    }

    /** @return The internal numerical representation for the reference number */
    public BigInteger getValue() {
        return value;
    }

    /**
     * Return a fielded string form of the identifier that reveals its components,
     * primarily for debugging. The transport form for an RN is
     * {@link #getEncodedForm()}
     *
     *   - authority
     *   - instance
     *   - type
     *   - instant (as a UTC 8601 dateTime string)
     *
     *   aaaa:i:tt:yyyy-MM-ddThh:mm:ss.uuuZ
     *
     * @return
     */
    public String toString() {
        return String.format("%04d:%01d:%02d:%s",
                                 getAuthority().getId(),
                              getInstance().getId(),
                              getType().getId(),
                              timestamp.getInstant());
    }


    @Override
    public boolean equals (Object other) {
        if(other instanceof RN) {
            RN o = (RN) other;
            return authority.equals(o.getAuthority()) &&
                    instance.equals(o.getInstance()) &&
                    type.equals(o.getType()) &&
                    timestamp.equals(o.getInstant()) ;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[] { timestamp, authority, instance, type });
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     *
     * Order by comparing internal values
     */
    @Override
    public int compareTo(RN other){
        return getValue().compareTo(other.getValue());
    }

    /**
     * Returns the (Base 33) encoded form of a RN
     *
     * @return The encoded string form of an RN (including check digits).
     */
    public String getEncodedForm() {
        return representation.getEncodedForm();
    }

    /**
     * Parses the integer value of an RN's decimal form into
     * its constituent Authority, Instance, Type and Instant fields.
     *
     * @param decimal The decimal value to be parsed.
     */
    protected void parseDecimalForm(BigInteger decimal) {
        // uuuaaaaittyyyyMMddhhmmss
        Pattern p = Pattern.compile("(?<u>\\d{3})(?<a>\\d{4})(?<i>\\d{1})(?<t>\\d{2})(?<y>\\d{4})(?<M>\\d{2})(?<d>\\d{2})(?<h>\\d{2})(?<m>\\d{2})(?<s>\\d{2})");
        Matcher m = p.matcher(String.format("%024d", decimal));
        if (!m.matches()) {
            throw new RNException("Bad decimal form (incorrect length): " + decimal);
        }

        type      = new Type(m.group("t"));
        instance  = new Instance(m.group("i"));
        authority = new Authority(m.group("a"));

        int milli = Integer.parseInt(m.group("u"));
        int sec   = Integer.parseInt(m.group("s"));
        int min   = Integer.parseInt(m.group("m"));
        int hour  = Integer.parseInt(m.group("h"));
        int day   = Integer.parseInt(m.group("d"));
        int month = Integer.parseInt(m.group("M"));
        int year  = Integer.parseInt(m.group("y"));

        timestamp = new TimeStamp( ZonedDateTime.of(year, month, day, hour, min, sec, milli*1000000, ZoneOffset.UTC) );
    }

    /**
     * Pack the elements of the reference number into a single big integer.
     *
     * @return A single number which packs the reference number information into a single value
     */
    protected BigInteger packElements(Authority authority, Instance instance, Type type, ZonedDateTime zdt) {
        long year = ChronoField.YEAR.getFrom(zdt);
        long month = ChronoField.MONTH_OF_YEAR.getFrom(zdt);
        long day = ChronoField.DAY_OF_MONTH.getFrom(zdt);
        long hour = ChronoField.HOUR_OF_DAY.getFrom(zdt);
        long min = ChronoField.MINUTE_OF_HOUR.getFrom(zdt);
        long sec = ChronoField.SECOND_OF_MINUTE.getFrom(zdt);
        long milli = ChronoField.MILLI_OF_SECOND.getFrom(zdt);

        String decimalForm = String.format(
                "%03d%04d%01d%02d%04d%02d%02d%02d%02d%02d",
                milli, authority.getId(), instance.getId(), type.getId(),
                year, month, day, hour, min, sec);

        return new BigInteger(decimalForm);
    }
}
