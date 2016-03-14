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
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;

import java.lang.ref.Reference;

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
    private ObservableMap<String, String> theMap;
    private ObservableSet<String> theSet;
    private ObservableList<String> theList;

    private Reference<Object> theReference;
}
