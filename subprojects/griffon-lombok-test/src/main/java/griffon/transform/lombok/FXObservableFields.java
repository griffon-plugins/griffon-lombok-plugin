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

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private Map<String, String> theMap;
    @FXObservable
    private Set<String> theSet;
    @FXObservable
    private List<String> theList;
}
