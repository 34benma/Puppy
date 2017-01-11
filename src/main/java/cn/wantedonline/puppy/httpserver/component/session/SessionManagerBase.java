/*
 *  Copyright [2016-2026] wangcheng(wantedonline@outlook.com)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */


package cn.wantedonline.puppy.httpserver.component.session;


import cn.wantedonline.puppy.exception.TooManyActiveSessionsException;
import cn.wantedonline.puppy.util.*;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


/**
 * <pre>
 *     Refer to Tomcat 7.0.65 Source code
 * </pre>
 *
 * @author wangcheng
 * @since V0.6.3 on 2017.01.09
 */
public abstract class SessionManagerBase implements SessionManager {
    private static Logger log = Log.getLogger();

    /**
     * The distributable flag for Sessions created by this Manager.  If this
     * flag is set to <code>true</code>, any user attributes added to a
     * session controlled by this Manager must be Serializable.
     */
    protected boolean distributable;

    /**
     * The default maximum inactive interval for Sessions created by
     * this Manager.
     */
    protected int maxInactiveInterval = 30 * 60;

    /**
     * The Java class name of the secure random number generator class to be
     * used when generating session identifiers. The random number generator
     * class must be self-seeding and have a zero-argument constructor. If not
     * specified, an instance of {@link java.security.SecureRandom} will be
     * generated.
     */
    protected String secureRandomClass = null;

    /**
     * The name of the algorithm to use to create instances of
     * {@link java.security.SecureRandom} which are used to generate session IDs.
     * If no algorithm is specified, SHA1PRNG is used. To use the platform
     * default (which may be SHA1PRNG), specify the empty string. If an invalid
     * algorithm and/or provider is specified the SecureRandom instances will be
     * created using the defaults. If that fails, the SecureRandom instances
     * will be created using platform defaults.
     */
    protected String secureRandomAlgorithm = "SHA1PRNG";

    /**
     * The name of the provider to use to create instances of
     * {@link java.security.SecureRandom} which are used to generate session IDs. 
     * If no algorithm is specified the of SHA1PRNG default is used. If an
     * invalid algorithm and/or provider is specified the SecureRandom instances
     * will be created using the defaults. If that fails, the SecureRandom
     * instances will be created using platform defaults.
     */
    protected String secureRandomProvider = null;

    protected SessionIdGenerator sessionIdGenerator = null;
    protected Class<? extends SessionIdGenerator> sessionIdGeneratorClass = null;

    /**
     * The longest time (in seconds) that an expired session had been alive.
     */
    protected volatile int sessionMaxAliveTime;
    private final Object sessionMaxAliveTimeUpdateLock = new Object();


    protected static final int TIMING_STATS_CACHE_SIZE = 100;

    protected final Deque<SessionTiming> sessionCreationTiming = new LinkedList<SessionTiming>();

    protected final Deque<SessionTiming> sessionExpirationTiming = new LinkedList<SessionTiming>();

    /**
     * Number of sessions that have expired.
     */
    protected final AtomicLong expiredSessions = new AtomicLong(0);


    /**
     * The set of currently active Sessions for this Manager, keyed by
     * session identifier.
     */
    protected Map<String, Session> sessions = new ConcurrentHashMap<String, Session>();

    // Number of sessions created by this manager
    protected long sessionCounter=0;

    protected volatile int maxActive=0;

    private final Object maxActiveUpdateLock = new Object();

    /**
     * The maximum number of active Sessions allowed, or -1 for no limit.
     */
    protected int maxActiveSessions = -1;

    /**
     * Number of session creations that failed due to maxActiveSessions.
     */
    protected int rejectedSessions = 0;

    // number of duplicated session ids - anything >0 means we have problems
    protected volatile int duplicates=0;

    /**
     * Processing time during session expiration.
     */
    protected long processingTime = 0;

    /**
     * Iteration count for background processing.
     */
    private int count = 0;


    /**
     * Frequency of the session expiration, and related manager operations.
     * Manager operations will be done once for the specified amount of
     * backgrondProcess calls (ie, the lower the amount, the most often the
     * checks will occur).
     */
    protected int processExpiresFrequency = 6;

    // ------------------------------------------------------------- Properties

    /** Returns the name of the implementation class.
     */
    public String getClassName() {
        return this.getClass().getName();
    }


    /**
     * Return the distributable flag for the sessions supported by
     * this Manager.
     */
    @Override
    public boolean getDistributable() {
        return (this.distributable);

    }

    /**
     * Set the distributable flag for the sessions supported by this
     * Manager.  If this flag is set, all user data objects added to
     * sessions associated with this manager must implement Serializable.
     *
     * @param distributable The new distributable flag
     */
    @Override
    public void setDistributable(boolean distributable) {
        this.distributable = distributable;
    }

    /**
     * Return the default maximum inactive interval (in seconds)
     * for Sessions created by this Manager.
     */
    @Override
    public int getMaxInactiveInterval() {
        return (this.maxInactiveInterval);
    }

    /**
     * Set the default maximum inactive interval (in seconds)
     * for Sessions created by this Manager.
     *
     * @param interval The new default value
     */
    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    /**
     * Gets the session id generator.
     *
     * @return The session id generator
     */
    public SessionIdGenerator getSessionIdGenerator() {
        if (sessionIdGenerator != null) {
            return sessionIdGenerator;
        } else if (sessionIdGeneratorClass != null) {
            try {
                sessionIdGenerator = sessionIdGeneratorClass.newInstance();
                return sessionIdGenerator;
            } catch(IllegalAccessException ex) {
                // Ignore
            } catch(InstantiationException ex) {
                // Ignore
            }
        }
        return null;
    }


    /**
     * Sets the session id generator
     *
     * @param sessionIdGenerator The session id generator
     */
    public void setSessionIdGenerator(SessionIdGenerator sessionIdGenerator) {
        this.sessionIdGenerator = sessionIdGenerator;
        sessionIdGeneratorClass = sessionIdGenerator.getClass();
    }

    /**
     * Return the secure random number generator class name.
     */
    public String getSecureRandomClass() {

        return (this.secureRandomClass);

    }


    /**
     * Set the secure random number generator class name.
     *
     * @param secureRandomClass The new secure random number generator class
     *                          name
     */
    public void setSecureRandomClass(String secureRandomClass) {
        this.secureRandomClass = secureRandomClass;
    }


    /**
     * Return the secure random number generator algorithm name.
     */
    public String getSecureRandomAlgorithm() {
        return secureRandomAlgorithm;
    }


    /**
     * Set the secure random number generator algorithm name.
     *
     * @param secureRandomAlgorithm The new secure random number generator
     *                              algorithm name
     */
    public void setSecureRandomAlgorithm(String secureRandomAlgorithm) {
        this.secureRandomAlgorithm = secureRandomAlgorithm;
    }


    /**
     * Return the secure random number generator provider name.
     */
    public String getSecureRandomProvider() {
        return secureRandomProvider;
    }


    /**
     * Set the secure random number generator provider name.
     *
     * @param secureRandomProvider The new secure random number generator
     *                             provider name
     */
    public void setSecureRandomProvider(String secureRandomProvider) {
        this.secureRandomProvider = secureRandomProvider;
    }


    /**
     * Number of session creations that failed due to maxActiveSessions
     * 
     * @return The count
     */
    @Override
    public int getRejectedSessions() {
        return rejectedSessions;
    }

    /**
     * Gets the number of sessions that have expired.
     *
     * @return Number of sessions that have expired
     */
    @Override
    public long getExpiredSessions() {
        return expiredSessions.get();
    }


    /**
     * Sets the number of sessions that have expired.
     *
     * @param expiredSessions Number of sessions that have expired
     */
    @Override
    public void setExpiredSessions(long expiredSessions) {
        this.expiredSessions.set(expiredSessions);
    }

    public long getProcessingTime() {
        return processingTime;
    }


    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }
    
    /**
     * Return the frequency of manager checks.
     */
    public int getProcessExpiresFrequency() {

        return (this.processExpiresFrequency);

    }

    /**
     * Set the manager checks frequency.
     *
     * @param processExpiresFrequency the new manager checks frequency
     */
    public void setProcessExpiresFrequency(int processExpiresFrequency) {
        if (processExpiresFrequency <= 0) {
            return;
        }
        this.processExpiresFrequency = processExpiresFrequency;
    }
    // --------------------------------------------------------- Public Methods


    /**
     * Implements the Manager interface, direct call to processExpires
     */
    @Override
    public void backgroundProcess() {
        count = (count + 1) % processExpiresFrequency;
        if (count == 0)
            processExpires();
    }

    /**
     * Invalidate all sessions that have expired.
     */
    public void processExpires() {

        long timeNow = System.currentTimeMillis();
        Session sessions[] = findSessions();
        int expireHere = 0 ;
        
        if(log.isDebugEnabled())
            log.debug("Start expire sessions at "+ getInstanceInfo() + ",time " + timeNow + " sessioncount " + sessions.length);
        for (int i = 0; i < sessions.length; i++) {
            if (sessions[i]!=null && !sessions[i].isValid()) {
                expireHere++;
            }
        }
        long timeEnd = System.currentTimeMillis();
        if(log.isDebugEnabled())
             log.debug("End expire sessions processingTime " + (timeEnd - timeNow) + " expired sessions: " + expireHere);
        processingTime += ( timeEnd - timeNow );

    }


    protected void startInternal() {

        // Ensure caches for timing stats are the right size by filling with
        // nulls.
        while (sessionCreationTiming.size() < TIMING_STATS_CACHE_SIZE) {
            sessionCreationTiming.add(null);
        }
        while (sessionExpirationTiming.size() < TIMING_STATS_CACHE_SIZE) {
            sessionExpirationTiming.add(null);
        }

        /* Create sessionIdGenerator if not explicitly configured */
        SessionIdGenerator sessionIdGenerator = getSessionIdGenerator();
        if (sessionIdGenerator == null) {
            sessionIdGenerator = new DefaultSessionIdGenerator();
            setSessionIdGenerator(sessionIdGenerator);
        }

        sessionIdGenerator.setJvmRoute(getJvmRoute());
        if (sessionIdGenerator instanceof SessionIdGeneratorBase) {
            SessionIdGeneratorBase sig = (SessionIdGeneratorBase)sessionIdGenerator;
            sig.setSecureRandomAlgorithm(getSecureRandomAlgorithm());
            sig.setSecureRandomClass(getSecureRandomClass());
            sig.setSecureRandomProvider(getSecureRandomProvider());
        }

        // Force initialization of the random number generator
        if (log.isDebugEnabled())
            log.debug("Force random number initialization starting");
            sessionIdGenerator.generateSessionId();
        if (log.isDebugEnabled())
            log.debug("Force random number initialization completed");
    }


    /**
     * Add this Session to the set of active Sessions for this Manager.
     *
     * @param session Session to be added
     */
    @Override
    public void add(Session session) {
        sessions.put(session.getId(), session);
        int size = getActiveSessions();
        if( size > maxActive ) {
            synchronized(maxActiveUpdateLock) {
                if( size > maxActive ) {
                    maxActive = size;
                }
            }
        }
    }

    /**
     * Construct and return a new session object, based on the default
     * settings specified by this Manager's properties.  The session
     * id specified will be used as the session id.  
     * If a new session cannot be created for any reason, return 
     * <code>null</code>.
     * 
     * @param sessionId The session id which should be used to create the
     *  new session; if <code>null</code>, a new session id will be
     *  generated
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     */
    @Override
    public Session createSession(String sessionId) {
        
        if ((maxActiveSessions >= 0) &&
                (getActiveSessions() >= maxActiveSessions)) {
            rejectedSessions++;
            throw new TooManyActiveSessionsException(
                    "createSession: Too many active sessions",
                    maxActiveSessions);
        }
        
        // Recycle or create a Session instance
        Session session = createEmptySession();

        // Initialize the properties of the new session and return it
        session.setNew(true);
        session.setValid(true);
        session.setCreationTime(System.currentTimeMillis());
        session.setMaxInactiveInterval(this.maxInactiveInterval);
        String id = sessionId;
        if (id == null) {
            id = generateSessionId();
        }
        session.setId(id);
        sessionCounter++;

        SessionTiming timing = new SessionTiming(session.getCreationTime(), 0);
        synchronized (sessionCreationTiming) {
            sessionCreationTiming.add(timing);
            sessionCreationTiming.poll();
        }
        sessions.put(sessionId, session);
        return (session);

    }
    
    
    /**
     * Get a session from the recycled ones or create a new empty one.
     * The PersistentManager manager does not need to create session data
     * because it reads it from the Store.
     */
    @Override
    public Session createEmptySession() {
        return (getNewSession());
    }


    /**
     * Return the active Session, associated with this Manager, with the
     * specified session id (if any); otherwise return <code>null</code>.
     *
     * @param id The session id for the session to be returned
     *
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     * @exception IOException if an input/output error occurs while
     *  processing this request
     */
    @Override
    public Session findSession(String id) throws IOException {
        if (id == null)
            return (null);
        return sessions.get(id);

    }


    /**
     * Return the set of active Sessions associated with this Manager.
     * If this Manager has no active Sessions, a zero-length array is returned.
     */
    @Override
    public Session[] findSessions() {

        return sessions.values().toArray(new Session[0]);

    }


    /**
     * Remove this Session from the active Sessions for this Manager.
     *
     * @param session Session to be removed
     */
    @Override
    public void remove(Session session) {
        remove(session, false);
    }
    
    /**
     * Remove this Session from the active Sessions for this Manager.
     *
     * @param session   Session to be removed
     * @param update    Should the expiration statistics be updated
     */
    @Override
    public void remove(Session session, boolean update) {
        
        // If the session has expired - as opposed to just being removed from
        // the manager because it is being persisted - update the expired stats
        if (update) {
            long timeNow = System.currentTimeMillis();
            int timeAlive =
                (int) (timeNow - session.getCreationTime())/1000;
            updateSessionMaxAliveTime(timeAlive);
            expiredSessions.incrementAndGet();
            SessionTiming timing = new SessionTiming(timeNow, timeAlive);
            synchronized (sessionExpirationTiming) {
                sessionExpirationTiming.add(timing);
                sessionExpirationTiming.poll();
            }
        }

        if (session.getId() != null) {
            sessions.remove(session.getId());
        }
    }


    /**
     * Change the session ID of the current session to a new randomly generated
     * session ID.
     * 
     * @param session   The session to change the session ID for
     */
    @Override
    public void changeSessionId(Session session) {
        session.setId(generateSessionId());
    }
    
    
    // ------------------------------------------------------ Protected Methods


    /**
     * Get new session class to be used in the doLoad() method.
     */
    protected Session getNewSession() {
        return new DefaultHttpSession();
    }


    /**
     * Generate and return a new session identifier.
     */
    protected String generateSessionId() {

        String result = null;

        do {
            if (result != null) {
                // Not thread-safe but if one of multiple increments is lost
                // that is not a big deal since the fact that there was any
                // duplicate is a much bigger issue.
                duplicates++;
            }

            result = sessionIdGenerator.generateSessionId();
            
        } while (sessions.containsKey(result));
        
        return result;
    }


    // ------------------------------------------------------ Protected Methods


    /**
     * 在分布式环境下，支持同一个Session落在不同的实例上
     * 因此，JvmRoute事实上作用不大，直接用进程名替代好了
     * @return the JvmRoute or null.
     */
    public String getJvmRoute() {
        return getInstanceInfo();
    }

    private String getInstanceInfo() {
        return ManagementFactory.getRuntimeMXBean().getName();
    }


    // -------------------------------------------------------- Package Methods


    @Override
    public void setSessionCounter(long sessionCounter) {
        this.sessionCounter = sessionCounter;
    }


    /** 
     * Total sessions created by this manager.
     *
     * @return sessions created
     */
    @Override
    public long getSessionCounter() {
        return sessionCounter;
    }


    /** 
     * Number of duplicated session IDs generated by the random source.
     * Anything bigger than 0 means problems.
     *
     * @return The count of duplicates
     */
    public int getDuplicates() {
        return duplicates;
    }


    public void setDuplicates(int duplicates) {
        this.duplicates = duplicates;
    }


    /** 
     * Returns the number of active sessions
     *
     * @return number of sessions active
     */
    @Override
    public int getActiveSessions() {
        return sessions.size();
    }


    /**
     * Max number of concurrent active sessions
     *
     * @return The highest number of concurrent active sessions
     */
    @Override
    public int getMaxActive() {
        return maxActive;
    }


    @Override
    public void setMaxActive(int maxActive) {
        synchronized (maxActiveUpdateLock) {
            this.maxActive = maxActive;
        }
    }


    /**
     * Return the maximum number of active Sessions allowed, or -1 for
     * no limit.
     */
    public int getMaxActiveSessions() {

        return (this.maxActiveSessions);

    }


    /**
     * Set the maximum number of active Sessions allowed, or -1 for
     * no limit.
     *
     * @param max The new maximum number of sessions
     */
    public void setMaxActiveSessions(int max) {
        this.maxActiveSessions = max;
    }


    /**
     * Gets the longest time (in seconds) that an expired session had been
     * alive.
     *
     * @return Longest time (in seconds) that an expired session had been
     * alive.
     */
    @Override
    public int getSessionMaxAliveTime() {
        return sessionMaxAliveTime;
    }


    /**
     * Sets the longest time (in seconds) that an expired session had been
     * alive. Typically used for resetting the current value.
     *
     * @param sessionMaxAliveTime Longest time (in seconds) that an expired
     * session had been alive.
     */
    @Override
    public void setSessionMaxAliveTime(int sessionMaxAliveTime) {
        synchronized (sessionMaxAliveTimeUpdateLock) {
            this.sessionMaxAliveTime = sessionMaxAliveTime;
        }
    }


    /**
     * Updates the sessionMaxAliveTime attribute if the candidate value is
     * larger than the current value.
     * 
     * @param sessionAliveTime  The candidate value (in seconds) for the new 
     *                          sessionMaxAliveTime value.
     */
    public void updateSessionMaxAliveTime(int sessionAliveTime) {
        if (sessionAliveTime > this.sessionMaxAliveTime) {
            synchronized (sessionMaxAliveTimeUpdateLock) {
                if (sessionAliveTime > this.sessionMaxAliveTime) {
                    this.sessionMaxAliveTime = sessionAliveTime;
                }
            }
        }
    }

    /**
     * Gets the average time (in seconds) that expired sessions had been
     * alive based on the last 100 sessions to expire. If less than
     * 100 sessions have expired then all available data is used.
     * 
     * @return Average time (in seconds) that expired sessions had been
     * alive.
     */
    @Override
    public int getSessionAverageAliveTime() {
        // Copy current stats
        List<SessionTiming> copy = new ArrayList<SessionTiming>();
        synchronized (sessionExpirationTiming) {
            copy.addAll(sessionExpirationTiming);
        }
        
        // Init
        int counter = 0;
        int result = 0;
        Iterator<SessionTiming> iter = copy.iterator();
        
        // Calculate average
        while (iter.hasNext()) {
            SessionTiming timing = iter.next();
            if (timing != null) {
                int timeAlive = timing.getDuration();
                counter++;
                // Very careful not to overflow - probably not necessary
                result =
                    (result * ((counter - 1)/counter)) + (timeAlive/counter);
            }
        }
        return result;
    }

    
    /**
     * Gets the current rate of session creation (in session per minute) based
     * on the creation time of the previous 100 sessions created. If less than
     * 100 sessions have been created then all available data is used.
     * 
     * @return  The current rate (in sessions per minute) of session creation
     */
    @Override
    public int getSessionCreateRate() {
        long now = System.currentTimeMillis();
        // Copy current stats
        List<SessionTiming> copy = new ArrayList<SessionTiming>();
        synchronized (sessionCreationTiming) {
            copy.addAll(sessionCreationTiming);
        }
        
        // Init
        long oldest = now;
        int counter = 0;
        int result = 0;
        Iterator<SessionTiming> iter = copy.iterator();
        
        // Calculate rate
        while (iter.hasNext()) {
            SessionTiming timing = iter.next();
            if (timing != null) {
                counter++;
                if (timing.getTimestamp() < oldest) {
                    oldest = timing.getTimestamp();
                }
            }
        }
        if (counter > 0) {
            if (oldest < now) {
                result = (1000*60*counter)/(int) (now - oldest);
            } else {
                result = Integer.MAX_VALUE;
            }
        }
        return result;
    }
    

    /**
     * Gets the current rate of session expiration (in session per minute) based
     * on the expiry time of the previous 100 sessions expired. If less than
     * 100 sessions have expired then all available data is used.
     * 
     * @return  The current rate (in sessions per minute) of session expiration
     */
    @Override
    public int getSessionExpireRate() {
        long now = System.currentTimeMillis();
        // Copy current stats
        List<SessionTiming> copy = new ArrayList<SessionTiming>();
        synchronized (sessionExpirationTiming) {
            copy.addAll(sessionExpirationTiming);
        }
        
        // Init
        long oldest = now;
        int counter = 0;
        int result = 0;
        Iterator<SessionTiming> iter = copy.iterator();
        
        // Calculate rate
        while (iter.hasNext()) {
            SessionTiming timing = iter.next();
            if (timing != null) {
                counter++;
                if (timing.getTimestamp() < oldest) {
                    oldest = timing.getTimestamp();
                }
            }
        }
        if (counter > 0) {
            if (oldest < now) {
                result = (1000*60*counter)/(int) (now - oldest);
            } else {
                // Better than reporting zero
                result = Integer.MAX_VALUE;
            }
        }
        return result;
    }


    /** 
     * For debugging: return a list of all session ids currently active
     *
     */
    public String listSessionIds() {
        StringBuilder sb=new StringBuilder();
        Iterator<String> keys = sessions.keySet().iterator();
        while (keys.hasNext()) {
            sb.append(keys.next()).append(" ");
        }
        return sb.toString();
    }


    /** 
     * For debugging: get a session attribute
     *
     * @param sessionId
     * @param key
     * @return The attribute value, if found, null otherwise
     */
    public String getSessionAttribute(String sessionId, String key ) {
        Session s = sessions.get(sessionId);
        if( s==null ) {
            if(log.isInfoEnabled())
                log.info("Session not found " + sessionId);
            return null;
        }
        Object o=s.getAttribute(key);
        if( o==null ) return null;
        return o.toString();
    }


    /**
     * Returns information about the session with the given session id.
     * 
     * <p>The session information is organized as a HashMap, mapping 
     * session attribute names to the String representation of their values.
     *
     * @param sessionId Session id
     * 
     * @return HashMap mapping session attribute names to the String
     * representation of their values, or null if no session with the
     * specified id exists, or if the session does not have any attributes
     */
    public HashMap<String, String> getSession(String sessionId) {
        Session s = sessions.get(sessionId);
        if (s == null) {
            if (log.isInfoEnabled()) {
                log.info("Session not found " + sessionId);
            }
            return null;
        }

        Set<String> ee = s.getAttributeNames();
        if (AssertUtil.isEmptyCollection(ee)) {
            return null;
        }

        HashMap<String, String> map = new HashMap<String, String>();
        for (String attrName : ee) {
            map.put(attrName, getSessionAttribute(sessionId, attrName));
        }

        return map;
    }


    public void expireSession( String sessionId ) {
        Session s=sessions.get(sessionId);
        if( s==null ) {
            if(log.isInfoEnabled())
                log.info("Session not found " + sessionId);
            return;
        }
        s.expire();
    }

    public long getThisAccessedTimestamp( String sessionId ) {
        Session s=sessions.get(sessionId);
        if(s== null)
            return -1 ;
        return s.getThisAccessedTime();
    }

    public String getThisAccessedTime( String sessionId ) {
        Session s=sessions.get(sessionId);
        if( s==null ) {
            if(log.isInfoEnabled())
                log.info("Session not found " + sessionId);
            return "";
        }
        return new Date(s.getThisAccessedTime()).toString();
    }

    public long getLastAccessedTimestamp( String sessionId ) {
        Session s=sessions.get(sessionId);
        if(s== null)
            return -1 ;
        return s.getLastAccessedTime();
    }

    public String getLastAccessedTime( String sessionId ) {
        Session s=sessions.get(sessionId);
        if( s==null ) {
            if(log.isInfoEnabled())
                log.info("Session not found " + sessionId);
            return "";
        }
        return new Date(s.getLastAccessedTime()).toString();
    }

    public String getCreationTime( String sessionId ) {
        Session s=sessions.get(sessionId);
        if( s==null ) {
            if(log.isInfoEnabled())
                log.info("Session not found " + sessionId);
            return "";
        }
        return new Date(s.getCreationTime()).toString();
    }

    public long getCreationTimestamp( String sessionId ) {
        Session s=sessions.get(sessionId);
        if(s== null)
            return -1 ;
        return s.getCreationTime();
    }

    // ----------------------------------------------------------- Inner classes
    
    protected static final class SessionTiming {
        private final long timestamp;
        private final int duration;
        
        public SessionTiming(long timestamp, int duration) {
            this.timestamp = timestamp;
            this.duration = duration;
        }
        
        /**
         * Time stamp associated with this piece of timing information in
         * milliseconds.
         */
        public long getTimestamp() {
            return timestamp;
        }
        
        /**
         * Duration associated with this piece of timing information in seconds.
         */
        public int getDuration() {
            return duration;
        }
    }
}
