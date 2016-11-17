/*
 * Copyright [2016-2026] wangcheng(wantedonline@outlook.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package cn.wantedonline.puppy.httpserver.common;

import java.io.Externalizable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**

 */

/**
 * URI path map to Object. This mapping implements the path specification recommended in the 2.2 Servlet API. Path specifications can be of the following forms:
 * 
 * <PRE>
 * /foo/bar           - an exact path specification.
 * /foo/*             - a prefix path specification (must end '/*').
 * *.ext              - a suffix path specification.
 * /                  - the default path specification.
 * </PRE>
 * 
 * Matching is performed in the following order
 * <NL>
 * <LI>Exact match.
 * <LI>Longest prefix match.
 * <LI>Longest suffix match.
 * <LI>default.
 * </NL>
 * Multiple path specifications can be mapped by providing a list of specifications. The list is separated by the characters specified in the "org.mortbay.http.PathMap.separators" System property,
 * which defaults to :
 * <P>
 * Special characters within paths such as '?锟?and ';' are not treated specially as it is assumed they would have been either encoded in the original URL or stripped from the path.
 * <P>
 * This class is not synchronized for get's. If concurrent modifications are possible then it should be synchronized at a higher level. 取自jetty6,实现url-pattern,去除default默认路径功能
 * 
 * @author Greg Wilkins (gregw)
 * @author yangyangyang
 * @since 2010-6-9 下午06:13:25
 */
@SuppressWarnings("unchecked")
public class PathMap extends HashMap<Object, Object> implements Externalizable {

    private static String pathSeperators = ":,";

    public static void setPathSpecSeparators(String s) {
        pathSeperators = s;
    }

    StringMap _prefixMap = new StringMap();
    StringMap _suffixMap = new StringMap();
    StringMap _exactMap = new StringMap();

    List _defaultSingletonList = null;
    Entry _prefixDefault = null;
    Set _entrySet;

    /**
     * Construct empty PathMap.
     */
    public PathMap() {
        super(11);
        _entrySet = entrySet();
    }

    /**
     * Construct empty PathMap.
     */
    public PathMap(int capacity) {
        super(capacity);
        _entrySet = entrySet();
    }

    /**
     * Construct from dictionary PathMap.
     */
    public PathMap(Map m) {
        putAll(m);
        _entrySet = entrySet();
    }

    @Override
    public void writeExternal(java.io.ObjectOutput out) throws java.io.IOException {
        HashMap map = new HashMap(this);
        out.writeObject(map);
    }

    @Override
    public void readExternal(java.io.ObjectInput in) throws java.io.IOException, ClassNotFoundException {
        HashMap map = (HashMap) in.readObject();
        this.putAll(map);
    }

    /**
     * Add a single path match to the PathMap.
     * 
     * @param pathSpec The path specification, or comma separated list of path specifications.
     * @param object The object the path maps to
     */
    @Override
    public synchronized Object put(Object pathSpec, Object object) {
        StringTokenizer tok = new StringTokenizer(pathSpec.toString(), pathSeperators);
        Object old = null;

        while (tok.hasMoreTokens()) {
            String spec = tok.nextToken();

            if (!spec.startsWith("/") && !spec.startsWith("*.")) {
                throw new IllegalArgumentException("PathSpec " + spec + ". must start with '/' or '*.'");
            }

            old = super.put(spec, object);

            // Make entry that was just created.
            Entry entry = new Entry(spec, object);

            if (entry.getKey().equals(spec)) {
                if (spec.equals("/*")) {
                    _prefixDefault = entry;
                } else if (spec.endsWith("/*")) {
                    String mapped = spec.substring(0, spec.length() - 2);
                    entry.setMapped(mapped);
                    _prefixMap.put(mapped, entry);
                    _exactMap.put(mapped, entry);
                } else if (spec.startsWith("*.")) {
                    _suffixMap.put(spec.substring(2), entry);
                }
            }
        }

        return old;
    }

    /**
     * Get object matched by the path.
     * 
     * @param path the path.
     * @return Best matched object or null.
     */
    public Object match(String path) {
        Map.Entry entry = getMatch(path);
        if (entry != null) {
            return entry.getValue();
        }
        return null;
    }

    /**
     * Get the entry mapped by the best specification.
     * 
     * @param path the path.
     * @return Map.Entry of the best matched or null.
     */
    public Entry getMatch(String path) {
        Map.Entry entry;

        if (path == null) {
            return null;
        }

        int l = path.length();

        // try exact match
        entry = _exactMap.getEntry(path, 0, l);
        if (entry != null) {
            return (Entry) entry.getValue();
        }

        // prefix search
        int i = l;
        while ((i = path.lastIndexOf('/', i - 1)) >= 0) {
            entry = _prefixMap.getEntry(path, 0, i);
            if (entry != null) {
                return (Entry) entry.getValue();
            }
        }

        // Prefix Default
        if (_prefixDefault != null) {
            return _prefixDefault;
        }

        // Extension search
        i = 0;
        while ((i = path.indexOf('.', i + 1)) > 0) {
            entry = _suffixMap.getEntry(path, i + 1, l - i - 1);
            if (entry != null) {
                return (Entry) entry.getValue();
            }
        }
        return null;
    }

    /**
     * Get all entries matched by the path. Best match first.
     * 
     * @param path Path to match
     * @return LazyList of Map.Entry instances key=pathSpec
     */
    public Object getLazyMatches(String path) {
        Map.Entry entry;
        Object entries = null;

        if (path == null) {
            return getList(entries);
        }

        int l = path.length();

        // try exact match
        entry = _exactMap.getEntry(path, 0, l);
        if (entry != null) {
            entries = add(entries, entry.getValue());
        }

        // prefix search
        int i = l - 1;
        while ((i = path.lastIndexOf('/', i - 1)) >= 0) {
            entry = _prefixMap.getEntry(path, 0, i);
            if (entry != null) {
                entries = add(entries, entry.getValue());
            }
        }

        // Prefix Default
        if (_prefixDefault != null) {
            entries = add(entries, _prefixDefault);
        }

        // Extension search
        i = 0;
        while ((i = path.indexOf('.', i + 1)) > 0) {
            entry = _suffixMap.getEntry(path, i + 1, l - i - 1);
            if (entry != null) {
                entries = add(entries, entry.getValue());
            }
        }

        return entries;
    }

    /**
     * Get all entries matched by the path. Best match first.
     * 
     * @param path Path to match
     * @return List of Map.Entry instances key=pathSpec
     */
    public List getMatches(String path) {
        return getList(getLazyMatches(path));
    }

    /**
     * Return whether the path matches any entries in the PathMap, excluding the default entry
     * 
     * @param path Path to match
     * @return Whether the PathMap contains any entries that match this
     */
    public boolean containsMatch(String path) {
        Entry match = getMatch(path);
        return match != null;
    }

    @Override
    public synchronized Object remove(Object pathSpec) {
        if (pathSpec != null) {
            String spec = (String) pathSpec;
            if (spec.equals("/*")) {
                _prefixDefault = null;
            } else if (spec.endsWith("/*")) {
                _prefixMap.remove(spec.substring(0, spec.length() - 2));
                _exactMap.remove(spec.substring(0, spec.length() - 1));
                _exactMap.remove(spec.substring(0, spec.length() - 2));
            } else if (spec.startsWith("*.")) {
                _suffixMap.remove(spec.substring(2));
            } else if (spec.equals("/")) {
                // _default = null;
                _defaultSingletonList = null;
            } else {
                _exactMap.remove(spec);
            }
        }
        return super.remove(pathSpec);
    }

    @Override
    public void clear() {
        _exactMap = new StringMap();
        _prefixMap = new StringMap();
        _suffixMap = new StringMap();
        // _default = null;
        _defaultSingletonList = null;
        super.clear();
    }

    /**
     * @return true if match.
     */
    public static boolean match(String pathSpec, String path) throws IllegalArgumentException {
        return match(pathSpec, path, false);
    }

    /**
     * @return true if match.
     */
    public static boolean match(String pathSpec, String path, boolean noDefault) throws IllegalArgumentException {
        char c = pathSpec.charAt(0);
        if (c == '/') {
            if (!noDefault && pathSpec.length() == 1 || pathSpec.equals(path)) {
                return true;
            }

            if (isPathWildcardMatch(pathSpec, path)) {
                return true;
            }
        } else if (c == '*') {
            return path.regionMatches(path.length() - pathSpec.length() + 1, pathSpec, 1, pathSpec.length() - 1);
        }
        return false;
    }

    private static boolean isPathWildcardMatch(String pathSpec, String path) {
        // For a spec of "/foo/*" match "/foo" , "/foo/..." but not "/foobar"
        int cpl = pathSpec.length() - 2;
        if (pathSpec.endsWith("/*") && path.regionMatches(0, pathSpec, 0, cpl)) {
            if (path.length() == cpl || '/' == path.charAt(cpl)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the portion of a path that matches a path spec.
     * 
     * @return null if no match at all.
     */
    public static String pathMatch(String pathSpec, String path) {
        char c = pathSpec.charAt(0);

        if (c == '/') {
            if (pathSpec.length() == 1) {
                return path;
            }

            if (pathSpec.equals(path)) {
                return path;
            }

            if (isPathWildcardMatch(pathSpec, path)) {
                return path.substring(0, pathSpec.length() - 2);
            }
        } else if (c == '*') {
            if (path.regionMatches(path.length() - (pathSpec.length() - 1), pathSpec, 1, pathSpec.length() - 1)) {
                return path;
            }
        }
        return null;
    }

    /**
     * Return the portion of a path that is after a path spec.
     * 
     * @return The path info string
     */
    public static String pathInfo(String pathSpec, String path) {
        char c = pathSpec.charAt(0);

        if (c == '/') {
            if (pathSpec.length() == 1) {
                return null;
            }

            boolean wildcard = isPathWildcardMatch(pathSpec, path);

            // handle the case where pathSpec uses a wildcard and path info is
            // "/*"
            if (pathSpec.equals(path) && !wildcard) {
                return null;
            }

            if (wildcard) {
                if (path.length() == pathSpec.length() - 2) {
                    return null;
                }
                return path.substring(pathSpec.length() - 2);
            }
        }
        return null;
    }

    /**
     * Relative path.
     * 
     * @param base The base the path is relative to.
     * @param pathSpec The spec of the path segment to ignore.
     * @param path the additional path
     * @return base plus path with pathspec removed
     */
    public static String relativePath(String base, String pathSpec, String path) {
        String info = pathInfo(pathSpec, path);
        if (info == null) {
            info = path;
        }

        if (info.startsWith("./")) {
            info = info.substring(2);
        }
        if (base.endsWith("/")) {
            if (info.startsWith("/")) {
                path = base + info.substring(1);
            } else {
                path = base + info;
            }
        } else if (info.startsWith("/")) {
            path = base + info;
        } else {
            path = base + "/" + info;
        }
        return path;
    }

    public static class Entry<K> implements Map.Entry<Object, Object> {

        private K key;
        private K value;
        private K mapped;

        Entry(K key, K value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public K getValue() {
            return value;
        }

        @Override
        public K setValue(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }

        public K getMapped() {
            return mapped;
        }

        void setMapped(K mapped) {
            this.mapped = mapped;
        }
    }

    public static class StringMap extends AbstractMap implements Externalizable {

        public static final boolean CASE_INSENSTIVE = true;
        protected static final int __HASH_WIDTH = 17;

        protected int _width = __HASH_WIDTH;
        protected Node _root = new Node();
        protected boolean _ignoreCase = false;
        // protected NullEntry _nullEntry = null;
        protected Object _nullValue = null;
        protected HashSet _entrySet = new HashSet(3);

        public StringMap() {
        }

        public StringMap(boolean ignoreCase) {
            this();
            _ignoreCase = ignoreCase;
        }

        /**
         * Constructor.
         * 
         * @param ignoreCase
         * @param width Width of hash tables, larger values are faster but use more memory.
         */
        public StringMap(boolean ignoreCase, int width) {
            this();
            _ignoreCase = ignoreCase;
            _width = width;
        }

        /**
         * Set the ignoreCase attribute.
         * 
         * @param ic If true, the map is case insensitive for keys.
         */
        public void setIgnoreCase(boolean ic) {
            if (_root._children != null) {
                throw new IllegalStateException("Must be set before first put");
            }
            _ignoreCase = ic;
        }

        public boolean isIgnoreCase() {
            return _ignoreCase;
        }

        /**
         * Set the hash width.
         * 
         * @param width Width of hash tables, larger values are faster but use more memory.
         */
        public void setWidth(int width) {
            _width = width;
        }

        public int getWidth() {
            return _width;
        }

        @Override
        public Object put(Object key, Object value) {
            if (key == null) {
                return put(null, value);
            }
            return put(key.toString(), value);
        }

        public Object put(String key, Object value) {
            if (key == null) {
                Object oldValue = _nullValue;
                _nullValue = value;
                return oldValue;
            }

            Node node = _root;
            int ni = -1;
            Node prev = null;
            Node parent = null;

            // look for best match
            charLoop: for (int i = 0; i < key.length(); i++) {
                char c = key.charAt(i);

                // Advance node
                if (ni == -1) {
                    parent = node;
                    prev = null;
                    ni = 0;
                    node = (node._children == null) ? null : node._children[c % _width];
                }

                // Loop through a node chain at the same level
                while (node != null) {
                    // If it is a matching node, goto next char
                    if (node._char[ni] == c || _ignoreCase && node._ochar[ni] == c) {
                        prev = null;
                        ni++;
                        if (ni == node._char.length) {
                            ni = -1;
                        }
                        continue charLoop;
                    }

                    // no char match
                    // if the first char,
                    if (ni == 0) {
                        // look along the chain for a char match
                        prev = node;
                        node = node._next;
                    } else {
                        // Split the current node!
                        node.split(this, ni);
                        i--;
                        ni = -1;
                        continue charLoop;
                    }
                }

                // We have run out of nodes, so as this is a put, make one
                node = new Node(_ignoreCase, key, i);

                if (prev != null) {
                    prev._next = node;
                } else if (parent != null) // add new child
                {
                    if (parent._children == null) {
                        parent._children = new Node[_width];
                    }
                    parent._children[c % _width] = node;
                    int oi = node._ochar[0] % _width;
                    if (node._ochar != null && node._char[0] % _width != oi) {
                        if (parent._children[oi] == null) {
                            parent._children[oi] = node;
                        } else {
                            Node n = parent._children[oi];
                            while (n._next != null) {
                                n = n._next;
                            }
                            n._next = node;
                        }
                    }
                } else {
                    // this is the root.
                    _root = node;
                }
                break;
            }

            // Do we have a node
            if (node != null) {
                // Split it if we are in the middle
                if (ni > 0) {
                    node.split(this, ni);
                }

                Object old = node._value;
                node._key = key;
                node._value = value;
                _entrySet.add(node);
                return old;
            }
            return null;
        }

        @Override
        public Object get(Object key) {
            if (key == null) {
                return _nullValue;
            }
            if (key instanceof String) {
                return get((String) key);
            }
            return get(key.toString());
        }

        public Object get(String key) {
            if (key == null) {
                return _nullValue;
            }

            Entry entry = getEntry(key, 0, key.length());
            if (entry == null) {
                return null;
            }
            return entry.getValue();
        }

        /**
         * Get a map entry by substring key.
         * 
         * @param key String containing the key
         * @param offset Offset of the key within the String.
         * @param length The length of the key
         * @return The Map.Entry for the key or null if the key is not in the map.
         */
        public Entry getEntry(String key, int offset, int length) {
            if (key == null) {
                return null;
            }

            Node node = _root;
            int ni = -1;

            // look for best match
            charLoop: for (int i = 0; i < length; i++) {
                char c = key.charAt(offset + i);

                // Advance node
                if (ni == -1) {
                    ni = 0;
                    node = (node._children == null) ? null : node._children[c % _width];
                }

                // Look through the node chain
                while (node != null) {
                    // If it is a matching node, goto next char
                    if (node._char[ni] == c || _ignoreCase && node._ochar[ni] == c) {
                        ni++;
                        if (ni == node._char.length) {
                            ni = -1;
                        }
                        continue charLoop;
                    }

                    // No char match, so if mid node then no match at all.
                    if (ni > 0) {
                        return null;
                    }

                    // try next in chain
                    node = node._next;
                }
                return null;
            }

            if (ni > 0) {
                return null;
            }
            if (node != null && node._key == null) {
                return null;
            }
            return node;
        }

        /**
         * Get a map entry by char array key.
         * 
         * @param key char array containing the key
         * @param offset Offset of the key within the array.
         * @param length The length of the key
         * @return The Map.Entry for the key or null if the key is not in the map.
         */
        public Entry getEntry(char[] key, int offset, int length) {
            if (key == null) {
                return null;
            }

            Node node = _root;
            int ni = -1;

            // look for best match
            charLoop: for (int i = 0; i < length; i++) {
                char c = key[offset + i];

                // Advance node
                if (ni == -1) {
                    ni = 0;
                    node = (node._children == null) ? null : node._children[c % _width];
                }

                // While we have a node to try
                while (node != null) {
                    // If it is a matching node, goto next char
                    if (node._char[ni] == c || _ignoreCase && node._ochar[ni] == c) {
                        ni++;
                        if (ni == node._char.length) {
                            ni = -1;
                        }
                        continue charLoop;
                    }

                    // No char match, so if mid node then no match at all.
                    if (ni > 0) {
                        return null;
                    }

                    // try next in chain
                    node = node._next;
                }
                return null;
            }

            if (ni > 0) {
                return null;
            }
            if (node != null && node._key == null) {
                return null;
            }
            return node;
        }

        /**
         * Get a map entry by byte array key, using as much of the passed key as needed for a match. A simple 8859-1 byte to char mapping is assumed.
         * 
         * @param key char array containing the key
         * @param offset Offset of the key within the array.
         * @param maxLength The length of the key
         * @return The Map.Entry for the key or null if the key is not in the map.
         */
        public Entry getBestEntry(byte[] key, int offset, int maxLength) {
            if (key == null) {
                return null;
            }

            Node node = _root;
            int ni = -1;

            // look for best match
            charLoop: for (int i = 0; i < maxLength; i++) {
                char c = (char) key[offset + i];

                // Advance node
                if (ni == -1) {
                    ni = 0;

                    Node child = (node._children == null) ? null : node._children[c % _width];

                    if (child == null && i > 0) {
                        return node; // This is the best match
                    }
                    node = child;
                }

                // While we have a node to try
                while (node != null) {
                    // If it is a matching node, goto next char
                    if (node._char[ni] == c || _ignoreCase && node._ochar[ni] == c) {
                        ni++;
                        if (ni == node._char.length) {
                            ni = -1;
                        }
                        continue charLoop;
                    }

                    // No char match, so if mid node then no match at all.
                    if (ni > 0) {
                        return null;
                    }

                    // try next in chain
                    node = node._next;
                }
                return null;
            }

            if (ni > 0) {
                return null;
            }
            if (node != null && node._key == null) {
                return null;
            }
            return node;
        }

        @Override
        public Object remove(Object key) {
            if (key == null) {
                return remove(null);
            }
            return remove(key.toString());
        }

        public Object remove(String key) {
            if (key == null) {
                Object oldValue = _nullValue;
                return oldValue;
            }

            Node node = _root;
            int ni = -1;

            // look for best match
            charLoop: for (int i = 0; i < key.length(); i++) {
                char c = key.charAt(i);

                // Advance node
                if (ni == -1) {
                    ni = 0;
                    node = (node._children == null) ? null : node._children[c % _width];
                }

                // While we have a node to try
                while (node != null) {
                    // If it is a matching node, goto next char
                    if (node._char[ni] == c || _ignoreCase && node._ochar[ni] == c) {
                        ni++;
                        if (ni == node._char.length) {
                            ni = -1;
                        }
                        continue charLoop;
                    }

                    // No char match, so if mid node then no match at all.
                    if (ni > 0) {
                        return null;
                    }

                    // try next in chain
                    node = node._next;
                }
                return null;
            }

            if (ni > 0) {
                return null;
            }
            if (node != null && node._key == null) {
                return null;
            }

            Object old = node._value;
            _entrySet.remove(node);
            node._value = null;
            node._key = null;

            return old;
        }

        @Override
        public HashSet entrySet() {
            return _entrySet;
        }

        @Override
        public int size() {
            return _entrySet.size();
        }

        @Override
        public boolean isEmpty() {
            return _entrySet.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            if (key == null) {
                return false;
            }
            return getEntry(key.toString(), 0, key == null ? 0 : key.toString().length()) != null;
        }

        @Override
        public void clear() {
            _root = new Node();
            _nullValue = null;
            _entrySet.clear();
        }

        private static class Node implements Entry {

            char[] _char;
            char[] _ochar;
            Node _next;
            Node[] _children;
            String _key;
            Object _value;

            Node() {
            }

            Node(boolean ignoreCase, String s, int offset) {
                int l = s.length() - offset;
                _char = new char[l];
                _ochar = new char[l];
                for (int i = 0; i < l; i++) {
                    char c = s.charAt(offset + i);
                    _char[i] = c;
                    if (ignoreCase) {
                        char o = c;
                        if (Character.isUpperCase(c)) {
                            o = Character.toLowerCase(c);
                        } else if (Character.isLowerCase(c)) {
                            o = Character.toUpperCase(c);
                        }
                        _ochar[i] = o;
                    }
                }
            }

            Node split(StringMap map, int offset) {
                Node split = new Node();
                int sl = _char.length - offset;

                char[] tmp = this._char;
                this._char = new char[offset];
                split._char = new char[sl];
                System.arraycopy(tmp, 0, this._char, 0, offset);
                System.arraycopy(tmp, offset, split._char, 0, sl);

                if (this._ochar != null) {
                    tmp = this._ochar;
                    this._ochar = new char[offset];
                    split._ochar = new char[sl];
                    System.arraycopy(tmp, 0, this._ochar, 0, offset);
                    System.arraycopy(tmp, offset, split._ochar, 0, sl);
                }

                split._key = this._key;
                split._value = this._value;
                this._key = null;
                this._value = null;
                if (map._entrySet.remove(this)) {
                    map._entrySet.add(split);
                }

                split._children = this._children;
                this._children = new Node[map._width];
                this._children[split._char[0] % map._width] = split;
                if (split._ochar != null && this._children[split._ochar[0] % map._width] != split) {
                    this._children[split._ochar[0] % map._width] = split;
                }

                return split;
            }

            @Override
            public Object getKey() {
                return _key;
            }

            @Override
            public Object getValue() {
                return _value;
            }

            @Override
            public Object setValue(Object o) {
                Object old = _value;
                _value = o;
                return old;
            }

            @Override
            public String toString() {
                StringBuffer buf = new StringBuffer();
                synchronized (buf) {
                    toString(buf);
                }
                return buf.toString();
            }

            private void toString(StringBuffer buf) {
                buf.append("{[");
                if (_char == null) {
                    buf.append('-');
                } else {
                    for (int i = 0; i < _char.length; i++) {
                        buf.append(_char[i]);
                    }
                }
                buf.append(':');
                buf.append(_key);
                buf.append('=');
                buf.append(_value);
                buf.append(']');
                if (_children != null) {
                    for (int i = 0; i < _children.length; i++) {
                        buf.append('|');
                        if (_children[i] != null) {
                            _children[i].toString(buf);
                        } else {
                            buf.append("-");
                        }
                    }
                }
                buf.append('}');
                if (_next != null) {
                    buf.append(",\n");
                    _next.toString(buf);
                }
            }
        }

        @Override
        public void writeExternal(java.io.ObjectOutput out) throws java.io.IOException {
            HashMap map = new HashMap(this);
            out.writeBoolean(_ignoreCase);
            out.writeObject(map);
        }

        @Override
        public void readExternal(java.io.ObjectInput in) throws java.io.IOException, ClassNotFoundException {
            boolean ic = in.readBoolean();
            HashMap map = (HashMap) in.readObject();
            setIgnoreCase(ic);
            this.putAll(map);
        }
    }

    public static Object add(Object list, Object item) {
        if (list == null) {
            if (item instanceof List || item == null) {
                List l = new ArrayList();
                l.add(item);
                return l;
            }

            return item;
        }

        if (list instanceof List) {
            ((List) list).add(item);
            return list;
        }

        List l = new ArrayList();
        l.add(list);
        l.add(item);
        return l;
    }

    public static List getList(Object list) {
        if (list == null) {
            return null;
        }
        if (list instanceof List) {
            return (List) list;
        }

        List l = new ArrayList(1);
        l.add(list);
        return l;
    }

}
