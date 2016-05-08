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
    private boolean theSimpleBoolean;
    private char theSimpleChar;
    private byte theSimpleByte;
    private short theSimpleShort;
    private int theSimpleInt;
    private long theSimpleLong;
    private float theSimpleFloat;
    private double theSimpleDouble;
    private Object theObject;
    private Boolean theBoolean;
    private Character theCharacter;
    private Byte theByte;
    private Short theShort;
    private Integer theInteger;
    private Long theLong;
    private Float theFloat;
    private Double theDouble;
    private ObservableMap<String, Integer> theMap;
    private ObservableSet<Integer> theSet;
    private ObservableList<Integer> theList;

    private String theStringWithDefault = "42";
    private boolean theSimpleBooleanWithDefault = true;
    private char theSimpleCharWithDefault = 42;
    private byte theSimpleByteWithDefault = 42;
    private short theSimpleShortWithDefault = 42;
    private int theSimpleIntWithDefault = 42;
    private long theSimpleLongWithDefault = 42L;
    private float theSimpleFloatWithDefault = 42f;
    private double theSimpleDoubleWithDefault = 42d;
    private Object theObjectWithDefault = Integer.valueOf(42);
    private Boolean theBooleanWithDefault = Boolean.TRUE;
    private Character theCharacterWithDefault = Character.valueOf((char) 42);
    private Byte theByteWithDefault = Byte.valueOf((byte) 42);
    private Short theShortWithDefault = Short.valueOf((short) 42);
    private Integer theIntegerWithDefault = Integer.valueOf(42);
    private Long theLongWithDefault = Long.valueOf(42);
    private Float theFloatWithDefault = Float.valueOf(42);
    private Double theDoubleWithDefault = Double.valueOf(42);
    private ObservableMap<String, Integer> theMapWithDefault = FXCollections.<String, Integer>observableHashMap();
    private ObservableSet<Integer> theSetWithDefault = FXCollections.observableSet(42);
    private ObservableList<Integer> theListWithDefault = FXCollections.observableArrayList(42);
}
