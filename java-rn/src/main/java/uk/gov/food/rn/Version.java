package uk.gov.food.rn;

public class Version {
    private int id;
    public static final int MIN_VERSION_ID = 0;
    public static final int MAX_VERSION_ID = 9;

    /**
     * Construct a new Version object with the given identifier.
     *
     * @param versionId The identity of the Version
     *
     */
    public Version(int versionId) {
        if (!isValidIdentifier(versionId))  {
            throw new RNException(
                    String.format("Illegal identifier for instance: %d is not in the range %d : %d",
                            id, MIN_VERSION_ID, MAX_VERSION_ID)
            );
        }

        this.id = versionId;
    }

    /**
     * Construct a new Version object from a string denoting the version identifier
     *
     * @param versionIdEnc The identity of the Version as a string
     *
     */
    public Version(String versionIdEnc) {
        this(Integer.parseInt(versionIdEnc));
    }

    /**
     * @return the version of this reference number
     */
    public int getId() { return id; }

    public static boolean isValidIdentifier(int instanceId) {
        return (instanceId >= MIN_VERSION_ID && instanceId <= MAX_VERSION_ID);
    }

}
