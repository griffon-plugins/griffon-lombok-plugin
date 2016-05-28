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
package griffon.transform.lombok;

import griffon.transform.FXObservable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;

@FXObservable
public class FXObservableType {
    private String theString;
    private boolean thePrimitiveBoolean;
    private char thePrimitiveChar;
    private byte thePrimitiveByte;
    private short thePrimitiveShort;
    private int thePrimitiveInt;
    private long thePrimitiveLong;
    private float thePrimitiveFloat;
    private double thePrimitiveDouble;
    private Object theObject;
    private Boolean theBoolean;
    private Character theCharacter;
    private Byte theByte;
    private Short theShort;
    private Integer theInteger;
    private Long theLong;
    private Float theFloat;
    private Double theDouble;
    private ObservableMap<String, Integer> theObservableMap;
    private ObservableSet<Integer> theObservableSet;
    private ObservableList<Integer> theObservableList;

    private String theStringWithDefault = "42";
    private boolean thePrimitiveBooleanWithDefault = true;
    private char thePrimitiveCharWithDefault = 42;
    private byte thePrimitiveByteWithDefault = 42;
    private short thePrimitiveShortWithDefault = 42;
    private int thePrimitiveIntWithDefault = 42;
    private long thePrimitiveLongWithDefault = 42L;
    private float thePrimitiveFloatWithDefault = 42f;
    private double thePrimitiveDoubleWithDefault = 42d;
    private Object theObjectWithDefault = Integer.valueOf(42);
    private Boolean theBooleanWithDefault = Boolean.TRUE;
    private Character theCharacterWithDefault = Character.valueOf((char) 42);
    private Byte theByteWithDefault = Byte.valueOf((byte) 42);
    private Short theShortWithDefault = Short.valueOf((short) 42);
    private Integer theIntegerWithDefault = Integer.valueOf(42);
    private Long theLongWithDefault = Long.valueOf(42);
    private Float theFloatWithDefault = Float.valueOf(42);
    private Double theDoubleWithDefault = Double.valueOf(42);
    private ObservableMap<String, Integer> theObservableMapWithDefault = FXCollections.<String, Integer>observableHashMap();
    private ObservableSet<Integer> theObservableSetWithDefault = FXCollections.observableSet(42);
    private ObservableList<Integer> theObservableListWithDefault = FXCollections.observableArrayList(42);
}
