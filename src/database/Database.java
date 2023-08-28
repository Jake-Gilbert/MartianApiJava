package database;

import models.Clearance;
import models.MartianEntity;

import java.util.*;

public class Database
{
    private final List<String> presentIds = new ArrayList<>();
    private final Map<String, MartianEntity> presentEntities = new HashMap<>();
    public String addMartian(String species, Clearance clearance)
    {
        if (species == null || clearance == null)
        {
            return null;
        }
        final var martian = new MartianEntity(species, clearance);
        final var id = UUID.randomUUID().toString();
        presentEntities.put(id, martian);
        presentIds.add(id);
        return id;
    }

    public boolean deleteMartian(String id, Clearance clearance)
    {
        return removeEntityFromIdAndMartianCollection(id, clearance);
    }

    public MartianEntity retrieveMartian(String id, Clearance clearance)
    {
        if (!presentIds.contains(id))
        {
            return null;
        }
        final var entity = presentEntities.get(id);

        if (!entity.getClearanceRequired().authorisesClearanceLevel(clearance))
        {
            return null;
        }
        return entity;
    }

    public MartianEntity retrieveMartianWithoutClearance(String id)
    {
        if (!presentIds.contains(id))
        {
            return null;
        }
        return presentEntities.get(id);
    }

    //Permission has already been granted when this method is called
    public boolean updateMartian(String id, MartianEntity entity)
    {
        if (!presentIds.contains(id))
        {
            return false;
        }
        presentEntities.put(id, entity);
        return true;
    }


    private boolean removeEntityFromIdAndMartianCollection(String id, Clearance clearance)
    {
        if (presentIds.contains(id) && presentEntities.containsKey(id))
        {
            if (!presentEntities.get(id).getClearanceRequired().authorisesClearanceLevel(clearance))
            {
                return false;
            }
            presentIds.remove(id);
            presentEntities.remove(id);
            return true;
        }
        return false;
    }

    public int count()
    {
        return presentEntities.size();
    }
}
