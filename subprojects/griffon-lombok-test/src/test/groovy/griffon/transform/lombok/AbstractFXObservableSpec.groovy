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

import javafx.beans.property.*
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
import javafx.collections.ObservableSet
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

abstract class AbstractFXObservableSpec<T> extends Specification {

    private static List<Map<String, Object>> testData = [
            [name: "theString", propertyType: StringProperty, simpleType: String, sampleValue: "42"],
            [name: "theSimpleBoolean", propertyType: BooleanProperty, simpleType: boolean, sampleValue: true],
            [name: "theSimpleChar", propertyType: IntegerProperty, simpleType: char, sampleValue: 42 as char],
            [name: "theSimpleByte", propertyType: IntegerProperty, simpleType: byte, sampleValue: 42 as byte],
            [name: "theSimpleShort", propertyType: IntegerProperty, simpleType: short, sampleValue: 42 as short],
            [name: "theSimpleInt", propertyType: IntegerProperty, simpleType: int, sampleValue: 42],
            [name: "theSimpleLong", propertyType: LongProperty, simpleType: long, sampleValue: 42],
            [name: "theSimpleFloat", propertyType: FloatProperty, simpleType: float, sampleValue: 42],
            [name: "theSimpleDouble", propertyType: DoubleProperty, simpleType: double, sampleValue: 42],
            [name: "theObject", propertyType: ObjectProperty, simpleType: Object, sampleValue: 42],
            [name: "theBoolean", propertyType: BooleanProperty, simpleType: Boolean, sampleValue: true],
            [name: "theCharacter", propertyType: IntegerProperty, simpleType: Character, sampleValue: 42 as Character],
            [name: "theByte", propertyType: IntegerProperty, simpleType: Byte, sampleValue: 42 as Byte],
            [name: "theShort", propertyType: IntegerProperty, simpleType: Short, sampleValue: 42 as Short],
            [name: "theInteger", propertyType: IntegerProperty, simpleType: Integer, sampleValue: 42],
            [name: "theLong", propertyType: LongProperty, simpleType: Long, sampleValue: 42],
            [name: "theFloat", propertyType: FloatProperty, simpleType: Float, sampleValue: 42],
            [name: "theDouble", propertyType: DoubleProperty, simpleType: Double, sampleValue: 42],
            [name: "theMap", propertyType: MapProperty, simpleType: ObservableMap, sampleValue: FXCollections.observableMap([key: 42])],
            [name: "theSet", propertyType: SetProperty, simpleType: ObservableSet, sampleValue: FXCollections.observableSet(42)],
            [name: "theList", propertyType: ListProperty, simpleType: ObservableList, sampleValue: FXCollections.observableArrayList(42)],
    ]

    T bean

    @Unroll
    def "Object exposes getter for #propertyName"() {
        given:
        Method getter = getter(propertyName, simpleType)

        expect:
        getter.returnType == simpleType
        getter.modifiers & Modifier.PUBLIC

        where:
        propertyName << testData*.name
        simpleType << testData*.simpleType
    }

    @Unroll
    def "Object exposes setter for #propertyName"() {
        given:
        Method setter = setter(propertyName, simpleType)

        expect:
        setter.returnType == void
        setter.modifiers & Modifier.PUBLIC

        where:
        propertyName << testData*.name
        simpleType << testData*.simpleType
    }

    @Unroll
    def "Object exposes property-method for #propertyName"() {
        given:
        Method propertyMethod = propertyMethod(propertyName)

        expect:
        propertyMethod.returnType == propertyType
        propertyMethod.modifiers & Modifier.PUBLIC

        where:
        propertyName << testData*.name
        propertyType << testData*.propertyType
    }

    @Unroll
    def "Object has private member for fx property #propertyName"() {
        given:
        Field field = propertyField(propertyName)

        expect:
        field.modifiers & Modifier.PRIVATE
        field.type == propertyType

        where:
        propertyName << testData*.name
        propertyType << testData*.propertyType
    }

    @Unroll
    def "Object has private member for simple #propertyName"() {
        given:
        Field field = simpleField(propertyName)

        expect:
        field.modifiers & Modifier.PRIVATE
        field.type == simpleType

        where:
        propertyName << testData*.name
        simpleType << testData*.simpleType
    }

    @Unroll
    def "can call setter and getter for #propertyName"() {
        when:
        bean."$propertyName" = value

        then:
        bean."$propertyName" == value

        where:
        propertyName << testData*.name
        value << testData*.sampleValue
    }

    private Method getter(String propertyName, Class<?> simpleType) {
        String getterName = "${simpleType == boolean.class ? 'is' : 'get'}${propertyName.capitalize()}"
        bean.class.getMethod(getterName)
    }

    private Method setter(String propertyName, Class<?> simpleType) {
        String setterName = "set${propertyName.capitalize()}"
        Method setterMethod = bean.class.getMethod(setterName, simpleType)
    }

    private Method propertyMethod(String propertyName) {
        String propertyMethodName = "${propertyName}Property"
        bean.class.getMethod(propertyMethodName)
    }

    private Field propertyField(String propertyName) {
        bean.class.getDeclaredField("${propertyName}Property")
    }

    private Field simpleField(String propertyName) {
        bean.class.getDeclaredField("${propertyName}")
    }
}
