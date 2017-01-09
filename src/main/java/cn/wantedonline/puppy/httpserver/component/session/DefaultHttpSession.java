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

import cn.wantedonline.puppy.util.AssertUtil;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <pre>
 *     Refer to Tomcat 7.0.65 Source Code
 * </pre>
 *
 * @author wangcheng
 * @since V 0.6.3 on 2017.01.06
 */
public class DefaultHttpSession implements Session, Serializable {

    /**
     * The collection of user data attributes associated with this Session.
     */
    protected Map<String, Object> attributes = new ConcurrentHashMap<String, Object>();

    /**
     * The time this session was created, in milliseconds since midnight,
     * January 1, 1970 GMT.
     */
    protected long creationTime = 0L;

    /**
     * We are currently processing a session expiration, so bypass
     * certain IllegalStateException tests.  NOTE:  This value is not
     * included in the serialized version of this object.
     */
    protected transient volatile boolean expiring = false;

    /**
     * The session identifier of this Session.
     */
    protected String id = null;

    /**
     * The last accessed time for this Session.
     */
    protected volatile long lastAccessedTime = creationTime;

    /**
     * The maximum time interval, in seconds, between client requests before
     * the servlet container may invalidate this session.  A negative time
     * indicates that the session should never time out.
     */
    protected int maxInactiveInterval = -1;


    /**
     * Flag indicating whether this session is new or not.
     */
    protected boolean isNew = false;


    /**
     * Flag indicating whether this session is valid or not.
     */
    protected volatile boolean isValid = false;


    /**
     * The current accessed time for this session.
     */
    protected volatile long thisAccessedTime = creationTime;


    @Override
    public Object getAttribute(String name) {
        if (!isValid()) {
            return null;
        }

        return attributes.get(name);
    }

    @Override
    public Set<String> getAttributeNames() {
        if (!isValid()) {
            return null;
        }

        return attributes.keySet();
    }

    @Override
    public void removeAttribute(String name) {
        if (AssertUtil.isNull(name)) return;
        attributes.remove(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        if (AssertUtil.isNull(name)) {
            throw new IllegalArgumentException("setAttribute: name parameter cannot be null");
        }

        if (AssertUtil.isNull(value)) {
            attributes.remove(name);
            return;
        }

        if (!isValid()) {
            throw new IllegalStateException("setAttribute: Session [" + this.id + "] has already been invalidated");
        }

        attributes.put(name, value);
    }

    @Override
    public long getCreationTime() {
        return this.creationTime;
    }

    @Override
    public void setCreationTime(long time) {
        this.creationTime = time;
        this.lastAccessedTime = time;
        this.thisAccessedTime = time;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public long getThisAccessedTime() {
        return this.thisAccessedTime;
    }

    @Override
    public long getLastAccessedTime() {
        return this.lastAccessedTime;
    }

    @Override
    public int getMaxInactiveInterval() {
        return this.maxInactiveInterval;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    @Override
    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    @Override
    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    @Override
    public boolean isValid() {
        if (!this.isValid) {
            return false;
        }

        if (this.expiring) {
            return true;
        }

        if (maxInactiveInterval > 0) {
            long timeNow = System.currentTimeMillis();
            int timeIdle = (int) ((timeNow - thisAccessedTime) / 1000L);
            if (timeIdle >= maxInactiveInterval) {
                expire();
            }
        }
        return this.isValid;
    }

    @Override
    public void access() {
        this.thisAccessedTime = System.currentTimeMillis();
    }

    @Override
    public void endAccess() {
        this.isNew = false;
        this.thisAccessedTime = System.currentTimeMillis();
        this.lastAccessedTime = this.thisAccessedTime;
    }

    @Override
    public void expire() {
        if (!isValid) return;
        synchronized (this) {
            if (expiring || !isValid) return;
            expiring = true;
            attributes.clear();
            setValid(false);
            expiring = false;
        }
    }

    @Override
    public void recycle() {
        attributes.clear();
        creationTime = 0L;
        expiring = false;
        id = null;
        lastAccessedTime = 0L;
        maxInactiveInterval = -1;
        isNew = false;
        isValid = false;
    }

}
