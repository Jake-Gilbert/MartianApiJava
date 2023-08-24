package models;

public enum Clearance
{
    ACCESS_RESTRICTED("Access Restricted", 0),
    MINIMAL_CLEARANCE("Minimal Clearance", 1),
    STANDARD_CLEARANCE("Standard Clearance", 2),
    ADVANCED_CLEARANCE("Advanced Clearance", 3),
    TOP_LEVEL_CLEARANCE("Top Level Clearance", 4);

    private final String clearanceLevel;
    private final int clearanceValue;
    private Clearance(String clearanceLevel, int clearanceValue)
    {
        this.clearanceLevel = clearanceLevel;
        this.clearanceValue = clearanceValue;
    }

    public boolean authorisesClearanceLevel(Clearance clearanceToCheck)
    {
        return clearanceToCheck.clearanceValue >= clearanceValue;
    }
}
