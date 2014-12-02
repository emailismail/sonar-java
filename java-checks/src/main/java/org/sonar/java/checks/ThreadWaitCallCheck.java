/*
 * SonarQube Java
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.java.checks;

import com.google.common.collect.ImmutableList;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.java.checks.methods.MethodInvocationMatcher;
import org.sonar.java.checks.methods.TypeCriteria;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.List;

@Rule( key = "S2236",
    priority = Priority.CRITICAL,
   tags = {"bug", "multithreading"})
@BelongsToProfile( title = "Sonar way", priority = Priority.CRITICAL)
public class ThreadWaitCallCheck extends SubscriptionBaseVisitor {

  private List<MethodInvocationMatcher> forbiddenCalls = ImmutableList.<MethodInvocationMatcher>builder()
      .add(MethodInvocationMatcher.create().callSite(TypeCriteria.subtypeOf("java.lang.Thread")).name("wait"))
      .add(MethodInvocationMatcher.create().callSite(TypeCriteria.subtypeOf("java.lang.Thread")).name("wait").addParameter("long"))
      .add(MethodInvocationMatcher.create().callSite(TypeCriteria.subtypeOf("java.lang.Thread")).name("wait").addParameter("long").addParameter("int"))
      .add(MethodInvocationMatcher.create().callSite(TypeCriteria.subtypeOf("java.lang.Thread")).name("notify"))
      .add(MethodInvocationMatcher.create().callSite(TypeCriteria.subtypeOf("java.lang.Thread")).name("notifyAll")).build();


  @Override
  public List<Tree.Kind> nodesToVisit() {
    return ImmutableList.of(Tree.Kind.METHOD_INVOCATION);
  }

  @Override
  public void visitNode(Tree tree) {
    if (hasSemantic()) {
      MethodInvocationTree mit = (MethodInvocationTree) tree;
      for (MethodInvocationMatcher forbiddenCall : forbiddenCalls) {
        if (forbiddenCall.matches(mit, getSemanticModel())) {
          addIssue(tree, "Refactor the synchronisation mechanism to not use a Thread instance as a monitor");
        }
      }
    }
  }
}
