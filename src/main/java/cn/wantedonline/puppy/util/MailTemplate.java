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

package cn.wantedonline.puppy.util;

import java.io.File;

import cn.wantedonline.puppy.httpserver.util.ch.qos.logback.LogFormatFactory;
import org.slf4j.Logger;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;

/**
 * 发送邮件的模板类，需要在初始化时注入一个JavaMailSender的实例
 *
 */
public class MailTemplate {

    private static Logger log = Log.getLogger();
    private static final LogFormatFactory logformat = LogFormatFactory.getInstance("|");
    /**
     * 邮件内容使用的默认编码方式
     */
    private String defaultCharset = "GBK";
    /**
     * 发件人邮箱地址，这个必须要和发邮件的帐号匹配，否则邮件发不出去
     */
    private String fromAddress;
    /**
     * 发件人名称，如 迅雷个人中心
     */
    private String fromName = "";
    /**
     * 发送不成功时的最大重试次数
     */
    private int maxRetryTime = 0;
    /**
     * 发送不成功时的重试间隔，单位为毫秒
     */
    private long retryIdle = 0;
    /**
     * 重试间隔加倍系统
     */
    private int retryIdleFactor = 2;
    /**
     * Spring提供的邮件发送类的对象
     */
    private JavaMailSender sender;

    /**
     * 构造方法
     * 
     * @param sender
     * @param fromAddress
     */
    public MailTemplate(JavaMailSender sender, String fromAddress) {
        this.sender = sender;
        this.fromAddress = fromAddress;
    }

    /**
     * 构造方法
     * 
     * @param sender
     * @param fromAddress
     * @param fromName
     * @param defaultCharset
     * @param maxTryTime
     * @param retryIdle
     */
    public MailTemplate(JavaMailSender sender, String fromAddress, String fromName, String defaultCharset, int maxTryTime, long retryIdle) {
        this.sender = sender;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
        this.defaultCharset = defaultCharset;
        this.maxRetryTime = maxTryTime;
        this.retryIdle = retryIdle;
    }

    /**
     * 发送邮件功能主体部分
     * 
     * @param to 收件人列表
     * @param subject 邮件标题
     * @param content 邮件正文内容
     * @param isMime 是否是MIME邮件
     * @param asHtml 邮件内容是否是HTML内容
     * @param attachments 附件列表
     * @return 发送成功返回true失败返回false
     */
    private boolean send(String[] to, String subject, String content, boolean isMime, boolean asHtml, File... attachments) {
        boolean r = false;
        Exception ex = null;
        String result = "OK";
        long idle = retryIdle;
        long firstNano = System.nanoTime(); // 用于对历次重试做唯一性标识
        // 按最大重试次数进行重试
        for (int i = 0; i <= maxRetryTime; i++) {
            try {
                // 对提交的邮件参数进行校验
                if (EmptyChecker.isEmpty(to)) {
                    throw new NullPointerException("To is null");
                }
                if (StringTools.isEmpty(subject)) {
                    throw new NullPointerException("Subject is empty");
                }
                if (StringTools.isEmpty(content)) {
                    throw new NullPointerException("Content is empty");
                }
                // 分是否是MIME邮件进行分别处理
                if (isMime) {
                    MimeMessage msg = sender.createMimeMessage();
                    MimeMessageHelper mail = new MimeMessageHelper(msg, true, defaultCharset);
                    if (StringTools.isEmpty(fromName)) {
                        mail.setFrom(fromAddress);
                    } else {
                        mail.setFrom(fromAddress, fromName);
                    }
                    mail.setTo(to);
                    mail.setSubject(subject);
                    mail.setText(content, asHtml);
                    // 将内嵌附件加入邮件中
                    if (null != attachments) {
                        for (File file : attachments) {
                            mail.addInline(file.getName(), file);
                        }
                    }
                    sender.send(msg);
                } else {
                    SimpleMailMessage mail = new SimpleMailMessage();
                    mail.setFrom(fromAddress);
                    mail.setTo(to);
                    mail.setSubject(subject);
                    mail.setText(content);
                    sender.send(mail);
                }
                r = true;
            } catch (Exception e) {
                ex = e;
                result = e.getClass().getName() + ": " + StringTools.removeNewLines(e.getMessage());
            } finally {
                if (r) {
                    Object[] args = {
                        firstNano,
                        result,
                        isMime ? "HTML" : "TEXT",
                        i + 1,
                        to,
                        subject,
                        StringTools.removeNewLines(content)
                    };
                    log.debug(logformat.getFormat(args), args);
                } else {
                    Object[] args = {
                        firstNano,
                        result,
                        isMime ? "HTML" : "TEXT",
                        i + 1,
                        to,
                        subject,
                        StringTools.removeNewLines(content),
                        null == ex ? "OK" : StringTools.removeNewLines(ex.getMessage())
                    };
                    log.error(logformat.getFormat(args), args);
                }
            }
            if (r || ex instanceof NullPointerException) {
                break;
            }
            if (i < maxRetryTime) {
                try {
                    Thread.sleep(idle);
                    idle *= retryIdleFactor; // 下一次重试时间间隔延长
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }
        return r;
    }

    /**
     * 发送MIME邮件
     * 
     * @param to 收件人邮箱地址
     * @param subject 邮件标题
     * @param content 邮件正文内容
     */
    public boolean sendMimeMail(String to, String subject, String content, File... attachments) {
        return send(new String[] {
            to
        }, subject, content, true, true, attachments);
    }

    /**
     * 发送MIME邮件
     * 
     * @param to 收件人列表
     * @param subject 邮件标题
     * @param content 邮件正文内容
     */
    public boolean sendMimeMail(String[] to, String subject, String content, File... attachments) {
        return send(to, subject, content, true, true, attachments);
    }

    /**
     * 发送MIME邮件
     * 
     * @param to 收件人列表
     * @param subject 邮件标题
     * @param content 邮件正文内容
     * @param asHtml 邮件内容是否是以HTML方式发送，默认为true。如果用false则可用于发送带附件的纯文本邮件
     */
    public boolean sendMimeMail(String[] to, String subject, String content, boolean asHtml, File... attachments) {
        return send(to, subject, content, true, asHtml, attachments);
    }

    /**
     * 发送MIME邮件
     * 
     * @param to 收件人列表
     * @param subject 邮件标题
     * @param content 邮件正文内容
     * @param asHtml 邮件内容是否是以HTML方式发送，默认为true。如果用false则可用于发送带附件的纯文本邮件
     */
    public boolean sendMimeMail(String to, String subject, String content, boolean asHtml, File... attachments) {
        return send(new String[] {
            to
        }, subject, content, true, asHtml, attachments);
    }

    /**
     * 发送纯文本邮件
     * 
     * @param to 收件人邮箱地址
     * @param subject 邮件标题
     * @param content 邮件正文内容
     */
    public boolean sendTextMail(String to, String subject, String content) {
        return send(new String[] {
            to
        }, subject, content, false, false);
    }

    /**
     * 发送纯文本邮件
     * 
     * @param to 收件人列表
     * @param subject 邮件标题
     * @param content 邮件正文内容
     */
    public boolean sendTextMail(String[] to, String subject, String content) {
        return send(to, subject, content, false, false);
    }

    /**
     * 设置默认的字符
     * 
     * @param defaultCharset
     */
    public void setDefaultCharset(String defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    /**
     * 设置发送人地址
     * 
     * @param fromAddress
     */
    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    /**
     * 设置发送人姓名
     * 
     * @param fromName
     */
    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    /**
     * 设置最大重试次数
     * 
     * @param maxRetryTime
     */
    public void setMaxRetryTime(int maxRetryTime) {
        this.maxRetryTime = maxRetryTime;
    }

    /**
     * 设置最大重试间隔，单位毫秒
     * 
     * @param retryIdle
     */
    public void setRetryIdle(long retryIdle) {
        this.retryIdle = retryIdle;
    }

    /**
     * 设置重试间隔加倍因数
     * 
     * @param retryIdleFactor
     */
    public void setRetryIdleFactor(int retryIdleFactor) {
        this.retryIdleFactor = retryIdleFactor;
    }

    /**
     * 设置发送器
     * 
     * @param sender
     */
    public void setSender(JavaMailSender sender) {
        this.sender = sender;
    }
}
