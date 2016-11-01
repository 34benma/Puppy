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

package cn.wantedonline.puppy.httpserver.util.concurrent;

import cn.wantedonline.puppy.util.EmptyChecker;
import cn.wantedonline.puppy.util.HttpUtil;
import cn.wantedonline.puppy.util.MailTemplate;

/**
 * BaseRunnable抽象类的子类，完成发功邮件的功能
 *
 */
public abstract class BaseRunnableWithMail extends BaseRunnable {

    /**
     * 重写父类的afterProcess方法，在完成父类本身的afterProcess方法后再发送邮件
     */
    @Override
    public void afterProcess() {
        super.afterProcess();
        this.sendMail();
    }

    /**
     * 获得邮件内容
     * 
     * @return 邮件的内容
     */
    public String getMailContent() {
        return this.toString();// TODO:其他更好的信息？
    }

    /**
     * 获得邮件的主题
     * 
     * @return 邮件主题
     */
    public String getMailSubject() {
        return "[" + HttpUtil.getLocalSampleIP() + "]" + getClass().getSimpleName() + " process result";// TODO:应该打服务器ip等信息
    }

    /**
     * 获得Mail模板
     * 
     * @return MailTemplate
     */
    public abstract MailTemplate getMailTemplate();

    /**
     * 获得Mail的邮件目的列表
     * 
     * @return
     */
    public abstract String[] getMailTo();

    /**
     * 发功邮件
     */
    public void sendMail() {
        String[] mailTo = getMailTo();
        if (EmptyChecker.isNotEmpty(mailTo)) {
            getMailTemplate().sendTextMail(getMailTo(), getMailSubject(), getMailContent());
        }
    }
}
