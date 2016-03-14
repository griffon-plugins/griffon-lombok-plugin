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

import java.util.*;

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

        // replace field with property
        typeNode.removeChild(fieldNode);
        JavacNode propertyNode = injectFieldAndMarkGenerated(typeNode, createPropertyField(fieldNode, errorNode));

        injectMethod(typeNode, createPropertyMethod(propertyNode, errorNode));
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

    private JCMethodDecl createPropertyMethod(JavacNode propertyNode, JavacNode source) {
        JCVariableDecl property = (JCVariableDecl) propertyNode.get();

        JCExpression methodType = property.vartype;
        Name methodName = propertyNode.toName(propertyNode.getName());

        List<JCStatement> statements = createLazyPropertyBody(propertyNode, source);

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

    private List<JCStatement> createLazyPropertyBody(JavacNode propertyNode, JavacNode source) {

        ListBuffer<JCStatement> statements = new ListBuffer<>();

        JCVariableDecl field = (JCVariableDecl) propertyNode.get();
        JavacTreeMaker maker = propertyNode.getTreeMaker();
        JCExpression propertyImpl = getPropertyImpl(propertyNode, source);

        statements.add(
                // if (fieldProperty == null)
                maker.If(maker.Binary(CTC_EQUAL, maker.Ident(field.getName()), maker.Literal(CTC_BOT, null)),
                        // fieldProperty = new ...
                        maker.Exec(
                                maker.Assign(
                                        maker.Ident(field.getName()),
                                        maker.NewClass(null, List.<JCExpression>nil(), propertyImpl,
                                                List.<JCExpression>nil(), null)
                                )
                        ),
                        // no else
                        null));
        // return fieldProperty
        statements.append(maker.Return(maker.Ident(field.getName())));
        return statements.toList();
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
        String returnType = field.vartype.type.toString();
        ListBuffer<JCStatement> statements = new ListBuffer<>();
        JavacTreeMaker maker = propertyNode.getTreeMaker();
        JCExpression propertyMethod = maker.Ident(propertyNode.toName(propertyNode.getName()));
        Name getMethodName = propertyNode.toName("get");
        Name getValueMethodName = propertyNode.toName("getValue");
        Name valueOfMethodName = propertyNode.toName("valueOf");
        List<JCExpression> nil = List.<JCExpression>nil();
        // just delegate to the xProperty() method
        JCExpression propertyDotGet = maker.Apply(nil, maker.Select(maker.Apply(nil, propertyMethod, nil), getMethodName), nil);
        JCExpression propertyDotGetValue = maker.Apply(nil, maker.Select(maker.Apply(nil, propertyMethod, nil), getValueMethodName), nil);
        if (Arrays.asList(
                "char",
                "byte",
                "short"
        ).contains(returnType)) {
            // return (char|byte|short)xProperty().get()
            statements.add(maker.Return(maker.TypeCast(field.vartype, propertyDotGet)));
        } else if (field.vartype.type.isPrimitive()) {
            // return xProperty().get()
            statements.add(maker.Return(propertyDotGet));
        } else if ("java.lang.Character".equals(returnType)) {
            // return Character.valueOf((char)xProperty.get());
            statements.add(maker.Return(
                    maker.Apply(nil,
                            maker.Select(field.vartype, valueOfMethodName),
                            List.<JCExpression>of(maker.TypeCast(maker.TypeIdent(Javac.CTC_CHAR), propertyDotGet))
                    )));
        } else if ("java.lang.Byte".equals(returnType)) {
            // return Byte.valueOf((byte)xProperty.get());
            statements.add(maker.Return(
                    maker.Apply(nil,
                            maker.Select(field.vartype, valueOfMethodName),
                            List.<JCExpression>of(maker.TypeCast(maker.TypeIdent(Javac.CTC_BYTE), propertyDotGet))
                    )));
        } else if ("java.lang.Short".equals(returnType)) {
            // return Short.valueOf((short)xProperty.get());
            statements.add(maker.Return(
                    maker.Apply(nil,
                            maker.Select(field.vartype, valueOfMethodName),
                            List.<JCExpression>of(maker.TypeCast(maker.TypeIdent(Javac.CTC_SHORT), propertyDotGet))
                    )));
        } else {
            // return xProperty().getValue()
            statements.add(maker.Return(propertyDotGetValue));
        }
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
        String paramType = field.vartype.type.toString();
        Name paramName = fieldNode.toName("value");
        ListBuffer<JCStatement> statements = new ListBuffer<>();
        JavacTreeMaker maker = propertyNode.getTreeMaker();
        JCExpression paramExpression = maker.Ident(paramName);
        JCExpression propertyMethod = maker.Ident(propertyNode.toName(propertyNode.getName()));
        Name setMethodName = propertyNode.toName("set");
        Name setValueMethodName = propertyNode.toName("setValue");
        List<JCExpression> nil = List.<JCExpression>nil();
        // just delegate to the xProperty() method
        JCExpression propertyDotSetValue = maker.Select(maker.Apply(nil, propertyMethod, nil), setValueMethodName);
        JCExpression propertyDotSet = maker.Select(maker.Apply(nil, propertyMethod, nil), setMethodName);
        if (field.vartype.type.isPrimitive()) {
            // xProperty().set(value)
            statements.add(maker.Exec(maker.Apply(nil, propertyDotSet, List.<JCExpression>of(paramExpression))));
        } else if ("java.lang.Character".equals(paramType)) {
            // xProperty().set(value != null ? value.charValue() : 0);
            statements.add(maker.Exec(maker.Apply(nil, propertyDotSet, List.<JCExpression>of(paramExpression))));
        } else if ("java.lang.Byte".equals(paramType) ||
                "java.lang.Short".equals(paramType) ||
                "java.lang.Integer".equals(paramType)) {
            // xProperty().set(value != null ? value.intValue() : 0);
            statements.add(maker.Exec(maker.Apply(nil, propertyDotSet, List.<JCExpression>of(paramExpression))));
        } else if ("java.lang.Long".equals(paramType)) {
            // xProperty().set(value != null ? value.longValue() : 0);
            statements.add(maker.Exec(maker.Apply(nil, propertyDotSet, List.<JCExpression>of(paramExpression))));
        } else if ("java.lang.Float".equals(paramType)) {
            // xProperty().set(value != null ? value.floatValue() : 0);
            statements.add(maker.Exec(maker.Apply(nil, propertyDotSet, List.<JCExpression>of(paramExpression))));
        } else if ("java.lang.Double".equals(paramType)) {
            // xProperty().set(value != null ? value.doubleValue() : 0);
            statements.add(maker.Exec(maker.Apply(nil, propertyDotSet, List.<JCExpression>of(paramExpression))));
        } else {
            // xProperty().setValue(value)
            statements.add(maker.Exec(maker.Apply(nil, propertyDotSetValue, List.<JCExpression>of(paramExpression))));
        }
        return statements.toList();
    }

}
