package tn.test.tools;

import tn.test.entities.User;
import tn.test.entities.Worker;

import java.security.SecureRandom;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private static SessionManager instance;

    private static final Map<String, Object> activeSessions = new HashMap<>();
    private String currentSessionId;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void createSession(Object userOrWorker) {
        String sessionId = generateSessionId();
        activeSessions.put(sessionId, userOrWorker);
        this.currentSessionId = sessionId;
    }

    public Object getCurrent() {
        return activeSessions.get(currentSessionId);
    }

    public User getCurrentUser() {
        Object obj = getCurrent();
        return obj instanceof User ? (User) obj : null;
    }

    public Worker getCurrentWorker() {
        Object obj = getCurrent();
        return obj instanceof Worker ? (Worker) obj : null;
    }

    public void invalidateCurrentSession() {
        if (currentSessionId != null) {
            activeSessions.remove(currentSessionId);
            currentSessionId = null;
        }
    }

    public static String generateSessionId() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[32];
        secureRandom.nextBytes(token);
        return new BigInteger(1, token).toString(16);
    }

    public void updateCurrent(Object updated) {
        if (currentSessionId != null) {
            activeSessions.put(currentSessionId, updated);
        }
    }

    public boolean isUserSession() {
        return getCurrent() instanceof User;
    }

    public boolean isWorkerSession() {
        return getCurrent() instanceof Worker;
    }
}
