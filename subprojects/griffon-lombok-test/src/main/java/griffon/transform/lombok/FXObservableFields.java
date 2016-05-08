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

public class FXObservableFields {
    @FXObservable
    private String theString;
    @FXObservable
    private boolean theSimpleBoolean;
    @FXObservable
    private char theSimpleChar;
    @FXObservable
    private byte theSimpleByte;
    @FXObservable
    private short theSimpleShort;
    @FXObservable
    private int theSimpleInt;
    @FXObservable
    private long theSimpleLong;
    @FXObservable
    private float theSimpleFloat;
    @FXObservable
    private double theSimpleDouble;
    @FXObservable
    private Object theObject;
    @FXObservable
    private Boolean theBoolean;
    @FXObservable
    private Character theCharacter;
    @FXObservable
    private Byte theByte;
    @FXObservable
    private Short theShort;
    @FXObservable
    private Integer theInteger;
    @FXObservable
    private Long theLong;
    @FXObservable
    private Float theFloat;
    @FXObservable
    private Double theDouble;
    @FXObservable
    private ObservableMap<String, Integer> theMap;
    @FXObservable
    private ObservableSet<Integer> theSet;
    @FXObservable
    private ObservableList<Integer> theList;

    @FXObservable
    private String theStringWithDefault = "42";
    @FXObservable
    private boolean theSimpleBooleanWithDefault = true;
    @FXObservable
    private char theSimpleCharWithDefault = 42;
    @FXObservable
    private byte theSimpleByteWithDefault = 42;
    @FXObservable
    private short theSimpleShortWithDefault = 42;
    @FXObservable
    private int theSimpleIntWithDefault = 42;
    @FXObservable
    private long theSimpleLongWithDefault = 42L;
    @FXObservable
    private float theSimpleFloatWithDefault = 42f;
    @FXObservable
    private double theSimpleDoubleWithDefault = 42d;
    @FXObservable
    private Object theObjectWithDefault = Integer.valueOf(42);
    @FXObservable
    private Boolean theBooleanWithDefault = Boolean.TRUE;
    @FXObservable
    private Character theCharacterWithDefault = Character.valueOf((char) 42);
    @FXObservable
    private Byte theByteWithDefault = Byte.valueOf((byte) 42);
    @FXObservable
    private Short theShortWithDefault = Short.valueOf((short) 42);
    @FXObservable
    private Integer theIntegerWithDefault = Integer.valueOf(42);
    @FXObservable
    private Long theLongWithDefault = Long.valueOf(42);
    @FXObservable
    private Float theFloatWithDefault = Float.valueOf(42);
    @FXObservable
    private Double theDoubleWithDefault = Double.valueOf(42);
    @FXObservable
    private ObservableMap<String, Integer> theMapWithDefault = FXCollections.<String, Integer>observableHashMap();
    @FXObservable
    private ObservableSet<Integer> theSetWithDefault = FXCollections.observableSet(42);
    @FXObservable
    private ObservableList<Integer> theListWithDefault = FXCollections.observableArrayList(42);
}
