package com.btxtech.shared.gameengine.datatypes.command;


/**
 * User: beat
 * Date: 07.10.2011
 * Time: 13:33:30
 */
public abstract class PathToDestinationCommand extends BaseCommand {
    private SimplePath simplePath;

    public SimplePath getSimplePath() {
        return simplePath;
    }

    public void setSimplePath(SimplePath simplePath) {
        this.simplePath = simplePath;
    }

    @Override
    public String toString() {
        return super.toString() + " simplePath: " + simplePath;
    }
}
