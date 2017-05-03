package tech.elc1798.projectpepe.activities.extras.vcs;

import java.util.LinkedList;

public class RevisionTrunk<T> {

    private LinkedList<T> undoStack;
    private LinkedList<T> redoStack;
    private RevisionDuplicator<T> duplicator;

    public RevisionTrunk(T initalRevision, RevisionDuplicator duplicator) {
        undoStack = new LinkedList<>();
        undoStack.addFirst(initalRevision);
        redoStack = new LinkedList<>();
        this.duplicator = duplicator;
    }

    /**
     * Returns the top element of the undo stack
     *
     * @return A Generic Specified Object
     */
    public T getCurrentRevision() {
        return undoStack.getFirst();
    }

    /**
     * Undoes an operation performed within the revision trunk. The original image CAN NOT be popped from the undo
     * stack. To improve runtime, rather than checking the size of the stack (O(n)), we pop the stack, check if it's
     * empty, and re-push the popped item if it is empty (O(1)).
     */
    public void rollBackRevision() {
        T undone = undoStack.pop();

        if (undoStack.isEmpty()) {
            undoStack.addFirst(undone);
        } else {
            redoStack.addFirst(undone);
        }
    }

    /**
     * Re-applies an undone operation if available
     */
    public void forwardRevision() {
        if (!redoStack.isEmpty()) {
            undoStack.addFirst(redoStack.pop());
        }
    }

    /**
     * Starts a new revision on the undo stack by making a duplicate of the previous current revision. Useful for
     * libraries like OpenCV, where objects are like references or pointers, and operations are in place.
     */
    public void startNewRevisionWithCopy() {
        undoStack.addFirst(duplicator.duplicate(getCurrentRevision()));

        // Clear the redo stack as we have diverged
        redoStack.clear();
    }

    /**
     * Starts a new revision that is not necessarily dependent on the previous current revision. Adds the given revision
     * to the trunk.
     *
     * @param revision The revision to add
     */
    public void addNewRevision(T revision) {
        undoStack.addFirst(revision);

        // Clear the redo stack as we have diverged
        redoStack.clear();
    }
}
