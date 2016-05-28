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

import java.util.*;

public class FXObservableFields {
    @FXObservable
    private String theString;
    @FXObservable
    private boolean thePrimitiveBoolean;
    @FXObservable
    private char thePrimitiveChar;
    @FXObservable
    private byte thePrimitiveByte;
    @FXObservable
    private short thePrimitiveShort;
    @FXObservable
    private int thePrimitiveInt;
    @FXObservable
    private long thePrimitiveLong;
    @FXObservable
    private float thePrimitiveFloat;
    @FXObservable
    private double thePrimitiveDouble;
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
    private Map<String, Integer> theMap;
    @FXObservable
    private Set<Integer> theSet;
    @FXObservable
    private List<Integer> theList;
    @FXObservable
    private ObservableMap<String, Integer> theObservableMap;
    @FXObservable
    private ObservableSet<Integer> theObservableSet;
    @FXObservable
    private ObservableList<Integer> theObservableList;

    @FXObservable
    private String theStringWithDefault = "42";
    @FXObservable
    private boolean thePrimitiveBooleanWithDefault = true;
    @FXObservable
    private char thePrimitiveCharWithDefault = 42;
    @FXObservable
    private byte thePrimitiveByteWithDefault = 42;
    @FXObservable
    private short thePrimitiveShortWithDefault = 42;
    @FXObservable
    private int thePrimitiveIntWithDefault = 42;
    @FXObservable
    private long thePrimitiveLongWithDefault = 42L;
    @FXObservable
    private float thePrimitiveFloatWithDefault = 42f;
    @FXObservable
    private double thePrimitiveDoubleWithDefault = 42d;
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
    private Map<String, Integer> theMapWithDefault = new HashMap<String, Integer>();
    @FXObservable
    private Set<Integer> theSetWithDefault = new HashSet<Integer>() {{
        add(42);
    }};
    @FXObservable
    private List<Integer> theListWithDefault = new ArrayList<Integer>() {{
        add(42);
    }};
    @FXObservable
    private ObservableMap<String, Integer> theObservableMapWithDefault = FXCollections.<String, Integer>observableHashMap();
    @FXObservable
    private ObservableSet<Integer> theObservableSetWithDefault = FXCollections.observableSet(42);
    @FXObservable
    private ObservableList<Integer> theObservableListWithDefault = FXCollections.observableArrayList(42);
}
