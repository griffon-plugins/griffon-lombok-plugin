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
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import org.kordamp.jipsy.ServiceProviderFor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static lombok.javac.Javac.CTC_BOT;
import static lombok.javac.Javac.CTC_EQUAL;
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
                createForField(node, annotationNode);
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
        //Skip non-final fields.
        if ((fieldDecl.mods.flags & Flags.PRIVATE) == 0) return false;
        return true;
    }

    private void createForType(JavacNode typeNode, JavacNode errorNode) {
        for (JavacNode field : typeNode.down()) {
            if (fieldQualifiesForGeneration(field) && !hasAnnotation(FXObservable.class, field))
                createForField(field, errorNode);
        }
    }

    private void createForField(JavacNode fieldNode, JavacNode errorNode) {
        JavacNode typeNode = fieldNode.up();

        JavacNode propertyNode = injectFieldAndMarkGenerated(typeNode, createPropertyField(fieldNode, errorNode));

        injectMethod(typeNode, createPropertyMethod(fieldNode, propertyNode, errorNode));
        // injectMethod(typeNode, createConvenienceMethod(propertyNode, JavacHandlerUtil.toGetterName(propertyNode), errorNode));
        // injectMethod(typeNode, createConvenienceMethod(propertyNode, fieldNode.getName(), errorNode));

        injectMethod(typeNode, createGetter(fieldNode, propertyNode, errorNode));
        injectMethod(typeNode, createSetter(fieldNode, propertyNode, errorNode));
    }

    private JCVariableDecl createPropertyField(JavacNode fieldNode, JavacNode source) {
        JCExpression propertyType = getPropertyType(fieldNode, source);
        String propertyName = fieldNode.getName() + "Property";
        JavacTreeMaker treeMaker = fieldNode.getTreeMaker();
        JCVariableDecl propertyField = treeMaker.VarDef(
                treeMaker.Modifiers(Flags.PRIVATE),
                fieldNode.toName(propertyName),
                propertyType,
                null);
        copyJavadoc(fieldNode, propertyField, CopyJavadoc.VERBATIM);
        return propertyField;
    }

    private JCExpression getPropertyType(JavacNode fieldNode, JavacNode errorNode) {
        JCVariableDecl field = (JCVariableDecl) fieldNode.get();
        Type type = field.vartype.type;
        List<Type> typeArguments = type.getTypeArguments();
        String typeString = typeString(type);
        String propertyType = PROPERTY_TYPE_MAP.get(typeString);
        if (propertyType == null) {
            try {
                Class typeClass = Class.forName(typeString);
                if (Map.class.isAssignableFrom(typeClass))
                    propertyType = "javafx.beans.property.MapProperty";
                else if (Set.class.isAssignableFrom(typeClass))
                    propertyType = "javafx.beans.property.SetProperty";
                else if (java.util.List.class.isAssignableFrom(typeClass))
                    propertyType = "javafx.beans.property.ListProperty";
            } catch (ClassNotFoundException e) {
                // ignore
            }
            if (propertyType == null) {
                propertyType = "javafx.beans.property.ObjectProperty";
                typeArguments = List.of(type);
            }
        }
        return namePlusTypeArguments(fieldNode, propertyType, typeArguments);
    }

    private JCExpression getPropertyImpl(JavacNode propertyNode, JavacNode errorNode) {
        JCVariableDecl property = (JCVariableDecl) propertyNode.get();
        JCExpression typeExpression = property.vartype;
        String typeString = typeExpression.toString();
        List<JCExpression> typeArguments = List.<JCExpression>nil();
        if (typeExpression instanceof JCTypeApply) {
            JCTypeApply typeApply = (JCTypeApply) typeExpression;
            typeString = typeApply.getType().toString();
            typeArguments = typeApply.getTypeArguments();
        }
        String implTypeString = typeString.replaceFirst("(javafx[.]beans[.]property[.])(.*)", "$1Simple$2");
        JCExpression implType = JavacHandlerUtil.chainDotsString(propertyNode, implTypeString);
        if (typeArguments.isEmpty())
            return implType;
        return propertyNode.getTreeMaker().TypeApply(implType, typeArguments);
    }

    private String typeString(Type type) {
        List<Type> typeArguments = type.getTypeArguments();
        return type.toString().replace("<" + typeArguments.toString() + ">", "");
    }

    private JCExpression namePlusTypeArguments(JavacNode fieldNode, String typeString, List<Type> typeArguments) {
        JavacTreeMaker maker = fieldNode.getTreeMaker();

        JCExpression type = JavacHandlerUtil.chainDotsString(fieldNode, typeString);

        if (typeArguments.isEmpty())
            return type;

        ListBuffer<JCExpression> typeExpressions = new ListBuffer<>();
        for (Type typeArgument : typeArguments) {
            typeExpressions.append(namePlusTypeArguments(fieldNode, typeString(typeArgument), typeArgument.getTypeArguments()));
        }

        return maker.TypeApply(type, typeExpressions.toList());
    }

    private JCMethodDecl createConvenienceMethod(JavacNode propertyNode, String name, JavacNode source) {
        JCVariableDecl property = (JCVariableDecl) propertyNode.get();

        JCExpression methodType = property.vartype;
        Name methodName = propertyNode.toName(name);

        List<JCStatement> statements = createConvenienceMethodBody(propertyNode, source);

        JavacTreeMaker treeMaker = propertyNode.getTreeMaker();
        JCBlock methodBody = treeMaker.Block(0, statements);

        List<JCTypeParameter> methodGenericParams = List.nil();
        List<JCVariableDecl> parameters = List.nil();
        List<JCExpression> throwsClauses = List.nil();
        JCExpression annotationMethodDefaultValue = null;

        JCMethodDecl decl = recursiveSetGeneratedBy(treeMaker.MethodDef(treeMaker.Modifiers(Flags.PUBLIC), methodName, methodType,
                methodGenericParams, parameters, throwsClauses, methodBody, annotationMethodDefaultValue), source.get(), propertyNode.getContext());

        copyJavadoc(propertyNode, decl, CopyJavadoc.VERBATIM);
        return decl;
    }

    private List<JCStatement> createConvenienceMethodBody(JavacNode propertyNode, JavacNode source) {

        ListBuffer<JCStatement> statements = new ListBuffer<>();
        JavacTreeMaker maker = propertyNode.getTreeMaker();
        JCExpression propertyMethodName = maker.Ident(propertyNode.toName(propertyNode.getName()));
        statements.add(
                // just delegate to the xProperty() method
                // return xProperty()
                maker.Return(maker.Apply(List.<JCExpression>nil(), propertyMethodName, List.<JCExpression>nil()))
        );
        return statements.toList();
    }

    private JCMethodDecl createPropertyMethod(JavacNode fieldNode, JavacNode propertyNode, JavacNode source) {
        JCVariableDecl property = (JCVariableDecl) propertyNode.get();

        JCExpression methodType = property.vartype;
        Name methodName = propertyNode.toName(propertyNode.getName());

        List<JCStatement> statements = createLazyPropertyBody(fieldNode, propertyNode, source);

        JavacTreeMaker treeMaker = propertyNode.getTreeMaker();

        JCBlock methodBody = treeMaker.Block(0, statements);

        List<JCTypeParameter> methodGenericParams = List.nil();
        List<JCVariableDecl> parameters = List.nil();
        List<JCExpression> throwsClauses = List.nil();
        JCExpression annotationMethodDefaultValue = null;

        JCMethodDecl decl = recursiveSetGeneratedBy(treeMaker.MethodDef(treeMaker.Modifiers(Flags.PUBLIC), methodName, methodType,
                methodGenericParams, parameters, throwsClauses, methodBody, annotationMethodDefaultValue), source.get(), propertyNode.getContext());

        copyJavadoc(propertyNode, decl, CopyJavadoc.VERBATIM);
        return decl;
    }

    private List<JCStatement> createLazyPropertyBody(JavacNode fieldNode, JavacNode propertyNode, JavacNode source) {

        ListBuffer<JCStatement> statements = new ListBuffer<>();

        JCVariableDecl field = (JCVariableDecl) fieldNode.get();
        JCVariableDecl propertyField = (JCVariableDecl) propertyNode.get();
        JavacTreeMaker maker = propertyNode.getTreeMaker();
        JCExpression propertyImpl = getPropertyImpl(propertyNode, source);
        JCExpression defaultValueExpression = setterConversion(fieldNode, maker.Ident(field.getName()));
        statements.add(
                // if (fieldProperty == null)
                maker.If(isNull(maker, maker.Ident(propertyField.getName())),
                        // fieldProperty = new ...
                        maker.Exec(
                                maker.Assign(
                                        maker.Ident(propertyField.getName()),
                                        maker.NewClass(null, List.<JCExpression>nil(), propertyImpl,
                                                List.<JCExpression>of(defaultValueExpression), null)
                                )
                        ),
                        // no else
                        null));
        // return fieldProperty
        statements.append(maker.Return(maker.Ident(propertyField.getName())));
        return statements.toList();
    }

    /**
     * Create the conversion from simple type to property type. This is used for calling property.set() and
     * for the lazy creation of the Property.
     */
    private JCExpression setterConversion(JavacNode fieldNode, JCExpression value) {
        JCVariableDecl field = (JCVariableDecl) fieldNode.get();
        Type type = field.vartype.type;
        String typeString = type.toString();
        JavacTreeMaker maker = fieldNode.getTreeMaker();
        if ("java.lang.Boolean".equals(typeString)) {
            // value == null ? false : value.booleanValue()
            return maker.Conditional(
                    isNull(maker, value),
                    maker.Literal(Boolean.FALSE),
                    maker.Apply(List.<JCExpression>nil(), maker.Select(value, fieldNode.toName("booleanValue")), List.<JCExpression>nil())
            );
        } else if ("java.lang.Character".equals(typeString)) {
            // value == null ? 0 : value.charValue()
            return maker.Conditional(
                    isNull(maker, value),
                    maker.TypeCast(maker.TypeIdent(Javac.CTC_CHAR), maker.Literal(Integer.valueOf(0))),
                    maker.Apply(List.<JCExpression>nil(), maker.Select(value, fieldNode.toName("charValue")), List.<JCExpression>nil())
            );
        } else if ("java.lang.Byte".equals(typeString) ||
                "java.lang.Short".equals(typeString) ||
                "java.lang.Integer".equals(typeString)) {
            // value != null ? value.intValue() : 0
            return maker.Conditional(
                    isNull(maker, value),
                    maker.Literal(Integer.valueOf(0)),
                    maker.Apply(List.<JCExpression>nil(), maker.Select(value, fieldNode.toName("intValue")), List.<JCExpression>nil())
            );
        } else if ("java.lang.Long".equals(typeString)) {
            // value == null ? 0 : value.longValue()
            return maker.Conditional(
                    isNull(maker, value),
                    maker.Literal(Long.valueOf(0)),
                    maker.Apply(List.<JCExpression>nil(), maker.Select(value, fieldNode.toName("longValue")), List.<JCExpression>nil())
            );
        } else if ("java.lang.Float".equals(typeString)) {
            // value == null ? 0 : value.floatValue()
            return maker.Conditional(
                    isNull(maker, value),
                    maker.Literal(Float.valueOf(0)),
                    maker.Apply(List.<JCExpression>nil(), maker.Select(value, fieldNode.toName("floatValue")), List.<JCExpression>nil())
            );
        } else if ("java.lang.Double".equals(typeString)) {
            // value == null ? 0 : value.doubleValue()
            return maker.Conditional(
                    isNull(maker, value),
                    maker.Literal(Double.valueOf(0)),
                    maker.Apply(List.<JCExpression>nil(), maker.Select(value, fieldNode.toName("doubleValue")), List.<JCExpression>nil())
            );
        } else {
            // value
            return value;
        }
    }

    /**
     * Create the conversion from property type to simple type. This is used when retrieving the value via property.get().
     */
    private JCExpression getterConversion(JavacNode fieldNode, JCExpression value) {
        JCVariableDecl field = (JCVariableDecl) fieldNode.get();
        Type type = field.vartype.type;
        String typeString = type.toString();
        JavacTreeMaker maker = fieldNode.getTreeMaker();
        JCExpression castToByte = maker.TypeCast(maker.TypeIdent(Javac.CTC_BYTE), value);
        JCExpression castToChar = maker.TypeCast(maker.TypeIdent(Javac.CTC_CHAR), value);
        JCExpression castToShort = maker.TypeCast(maker.TypeIdent(Javac.CTC_SHORT), value);
        JCExpression typeValueOf = JavacHandlerUtil.chainDotsString(fieldNode, typeString + ".valueOf");
        if ("java.lang.Byte".equals(typeString)) {
            // Byte.valueOf((byte)value)
            return maker.Apply(List.<JCExpression>nil(), typeValueOf, List.of(castToByte));
        } else if ("byte".equals(typeString)) {
            // (byte)value
            return castToByte;
        } else if ("java.lang.Character".equals(typeString)) {
            // Character.valueOf((char)value)
            return maker.Apply(List.<JCExpression>nil(), typeValueOf, List.of(castToChar));
        } else if ("char".equals(typeString)) {
            // (char)value
            return castToChar;
        } else if ("java.lang.Short".equals(typeString)) {
            // Character.valueOf((short)value)
            return maker.Apply(List.<JCExpression>nil(), typeValueOf, List.of(castToShort));
        } else if ("short".equals(typeString)) {
            // (short)value
            return castToShort;
        } else if ("java.lang.Boolean".equals(typeString) ||
                "java.lang.Integer".equals(typeString) ||
                "java.lang.Long".equals(typeString) ||
                "java.lang.Float".equals(typeString) ||
                "java.lang.Double".equals(typeString)) {
            // X.valueOf(value)
            return maker.Apply(List.<JCExpression>nil(), typeValueOf, List.of(value));
        } else {
            // value
            return value;
        }
    }

    private JCExpression isNull(JavacTreeMaker maker, JCExpression value) {
        return maker.Binary(CTC_EQUAL, value, maker.Literal(CTC_BOT, null));
    }

    private JCMethodDecl createGetter(JavacNode fieldNode, JavacNode propertyNode, JavacNode source) {
        JCVariableDecl field = (JCVariableDecl) fieldNode.get();
        JCVariableDecl property = (JCVariableDecl) propertyNode.get();

        JCExpression methodType = field.vartype;
        Name methodName = fieldNode.toName(JavacHandlerUtil.toGetterName(fieldNode));

        List<JCStatement> statements = createGetterBody(fieldNode, propertyNode, source);

        JavacTreeMaker treeMaker = propertyNode.getTreeMaker();
        JCBlock methodBody = treeMaker.Block(0, statements);

        List<JCTypeParameter> methodGenericParams = List.nil();
        List<JCVariableDecl> parameters = List.nil();
        List<JCExpression> throwsClauses = List.nil();
        JCExpression annotationMethodDefaultValue = null;

        JCMethodDecl decl = recursiveSetGeneratedBy(treeMaker.MethodDef(treeMaker.Modifiers(Flags.PUBLIC), methodName, methodType,
                methodGenericParams, parameters, throwsClauses, methodBody, annotationMethodDefaultValue), source.get(), propertyNode.getContext());

        copyJavadoc(propertyNode, decl, CopyJavadoc.VERBATIM);
        return decl;
    }

    private List<JCStatement> createGetterBody(JavacNode fieldNode, JavacNode propertyNode, JavacNode source) {
        JCVariableDecl field = (JCVariableDecl) fieldNode.get();
        JCVariableDecl propertyField = (JCVariableDecl) propertyNode.get();
        ListBuffer<JCStatement> statements = new ListBuffer<>();
        JavacTreeMaker maker = propertyNode.getTreeMaker();
        JCExpression fieldAccess = maker.Ident(field.getName());
        JCExpression propertyFieldAccess = maker.Ident(propertyField.getName());
        Name getMethodName = propertyNode.toName("get");
        JCExpression propertyDotGet = maker.Apply(List.<JCExpression>nil(), maker.Select(propertyFieldAccess, getMethodName), List.<JCExpression>nil());
        // return property == null ? field : property.get()
        statements.add(maker.Return(maker.Conditional(isNull(maker, propertyFieldAccess), fieldAccess, getterConversion(fieldNode, propertyDotGet))));
        return statements.toList();
    }

    private JCMethodDecl createSetter(JavacNode fieldNode, JavacNode propertyNode, JavacNode source) {
        JCVariableDecl field = (JCVariableDecl) fieldNode.get();
        JCVariableDecl property = (JCVariableDecl) propertyNode.get();

        Name methodName = fieldNode.toName(JavacHandlerUtil.toSetterName(fieldNode));

        List<JCStatement> statements = createSetterBody(fieldNode, propertyNode, source);

        JavacTreeMaker treeMaker = propertyNode.getTreeMaker();
        JCExpression methodType = treeMaker.Type(Javac.createVoidType(treeMaker, Javac.CTC_VOID));
        JCBlock methodBody = treeMaker.Block(0, statements);

        Name paramName = fieldNode.toName("value");
        List<JCTypeParameter> methodGenericParams = List.nil();
        List<JCVariableDecl> parameters = List.of(treeMaker.VarDef(treeMaker.Modifiers(Flags.PARAMETER), paramName, field.vartype, null));
        List<JCExpression> throwsClauses = List.nil();
        JCExpression annotationMethodDefaultValue = null;

        JCMethodDecl decl = recursiveSetGeneratedBy(treeMaker.MethodDef(treeMaker.Modifiers(Flags.PUBLIC), methodName, methodType,
                methodGenericParams, parameters, throwsClauses, methodBody, annotationMethodDefaultValue), source.get(), propertyNode.getContext());

        copyJavadoc(propertyNode, decl, CopyJavadoc.VERBATIM);
        return decl;
    }

    private List<JCStatement> createSetterBody(JavacNode fieldNode, JavacNode propertyNode, JavacNode source) {
        JCVariableDecl field = (JCVariableDecl) fieldNode.get();
        JCVariableDecl propertyField = (JCVariableDecl) propertyNode.get();
        ListBuffer<JCStatement> statements = new ListBuffer<>();
        JavacTreeMaker maker = propertyNode.getTreeMaker();
        JCExpression fieldAccess = maker.Ident(field.getName());
        JCExpression propertyFieldAccess = maker.Ident(propertyField.getName());
        Name setMethodName = propertyNode.toName("set");
        JCExpression value = maker.Ident(fieldNode.toName("value"));
        JCExpression propertyDotSet = maker.Apply(List.<JCExpression>nil(), maker.Select(propertyFieldAccess, setMethodName), List.of(setterConversion(fieldNode, value)));
        // if (property == null) field = value; else property.set(value);
        statements.add(maker.If(isNull(maker, propertyFieldAccess), maker.Exec(maker.Assign(fieldAccess, value)), maker.Exec(propertyDotSet)));
        return statements.toList();
    }

}
