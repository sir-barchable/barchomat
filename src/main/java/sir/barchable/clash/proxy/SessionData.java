package sir.barchable.clash.proxy;

import java.util.*;

/**
 * @author Sir Barchable
 *         Date: 1/05/15
 */
public class SessionData {
    private long userId;
    private String userName;
    private int townHallLevel;
    private final Map<String, Object> attributes = Collections.synchronizedMap(new HashMap<>());

    synchronized public long getUserId() {
        return userId;
    }

    synchronized public void setUserId(long userId) {
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

    public Set<String> getAttributeNames() {
        synchronized (attributes) {
            return new HashSet<>(attributes.keySet());
        }
    }

    public Map<String, Object> getAttributes() {
        synchronized (attributes) {
            return new HashMap<>(attributes);
        }
    }
}
