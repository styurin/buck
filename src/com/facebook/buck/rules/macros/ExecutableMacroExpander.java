/*
 * Copyright 2014-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.rules.macros;

import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.model.macros.MacroException;
import com.facebook.buck.rules.BinaryBuildRule;
import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.BuildRuleResolver;
import com.facebook.buck.rules.CellPathResolver;
import com.facebook.buck.rules.SourcePathResolver;
import com.facebook.buck.rules.SourcePathRuleFinder;
import com.facebook.buck.rules.Tool;
import com.facebook.buck.util.Escaper;
import com.google.common.collect.ImmutableList;
import java.util.stream.Collectors;

/** Resolves to the executable command for a build target referencing a {@link BinaryBuildRule}. */
public class ExecutableMacroExpander extends BuildTargetMacroExpander<ExecutableMacro> {

  @Override
  public Class<ExecutableMacro> getInputClass() {
    return ExecutableMacro.class;
  }

  protected Tool getTool(BuildRule rule) throws MacroException {
    if (!(rule instanceof BinaryBuildRule)) {
      throw new MacroException(
          String.format(
              "%s used in executable macro does not correspond to a binary rule",
              rule.getBuildTarget()));
    }
    return ((BinaryBuildRule) rule).getExecutableCommand();
  }

  @Override
  protected ExecutableMacro parse(
      BuildTarget target, CellPathResolver cellNames, ImmutableList<String> input)
      throws MacroException {
    return ExecutableMacro.of(parseBuildTarget(target, cellNames, input));
  }

  @Override
  protected ImmutableList<BuildRule> extractBuildTimeDeps(
      BuildRuleResolver resolver, BuildRule rule) throws MacroException {
    return ImmutableList.copyOf(getTool(rule).getDeps(new SourcePathRuleFinder(resolver)));
  }

  @Override
  public String expand(SourcePathResolver resolver, BuildRule rule) throws MacroException {
    // TODO(mikekap): Pass environment variables through.
    return getTool(rule)
        .getCommandPrefix(resolver)
        .stream()
        .map(Escaper.SHELL_ESCAPER)
        .collect(Collectors.joining(" "));
  }

  @Override
  public Object extractRuleKeyAppendablesFrom(
      BuildTarget target,
      CellPathResolver cellNames,
      BuildRuleResolver resolver,
      ExecutableMacro input)
      throws MacroException {
    return getTool(resolve(resolver, input));
  }
}
