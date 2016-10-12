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
package griffon.transform.lombok.fxobservable;

import griffon.transform.FXObservable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;

import java.util.*;

public class FXObservableOnFieldsShadowField {
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private String theString;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private boolean thePrimitiveBoolean;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private char thePrimitiveChar;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private byte thePrimitiveByte;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private short thePrimitiveShort;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private int thePrimitiveInt;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private long thePrimitiveLong;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private float thePrimitiveFloat;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private double thePrimitiveDouble;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private Object theObject;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private Boolean theBoolean;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private Character theCharacter;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private Byte theByte;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private Short theShort;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private Integer theInteger;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private Long theLong;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private Float theFloat;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private Double theDouble;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private Map<String, Integer> theMap;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private Set<Integer> theSet;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private List<Integer> theList;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private ObservableMap<String, Integer> theObservableMap;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private ObservableSet<Integer> theObservableSet;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private ObservableList<Integer> theObservableList;

    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private String theStringWithDefault = "42";
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private boolean thePrimitiveBooleanWithDefault = true;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private char thePrimitiveCharWithDefault = 42;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private byte thePrimitiveByteWithDefault = 42;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private short thePrimitiveShortWithDefault = 42;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private int thePrimitiveIntWithDefault = 42;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private long thePrimitiveLongWithDefault = 42L;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private float thePrimitiveFloatWithDefault = 42f;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private double thePrimitiveDoubleWithDefault = 42d;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private Object theObjectWithDefault = Integer.valueOf(42);
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private Boolean theBooleanWithDefault = Boolean.TRUE;
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private Character theCharacterWithDefault = Character.valueOf((char) 42);
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private Byte theByteWithDefault = Byte.valueOf((byte) 42);
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private Short theShortWithDefault = Short.valueOf((short) 42);
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private Integer theIntegerWithDefault = Integer.valueOf(42);
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private Long theLongWithDefault = Long.valueOf(42);
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private Float theFloatWithDefault = Float.valueOf(42);
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private Double theDoubleWithDefault = Double.valueOf(42);
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private Map<String, Integer> theMapWithDefault = new HashMap<String, Integer>();
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private Set<Integer> theSetWithDefault = new HashSet<Integer>(Arrays.asList(42));
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private List<Integer> theListWithDefault = new ArrayList<Integer>(Arrays.asList(42));
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private ObservableMap<String, Integer> theObservableMapWithDefault = FXCollections.<String, Integer>observableHashMap();
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private ObservableSet<Integer> theObservableSetWithDefault = FXCollections.observableSet(42);
    @FXObservable(FXObservable.Strategy.SHADOW_FIELD)
    private ObservableList<Integer> theObservableListWithDefault = FXCollections.observableArrayList(42);
}
