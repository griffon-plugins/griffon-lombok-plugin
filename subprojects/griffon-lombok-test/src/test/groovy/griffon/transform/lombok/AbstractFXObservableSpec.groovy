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
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

abstract class AbstractFXObservableSpec<T> extends Specification {

    private static List<Map<String, Object>> testData = [
            [name: "theString", propertyType: StringProperty, simpleType: String, simpleValue: "42", propertyValue: "42", defaultSimpleValue: null, defaultPropertyValue: null],
            [name: "theSimpleBoolean", propertyType: BooleanProperty, simpleType: boolean, simpleValue: true, propertyValue: true, defaultSimpleValue: false, defaultPropertyValue: false],
            [name: "theSimpleChar", propertyType: IntegerProperty, simpleType: char, simpleValue: 42 as char, propertyValue: 42, defaultSimpleValue: 0 as char, defaultPropertyValue: 0 as char],
            [name: "theSimpleByte", propertyType: IntegerProperty, simpleType: byte, simpleValue: 42 as byte, propertyValue: 42, defaultSimpleValue: 0 as byte, defaultPropertyValue: 0 as byte],
            [name: "theSimpleShort", propertyType: IntegerProperty, simpleType: short, simpleValue: 42 as short, propertyValue: 42, defaultSimpleValue: 0 as short, defaultPropertyValue: 0 as short],
            [name: "theSimpleInt", propertyType: IntegerProperty, simpleType: int, simpleValue: 42, propertyValue: 42, defaultSimpleValue: 0, defaultPropertyValue: 0],
            [name: "theSimpleLong", propertyType: LongProperty, simpleType: long, simpleValue: 42, propertyValue: 42L, , defaultSimpleValue: 0L, defaultPropertyValue: 0L],
            [name: "theSimpleFloat", propertyType: FloatProperty, simpleType: float, simpleValue: 42, propertyValue: 42f, defaultSimpleValue: 0f, defaultPropertyValue: 0f],
            [name: "theSimpleDouble", propertyType: DoubleProperty, simpleType: double, simpleValue: 42, propertyValue: 42d, defaultSimpleValue: 0d, defaultPropertyValue: 0d],
            [name: "theObject", propertyType: ObjectProperty, simpleType: Object, simpleValue: 42, propertyValue: 42, defaultSimpleValue: null, defaultPropertyValue: null],
            [name: "theBoolean", propertyType: BooleanProperty, simpleType: Boolean, simpleValue: true, propertyValue: true, defaultSimpleValue: null, defaultPropertyValue: false],
            [name: "theCharacter", propertyType: IntegerProperty, simpleType: Character, simpleValue: 42 as Character, propertyValue: 42, defaultSimpleValue: null, defaultPropertyValue: 0 as Character],
            [name: "theByte", propertyType: IntegerProperty, simpleType: Byte, simpleValue: 42 as Byte, propertyValue: 42, defaultSimpleValue: null, defaultPropertyValue: 0 as Byte],
            [name: "theShort", propertyType: IntegerProperty, simpleType: Short, simpleValue: 42 as Short, propertyValue: 42, defaultSimpleValue: null, defaultPropertyValue: 0 as Short],
            [name: "theInteger", propertyType: IntegerProperty, simpleType: Integer, simpleValue: 42, propertyValue: 42, defaultSimpleValue: null, defaultPropertyValue: 0],
            [name: "theLong", propertyType: LongProperty, simpleType: Long, simpleValue: 42, propertyValue: 42L, defaultSimpleValue: null, defaultPropertyValue: 0L],
            [name: "theFloat", propertyType: FloatProperty, simpleType: Float, simpleValue: 42, propertyValue: 42f, defaultSimpleValue: null, defaultPropertyValue: 0f],
            [name: "theDouble", propertyType: DoubleProperty, simpleType: Double, simpleValue: 42, propertyValue: 42d, defaultSimpleValue: null, defaultPropertyValue: 0d],
            [name: "theMap", propertyType: MapProperty, simpleType: ObservableMap, simpleValue: FXCollections.observableMap([key: 42]), propertyValue: FXCollections.observableMap([key: 42]), defaultSimpleValue: null, defaultPropertyValue: null],
            [name: "theSet", propertyType: SetProperty, simpleType: ObservableSet, simpleValue: FXCollections.observableSet(42), propertyValue: FXCollections.observableSet(42), defaultSimpleValue: null, defaultPropertyValue: null],
            [name: "theList", propertyType: ListProperty, simpleType: ObservableList, simpleValue: FXCollections.observableArrayList(42), propertyValue: FXCollections.observableArrayList(42), defaultSimpleValue: null, defaultPropertyValue: null],

            [name: "theStringWithDefault", propertyType: StringProperty, simpleType: String, simpleValue: "42", propertyValue: "42", defaultSimpleValue: "42", defaultPropertyValue: "42"],
            [name: "theSimpleBooleanWithDefault", propertyType: BooleanProperty, simpleType: boolean, simpleValue: true, propertyValue: true, defaultSimpleValue: true, defaultPropertyValue: true],
            [name: "theSimpleCharWithDefault", propertyType: IntegerProperty, simpleType: char, simpleValue: 42 as char, propertyValue: 42, defaultSimpleValue: 42, defaultPropertyValue: 42],
            [name: "theSimpleByteWithDefault", propertyType: IntegerProperty, simpleType: byte, simpleValue: 42 as byte, propertyValue: 42, defaultSimpleValue: 42, defaultPropertyValue: 42],
            [name: "theSimpleShortWithDefault", propertyType: IntegerProperty, simpleType: short, simpleValue: 42 as short, propertyValue: 42, defaultSimpleValue: 42, defaultPropertyValue: 42],
            [name: "theSimpleIntWithDefault", propertyType: IntegerProperty, simpleType: int, simpleValue: 42, propertyValue: 42, defaultSimpleValue: 42, defaultPropertyValue: 42],
            [name: "theSimpleLongWithDefault", propertyType: LongProperty, simpleType: long, simpleValue: 42, propertyValue: 42L, defaultSimpleValue: 42L, defaultPropertyValue: 42L],
            [name: "theSimpleFloatWithDefault", propertyType: FloatProperty, simpleType: float, simpleValue: 42, propertyValue: 42f, defaultSimpleValue: 42f, defaultPropertyValue: 42f],
            [name: "theSimpleDoubleWithDefault", propertyType: DoubleProperty, simpleType: double, simpleValue: 42, propertyValue: 42d, defaultSimpleValue: 42d, defaultPropertyValue: 42d],
            [name: "theObjectWithDefault", propertyType: ObjectProperty, simpleType: Object, simpleValue: 42, propertyValue: 42, defaultSimpleValue: 42, defaultPropertyValue: 42],
            [name: "theBooleanWithDefault", propertyType: BooleanProperty, simpleType: Boolean, simpleValue: true, propertyValue: true, defaultSimpleValue: true, defaultPropertyValue: true],
            [name: "theCharacterWithDefault", propertyType: IntegerProperty, simpleType: Character, simpleValue: 42 as Character, propertyValue: 42, defaultSimpleValue: 42, defaultPropertyValue: 42],
            [name: "theByteWithDefault", propertyType: IntegerProperty, simpleType: Byte, simpleValue: 42 as Byte, propertyValue: 42, defaultSimpleValue: 42, defaultPropertyValue: 42],
            [name: "theShortWithDefault", propertyType: IntegerProperty, simpleType: Short, simpleValue: 42 as Short, propertyValue: 42, defaultSimpleValue: 42, defaultPropertyValue: 42],
            [name: "theIntegerWithDefault", propertyType: IntegerProperty, simpleType: Integer, simpleValue: 42, propertyValue: 42, defaultSimpleValue: 42, defaultPropertyValue: 42],
            [name: "theLongWithDefault", propertyType: LongProperty, simpleType: Long, simpleValue: 42, propertyValue: 42L, defaultSimpleValue: 42L, defaultPropertyValue: 42],
            [name: "theFloatWithDefault", propertyType: FloatProperty, simpleType: Float, simpleValue: 42, propertyValue: 42f, defaultSimpleValue: 42f, defaultPropertyValue: 42f],
            [name: "theDoubleWithDefault", propertyType: DoubleProperty, simpleType: Double, simpleValue: 42, propertyValue: 42d, defaultSimpleValue: 42d, defaultPropertyValue: 42d],
            [name: "theMapWithDefault", propertyType: MapProperty, simpleType: ObservableMap, simpleValue: FXCollections.observableMap([key: 42]), propertyValue: FXCollections.observableMap([key: 42]), defaultSimpleValue: FXCollections.observableHashMap(), defaultPropertyValue: FXCollections.observableHashMap()],
            [name: "theSetWithDefault", propertyType: SetProperty, simpleType: ObservableSet, simpleValue: FXCollections.observableSet(42), propertyValue: FXCollections.observableSet(42), defaultSimpleValue: FXCollections.observableSet(42), defaultPropertyValue: FXCollections.observableSet(42)],
            [name: "theListWithDefault", propertyType: ListProperty, simpleType: ObservableList, simpleValue: FXCollections.observableArrayList(42), propertyValue: FXCollections.observableArrayList(42), defaultSimpleValue: FXCollections.observableArrayList(42), defaultPropertyValue: FXCollections.observableArrayList(42)],
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
        bean."$propertyName" == defaultSimpleValue

        where:
        propertyName << testData*.name
        defaultSimpleValue << testData*.defaultSimpleValue
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
        property.getValue() == defaultPropertyValue

        where:
        propertyName << testData*.name
        defaultPropertyValue << testData*.defaultPropertyValue
    }

    @Unroll
    def "using only setter and getter does not create a property instance for #propertyName (Shadow Fields Pattern)"() {
        given:
        Field propertyField = propertyField(propertyName)
        propertyField.accessible = true
        Field simpleField = simpleField(propertyName)
        simpleField.accessible = true

        expect:
        propertyField.get(bean) == null

        when:
        bean."$propertyName" = simpleValue

        then:
        propertyField.get(bean) == null

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

    private Field propertyField(String propertyName) {
        bean.class.getDeclaredField("${propertyName}Property")
    }

    private Field simpleField(String propertyName) {
        bean.class.getDeclaredField("${propertyName}")
    }
}
