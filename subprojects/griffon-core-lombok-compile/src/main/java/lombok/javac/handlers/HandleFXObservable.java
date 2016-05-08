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
package lombok.javac.handlers;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import griffon.transform.FXObservable;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import org.kordamp.jipsy.ServiceProviderFor;

import java.util.*;

import static lombok.javac.Javac.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;

@ServiceProviderFor(JavacAnnotationHandler.class)
public class HandleFXObservable extends JavacAnnotationHandler<FXObservable> {

    private static final java.util.Map<String, String> PROPERTY_TYPE_MAP;

    static {
        Map<String, String> m = new HashMap<>();
        m.put("boolean", "javafx.beans.property.BooleanProperty");
        m.put("java.lang.Boolean", "javafx.beans.property.BooleanProperty");
        m.put("byte", "javafx.beans.property.IntegerProperty");
        m.put("java.lang.Byte", "javafx.beans.property.IntegerProperty");
        m.put("short", "javafx.beans.property.IntegerProperty");
        m.put("java.lang.Short", "javafx.beans.property.IntegerProperty");
        m.put("int", "javafx.beans.property.IntegerProperty");
        m.put("java.lang.Integer", "javafx.beans.property.IntegerProperty");
        m.put("char", "javafx.beans.property.IntegerProperty");
        m.put("java.lang.Character", "javafx.beans.property.IntegerProperty");
        m.put("long", "javafx.beans.property.LongProperty");
        m.put("java.lang.Long", "javafx.beans.property.LongProperty");
        m.put("float", "javafx.beans.property.FloatProperty");
        m.put("java.lang.Float", "javafx.beans.property.FloatProperty");
        m.put("double", "javafx.beans.property.DoubleProperty");
        m.put("java.lang.Double", "javafx.beans.property.DoubleProperty");
        m.put("java.lang.String", "javafx.beans.property.StringProperty");
        PROPERTY_TYPE_MAP = Collections.unmodifiableMap(m);
    }

    @Override
    public void handle(AnnotationValues<FXObservable> annotation, JCAnnotation source, JavacNode annotationNode) {
        deleteAnnotationIfNeccessary(annotationNode, FXObservable.class);
        JavacNode node = annotationNode.up();
        if (node == null) return;

        switch (node.getKind()) {
            case TYPE:
                if ((getModifiers(getTypeDecl(node)) & (Flags.INTERFACE | Flags.ANNOTATION)) != 0) {
                    addUsageError(annotationNode);
                    return;
                }
                createForType(node, annotationNode);
                break;
            case FIELD:
                if (!fieldQualifiesForGeneration(node)) {
                    addUsageError(annotationNode);
                    return;
                }
                new FXObservableFieldHandler(node, annotationNode).handle();
                break;
            default:
                addUsageError(annotationNode);
        }
    }

    private void addUsageError(JavacNode errorNode) {
        errorNode.addError("@FXObservable is only supported on a class, an enum, or a non-final, private field.");
    }

    private long getModifiers(JCClassDecl typeDecl) {
        return typeDecl != null ? typeDecl.mods.flags : 0;
    }

    private JCClassDecl getTypeDecl(JavacNode typeNode) {
        JCTree type = typeNode.get();
        return type instanceof JCClassDecl ? (JCClassDecl) type : null;
    }

    private boolean fieldQualifiesForGeneration(JavacNode field) {
        if (field.getKind() != Kind.FIELD) return false;
        JCVariableDecl fieldDecl = (JCVariableDecl) field.get();
        //Skip fields that start with $
        if (fieldDecl.name.toString().startsWith("$")) return false;
        //Skip static fields.
        if ((fieldDecl.mods.flags & Flags.STATIC) != 0) return false;
        //Skip final fields.
        if ((fieldDecl.mods.flags & Flags.FINAL) != 0) return false;
        //Skip non-private fields.
        if ((fieldDecl.mods.flags & Flags.PRIVATE) == 0) return false;
        return true;
    }

    private void createForType(JavacNode typeNode, JavacNode errorNode) {
        for (JavacNode field : typeNode.down()) {
            if (fieldQualifiesForGeneration(field) && !hasAnnotation(FXObservable.class, field))
                new FXObservableFieldHandler(field, errorNode).handle();
        }
    }

    private static class FXObservableFieldHandler {
        private JavacNode typeNode;
        private JavacNode fieldNode;
        private JCVariableDecl field;
        private JCFieldAccess fieldAccess;
        private JCExpression type;
        private Class<?> typeClass;
        private JCExpression propertyType;
        private JavacNode errorNode;
        private JavacTreeMaker treeMaker;
        private Name getterName;
        private JCExpression lazyInit;

        public FXObservableFieldHandler(JavacNode fieldNode, JavacNode errorNode) {
            typeNode = fieldNode.up();
            this.fieldNode = fieldNode;
            field = (JCVariableDecl) fieldNode.get();
            this.errorNode = errorNode;
            treeMaker = fieldNode.getTreeMaker();
            type = field.vartype;
            try {
                typeClass = Class.forName(rawTypeString(type.type));
            } catch (ClassNotFoundException e) {
                // ignore
            }
            propertyType = getPropertyType();
            fieldAccess = treeMaker.Select(treeMaker.Ident(fieldNode.toName("this")), field.getName());
            getterName = fieldNode.toName(JavacHandlerUtil.toGetterName(fieldNode));
        }

        public void handle() {
            changeFieldTypeToObject();
            checkPrimitiveFieldInitializer();
            maybeSetupLazyInitializer();

            injectMethod(typeNode, createPropertyMethod());
            injectMethod(typeNode, createGetter());
            injectMethod(typeNode, createSetter());
        }

        private void changeFieldTypeToObject() {
            field.vartype = genericType("Object", List.<Type>nil());
        }

        private void checkPrimitiveFieldInitializer() {
            if (field.init != null && type.type.isPrimitive()) {
                field.init = setterConversionToField(treeMaker.TypeCast(type, field.init));
            }
        }

        private void maybeSetupLazyInitializer() {
            if (isMap() || isSet() || isList()) {
                lazyInit = field.init;
                field.init = null;
                if (lazyInit == null) {
                    lazyInit = getDefaultValue();
                }
            }
        }

        private JCExpression getDefaultValue() {
            if (isObservableMap()) {
                return treeMaker.Apply(
                        typeArguments(type),
                        JavacHandlerUtil.chainDotsString(
                                fieldNode,
                                "javafx.collections.FXCollections.observableHashMap"
                        ),
                        List.<JCExpression>nil()
                );
            }
            if (isObservableSet()) {
                return treeMaker.Apply(
                        typeArguments(type),
                        JavacHandlerUtil.chainDotsString(
                                fieldNode,
                                "javafx.collections.FXCollections.observableSet"
                        ),
                        List.<JCExpression>nil()
                );
            }
            if (isObservableList()) {
                return treeMaker.Apply(
                        typeArguments(type),
                        JavacHandlerUtil.chainDotsString(
                                fieldNode,
                                "javafx.collections.FXCollections.observableArrayList"
                        ),
                        List.<JCExpression>nil()
                );
            }
            return null;
        }

        private JCExpression getPropertyType() {
            List<Type> typeArguments = type.type.getTypeArguments();
            String rawType = rawTypeString(type.type);
            String propertyType = PROPERTY_TYPE_MAP.get(rawType);
            if (propertyType == null) {
                if (isMap())
                    propertyType = "javafx.beans.property.MapProperty";
                else if (isSet())
                    propertyType = "javafx.beans.property.SetProperty";
                else if (isList())
                    propertyType = "javafx.beans.property.ListProperty";
                if (propertyType == null) {
                    propertyType = "javafx.beans.property.ObjectProperty";
                    typeArguments = List.of(type.type);
                }
            }
            return genericType(propertyType, typeArguments);
        }

        private boolean isMap() {
            return typeClass != null && Map.class.isAssignableFrom(typeClass);
        }

        private boolean isObservableMap() {
            return typeClass != null && ObservableMap.class.isAssignableFrom(typeClass);
        }

        private boolean isSet() {
            return typeClass != null && Set.class.isAssignableFrom(typeClass);
        }

        private boolean isObservableSet() {
            return typeClass != null && ObservableSet.class.isAssignableFrom(typeClass);
        }

        private boolean isList() {
            return typeClass != null && java.util.List.class.isAssignableFrom(typeClass);
        }

        private boolean isObservableList() {
            return typeClass != null && ObservableList.class.isAssignableFrom(typeClass);
        }

        private JCExpression getPropertyImpl() {
            String rawType = rawTypeString(propertyType);
            List<JCExpression> typeArguments = typeArguments(propertyType);
            String implTypeString = rawType.replaceFirst("(javafx[.]beans[.]property[.])(.*)", "$1Simple$2");
            JCExpression implType = JavacHandlerUtil.chainDotsString(fieldNode, implTypeString);
            if (typeArguments.isEmpty())
                return implType;
            return treeMaker.TypeApply(implType, typeArguments);
        }

        private JCExpression genericType(String rawType, List<Type> typeArguments) {
            JCExpression type = JavacHandlerUtil.chainDotsString(fieldNode, rawType);

            if (typeArguments.isEmpty())
                return type;

            ListBuffer<JCExpression> typeExpressions = new ListBuffer<>();
            for (Type typeArgument : typeArguments) {
                typeExpressions.append(genericType(rawTypeString(typeArgument), typeArgument.getTypeArguments()));
            }

            return treeMaker.TypeApply(type, typeExpressions.toList());
        }

        private JCExpression rawType(JCExpression type) {
            if (type instanceof JCTypeApply)
                return ((JCTypeApply) type).clazz;
            return type;
        }

        private String rawTypeString(JCExpression type) {
            return rawType(type).toString();
        }

        private String rawTypeString(Type type) {
            List<Type> typeArguments = type.getTypeArguments();
            return type.toString().replace("<" + typeArguments.toString() + ">", "");
        }

        private List<JCExpression> typeArguments(JCExpression type) {
            if (type instanceof JCTypeApply)
                return ((JCTypeApply) type).getTypeArguments();
            return List.<JCExpression>nil();
        }

        private JCMethodDecl createPropertyMethod() {
            Name methodName = fieldNode.toName(fieldNode.getName() + "Property");

            List<JCStatement> statements = createPropertyMethodBody();

            JCBlock methodBody = treeMaker.Block(0, statements);

            List<JCTypeParameter> methodGenericParams = List.nil();
            List<JCVariableDecl> parameters = List.nil();
            List<JCExpression> throwsClauses = List.nil();
            JCExpression annotationMethodDefaultValue = null;

            JCMethodDecl decl = recursiveSetGeneratedBy(treeMaker.MethodDef(treeMaker.Modifiers(Flags.PUBLIC), methodName, propertyType,
                    methodGenericParams, parameters, throwsClauses, methodBody, annotationMethodDefaultValue), fieldNode.get(), fieldNode.getContext());

            copyJavadoc(fieldNode, decl, CopyJavadoc.VERBATIM);
            return decl;
        }

        private List<JCStatement> createPropertyMethodBody() {

            ListBuffer<JCStatement> statements = new ListBuffer<>();

            JCExpression propertyImpl = getPropertyImpl();
            JCExpression callGetter = treeMaker.Apply(List.<JCExpression>nil(), treeMaker.Ident(getterName), List.<JCExpression>nil());
            statements.add(
                    // if (!(field instanceof Property))
                    treeMaker.If(treeMaker.Unary(CTC_NOT, treeMaker.TypeTest(fieldAccess, rawType(propertyType))),
                            // field = new ...
                            treeMaker.Exec(
                                    treeMaker.Assign(
                                            fieldAccess,
                                            treeMaker.NewClass(
                                                    null,
                                                    List.<JCExpression>nil(),
                                                    propertyImpl,
                                                    List.<JCExpression>of(
                                                            treeMaker.Ident(fieldNode.toName("this")),
                                                            treeMaker.Literal(field.getName().toString()),
                                                            setterConversionToProperty(callGetter)
                                                    ),
                                                    null
                                            )
                                    )
                            ),
                            // no else
                            null));
            // return fieldProperty
            statements.append(treeMaker.Return(treeMaker.TypeCast(propertyType, fieldAccess)));
            return statements.toList();
        }

        private JCMethodDecl createGetter() {
            List<JCStatement> statements = createGetterBody();

            JCBlock methodBody = treeMaker.Block(0, statements);


            JCMethodDecl decl = recursiveSetGeneratedBy(
                    treeMaker.MethodDef(
                            treeMaker.Modifiers(Flags.PUBLIC),
                            getterName,
                            type,
                            List.<JCTypeParameter>nil(),
                            List.<JCVariableDecl>nil(),
                            List.<JCExpression>nil(),
                            methodBody,
                            null),
                    fieldNode.get(),
                    fieldNode.getContext()
            );

            copyJavadoc(fieldNode, decl, CopyJavadoc.VERBATIM);
            return decl;
        }

        private List<JCStatement> createGetterBody() {
            ListBuffer<JCStatement> statements = new ListBuffer<>();
            Name getMethodName = fieldNode.toName("get");
            JCExpression propertyDotGet = treeMaker.Apply(
                    List.<JCExpression>nil(),
                    treeMaker.Select(treeMaker.TypeCast(propertyType, fieldAccess), getMethodName),
                    List.<JCExpression>nil()
            );
            JCExpression convertedPropertyValue = getterConversionFromProperty(propertyDotGet);
            JCExpression convertedFieldValue = getterConversionFromField();
            if (lazyInit != null) {
                // if (value == null) setValue(init)
                JCExpression setter = treeMaker.Ident(fieldNode.toName(JavacHandlerUtil.toSetterName(fieldNode)));
                statements.add(
                        treeMaker.If(
                                isNull(fieldAccess),
                                treeMaker.Exec(treeMaker.Apply(List.<JCExpression>nil(), setter, List.<JCExpression>of(lazyInit))),
                                null
                        )
                );
            }
            // return value instanceof XProperty ? ((XProperty)name).get() : (X) name;
            statements.add(
                    treeMaker.Return(
                            treeMaker.Conditional(
                                    treeMaker.TypeTest(fieldAccess, rawType(propertyType)),
                                    convertedPropertyValue,
                                    convertedFieldValue
                            )
                    )
            );
            return statements.toList();
        }

        private JCMethodDecl createSetter() {
            Name methodName = fieldNode.toName(JavacHandlerUtil.toSetterName(fieldNode));

            List<JCStatement> statements = createSetterBody();

            JCExpression methodType = treeMaker.Type(Javac.createVoidType(treeMaker, Javac.CTC_VOID));
            JCBlock methodBody = treeMaker.Block(0, statements);

            Name paramName = field.getName();
            List<JCTypeParameter> methodGenericParams = List.nil();
            List<JCVariableDecl> parameters = List.of(
                    treeMaker.VarDef(
                            treeMaker.Modifiers(Flags.PARAMETER),
                            paramName,
                            type,
                            null
                    )
            );
            List<JCExpression> throwsClauses = List.nil();
            JCExpression annotationMethodDefaultValue = null;

            JCMethodDecl decl = recursiveSetGeneratedBy(
                    treeMaker.MethodDef(
                            treeMaker.Modifiers(Flags.PUBLIC),
                            methodName,
                            methodType,
                            methodGenericParams,
                            parameters,
                            throwsClauses,
                            methodBody,
                            annotationMethodDefaultValue
                    ),
                    fieldNode.get(), fieldNode.getContext());

            copyJavadoc(fieldNode, decl, CopyJavadoc.VERBATIM);
            return decl;
        }

        private List<JCStatement> createSetterBody() {
            ListBuffer<JCStatement> statements = new ListBuffer<>();
            Name setMethodName = fieldNode.toName("set");
            JCExpression value = treeMaker.Ident(field.getName());
            JCExpression convertedValue = setterConversionToProperty(value);

            JCExpression propertyDotSet = treeMaker.Apply(
                    List.<JCExpression>nil(),
                    treeMaker.Select(treeMaker.TypeCast(propertyType, fieldAccess), setMethodName),
                    List.of(convertedValue));
            JCExpression defaultValue = getDefaultValue();
            if (defaultValue != null) {
                // if (value == null) value = defaultValue
                statements.add(
                        treeMaker.If(
                                isNull(value),
                                treeMaker.Exec(treeMaker.Assign(value, defaultValue)),
                                null
                        )
                );
            }
            statements.add(
                    treeMaker.If(
                            // if (this.value instanceof XProperty)
                            treeMaker.TypeTest(fieldAccess, rawType(propertyType)),
                            // ((StringProperty)this.value).set(value);
                            treeMaker.Exec(propertyDotSet),
                            // else this.value = value;
                            treeMaker.Exec(
                                    treeMaker.Assign(fieldAccess, setterConversionToField(value))
                            )
                    ));
            return statements.toList();
        }


        /**
         * Create the conversion from simple type to property type. This is used for calling property.set().
         */
        private JCExpression setterConversionToProperty(JCExpression value) {
            String rawType = rawTypeString(type.type);
            if ("java.lang.Boolean".equals(rawType)) {
                // value == null ? false : ((Boolean) value).booleanValue()
                return treeMaker.Conditional(
                        isNull(value),
                        treeMaker.Literal(Boolean.FALSE),
                        call(valueAs(value, "Boolean"), "booleanValue")
                );
            } else if ("java.lang.Character".equals(rawType)) {
                // value == null ? 0 : (int) ((Character) value).charValue()
                return treeMaker.Conditional(
                        isNull(value),
                        treeMaker.Literal(Integer.valueOf(0)),
                        treeMaker.TypeCast(
                                treeMaker.TypeIdent(CTC_INT),
                                call(valueAs(value, "Character"), "charValue")
                        )
                );
            } else if ("java.lang.Byte".equals(rawType) ||
                    "java.lang.Short".equals(rawType) ||
                    "java.lang.Integer".equals(rawType)) {
                // value == null ? 0 : (Number) value).intValue()
                return treeMaker.Conditional(
                        isNull(value),
                        treeMaker.Literal(Integer.valueOf(0)),
                        call(valueAs(value, "Number"), "intValue")
                );
            } else if ("java.lang.Long".equals(rawType)) {
                // value == null ? 0L : (Number) value).longValue()
                return treeMaker.Conditional(
                        isNull(value),
                        treeMaker.Literal(Long.valueOf(0)),
                        call(valueAs(value, "Number"), "longValue")
                );
            } else if ("java.lang.Float".equals(rawType)) {
                // value == null ? 0f : ((Number) value).floatValue()
                return treeMaker.Conditional(
                        isNull(value),
                        treeMaker.Literal(Float.valueOf(0)),
                        call(valueAs(value, "Number"), "floatValue")
                );
            } else if ("java.lang.Double".equals(rawType)) {
                // value == null ? 0 : ((Number) value).doubleValue()
                return treeMaker.Conditional(
                        isNull(value),
                        treeMaker.Literal(Double.valueOf(0)),
                        call(valueAs(value, "Number"), "doubleValue")
                );
            } else if ("byte".equals(rawType) ||
                    "char".equals(rawType) ||
                    "short".equals(rawType)) {
                // (int) value
                return treeMaker.TypeCast(treeMaker.TypeIdent(CTC_INT), value);
            } else {
                // value
                return value;
            }
        }

        /**
         * Create the conversion from property type to getter type. This is used when retrieving the value via property.get().
         */
        private JCExpression getterConversionFromProperty(JCExpression value) {
            String rawType = rawTypeString(type.type);
            JCExpression castToByte = cast(Javac.CTC_BYTE, value);
            JCExpression castToChar = cast(Javac.CTC_CHAR, value);
            JCExpression castToShort = cast(Javac.CTC_SHORT, value);
            if ("java.lang.Byte".equals(rawType)) {
                // Byte.valueOf((byte)value)
                return valueOf(rawType, castToByte);
            } else if ("byte".equals(rawType)) {
                // (byte)value
                return castToByte;
            } else if ("java.lang.Character".equals(rawType)) {
                // Character.valueOf((char)value)
                return valueOf(rawType, castToChar);
            } else if ("char".equals(rawType)) {
                // (char)value
                return castToChar;
            } else if ("java.lang.Short".equals(rawType)) {
                // Short.valueOf((short)value)
                return valueOf(rawType, castToShort);
            } else if ("short".equals(rawType)) {
                // (short)value
                return castToShort;
            } else if ("java.lang.Boolean".equals(rawType) ||
                    "java.lang.Integer".equals(rawType) ||
                    "java.lang.Long".equals(rawType) ||
                    "java.lang.Float".equals(rawType) ||
                    "java.lang.Double".equals(rawType)) {
                // X.valueOf(value)
                return valueOf(rawType, value);
            } else {
                // value
                return value;
            }
        }

        /**
         * Create the conversion from Object to getter type. This is used for the getter when no property is used.
         */
        private JCExpression getterConversionFromField() {
            String rawType = rawTypeString(type.type);
            if ("java.lang.Boolean".equals(rawType)) {
                // value == null ? Boolean.FALSE : (Boolean)value
                return treeMaker.Conditional(
                        isNull(fieldAccess),
                        valueOf(rawType, treeMaker.Literal(Boolean.FALSE)),
                        fieldAs("Boolean")
                );
            } else if ("boolean".equals(rawType)) {
                // value == null ? false : ((Boolean)value).booleanValue()
                return treeMaker.Conditional(
                        isNull(fieldAccess),
                        treeMaker.Literal(Boolean.FALSE),
                        call(fieldAs("Boolean"), "booleanValue")
                );
            } else if ("java.lang.Character".equals(rawType)) {
                // value == null ? Character.valueOf(0) : (Character)value
                return treeMaker.Conditional(
                        isNull(fieldAccess),
                        valueOf(rawType, cast(CTC_CHAR, treeMaker.Literal(Integer.valueOf(0)))),
                        fieldAs("Character")
                );
            } else if ("char".equals(rawType)) {
                // value == null ? (char)0 : ((Character)value).charValue()
                return treeMaker.Conditional(
                        isNull(fieldAccess),
                        cast(CTC_CHAR, treeMaker.Literal(Integer.valueOf(0))),
                        call(fieldAs("Character"), "charValue")
                );
            } else if ("java.lang.Byte".equals(rawType)) {
                // value == null ? Byte.valueOf(0) : (Byte)value
                return treeMaker.Conditional(
                        isNull(fieldAccess),
                        valueOf(rawType, cast(CTC_BYTE, treeMaker.Literal(Integer.valueOf(0)))),
                        fieldAs("Byte")
                );
            } else if ("byte".equals(rawType)) {
                // value == null ? (byte)0 : ((Byte)value).byteValue()
                return treeMaker.Conditional(
                        isNull(fieldAccess),
                        cast(CTC_BYTE, treeMaker.Literal(Integer.valueOf(0))),
                        call(fieldAs("Byte"), "byteValue")
                );
            } else if ("java.lang.Short".equals(rawType)) {
                // value == null ? Short.valueOf(0) : (Short)value
                return treeMaker.Conditional(
                        isNull(fieldAccess),
                        valueOf(rawType, cast(CTC_SHORT, treeMaker.Literal(Integer.valueOf(0)))),
                        fieldAs("Short")
                );
            } else if ("short".equals(rawType)) {
                // value == null ? (short)0 : ((Short)value).shortValue()
                return treeMaker.Conditional(
                        isNull(fieldAccess),
                        cast(CTC_SHORT, treeMaker.Literal(Integer.valueOf(0))),
                        call(fieldAs("Short"), "shortValue")
                );
            } else if ("java.lang.Integer".equals(rawType)) {
                // value == null ? Integer.valueOf(0) : (Integer)value
                return treeMaker.Conditional(
                        isNull(fieldAccess),
                        valueOf(rawType, treeMaker.Literal(Integer.valueOf(0))),
                        fieldAs("Integer")
                );
            } else if ("int".equals(rawType)) {
                // value == null ? (int)0 : ((Integer)value).intValue()
                return treeMaker.Conditional(
                        isNull(fieldAccess),
                        treeMaker.Literal(Integer.valueOf(0)),
                        call(fieldAs("Integer"), "intValue")
                );
            } else if ("java.lang.Long".equals(rawType)) {
                // value == null ? Long.valueOf(0) : (Long)value
                return treeMaker.Conditional(
                        isNull(fieldAccess),
                        valueOf(rawType, treeMaker.Literal(Long.valueOf(0))),
                        fieldAs("Long")
                );
            } else if ("long".equals(rawType)) {
                // value == null ? (long)0 : ((Long)value).longValue()
                return treeMaker.Conditional(
                        isNull(fieldAccess),
                        treeMaker.Literal(Long.valueOf(0)),
                        call(fieldAs("Long"), "longValue")
                );
            } else if ("java.lang.Float".equals(rawType)) {
                // value == null ? Float.valueOf(0) : (Float)value
                return treeMaker.Conditional(
                        isNull(fieldAccess),
                        valueOf(rawType, treeMaker.Literal(Float.valueOf(0))),
                        fieldAs("Float")
                );
            } else if ("float".equals(rawType)) {
                // value == null ? (float)0 : ((Float)value).floatValue()
                return treeMaker.Conditional(
                        isNull(fieldAccess),
                        treeMaker.Literal(Float.valueOf(0)),
                        call(fieldAs("Float"), "floatValue")
                );
            } else if ("java.lang.Double".equals(rawType)) {
                // value == null ? Double.valueOf(0) : (Double)value
                return treeMaker.Conditional(
                        isNull(fieldAccess),
                        valueOf(rawType, treeMaker.Literal(Double.valueOf(0))),
                        fieldAs("Double")
                );
            } else if ("double".equals(rawType)) {
                // value == null ? (double)0 : ((Double)value).doubleValue()
                return treeMaker.Conditional(
                        isNull(fieldAccess),
                        treeMaker.Literal(Double.valueOf(0)),
                        call(fieldAs("Double"), "doubleValue")
                );
            } else {
                // (Type)value
                return treeMaker.TypeCast(type, fieldAccess);
            }
        }

        /**
         * Create the conversion from setter type to Object. This is used for the setter when no property is used.
         */
        private JCExpression setterConversionToField(JCExpression value) {
            String rawType = rawTypeString(type.type);
            if ("boolean".equals(rawType))
                return valueOf("Boolean", value);
            if ("byte".equals(rawType))
                return valueOf("Byte", value);
            if ("char".equals(rawType))
                return valueOf("Character", value);
            if ("short".equals(rawType))
                return valueOf("Short", value);
            if ("int".equals(rawType))
                return valueOf("Integer", value);
            if ("long".equals(rawType))
                return valueOf("Long", value);
            if ("float".equals(rawType))
                return valueOf("Float", value);
            if ("double".equals(rawType))
                return valueOf("Double", value);
            return value;
        }

        private JCExpression valueOf(String type, JCExpression value) {
            return treeMaker.Apply(List.<JCExpression>nil(),
                    JavacHandlerUtil.chainDotsString(fieldNode, type + ".valueOf"),
                    List.of(value)
            );
        }

        private JCExpression fieldAs(String type) {
            return valueAs(fieldAccess, type);
        }

        private JCExpression valueAs(JCExpression value, String type) {
            return treeMaker.TypeCast(genericType(type, List.<Type>nil()), value);
        }

        private JCExpression cast(JavacTreeMaker.TypeTag type, JCExpression value) {
            return treeMaker.TypeCast(treeMaker.TypeIdent(type), value);
        }

        private JCExpression call(JCExpression value, String method) {
            return treeMaker.Apply(List.<JCExpression>nil(), treeMaker.Select(value, fieldNode.toName(method)), List.<JCExpression>nil());
        }

        private JCExpression isNull(JCExpression value) {
            return treeMaker.Binary(CTC_EQUAL, value, treeMaker.Literal(CTC_BOT, null));
        }

    }

}
