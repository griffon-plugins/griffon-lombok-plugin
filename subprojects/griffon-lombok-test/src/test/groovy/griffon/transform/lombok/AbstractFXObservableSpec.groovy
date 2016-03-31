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

abstract class AbstractFXObservableSpec<T> extends Specification {

    T bean

    @Unroll
    def "Object exposes getters, setters and properties for #property"() {
        expect:
        bean.class.getMethod("${property}Property").getReturnType() == propertyType
        bean.class.getMethod("${simpleType == boolean.class ? 'is' : 'get'}${property.capitalize()}").getReturnType() == simpleType
        bean.class.getMethod("set${property.capitalize()}", simpleType).getReturnType() == void

        where:
        property           | propertyType    | simpleType
        "theString"        | StringProperty  | String
        "theSimpleBoolean" | BooleanProperty | boolean
        "theSimpleChar"    | IntegerProperty | char
        "theSimpleByte"    | IntegerProperty | byte
        "theSimpleShort"   | IntegerProperty | short
        "theSimpleInt"     | IntegerProperty | int
        "theSimpleLong"    | LongProperty    | long
        "theSimpleFloat"   | FloatProperty   | float
        "theSimpleDouble"  | DoubleProperty  | double
        "theObject"        | ObjectProperty  | Object
        "theBoolean"       | BooleanProperty | Boolean
        "theCharacter"     | IntegerProperty | Character
        "theByte"          | IntegerProperty | Byte
        "theShort"         | IntegerProperty | Short
        "theInteger"       | IntegerProperty | Integer
        "theLong"          | LongProperty    | Long
        "theFloat"         | FloatProperty   | Float
        "theDouble"        | DoubleProperty  | Double
        "theMap"           | MapProperty     | ObservableMap
        "theSet"           | SetProperty     | ObservableSet
        "theList"          | ListProperty    | ObservableList
    }

    @Unroll
    def "can call setter and getter"() {
        when:
        bean."$property" = value

        then:
        bean."$property" == value

        where:
        property           | value
        "theString"        | "42"
        "theSimpleBoolean" | true
        "theSimpleChar"    | 42 as char
        "theSimpleByte"    | 42 as byte
        "theSimpleShort"   | 42 as short
        "theSimpleInt"     | 42
        "theSimpleLong"    | 42
        "theSimpleFloat"   | 42
        "theSimpleDouble"  | 42
        "theObject"        | 42
        "theBoolean"       | true
        "theCharacter"     | 42 as Character
        "theByte"          | 42 as Byte
        "theShort"         | 42 as Short
        "theInteger"       | 42
        "theLong"          | 42
        "theFloat"         | 42
        "theDouble"        | 42
        "theMap"           | FXCollections.observableMap([key: 42])
        "theSet"           | FXCollections.observableSet(42)
        "theList"          | FXCollections.observableArrayList(42)
    }
}
