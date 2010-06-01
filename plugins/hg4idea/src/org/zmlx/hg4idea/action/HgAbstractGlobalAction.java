// Copyright 2008-2010 Victor Iacoban
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distributed under
// the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
// either express or implied. See the License for the specific language governing permissions and
// limitations under the License.
package org.zmlx.hg4idea.action;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vfs.*;
import com.intellij.vcsUtil.*;
import org.jetbrains.annotations.*;
import org.zmlx.hg4idea.*;
import org.zmlx.hg4idea.command.*;

import java.util.*;

abstract class HgAbstractGlobalAction extends AnAction {

  protected abstract HgGlobalCommandBuilder getHgGlobalCommandBuilder(Project project);

  public void actionPerformed(AnActionEvent event) {
    final DataContext dataContext = event.getDataContext();
    final Project project = PlatformDataKeys.PROJECT.getData(dataContext);
    if (project == null) {
      return;
    }

    HgGlobalCommand command = getHgGlobalCommandBuilder(project).build(findRepos(project));
    if (command == null) {
      return;
    }
    try {
      command.execute();
    } catch (HgCommandException e) {
      VcsUtil.showErrorMessage(project, e.getMessage(), "Error");
    }

    HgUtil.markDirectoryDirty(project,command.getRepo());
  }

  @Override
  public void update(AnActionEvent e) {
    super.update(e);

    Presentation presentation = e.getPresentation();
    final DataContext dataContext = e.getDataContext();

    Project project = PlatformDataKeys.PROJECT.getData(dataContext);
    if (project == null) {
      presentation.setEnabled(false);
      return;
    }

    HgVcs vcs = (HgVcs) ProjectLevelVcsManager.getInstance(project).findVcsByName(HgVcs.VCS_NAME);

    if (!vcs.isStarted()) {
      presentation.setEnabled(false);
    }
  }

  private List<VirtualFile> findRepos(Project project) {
    List<VirtualFile> repos = new LinkedList<VirtualFile>();
    VcsRoot[] roots = ProjectLevelVcsManager.getInstance(project).getAllVcsRoots();
    for (VcsRoot root : roots) {
      if (HgVcs.VCS_NAME.equals(root.vcs.getName())) {
        repos.add(root.path);
      }
    }
    return repos;
  }

  protected interface HgGlobalCommand {
    VirtualFile getRepo();
    void execute() throws HgCommandException;
  }

  protected interface HgGlobalCommandBuilder {
    @Nullable
    HgGlobalCommand build(Collection<VirtualFile> repos);
  }

}
