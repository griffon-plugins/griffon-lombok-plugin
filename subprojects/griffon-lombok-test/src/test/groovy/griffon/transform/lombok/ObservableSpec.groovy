/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package griffon.transform.lombok

import spock.lang.Specification
import spock.lang.Unroll

import java.beans.PropertyChangeListener
import java.lang.reflect.Method

class ObservableSpec extends Specification {

    @Unroll
    def "object with @Observable annotation on #target has all methods of class Observable"() {
        when:
        def bean = type.newInstance()

        then: "bean is an Observable"
        bean instanceof griffon.core.Observable
        griffon.core.Observable.methods.each { Method target ->
            assert bean.class.declaredMethods.find { Method candidate ->
                candidate.name == target.name &&
                        candidate.returnType == target.returnType &&
                        candidate.parameterTypes == target.parameterTypes &&
                        candidate.exceptionTypes == target.exceptionTypes
            }
        }

        when:
        PropertyChangeListener listener = Mock()
        bean.addProperyChangeListener("stringProperty", listener)

        and:
        bean.stringProperty = "test"

        then:
        1 * listener.propertyChange(_)

        when:
        bean.intProperty = 1

        then:
        0 * listener.propertyChange(_)

        where:
        type             | target
        ObservableFields | "member"
        ObservableType   | "class"
    }
}
