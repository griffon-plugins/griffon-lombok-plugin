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

import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.FloatProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.ListProperty
import javafx.beans.property.LongProperty
import javafx.beans.property.MapProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SetProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import spock.lang.Specification
import spock.lang.Unroll

import java.beans.PropertyChangeListener
import java.lang.reflect.Method

class FXObservableSpec extends Specification {

    @Unroll
    def "Object with @FXObservable annotation on #target exposes getters, setters and properties"() {
        when:
        def bean = type.newInstance()

        then:
        bean.theStringProperty() instanceof StringProperty
        bean.theSimpleBooleanProperty() instanceof BooleanProperty
        bean.theSimpleCharProperty() instanceof IntegerProperty
        bean.theSimpleByteProperty() instanceof IntegerProperty
        bean.theSimpleShortProperty() instanceof IntegerProperty
        bean.theSimpleIntProperty() instanceof IntegerProperty
        bean.theSimpleLongProperty() instanceof LongProperty
        bean.theSimpleFloatProperty() instanceof FloatProperty
        bean.theSimpleDoubleProperty() instanceof DoubleProperty
        bean.theObjectProperty() instanceof ObjectProperty
        bean.theBooleanProperty() instanceof BooleanProperty
        bean.theCharacterProperty() instanceof IntegerProperty
        bean.theByteProperty() instanceof IntegerProperty
        bean.theShortProperty() instanceof IntegerProperty
        bean.theIntegerProperty() instanceof IntegerProperty
        bean.theLongProperty() instanceof LongProperty
        bean.theFloatProperty() instanceof FloatProperty
        bean.theDoubleProperty() instanceof DoubleProperty
        bean.theMapProperty() instanceof MapProperty
        bean.theSetProperty() instanceof SetProperty
        bean.theListProperty() instanceof ListProperty

        where:
        type               | target
        FXObservableFields | "member"
        FXObservableType   | "class"
    }

    @Unroll
    def "can call setter and getter"() {
        when:
        def bean = type.newInstance()
        bean.theString = "42"
        bean.theSimpleBoolean = true
        bean.theSimpleChar = 42 as char
        bean.theSimpleByte = 42 as byte
        bean.theSimpleShort = 42 as short
        bean.theSimpleInt = 42
        bean.theSimpleLong = 42
        bean.theSimpleFloat = 42
        bean.theSimpleDouble = 42
        bean.theObject = 42
        bean.theBoolean = true
        bean.theCharacter = 42 as Character
        bean.theByte = 42 as Byte
        bean.theShort = 42 as Short
        bean.theInteger = 42
        bean.theLong = 42
        bean.theFloat = 42
        bean.theDouble = 42
        bean.theMap = FXCollections.observableMap([key: 42])
        bean.theSet = FXCollections.observableSet(42)
        bean.theList = FXCollections.observableArrayList(42)

        then:
        bean.theString == "42"
        bean.theSimpleBoolean == true
        bean.theSimpleChar == 42
        bean.theSimpleByte == 42
        bean.theSimpleShort == 42
        bean.theSimpleInt == 42
        bean.theSimpleLong == 42
        bean.theSimpleFloat == 42
        bean.theSimpleDouble == 42
        bean.theObject == 42
        bean.theBoolean == true
        bean.theCharacter == 42
        bean.theByte == 42
        bean.theShort == 42
        bean.theInteger == 42
        bean.theLong == 42
        bean.theFloat == 42
        bean.theDouble == 42
        bean.theMap == FXCollections.observableMap([key: 42])
        bean.theSet == FXCollections.observableSet(42)
        bean.theList == FXCollections.observableArrayList(42)

        where:
        type               | target
        FXObservableFields | "member"
        FXObservableType   | "class"
    }
}
