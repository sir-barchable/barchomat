package sir.barchable.clash.model;

import java.util.*;

/**
 * Session state. Used by the proxy/server as a place to store stuff during a session.
 *
 * @author Sir Barchable
 *         Date: 1/05/15
 */
public class SessionState {
    private long userId;
    private String userName;
    private int townHallLevel;
    private final Map<String, Object> attributes = Collections.synchronizedMap(new HashMap<>());

    synchronized public long getUserId() {
        return userId;
    }

    synchronized public void setUserId(long userId) {
        if (this.userId != 0) {
            throw new IllegalStateException("User ID already set");
        }
        this.userId = userId;
    }

    synchronized public String getUserName() {
        return userName;
    }

    synchronized public void setUserName(String userName) {
        this.userName = userName;
    }

    synchronized public int getTownHallLevel() {
        return townHallLevel;
    }

    synchronized public void setTownHallLevel(int townHallLevel) {
        this.townHallLevel = townHallLevel;
    }

    public Object setAttribute(String key, Object value) {
        return attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Get the names of the attributes in the session. This returns a copy of the names at the time of the call.
     */
    public Set<String> getAttributeNames() {
        synchronized (attributes) {
            return new HashSet<>(attributes.keySet());
        }
    }

    /**
     * Get a copy of the attribute map. This returns a copy of the attributes at the time of the call.
     */
    public Map<String, Object> getAttributes() {
        synchronized (attributes) {
            return new HashMap<>(attributes);
        }
    }
}
