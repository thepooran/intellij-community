// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.idea.devkit.inspections.internal;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.codeInspection.bugs.GrAccessibilityChecker;
import org.jetbrains.plugins.groovy.codeInspection.type.GroovyStaticTypeCheckVisitor;
import org.jetbrains.plugins.groovy.codeInspection.untypedUnresolvedAccess.GrUnresolvedAccessChecker;
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementVisitor;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFileBase;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementVisitor;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrCodeReferenceElement;

import java.util.List;

public class GroovyGoodCodeRedVisitor implements GoodCodeRedVisitor {

  @NotNull
  @Override
  public PsiElementVisitor createVisitor(ProblemsHolder holder) {
    if (!Registry.is("groovy.good.code.is.red", false)) {
      return PsiElementVisitor.EMPTY_VISITOR;
    }
    GroovyFileBase file = (GroovyFileBase)holder.getFile();
    Project project = holder.getProject();
    GrUnresolvedAccessChecker unresolvedAccessChecker = new GrUnresolvedAccessChecker(file, project);
    GrAccessibilityChecker accessibilityChecker = new GrAccessibilityChecker(file, project);
    GroovyStaticTypeCheckVisitor typeCheckVisitor = new GroovyStaticTypeCheckVisitor() {
      @Override
      protected void registerError(@NotNull PsiElement location,
                                   @NotNull String description,
                                   @Nullable LocalQuickFix[] fixes,
                                   ProblemHighlightType highlightType) {
        if (highlightType == ProblemHighlightType.GENERIC_ERROR) {
          holder.registerProblem(location, description);
        }
      }
    };

    return new GroovyPsiElementVisitor(new GroovyElementVisitor() {
      @Override
      public void visitElement(@NotNull GroovyPsiElement element) {
        super.visitElement(element);
        element.accept(typeCheckVisitor);
      }

      @Override
      public void visitReferenceExpression(@NotNull GrReferenceExpression referenceExpression) {
        super.visitReferenceExpression(referenceExpression);
        List<HighlightInfo> infos = unresolvedAccessChecker.checkReferenceExpression(referenceExpression);
        if (infos != null) {
          infos.forEach(info -> registerProblem(holder, info, referenceExpression));
        }
        else {
          HighlightInfo info = accessibilityChecker.checkReferenceExpression(referenceExpression);
          if (info != null) {
            registerProblem(holder, info, referenceExpression);
          }
        }
      }

      @Override
      public void visitCodeReferenceElement(@NotNull GrCodeReferenceElement refElement) {
        super.visitCodeReferenceElement(refElement);
        HighlightInfo info = unresolvedAccessChecker.checkCodeReferenceElement(refElement);
        if (info != null) {
          registerProblem(holder, info, refElement);
        }
        else {
          info = accessibilityChecker.checkCodeReferenceElement(refElement);
          if (info != null) {
            registerProblem(holder, info, refElement);
          }
        }
      }

      private void registerProblem(ProblemsHolder holder, HighlightInfo info, PsiElement e) {
        if (info.getSeverity() == HighlightSeverity.ERROR) {
          holder.registerProblem(e, info.getDescription());
        }
      }
    });
  }
}
