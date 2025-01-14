/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.compile.tools.annotation.processor;

import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.FieldInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.MethodInfo;
import com.dreameddeath.compile.tools.annotation.processor.testing.ClassWithGenerics;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by Christophe Jeunesse on 22/10/2015.
 */
public class ClassInfoTest {
    @Test
    public void test(){
        ClassInfo classInfo = (ClassInfo)AbstractClassInfo.getClassInfo(ClassWithGenerics.class);
        FieldInfo field = classInfo.getFieldByName("value");
        MethodInfo info = classInfo.getMethod("classWithTreq", field.getType());
        assertNotNull(info);
    }
}
