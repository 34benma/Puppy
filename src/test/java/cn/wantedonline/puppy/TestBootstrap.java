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

package cn.wantedonline.puppy;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by louiswang on 17/8/1.
 */

public class TestBootstrap {

    @Test
    public void testIsArgsWhat() {
        Assert.assertTrue(Bootstrap.isArgWhat(new String[] {"1", "2", "3"},"1", "3"));
        Assert.assertFalse(Bootstrap.isArgWhat(new String[] {"1", "3", "5"}, "7", "9"));
    }
}
