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

import griffon.transform.Observable;

public class ObservableFields {
    @Observable
    private String theString;
    @Observable
    private int theNativeInt;
    @Observable
    private long theNativeLong;
    @Observable
    private float theNativeFloat;
    @Observable
    private double theNativeDouble;
    @Observable
    private Object theObject;
    @Observable
    private Integer theInteger;
    @Observable
    private Long theLong;
    @Observable
    private Float theFloat;
    @Observable
    private Double theDouble;
}
