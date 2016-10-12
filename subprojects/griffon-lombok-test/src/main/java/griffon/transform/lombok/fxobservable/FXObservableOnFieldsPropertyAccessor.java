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

public class FXObservableOnFieldsPropertyAccessor {
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private String theString;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private boolean thePrimitiveBoolean;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private char thePrimitiveChar;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private byte thePrimitiveByte;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private short thePrimitiveShort;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private int thePrimitiveInt;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private long thePrimitiveLong;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private float thePrimitiveFloat;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private double thePrimitiveDouble;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private Object theObject;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private Boolean theBoolean;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private Character theCharacter;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private Byte theByte;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private Short theShort;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private Integer theInteger;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private Long theLong;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private Float theFloat;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private Double theDouble;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private Map<String, Integer> theMap;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private Set<Integer> theSet;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private List<Integer> theList;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private ObservableMap<String, Integer> theObservableMap;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private ObservableSet<Integer> theObservableSet;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private ObservableList<Integer> theObservableList;

    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private String theStringWithDefault = "42";
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private boolean thePrimitiveBooleanWithDefault = true;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private char thePrimitiveCharWithDefault = 42;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private byte thePrimitiveByteWithDefault = 42;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private short thePrimitiveShortWithDefault = 42;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private int thePrimitiveIntWithDefault = 42;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private long thePrimitiveLongWithDefault = 42L;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private float thePrimitiveFloatWithDefault = 42f;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private double thePrimitiveDoubleWithDefault = 42d;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private Object theObjectWithDefault = Integer.valueOf(42);
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private Boolean theBooleanWithDefault = Boolean.TRUE;
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private Character theCharacterWithDefault = Character.valueOf((char) 42);
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private Byte theByteWithDefault = Byte.valueOf((byte) 42);
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private Short theShortWithDefault = Short.valueOf((short) 42);
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private Integer theIntegerWithDefault = Integer.valueOf(42);
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private Long theLongWithDefault = Long.valueOf(42);
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private Float theFloatWithDefault = Float.valueOf(42);
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private Double theDoubleWithDefault = Double.valueOf(42);
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private Map<String, Integer> theMapWithDefault = new HashMap<>();
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private Set<Integer> theSetWithDefault = new HashSet<>(Arrays.asList(42));
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private List<Integer> theListWithDefault = new ArrayList<>(Arrays.asList(42));
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private ObservableMap<String, Integer> theObservableMapWithDefault = FXCollections.observableHashMap();
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private ObservableSet<Integer> theObservableSetWithDefault = FXCollections.observableSet(42);
    @FXObservable(FXObservable.Strategy.PROPERTY_ACCESOR)
    private ObservableList<Integer> theObservableListWithDefault = FXCollections.observableArrayList(42);
}
