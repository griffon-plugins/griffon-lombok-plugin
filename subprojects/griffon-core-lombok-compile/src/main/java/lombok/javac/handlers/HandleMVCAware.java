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

import com.sun.tools.javac.tree.JCTree;
import griffon.transform.MVCAware;
import lombok.core.AnnotationValues;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import org.kordamp.jipsy.ServiceProviderFor;

@ServiceProviderFor(JavacAnnotationHandler.class)
public class HandleMVCAware extends JavacAnnotationHandler<MVCAware> {

    @Override
    public void handle(AnnotationValues<MVCAware> annotation, JCTree.JCAnnotation source, JavacNode annotationNode) {
        JavacHandlerUtil.deleteAnnotationIfNeccessary(annotationNode, MVCAware.class);
        JavacNode typeNode = annotationNode.up();
    }
}
