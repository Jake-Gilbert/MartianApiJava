import database.Database;
import models.Clearance;
import models.MartianEntity;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDatabase
{
    private final Database database = new Database();
    private final Map<String, String> entityAndIdMap = new HashMap<>();
    private final MartianEntity xenomorph = new MartianEntity(XENOMORPH, Clearance.TOP_LEVEL_CLEARANCE);
    private final MartianEntity updatedXenomorph = new MartianEntity(String.format("New and improved %s", XENOMORPH), Clearance.ADVANCED_CLEARANCE);
    private final MartianEntity fredTheMartian = new MartianEntity("Fred the Martian", Clearance.MINIMAL_CLEARANCE);
    private final MartianEntity theArbiter = new MartianEntity("The Arbiter", Clearance.ADVANCED_CLEARANCE);
    private static final String XENOMORPH = "Xenomorph";
    private static final String NON_EXISTENT_ID = "Non-existent ID";

    @Before
    public void init()
    {
        entityAndIdMap.put(XENOMORPH, database.addMartian(XENOMORPH, xenomorph.getClearanceRequired()));
        entityAndIdMap.put(fredTheMartian.getSpecies(), database.addMartian(fredTheMartian.getSpecies(), fredTheMartian.getClearanceRequired()));
        entityAndIdMap.put(theArbiter.getSpecies(), database.addMartian(theArbiter.getSpecies(), theArbiter.getClearanceRequired()));
    }

    @Test
    public void attemptingToAddEntityToDbWhenSpeciesIsNullShouldReturnNull()
    {
        final var generatedId = database.addMartian(null, Clearance.ACCESS_RESTRICTED);
        assertThat(generatedId).isEqualTo(null);
    }

    @Test
    public void attemptingToAddEntityToDbWhenClearanceIsNullShouldReturnNull()
    {
        final var generatedId = database.addMartian("Test", null);
        assertThat(generatedId).isEqualTo(null);
    }

    @Test
    public void attemptingToAddEntityToDbWhenUserAuthorisedShouldResultInSuccess()
    {
        assertThat(database.count()).isEqualTo(3);
        final var generatedId = database.addMartian("Test", Clearance.MINIMAL_CLEARANCE);
        assertThat(generatedId).isNotEmpty();
        assertThat(database.count()).isEqualTo(4);
    }

    @Test
    public void attemptingToRetrieveEntityWhichExistsIenDbAndUserAuthoriseToAccessShouldReturnEntity()
    {
        final var retrievedEntity = database.retrieveMartian(entityAndIdMap.get(XENOMORPH), Clearance.TOP_LEVEL_CLEARANCE);
        assertThat(retrievedEntity)
                .extracting(MartianEntity::getSpecies, MartianEntity::getClearanceRequired)
                .contains(XENOMORPH, Clearance.TOP_LEVEL_CLEARANCE);
    }

    @Test
    public void attemptingToRetrieveEntityWhichDoesNotExistInDbShouldReturnNull()
    {
        final var retrievedEntity = database.retrieveMartian(NON_EXISTENT_ID, Clearance.ACCESS_RESTRICTED);
        assertThat(retrievedEntity).isEqualTo(null);
    }

    @Test
    public void attemptingToRetrieveEntityWhichExistsInDbButUserNotAuthorisedShouldReturnNull()
    {
        final var retrievedEntity = database.retrieveMartian(entityAndIdMap.get("Xenomorph"), Clearance.ACCESS_RESTRICTED);
        assertThat(retrievedEntity).isEqualTo(null);
    }

    @Test
    public void attemptingToUpdateEntityInDbWhenIdNotPresentInDbShouldReturnFalse()
    {
        final var hasBeenUpdated = database.updateMartian(NON_EXISTENT_ID, updatedXenomorph);
        assertThat(hasBeenUpdated).isEqualTo(false);
    }

    @Test
    public void attemptingToUpdateEntityInDbWhenIdPresentInDbShouldReturnTrue()
    {
        final var hasBeenUpdated = database.updateMartian(entityAndIdMap.get(XENOMORPH), updatedXenomorph);
        assertThat(hasBeenUpdated).isEqualTo(true);
        assertThat(database.retrieveMartianWithoutClearance(entityAndIdMap.get(XENOMORPH))).isEqualTo(updatedXenomorph);
    }

    @Test
    public void attemptingToDeleteEntityInDbWhenIdNotPresentInDbShouldReturnFalse()
    {
        final var hasBeenDeleted = database.deleteMartian(NON_EXISTENT_ID, Clearance.TOP_LEVEL_CLEARANCE);
        assertThat(hasBeenDeleted).isEqualTo(false);
    }

    @Test
    public void attemptingToDeleteEntityInDbWhenClearanceNotSufficientShouldReturnFalse()
    {
        final var hasBeenDeleted = database.deleteMartian(entityAndIdMap.get(XENOMORPH), Clearance.ACCESS_RESTRICTED);
        assertThat(hasBeenDeleted).isEqualTo(false);
    }

    @Test
    public void attemptingToDeleteEntityInDbWhenIdPresentInDbAndClearanceSufficientShouldReturnTrue()
    {
        final var hasBeenDeleted = database.deleteMartian(entityAndIdMap.get(XENOMORPH), Clearance.TOP_LEVEL_CLEARANCE);
        assertThat(hasBeenDeleted).isEqualTo(true);
        assertThat(database.retrieveMartianWithoutClearance(entityAndIdMap.get(XENOMORPH))).isEqualTo(null);
        assertThat(database.count()).isEqualTo(2);
    }
}
