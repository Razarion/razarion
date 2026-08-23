package com.btxtech.server.service.tracking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Which records belong to the same visitor.
 * <p>
 * A record can carry several handles - a click id, the game session, the http session - and each
 * record is a statement that its handles describe one person: a startup beacon carrying a click id
 * and a game session says the two are the same visitor, so every other record of that game session
 * belongs to that click id as well. Chains of such statements are followed to the end, which no
 * single lookup table can do, because the link that joins two records may only appear in a third.
 * <p>
 * None of the three handles is enough on its own. The click id survives a session that does not: a
 * browser accepting no cookies is handed a fresh http session on every single request, eleven
 * thousand of them in a week for a few hundred arrivals. The game session holds the startup beacons
 * of that same browser together - one boot arrived as a dozen sessions of one task each, which read
 * as a dozen visitors who all opened the game and never got past the first step. And the http
 * session carries everyone who has neither, because an organic visitor never gets a click id.
 * <p>
 * The same grouping the funnel does in the browser (HandleGroups in tracking-container-analyzer.ts)
 * - both views count the same visitors or they contradict each other.
 */
public class VisitorGroups {
    private final Map<String, String> parents = new HashMap<>();

    /** Records the statement that these handles are one visitor. */
    public void join(List<String> handles) {
        for (int index = 1; index < handles.size(); index++) {
            union(handles.get(0), handles.get(index));
        }
    }

    /**
     * The one name shared by every record of this visitor, or null when the record names nobody at
     * all. Such a record is left out rather than counted: an absent key matches every other absent
     * key, so all of them would collapse into a single phantom visitor.
     */
    public String keyOf(List<String> handles) {
        return handles.isEmpty() ? null : root(handles.get(0));
    }

    private void union(String left, String right) {
        String leftRoot = root(left);
        String rightRoot = root(right);
        if (!leftRoot.equals(rightRoot)) {
            parents.put(rightRoot, leftRoot);
        }
    }

    private String root(String handle) {
        String current = handle;
        String parent = parents.get(current);
        if (parent == null) {
            parents.put(current, current);
            return current;
        }
        while (!parent.equals(current)) {
            current = parent;
            parent = parents.get(current);
        }
        // Point straight at the root, so a long chain is walked once rather than once per lookup.
        parents.put(handle, current);
        return current;
    }
}
