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
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
import javafx.collections.ObservableSet
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

abstract class AbstractFXObservableSpec<T> extends Specification {

    private static List<Map<String, Object>> testData = [
            [name: "theString", propertyType: StringProperty, simpleType: String, simpleValue: "42", propertyValue: "42", defaultValue: null],
            [name: "thePrimitiveBoolean", propertyType: BooleanProperty, simpleType: boolean, simpleValue: true, propertyValue: true, defaultValue: false],
            [name: "thePrimitiveChar", propertyType: IntegerProperty, simpleType: char, simpleValue: 42 as char, propertyValue: 42, defaultValue: 0 as char],
            [name: "thePrimitiveByte", propertyType: IntegerProperty, simpleType: byte, simpleValue: 42 as byte, propertyValue: 42, defaultValue: 0 as byte],
            [name: "thePrimitiveShort", propertyType: IntegerProperty, simpleType: short, simpleValue: 42 as short, propertyValue: 42, defaultValue: 0 as short],
            [name: "thePrimitiveInt", propertyType: IntegerProperty, simpleType: int, simpleValue: 42, propertyValue: 42, defaultValue: 0],
            [name: "thePrimitiveLong", propertyType: LongProperty, simpleType: long, simpleValue: 42, propertyValue: 42L, , defaultValue: 0L],
            [name: "thePrimitiveFloat", propertyType: FloatProperty, simpleType: float, simpleValue: 42, propertyValue: 42f, defaultValue: 0f],
            [name: "thePrimitiveDouble", propertyType: DoubleProperty, simpleType: double, simpleValue: 42, propertyValue: 42d, defaultValue: 0d],
            [name: "theObject", propertyType: ObjectProperty, simpleType: Object, simpleValue: 42, propertyValue: 42, defaultValue: null],
            [name: "theBoolean", propertyType: BooleanProperty, simpleType: Boolean, simpleValue: true, propertyValue: true, defaultValue: false],
            [name: "theCharacter", propertyType: IntegerProperty, simpleType: Character, simpleValue: 42 as Character, propertyValue: 42, defaultValue: 0 as Character],
            [name: "theByte", propertyType: IntegerProperty, simpleType: Byte, simpleValue: 42 as Byte, propertyValue: 42, defaultValue: 0 as Byte],
            [name: "theShort", propertyType: IntegerProperty, simpleType: Short, simpleValue: 42 as Short, propertyValue: 42, defaultValue: 0 as Short],
            [name: "theInteger", propertyType: IntegerProperty, simpleType: Integer, simpleValue: 42, propertyValue: 42, defaultValue: 0],
            [name: "theLong", propertyType: LongProperty, simpleType: Long, simpleValue: 42, propertyValue: 42L, defaultValue: 0L],
            [name: "theFloat", propertyType: FloatProperty, simpleType: Float, simpleValue: 42, propertyValue: 42f, defaultValue: 0f],
            [name: "theDouble", propertyType: DoubleProperty, simpleType: Double, simpleValue: 42, propertyValue: 42d, defaultValue: 0d],
            [name: "theObservableMap", propertyType: MapProperty, simpleType: ObservableMap, simpleValue: FXCollections.observableMap([key: 42]), propertyValue: FXCollections.observableMap([key: 42]), defaultValue: FXCollections.observableHashMap()],
            [name: "theObservableSet", propertyType: SetProperty, simpleType: ObservableSet, simpleValue: FXCollections.observableSet(42), propertyValue: FXCollections.observableSet(42), defaultValue: FXCollections.observableSet()],
            [name: "theObservableList", propertyType: ListProperty, simpleType: ObservableList, simpleValue: FXCollections.observableArrayList(42), propertyValue: FXCollections.observableArrayList(42), defaultValue: FXCollections.observableArrayList()],

            [name: "theStringWithDefault", propertyType: StringProperty, simpleType: String, simpleValue: "42", propertyValue: "42", defaultValue: "42"],
            [name: "thePrimitiveBooleanWithDefault", propertyType: BooleanProperty, simpleType: boolean, simpleValue: true, propertyValue: true, defaultValue: true],
            [name: "thePrimitiveCharWithDefault", propertyType: IntegerProperty, simpleType: char, simpleValue: 42 as char, propertyValue: 42, defaultValue: 42],
            [name: "thePrimitiveByteWithDefault", propertyType: IntegerProperty, simpleType: byte, simpleValue: 42 as byte, propertyValue: 42, defaultValue: 42],
            [name: "thePrimitiveShortWithDefault", propertyType: IntegerProperty, simpleType: short, simpleValue: 42 as short, propertyValue: 42, defaultValue: 42],
            [name: "thePrimitiveIntWithDefault", propertyType: IntegerProperty, simpleType: int, simpleValue: 42, propertyValue: 42, defaultValue: 42],
            [name: "thePrimitiveLongWithDefault", propertyType: LongProperty, simpleType: long, simpleValue: 42, propertyValue: 42L, defaultValue: 42L],
            [name: "thePrimitiveFloatWithDefault", propertyType: FloatProperty, simpleType: float, simpleValue: 42, propertyValue: 42f, defaultValue: 42f],
            [name: "thePrimitiveDoubleWithDefault", propertyType: DoubleProperty, simpleType: double, simpleValue: 42, propertyValue: 42d, defaultValue: 42d],
            [name: "theObjectWithDefault", propertyType: ObjectProperty, simpleType: Object, simpleValue: 42, propertyValue: 42, defaultValue: 42],
            [name: "theBooleanWithDefault", propertyType: BooleanProperty, simpleType: Boolean, simpleValue: true, propertyValue: true, defaultValue: true],
            [name: "theCharacterWithDefault", propertyType: IntegerProperty, simpleType: Character, simpleValue: 42 as Character, propertyValue: 42, defaultValue: 42],
            [name: "theByteWithDefault", propertyType: IntegerProperty, simpleType: Byte, simpleValue: 42 as Byte, propertyValue: 42, defaultValue: 42],
            [name: "theShortWithDefault", propertyType: IntegerProperty, simpleType: Short, simpleValue: 42 as Short, propertyValue: 42, defaultValue: 42],
            [name: "theIntegerWithDefault", propertyType: IntegerProperty, simpleType: Integer, simpleValue: 42, propertyValue: 42, defaultValue: 42],
            [name: "theLongWithDefault", propertyType: LongProperty, simpleType: Long, simpleValue: 42, propertyValue: 42L, defaultValue: 42L],
            [name: "theFloatWithDefault", propertyType: FloatProperty, simpleType: Float, simpleValue: 42, propertyValue: 42f, defaultValue: 42f],
            [name: "theDoubleWithDefault", propertyType: DoubleProperty, simpleType: Double, simpleValue: 42, propertyValue: 42d, defaultValue: 42d],
            [name: "theObservableMapWithDefault", propertyType: MapProperty, simpleType: ObservableMap, simpleValue: FXCollections.observableMap([key: 42]), propertyValue: FXCollections.observableMap([key: 42]), defaultValue: FXCollections.observableHashMap()],
            [name: "theObservableSetWithDefault", propertyType: SetProperty, simpleType: ObservableSet, simpleValue: FXCollections.observableSet(42), propertyValue: FXCollections.observableSet(42), defaultValue: FXCollections.observableSet(42)],
            [name: "theObservableListWithDefault", propertyType: ListProperty, simpleType: ObservableList, simpleValue: FXCollections.observableArrayList(42), propertyValue: FXCollections.observableArrayList(42), defaultValue: FXCollections.observableArrayList(42)],
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
        simpleType << testData*.simpleType }

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
    def "can call setter and getter for #propertyName"() {
        when:
        bean."$propertyName" = simpleValue

        then:
        bean."$propertyName" == simpleValue

        where:
        propertyName << testData*.name
        simpleValue << testData*.simpleValue
    }

    @Unroll
    def "getter for #propertyName returns the default value after instantiation"() {
        expect:
        bean."$propertyName" == defaultValue

        where:
        propertyName << testData*.name
        defaultValue << testData*.defaultValue
    }

    @Unroll
    def "can use property-method for #propertyName"() {
        given:
        Property property = bean."${propertyName}Property"()

        when:
        property.setValue(propertyValue)

        then:
        property.getValue() == propertyValue

        where:
        propertyName << testData*.name
        propertyValue << testData*.propertyValue
    }

    @Unroll
    def "property-method exposes default value for #propertyName after instantiation"() {
        given:
        Property property = bean."${propertyName}Property"()

        expect:
        property.getValue() == defaultValue

        where:
        propertyName << testData*.name
        defaultValue << testData*.defaultValue
    }

    @Unroll
    def "can use getter and setter after property is initialized for #propertyName"() {
        given:
        Property property = bean."${propertyName}Property"()

        expect:
        property.getValue() == bean."$propertyName"

        when:
        bean."$propertyName" = simpleValue

        then:
        property.getValue() == simpleValue

        and:
        bean."$propertyName" == simpleValue

        where:
        propertyName << testData*.name
        defaultValue << testData*.defaultValue
        simpleValue << testData*.simpleValue
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

    private Field field(String propertyName) {
        bean.class.getDeclaredField("${propertyName}")
    }
}
