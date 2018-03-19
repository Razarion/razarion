package com.btxtech.client.editor.basemgmt;

import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import org.jboss.errai.databinding.client.api.Bindable;

import java.util.Date;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 12.03.2018.
 */
@Bindable
public class BaseMgmtModel {
    private PlayerBaseDto playerBase;
    private Date lastLoggedIn;
    private Consumer<Integer> killCallback;

    public PlayerBaseDto getPlayerBase() {
        return playerBase;
    }

    public void setPlayerBase(PlayerBaseDto playerBase) {
        this.playerBase = playerBase;
    }

    public Integer getId() {
        if (playerBase == null) {
            return null;
        }
        return playerBase.getBaseId();
    }

    public String getName() {
        if (playerBase == null) {
            return null;
        }
        return playerBase.getName();
    }

    public Date getLastLoggedIn() {
        return lastLoggedIn;
    }

    public void setLastLoggedIn(Date lastLoggedIn) {
        this.lastLoggedIn = lastLoggedIn;
    }

    public Consumer<Integer> getKillCallback() {
        return killCallback;
    }

    public void setKillCallback(Consumer<Integer> killCallback) {
        this.killCallback = killCallback;
    }

    public int compareId(BaseMgmtModel other, boolean sortDesc) {
        if (sortDesc) {
            return Integer.compare(other.getId(), getId());
        } else {
            return Integer.compare(getId(), other.getId());
        }
    }

    public int compareName(BaseMgmtModel other, boolean sortDesc) {
        if (getName() == null) {
            if (other.getName() == null) {
                return 0;
            } else {
                return sortDesc ? 1 : -1;
            }
        } else if (other.getName() == null) {
            return sortDesc ? -1 : 1;
        }
        if (sortDesc) {
            return other.getName().compareTo(getName());
        } else {
            return getName().compareTo(other.getName());
        }
    }

    public int compareLastLoggedIn(BaseMgmtModel other, boolean sortDesc) {
        if (getLastLoggedIn() == null) {
            if (other.getLastLoggedIn() == null) {
                return 0;
            } else {
                return sortDesc ? 1 : -1;
            }
        } else if (other.getLastLoggedIn() == null) {
            return sortDesc ? -1 : 1;
        }
        if (sortDesc) {
            return other.getLastLoggedIn().compareTo(getLastLoggedIn());
        } else {
            return getLastLoggedIn().compareTo(other.getLastLoggedIn());
        }
    }
}
