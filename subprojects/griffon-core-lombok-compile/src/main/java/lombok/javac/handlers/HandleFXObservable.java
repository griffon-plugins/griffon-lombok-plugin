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
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import griffon.transform.FXObservable;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import org.kordamp.jipsy.ServiceProviderFor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static lombok.javac.Javac.*;
import static lombok.javac.JavacTreeMaker.TypeTag.typeTag;
import static lombok.javac.handlers.JavacHandlerUtil.*;

@ServiceProviderFor(JavacAnnotationHandler.class)
public class HandleFXObservable extends JavacAnnotationHandler<FXObservable> {

    public static final java.util.Map<JavacTreeMaker.TypeTag, String> TYPE_MAP;

    static {
        Map<JavacTreeMaker.TypeTag, String> m = new HashMap<JavacTreeMaker.TypeTag, String>();
        m.put(CTC_INT, "Integer");
        m.put(CTC_DOUBLE, "Double");
        m.put(CTC_FLOAT, "Float");
        m.put(CTC_SHORT, "Integer");
        m.put(CTC_BYTE, "Integer");
        m.put(CTC_LONG, "Long");
        m.put(CTC_BOOLEAN, "Boolean");
        m.put(CTC_CHAR, "Integer");
        TYPE_MAP = Collections.unmodifiableMap(m);
    }

    @Override
    public void handle(AnnotationValues<FXObservable> annotation, JCAnnotation source, JavacNode annotationNode) {
        deleteAnnotationIfNeccessary(annotationNode, FXObservable.class);
        JavacNode node = annotationNode.up();
        if (node == null) return;

        switch (node.getKind()) {
            case TYPE:
                createForType(node, annotationNode);
                break;
            case FIELD:
                createForField(node, annotationNode);
                break;
            default:
                addUsageError(annotationNode);
        }
    }

    private void createForType(JavacNode typeNode, JavacNode errorNode) {
        if ((getModifiers(getTypeDecl(typeNode)) & (Flags.INTERFACE | Flags.ANNOTATION)) != 0) {
            addUsageError(errorNode);
            return;
        }
        for (JavacNode field : typeNode.down()) {
            if (fieldQualifiesForGeneration(field) && !hasAnnotation(FXObservable.class, field))
                createForField(field, errorNode);
        }
    }

    private void addUsageError(JavacNode errorNode) {
        errorNode.addError("@FXObservable is only supported on a class, an enum, or a field.");
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
        return true;
    }

    private void createForField(JavacNode fieldNode, JavacNode errorNode) {
        if (fieldNode.getKind() != Kind.FIELD) {
            addUsageError(errorNode);
            return;
        }
        JavacNode typeNode = fieldNode.up();

        // replace field with property
        typeNode.removeChild(fieldNode);
        JavacNode propertyNode = injectFieldAndMarkGenerated(typeNode, createPropertyField(fieldNode, errorNode));

        injectMethod(typeNode, createPropertyMethod(propertyNode, errorNode));
    }

    private JCVariableDecl createPropertyField(JavacNode fieldNode, JavacNode source) {
        JCVariableDecl field = (JCVariableDecl) fieldNode.get();

        String propertyName = fieldNode.getName() + "Property";
        String propertyTypePrefix = null;
        if (field.vartype instanceof JCPrimitiveTypeTree)
            propertyTypePrefix = TYPE_MAP.get(typeTag(field.vartype));
        String propertyType = null;
        if (propertyTypePrefix != null) {
            propertyType = "javafx.beans.property." + propertyTypePrefix + "Property";
        } else {
            propertyType = "javafx.beans.property.ObjectProperty";
        }
        JavacTreeMaker treeMaker = fieldNode.getTreeMaker();
        JCVariableDecl propertyField = treeMaker.VarDef(
                treeMaker.Modifiers(Flags.PRIVATE),
                fieldNode.toName(propertyName),
                JavacHandlerUtil.chainDotsString(fieldNode, propertyType),
                null);
        copyJavadoc(fieldNode, propertyField, CopyJavadoc.VERBATIM);
        return propertyField;
    }

    private JCMethodDecl createPropertyMethod(JavacNode fieldNode, JavacNode source) {
        JCVariableDecl field = (JCVariableDecl) fieldNode.get();

        JCExpression methodType = field.vartype;
        // Generate the methodName; lazy will change the field type
        Name methodName = fieldNode.toName(fieldNode.getName());

        List<JCStatement> statements = createLazyPropertyBody(fieldNode, source);

        JavacTreeMaker treeMaker = fieldNode.getTreeMaker();

        JCBlock methodBody = treeMaker.Block(0, statements);

        List<JCTypeParameter> methodGenericParams = List.nil();
        List<JCVariableDecl> parameters = List.nil();
        List<JCExpression> throwsClauses = List.nil();
        JCExpression annotationMethodDefaultValue = null;

        JCMethodDecl decl = recursiveSetGeneratedBy(treeMaker.MethodDef(treeMaker.Modifiers(Flags.PUBLIC), methodName, methodType,
                methodGenericParams, parameters, throwsClauses, methodBody, annotationMethodDefaultValue), source.get(), fieldNode.getContext());

        copyJavadoc(fieldNode, decl, CopyJavadoc.VERBATIM);
        return decl;
    }

    public List<JCStatement> createLazyPropertyBody(JavacNode fieldNode, JavacNode source) {

        ListBuffer<JCStatement> statements = new ListBuffer<>();

        JCVariableDecl field = (JCVariableDecl) fieldNode.get();
        JavacTreeMaker maker = fieldNode.getTreeMaker();

        statements.add(
                // if (fieldProperty == null)
                maker.If(maker.Binary(CTC_EQUAL, maker.Ident(field.getName()), maker.Literal(CTC_BOT, null)),
                        // fieldProperty = new ...
                        maker.Exec(maker.Assign(maker.Ident(field.getName()), maker.Literal(CTC_BOT, null))),
                        // no else
                        null));
        // return fieldProperty
        statements.append(maker.Return(maker.Ident(field.getName())));
        return statements.toList();
    }

}
