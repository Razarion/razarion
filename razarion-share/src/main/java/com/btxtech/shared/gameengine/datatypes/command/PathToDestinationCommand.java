package com.btxtech.shared.gameengine.datatypes.command;


import com.btxtech.shared.gameengine.datatypes.Path;

/**
 * User: beat
 * Date: 07.10.2011
 * Time: 13:33:30
 */
public abstract class PathToDestinationCommand extends BaseCommand {
    private Path path;

    public Path getPathToDestination() {
        return path;
    }

    public void setPathToDestination(Path path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return super.toString() + " " + path;
    }
}
