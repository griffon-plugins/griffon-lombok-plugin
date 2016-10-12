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
package griffon.transform.lombok.fxobservable

import javafx.beans.property.Property
import spock.lang.Unroll

import java.lang.reflect.Field
import java.lang.reflect.Modifier

abstract class AbstractFXObservableShadowFieldSpec<T> extends AbstractFXObservableSpec<T> {

    @Unroll
    def "Object has private member of type Object #propertyName"() {
        given:
        Field field = field(propertyName)

        expect:
        field.modifiers & Modifier.PRIVATE
        field.type == Object

        where:
        propertyName << testData*.name
        propertyType << testData*.propertyType
    }

    @Unroll
    def "member for #propertyName is lazily instantiated"() {
        given:
        Field field = field(propertyName)
        field.accessible = true

        expect:
        field.get(bean) == null

        where:
        propertyName << ['theObservableMapWithDefault', 'theObservableSetWithDefault', 'theObservableListWithDefault']
    }

    @Unroll
    def "using only setter and getter does not create a property instance for #propertyName"() {
        given:
        Field field = field(propertyName)
        field.accessible = true

        expect:
        !(field.get(bean) instanceof Property)

        when:
        bean."$propertyName" = simpleValue

        then:
        !(field.get(bean) instanceof Property)

        where:
        propertyName << testData*.name
        simpleValue << testData*.simpleValue
    }

}
