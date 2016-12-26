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

package cn.wantedonline.puppy.jdbc.exception;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * 直接将spring的异常处理纳入
 *
 * @since V0.5.0
 * @author thunder
 */
public class NestedRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 5439915454935047936L;
    /** Root cause of this nested exception */
    private Throwable cause;

    /**
     * Construct a <code>NestedRuntimeException</code> with the specified detail message.
     * 
     * @param msg the detail message
     */
    public NestedRuntimeException(String msg) {
        super(msg);
    }

    /**
     * Construct a <code>NestedRuntimeException</code> with the specified detail message and nested exception.
     * 
     * @param msg the detail message
     * @param cause the nested exception
     */
    public NestedRuntimeException(String msg, Throwable cause) {
        super(msg);
        this.cause = cause;
    }

    /**
     * Return the nested cause, or <code>null</code> if none.
     * <p>
     * Note that this will only check one level of nesting. Use {@link #getRootCause()} to retrieve the innermost cause.
     */
    @Override
    public Throwable getCause() {
        // Even if you cannot set the cause of this exception other than through
        // the constructor, we check for the cause being "this" here, as the cause
        // could still be set to "this" via reflection: for example, by a remoting
        // deserializer like Hessian's.
        return (this.cause == this ? null : this.cause);
    }

    /**
     * Return the detail message, including the message from the nested exception if there is one.
     */
    @Override
    public String getMessage() {
        if (cause != null) {
            StringBuffer buf = new StringBuffer();
            if (super.getMessage() != null) {
                buf.append(super.getMessage()).append("; ");
            }
            buf.append("nested exception is ").append(cause);
            return buf.toString();
        }
        return super.getMessage();
    }

    /**
     * Print the composite message and the embedded stack trace to the specified stream.
     * 
     * @param ps the print stream
     */
    @Override
    public void printStackTrace(PrintStream ps) {
        if (getCause() == null) {
            super.printStackTrace(ps);
        } else {
            ps.println(this);
            ps.print("Caused by: ");
            getCause().printStackTrace(ps);
        }
    }

    /**
     * Print the composite message and the embedded stack trace to the specified writer.
     * 
     * @param pw the print writer
     */
    @Override
    public void printStackTrace(PrintWriter pw) {
        if (getCause() == null) {
            super.printStackTrace(pw);
        } else {
            pw.println(this);
            pw.print("Caused by: ");
            getCause().printStackTrace(pw);
        }
    }

    /**
     * Retrieve the innermost cause of this exception, if any.
     * <p>
     * Currently just traverses <code>NestedRuntimeException</code> causes. Will use the JDK 1.4 exception cause mechanism once Spring requires JDK 1.4.
     * 
     * @return the innermost exception, or <code>null</code> if none
     * @since 2.0
     */
    public Throwable getRootCause() {
        Throwable cs = getCause();
        if (cs instanceof NestedRuntimeException) {
            Throwable rootCause = ((NestedRuntimeException) cs).getRootCause();
            return (rootCause != null ? rootCause : cs);
        }
        return cs;
    }

    /**
     * Retrieve the most specific cause of this exception, that is, either the innermost cause (root cause) or this exception itself.
     * <p>
     * Differs from {@link #getRootCause()} in that it falls back to the present exception if there is no root cause.
     * 
     * @return the most specific cause (never <code>null</code>)
     * @since 2.0.3
     */
    public Throwable getMostSpecificCause() {
        Throwable rootCause = getRootCause();
        return (rootCause != null ? rootCause : this);
    }

    /**
     * Check whether this exception contains an exception of the given type: either it is of the given class itself or it contains a nested cause of the given type.
     * <p>
     * Currently just traverses <code>NestedRuntimeException</code> causes. Will use the JDK 1.4 exception cause mechanism once Spring requires JDK 1.4.
     * 
     * @param exType the exception type to look for
     * @return whether there is a nested exception of the specified type
     */
    public boolean contains(Class<?> exType) {
        if (exType == null) {
            return false;
        }
        if (exType.isInstance(this)) {
            return true;
        }
        Throwable cs = getCause();
        if (cs instanceof NestedRuntimeException) {
            return ((NestedRuntimeException) cs).contains(exType);
        }
        return (cs != null && exType.isInstance(cs));
    }
}
