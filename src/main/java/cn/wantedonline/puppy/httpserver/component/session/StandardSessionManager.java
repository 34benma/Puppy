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

import cn.wantedonline.puppy.util.Log;
import org.slf4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * <pre>
 *     Refer to Tomcat 7.0.65 Source code
 * </pre>
 *
 * @author wangcheng
 * @since V0.6.3 on 2017.01.09
 */
public class StandardSessionManager extends SessionManagerBase {

    private static StandardSessionManager instance = new StandardSessionManager();

    private StandardSessionManager() {}

    public static StandardSessionManager getInstance() {
        return instance;
    }

    private final Logger log = Log.getLogger(); // must not be static

    /**
     * Path name of the disk file in which active sessions are saved
     * when we stop, and from which these sessions are loaded when we start.
     * A <code>null</code> value indicates that no persistence is desired.
     * If this pathname is relative, it will be resolved against the
     * temporary working directory provided by our context, available via
     * the <code>javax.servlet.context.tempdir</code> context attribute.
     */
    protected String pathname = "SESSIONS.ser";

    /**
     * Return the session persistence pathname, if any.
     */
    public String getPathname() {
        return (this.pathname);
    }


    /**
     * Set the session persistence pathname to the specified value.  If no
     * persistence support is desired, set the pathname to <code>null</code>.
     *
     * @param pathname New session persistence pathname
     */
    public void setPathname(String pathname) {
        this.pathname = pathname;
    }


    // --------------------------------------------------------- Public Methods

    /**
     * Load any currently active sessions that were previously unloaded
     * to the appropriate persistence mechanism, if any.  If persistence is not
     * supported, this method returns without doing anything.
     *
     * @exception ClassNotFoundException if a serialized class cannot be
     *  found during the reload
     * @exception IOException if an input/output error occurs
     */
    @Override
    public void load() throws ClassNotFoundException, IOException {
        doLoad();
    }


    /**
     * Load any currently active sessions that were previously unloaded
     * to the appropriate persistence mechanism, if any.  If persistence is not
     * supported, this method returns without doing anything.
     *
     * @exception ClassNotFoundException if a serialized class cannot be
     *  found during the reload
     * @exception IOException if an input/output error occurs
     */
    protected void doLoad() throws ClassNotFoundException, IOException {
        if (log.isDebugEnabled())
            log.debug("Start: Loading persisted sessions");

        // Initialize our internal data structures
        sessions.clear();
        // Open an input stream to the specified pathname, if any
        File file = file();
        if (file == null)
            return;
        if (log.isDebugEnabled())
            log.debug("Loading persisted sessions from {0}", pathname);
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(file.getAbsolutePath());
            bis = new BufferedInputStream(fis);
            if (log.isDebugEnabled())
                log.debug("Creating standard object input stream");
            ois = new ObjectInputStream(bis);
        } catch (FileNotFoundException e) {
            if (log.isDebugEnabled())
                log.debug("No persisted data file found");
            return;
        } catch (IOException e) {
            log.error("IOException while loading persisted sessions: ", e);
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException f) {
                    // Ignore
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException f) {
                    // Ignore
                }
            }
            throw e;
        }
/*
        // Load the previously unloaded active sessions
        synchronized (sessions) {
            try {
                Integer count = (Integer) ois.readObject();
                int n = count.intValue();
                if (log.isDebugEnabled())
                    log.debug("Loading " + n + " persisted sessions");
                for (int i = 0; i < n; i++) {
                    Session session = getNewSession();
                    session.readObjectData(ois);
                    session.setManager(this);
                    sessions.put(session.getIdInternal(), session);
                    session.activate();
                    if (!session.isValidInternal()) {
                        // If session is already invalid,
                        // expire session to prevent memory leak.
                        session.setValid(true);
                        session.expire();
                    }
                    sessionCounter++;
                }
            } catch (ClassNotFoundException e) {
                log.error(sm.getString("standardManager.loading.cnfe", e), e);
                try {
                    ois.close();
                } catch (IOException f) {
                    // Ignore
                }
                throw e;
            } catch (IOException e) {
                log.error(sm.getString("standardManager.loading.ioe", e), e);
                try {
                    ois.close();
                } catch (IOException f) {
                    // Ignore
                }
                throw e;
            } finally {
                // Close the input stream
                try {
                    ois.close();
                } catch (IOException f) {
                    // ignored
                }

                // Delete the persistent storage file
                if (file.exists() )
                    file.delete();
            }
        }

        if (log.isDebugEnabled())
            log.debug("Finish: Loading persisted sessions");
            */
    }


    /**
     * Save any currently active sessions in the appropriate persistence
     * mechanism, if any.  If persistence is not supported, this method
     * returns without doing anything.
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    public void unload() throws IOException {
        doUnload();
    }


    /**
     * Save any currently active sessions in the appropriate persistence
     * mechanism, if any.  If persistence is not supported, this method
     * returns without doing anything.
     *
     * @exception IOException if an input/output error occurs
     */
    protected void doUnload() throws IOException {
/*
        if (log.isDebugEnabled())
            log.debug(sm.getString("standardManager.unloading.debug"));

        if (sessions.isEmpty()) {
            log.debug(sm.getString("standardManager.unloading.nosessions"));
            return; // nothing to do
        }

        // Open an output stream to the specified pathname, if any
        File file = file();
        if (file == null)
            return;
        if (log.isDebugEnabled())
            log.debug(sm.getString("standardManager.unloading", pathname));
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ObjectOutputStream oos = null;
        boolean error = false;
        try {
            fos = new FileOutputStream(file.getAbsolutePath());
            bos = new BufferedOutputStream(fos);
            oos = new ObjectOutputStream(bos);
        } catch (IOException e) {
            error = true;
            log.error(sm.getString("standardManager.unloading.ioe", e), e);
            throw e;
        } finally {
            if (error) {
                if (oos != null) {
                    try {
                        oos.close();
                    } catch (IOException ioe) {
                        // Ignore
                    }
                }
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException ioe) {
                        // Ignore
                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ioe) {
                        // Ignore
                    }
                }
            }
        }

        // Write the number of active sessions, followed by the details
        ArrayList<StandardSession> list = new ArrayList<StandardSession>();
        synchronized (sessions) {
            if (log.isDebugEnabled())
                log.debug("Unloading " + sessions.size() + " sessions");
            try {
                oos.writeObject(new Integer(sessions.size()));
                Iterator<Session> elements = sessions.values().iterator();
                while (elements.hasNext()) {
                    StandardSession session =
                        (StandardSession) elements.next();
                    list.add(session);
                    session.passivate();
                    session.writeObjectData(oos);
                }
            } catch (IOException e) {
                log.error(sm.getString("standardManager.unloading.ioe", e), e);
                try {
                    oos.close();
                } catch (IOException f) {
                    // Ignore
                }
                throw e;
            }
        }

        // Flush and close the output stream
        try {
            oos.flush();
        } finally {
            try {
                oos.close();
            } catch (IOException f) {
                // Ignore
            }
        }

        // Expire all the sessions we just wrote
        if (log.isDebugEnabled())
            log.debug("Expiring " + list.size() + " persisted sessions");
        Iterator<StandardSession> expires = list.iterator();
        while (expires.hasNext()) {
            StandardSession session = expires.next();
            try {
                session.expire(false);
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
            } finally {
                session.recycle();
            }
        }

        if (log.isDebugEnabled())
            log.debug("Unloading complete");
*/
    }


    /**
     * Start this component and implement the requirements
     * of {@link org.apache.catalina.util.LifecycleBase#startInternal()}.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    @Override
    protected synchronized void startInternal() {
        /*
        super.startInternal();
        try {
            load();
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            log.error(sm.getString("standardManager.managerLoad"), t);
        }
        */
    }


    /**
     * Stop this component and implement the requirements
     * of {@link org.apache.catalina.util.LifecycleBase#stopInternal()}.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    protected synchronized void stopInternal() {
/*
        if (log.isDebugEnabled())
            log.debug("Stopping");

        setState(LifecycleState.STOPPING);
        
        // Write out sessions
        try {
            unload();
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            log.error(sm.getString("standardManager.managerUnload"), t);
        }

        // Expire all active sessions
        Session sessions[] = findSessions();
        for (int i = 0; i < sessions.length; i++) {
            Session session = sessions[i];
            try {
                if (session.isValid()) {
                    session.expire();
                }
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
            } finally {
                // Measure against memory leaking if references to the session
                // object are kept in a shared field somewhere
                session.recycle();
            }
        }

        // Require a new random number generator if we are restarted
        super.stopInternal();
        */
    }


    // ------------------------------------------------------ Protected Methods

    /**
     * Return a File object representing the pathname to our
     * persistence file, if any.
     */
    protected File file() {

        if ((pathname == null) || (pathname.length() == 0))
            return (null);
        File file = new File(pathname);
        if (!file.isAbsolute()) {
            /*
            if (container instanceof Context) {
                ServletContext servletContext =
                    ((Context) container).getServletContext();
                File tempdir = (File)
                    servletContext.getAttribute(ServletContext.TEMPDIR);
                if (tempdir != null)
                    file = new File(tempdir, pathname);
            }
            */
        }
        return (file);
    }
}
