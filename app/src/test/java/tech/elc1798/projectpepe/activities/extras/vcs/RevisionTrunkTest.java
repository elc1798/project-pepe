package tech.elc1798.projectpepe.activities.extras.vcs;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RevisionTrunkTest {
    private RevisionTrunk<Integer> versionController;

    @Before
    public void setup() throws Exception {
        versionController = new RevisionTrunk<>(0, new RevisionDuplicator<Integer>() {
            @Override
            public Integer duplicate(Integer revision) {
                return revision;
            }
        });
    }

    @Test
    public void orderedTest() throws Exception {
        assertEquals((int) versionController.getCurrentRevision(), 0);

        versionController.rollBackRevision();
        assertEquals((int) versionController.getCurrentRevision(), 0);

        versionController.addNewRevision(1);
        assertEquals((int) versionController.getCurrentRevision(), 1);

        versionController.startNewRevisionWithCopy();
        assertEquals((int) versionController.getCurrentRevision(), 1);

        versionController.rollBackRevision();
        assertEquals((int) versionController.getCurrentRevision(), 1);

        versionController.rollBackRevision();
        assertEquals((int) versionController.getCurrentRevision(), 0);

        versionController.forwardRevision();
        assertEquals((int) versionController.getCurrentRevision(), 1);

        versionController.forwardRevision();
        assertEquals((int) versionController.getCurrentRevision(), 1);

        versionController.addNewRevision(2);
        assertEquals((int) versionController.getCurrentRevision(), 2);

        versionController.rollBackRevision();
        assertEquals((int) versionController.getCurrentRevision(), 1);

        versionController.addNewRevision(3);
        assertEquals((int) versionController.getCurrentRevision(), 3);

        versionController.forwardRevision();
        assertEquals((int) versionController.getCurrentRevision(), 3);
    }
}